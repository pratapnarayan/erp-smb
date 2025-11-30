package com.erp.smb.gateway.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ProxyController {
    private final List<Route> routes;

    public ProxyController(@Value("${proxy.routes}") List<Map<String,String>> rawRoutes) {
        this.routes = rawRoutes.stream().map(Route::from).toList();
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
        String targetUrl = match.url + pathWithinApi + (query==null?"":"?"+query);
        RestClient client = RestClient.create();
        var reqSpec = client.method(HttpMethod.valueOf(method)).uri(targetUrl)
                .header("Authorization", authorization==null?"":authorization)
                .header("Content-Type", contentType==null?"application/json":contentType)
                .header("Accept", accept==null?"*/*":accept);
        ResponseEntity<byte[]> resp = (body==null?reqSpec.retrieve().toEntity(byte[].class):reqSpec.body(body).retrieve().toEntity(byte[].class));
        return ResponseEntity.status(resp.getStatusCode()).headers(resp.getHeaders()).body(resp.getBody());
    }

    record Route(String id, String path, String url){
        static Route from(Map<String,String> m){return new Route(m.get("id"), m.get("path"), m.get("url"));}
    }
}
