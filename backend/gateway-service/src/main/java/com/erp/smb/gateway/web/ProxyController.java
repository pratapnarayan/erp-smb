package com.erp.smb.gateway.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ProxyController {
    private final List<Route> routes;
    private final LoadBalancerClient lb;

    public ProxyController(@Value("${proxy.routes}") List<Map<String,String>> rawRoutes, LoadBalancerClient lb) {
        this.routes = rawRoutes.stream().map(Route::from).toList();
        this.lb = lb;
    }

    @RequestMapping(value = "/**")
    public ResponseEntity<byte[]> proxy(HttpServletRequest req,
                                        @RequestBody(required = false) byte[] body) {
        String method = req.getMethod();
        String fullPath = req.getRequestURI();
        String pathWithinApi = fullPath.replaceFirst("/api", "");
        String authorization = req.getHeader("Authorization");
        String contentType = req.getHeader("Content-Type");
        String accept = req.getHeader("Accept");
        Route match = routes.stream().filter(r -> pathWithinApi.startsWith(r.path.replaceFirst("/api", ""))).findFirst().orElse(null);
        if (match == null) return ResponseEntity.notFound().build();
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
        String targetUrl = base + pathWithinApi + (query==null?"":"?"+query);
        RestClient client = RestClient.create();
        var reqSpec = client.method(HttpMethod.valueOf(method)).uri(URI.create(targetUrl))
                .header("Authorization", authorization==null?"":authorization)
                .header("Content-Type", contentType==null?"application/json":contentType)
                .header("Accept", accept==null?"*/*":accept);
        ResponseEntity<byte[]> resp = (body==null?reqSpec.retrieve().toEntity(byte[].class):reqSpec.body(body).retrieve().toEntity(byte[].class));
        return ResponseEntity.status(resp.getStatusCode()).headers(resp.getHeaders()).body(resp.getBody());
    }

    static class Route {
        final String id; final String path; final String url; final String uri;
        Route(String id, String path, String url, String uri){ this.id=id; this.path=path; this.url=url; this.uri=uri; }
        static Route from(Map<String,String> m){return new Route(m.get("id"), m.get("path"), m.get("url"), m.get("uri"));}
    }
}
