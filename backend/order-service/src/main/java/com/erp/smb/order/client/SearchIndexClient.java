package com.erp.smb.order.client;

import com.erp.smb.common.search.SearchDocument;
import com.erp.smb.common.search.SearchEntityType;
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
 * Uses SYSTEM role / internal authentication.
 * Failures are logged but do not propagate to caller.
 */
@Component
public class SearchIndexClient {
    
    private static final Logger log = LoggerFactory.getLogger(SearchIndexClient.class);
    
    private final RestTemplate restTemplate;
    private final String searchServiceUrl;
    private final String systemToken;
    
    public SearchIndexClient(
            RestTemplate restTemplate,
            @Value("${app.search.service-url:http://localhost:8090}") String searchServiceUrl,
            @Value("${app.search.system-token:SYSTEM_INTERNAL_TOKEN}") String systemToken) {
        this.restTemplate = restTemplate;
        this.searchServiceUrl = searchServiceUrl;
        this.systemToken = systemToken;
    }
    
    /**
     * Index an order in search service.
     * Non-blocking, failure-tolerant.
     */
    public void indexOrder(String entityId, String tenantId, Map<String, Object> fields) {
        try {
            SearchDocument document = new SearchDocument();
            document.setEntityId(entityId);
            document.setTenantId(tenantId);
            document.setEntityType(SearchEntityType.ORDER);
            document.setSearchableFields(fields);
            
            HttpHeaders headers = createHeaders();
            HttpEntity<SearchDocument> request = new HttpEntity<>(document, headers);
            
            String url = searchServiceUrl + "/api/search/index/orders";
            restTemplate.postForEntity(url, request, Void.class);
            
            log.debug("Indexed order: entityId={}, tenantId={}", entityId, tenantId);
        } catch (Exception e) {
            log.error("Failed to index order: entityType=ORDER, entityId={}, tenantId={}, error={}",
                entityId, tenantId, e.getMessage());
        }
    }
    
    /**
     * Delete an order from search index.
     * Non-blocking, failure-tolerant.
     */
    public void deleteOrder(String entityId, String tenantId) {
        try {
            HttpHeaders headers = createHeaders();
            headers.set("X-Tenant-Id", tenantId);
            HttpEntity<Void> request = new HttpEntity<>(headers);
            
            String url = searchServiceUrl + "/api/search/index/ORDER/" + entityId;
            restTemplate.exchange(url, HttpMethod.DELETE, request, Void.class);
            
            log.debug("Deleted order from index: entityId={}, tenantId={}", entityId, tenantId);
        } catch (Exception e) {
            log.error("Failed to delete order from index: entityType=ORDER, entityId={}, tenantId={}, error={}",
                entityId, tenantId, e.getMessage());
        }
    }
    
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(systemToken);
        return headers;
    }
}
