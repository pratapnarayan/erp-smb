package com.erp.smb.product.client;

import com.erp.smb.common.search.SearchDocument;
import com.erp.smb.common.search.SearchEntityType;
import com.erp.smb.common.security.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Client for interacting with search-service indexing APIs.
 * Uses ADMIN role / internal authentication.
 * Failures are logged but do not propagate to caller.
 */
@Component
public class SearchIndexClient {
    
    private static final Logger log = LoggerFactory.getLogger(SearchIndexClient.class);
    
    private final RestTemplate restTemplate;
    private final String searchServiceUrl;
    private final String systemJwt;

    public SearchIndexClient(
            RestTemplate restTemplate,
            @Value("${app.search.service-url:http://localhost:8090}") String searchServiceUrl,
            @Value("${app.search.system-token:}") String systemToken,
            @Value("${app.jwt.secret}") String jwtSecret,
            @Value("${app.jwt.access-ttl:3600}") long accessTtlSeconds) {
        this.restTemplate = restTemplate;
        this.searchServiceUrl = searchServiceUrl;

        // Prefer an explicitly provided ADMIN JWT (if configured); otherwise generate
        // a signed token using the shared platform secret.
        if (systemToken != null && systemToken.contains(".") && systemToken.split("\\.").length >= 3) {
            this.systemJwt = systemToken;
        } else {
            JwtUtils jwtUtils = new JwtUtils(jwtSecret, accessTtlSeconds, accessTtlSeconds * 24);
            this.systemJwt = jwtUtils.generateAccessToken(
                    "system",
                    Map.of("roles", java.util.List.of("ADMIN"), "tenantId", "system"));
        }
    }
    
    /**
     * Index a product in search service.
     * Non-blocking, failure-tolerant.
     */
    public void indexProduct(String entityId, String tenantId, Map<String, Object> fields) {
        try {
            SearchDocument document = new SearchDocument();
            document.setEntityId(entityId);
            document.setTenantId(tenantId);
            document.setEntityType(SearchEntityType.PRODUCT);
            document.setSearchableFields(fields);
            
            HttpHeaders headers = createHeaders();
            HttpEntity<SearchDocument> request = new HttpEntity<>(document, headers);
            
            String url = searchServiceUrl + "/api/search/index/products";
            restTemplate.postForEntity(url, request, Void.class);
            
            log.debug("Indexed product: entityId={}, tenantId={}", entityId, tenantId);
        } catch (Exception e) {
            log.error("Failed to index product: entityType=PRODUCT, entityId={}, tenantId={}, error={}",
                entityId, tenantId, e.getMessage());
        }
    }
    
    /**
     * Delete a product from search index.
     * Non-blocking, failure-tolerant.
     */
    public void deleteProduct(String entityId, String tenantId) {
        try {
            HttpHeaders headers = createHeaders();
            headers.set("X-Tenant-Id", tenantId);
            HttpEntity<Void> request = new HttpEntity<>(headers);
            
            String url = searchServiceUrl + "/api/search/index/PRODUCT/" + entityId;
            restTemplate.exchange(url, HttpMethod.DELETE, request, Void.class);
            
            log.debug("Deleted product from index: entityId={}, tenantId={}", entityId, tenantId);
        } catch (Exception e) {
            log.error("Failed to delete product from index: entityType=PRODUCT, entityId={}, tenantId={}, error={}",
                entityId, tenantId, e.getMessage());
        }
    }
    
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(systemJwt);
        return headers;
    }
}
