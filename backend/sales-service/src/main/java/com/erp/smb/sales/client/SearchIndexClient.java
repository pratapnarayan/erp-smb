package com.erp.smb.sales.client;

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
     * Index a customer in search service.
     * Non-blocking, failure-tolerant.
     */
    public void indexCustomer(String entityId, String tenantId, Map<String, Object> fields) {
        try {
            SearchDocument document = new SearchDocument();
            document.setEntityId(entityId);
            document.setTenantId(tenantId);
            document.setEntityType(SearchEntityType.CUSTOMER);
            document.setSearchableFields(fields);
            
            HttpHeaders headers = createHeaders();
            HttpEntity<SearchDocument> request = new HttpEntity<>(document, headers);
            
            String url = searchServiceUrl + "/api/search/index/customers";
            restTemplate.postForEntity(url, request, Void.class);
            
            log.debug("Indexed customer: entityId={}, tenantId={}", entityId, tenantId);
        } catch (Exception e) {
            log.error("Failed to index customer: entityType=CUSTOMER, entityId={}, tenantId={}, error={}",
                entityId, tenantId, e.getMessage());
        }
    }
    
    /**
     * Delete a customer from search index.
     * Non-blocking, failure-tolerant.
     */
    public void deleteCustomer(String entityId, String tenantId) {
        try {
            HttpHeaders headers = createHeaders();
            headers.set("X-Tenant-Id", tenantId);
            HttpEntity<Void> request = new HttpEntity<>(headers);
            
            String url = searchServiceUrl + "/api/search/index/CUSTOMER/" + entityId;
            restTemplate.exchange(url, HttpMethod.DELETE, request, Void.class);
            
            log.debug("Deleted customer from index: entityId={}, tenantId={}", entityId, tenantId);
        } catch (Exception e) {
            log.error("Failed to delete customer from index: entityType=CUSTOMER, entityId={}, tenantId={}, error={}",
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
