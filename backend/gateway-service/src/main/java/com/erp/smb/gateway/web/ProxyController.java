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
            System.out.println("  - " + r.id + " path=" + r.path + " stripPrefix=" + r.stripPrefix + " -> " + (r.url!=null?r.url:r.uri));
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
        String tenant = req.getHeader("X-Tenant-Id");
        // Derive tenantId from JWT if header is missing
        if ((tenant == null || tenant.isBlank()) && authorization != null && authorization.startsWith("Bearer ")) {
            try {
                var jwt = new com.erp.smb.common.security.JwtUtils(
                        System.getProperty("app.jwt.secret", System.getenv().getOrDefault("APP_JWT_SECRET", "dev-secret-please-change-32-chars-minimum-123456")),
                        3600, 3600*24
                );
                var claims = jwt.parse(authorization.substring(7)).getBody();
                Object t = claims.get("tenantId");
                if (t == null) t = claims.get("tenant_id");
                if (t == null) t = claims.get("tenant");
                if (t != null) tenant = String.valueOf(t);
            } catch (Exception ignored) {}
        }
       // For local development: if no tenant was resolved and the request targets reporting, default to 'demo'
       try {
           String p = req.getRequestURI();
           if ((tenant == null || tenant.isBlank()) && p != null && p.startsWith("/api/reports/")) {
               tenant = "demo";
           }
       } catch (Exception ignored) {}
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
        // Optionally strip the matched route prefix (e.g., /api/reports) before forwarding so downstream services receive their native path
        String routePrefix = match.path.endsWith("/**") ? match.path.substring(0, match.path.length() - 3) : match.path;
        String forwardedPath;
        boolean shouldStrip = Boolean.TRUE.equals(match.stripPrefix) || "reports".equalsIgnoreCase(match.id);
        if (shouldStrip) {
            forwardedPath = fullPath.startsWith(routePrefix) ? fullPath.substring(routePrefix.length()) : fullPath;
            if (forwardedPath.isEmpty()) forwardedPath = "/";
        } else {
            forwardedPath = fullPath; // keep fullPath by default
        }
        String targetUrl = base + forwardedPath + (query==null?"":"?"+query);
        System.out.println("[Gateway] " + method + " " + fullPath + " -> " + targetUrl + " (route=" + match.id + ")");
        RestClient client = RestClient.create();
        // Use exchange(..) to avoid throwing on 4xx/5xx and faithfully pass-through status and headers
        try {
            RestClient.RequestBodySpec reqSpec = client
                .method(HttpMethod.valueOf(method))
                .uri(URI.create(targetUrl));
            if (authorization != null && !authorization.isBlank()) reqSpec = reqSpec.header("Authorization", authorization);
            if (accept != null && !accept.isBlank()) reqSpec = reqSpec.header("Accept", accept);
            if (tenant != null && !tenant.isBlank()) reqSpec = reqSpec.header("X-Tenant-Id", tenant);
            // Only set Content-Type if provided by the client or if we have a body
            boolean hasBody = body != null && body.length > 0;
            if (contentType != null && !contentType.isBlank()) {
                reqSpec = reqSpec.header("Content-Type", contentType);
            } else if (hasBody) {
                reqSpec = reqSpec.header("Content-Type", "application/json");
            }
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
        final String id; final String path; final String url; final String uri; final Boolean stripPrefix;
        Route(String id, String path, String url, String uri, Boolean stripPrefix){ this.id=id; this.path=path; this.url=url; this.uri=uri; this.stripPrefix=stripPrefix; }
        static Route from(ProxyProperties.Route r){return new Route(r.getId(), r.getPath(), r.getUrl(), r.getUri(), r.getStripPrefix());}
    }
}
