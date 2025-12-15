package com.erp.smb.gateway.web;

import jakarta.servlet.http.HttpServletRequest;
import com.erp.smb.gateway.config.ProxyProperties;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ProxyController {
    private final List<Route> routes;
    private final LoadBalancerClient lb;

    public ProxyController(ProxyProperties props, LoadBalancerClient lb) {
        this.routes = props.getRoutes().stream().map(Route::from).toList();
        this.lb = lb;
        System.out.println("[Gateway] Loaded routes (" + this.routes.size() + ")");
        for (Route r : this.routes) {
            System.out.println("  - " + r.id + " path=" + r.path + " -> " + (r.url!=null?r.url:r.uri));
        }
    }

    @RequestMapping(value = "/**")
    public ResponseEntity<byte[]> proxy(HttpServletRequest req,
                                        @RequestBody(required = false) byte[] body) {
        String method = req.getMethod();
        String fullPath = req.getRequestURI();
        String authorization = req.getHeader("Authorization");
        String contentType = req.getHeader("Content-Type");
        String accept = req.getHeader("Accept");
        AntPathMatcher matcher = new AntPathMatcher();
        Route match = routes.stream().filter(r -> matcher.match(r.path, fullPath)).findFirst().orElse(null);
        if (match == null) {
            System.out.println("[Gateway] No route matched for path=" + fullPath);
            return ResponseEntity.notFound().build();
        }
        String query = req.getQueryString();
        String base;
        if (match.uri != null && match.uri.startsWith("lb://")) {
            String serviceId = match.uri.substring("lb://".length());
            var instance = lb.choose(serviceId);
            if (instance == null) return ResponseEntity.status(503).build();
            base = "http://" + instance.getHost() + ":" + instance.getPort();
        } else if (match.url != null) {
            base = match.url;
        } else {
            return ResponseEntity.status(500).build();
        }
        // Forward the full incoming path so downstream services that are mapped under /api/... resolve correctly
        String targetUrl = base + fullPath + (query==null?"":"?"+query);
        System.out.println("[Gateway] " + method + " " + fullPath + " -> " + targetUrl + " (route=" + match.id + ")");
        RestClient client = RestClient.create();
        // Use exchange(..) to avoid throwing on 4xx/5xx and faithfully pass-through status and headers
        try {
            RestClient.RequestBodySpec reqSpec = client
                .method(HttpMethod.valueOf(method))
                .uri(URI.create(targetUrl))
                .header("Authorization", authorization==null?"":authorization)
                .header("Content-Type", contentType==null?"application/json":contentType)
                .header("Accept", accept==null?"*/*":accept);
            // Only attach a body if present; avoid NPE for GET/HEAD without body
            ResponseEntity<byte[]> resp = (body != null ? reqSpec.body(body) : reqSpec)
                .exchange((request, response) -> {
                    byte[] responseBody = response.getBody() != null ? response.getBody().readAllBytes() : new byte[0];
                    return ResponseEntity.status(response.getStatusCode())
                            .headers(response.getHeaders())
                            .body(responseBody);
                });
            return resp;
        } catch (Exception ex) {
            System.out.println("[Gateway] Error proxying to " + targetUrl + ": " + ex.getClass().getSimpleName() + " - " + ex.getMessage());
            return ResponseEntity.status(502).build();
        }
    }

    static class Route {
        final String id; final String path; final String url; final String uri;
        Route(String id, String path, String url, String uri){ this.id=id; this.path=path; this.url=url; this.uri=uri; }
        static Route from(ProxyProperties.Route r){return new Route(r.getId(), r.getPath(), r.getUrl(), r.getUri());}
    }
}
