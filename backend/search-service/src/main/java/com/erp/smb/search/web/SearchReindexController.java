package com.erp.smb.search.web;

import com.erp.smb.common.search.SearchEntityType;
import com.erp.smb.search.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/search/reindex")
@Tag(name = "Search Reindex", description = "Bulk reindexing operations for search service")
@SecurityRequirement(name = "bearer-jwt")
public class SearchReindexController {
    
    private final SearchService searchService;
    
    public SearchReindexController(SearchService searchService) {
        this.searchService = searchService;
    }
    
    @PostMapping("/{entityType}")
    @PreAuthorize("hasRole('SYSTEM')")
    @Operation(summary = "Bulk reindex entity type", 
               description = "Rebuild search index for specific entity type. SYSTEM role only. Idempotent.")
    public ResponseEntity<Map<String, Object>> reindexEntityType(
            @PathVariable SearchEntityType entityType,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        
        if (!entityType.isV1Supported()) {
            return ResponseEntity.badRequest().body(
                Map.of("error", "Entity type " + entityType + " is not supported in v1")
            );
        }
        
        long startTime = System.currentTimeMillis();
        long count = searchService.bulkReindex(entityType, tenantId);
        long duration = System.currentTimeMillis() - startTime;
        
        Map<String, Object> response = new HashMap<>();
        response.put("entityType", entityType.name());
        response.put("tenantId", tenantId);
        response.put("reindexedCount", count);
        response.put("durationMs", duration);
        response.put("status", "completed");
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/all")
    @PreAuthorize("hasRole('SYSTEM')")
    @Operation(summary = "Bulk reindex all entities", 
               description = "Rebuild search index for all v1 entity types. SYSTEM role only. Idempotent.")
    public ResponseEntity<Map<String, Object>> reindexAll(
            @RequestHeader("X-Tenant-Id") String tenantId) {
        
        long startTime = System.currentTimeMillis();
        long totalCount = searchService.bulkReindexAll(tenantId);
        long duration = System.currentTimeMillis() - startTime;
        
        Map<String, Object> response = new HashMap<>();
        response.put("tenantId", tenantId);
        response.put("entityTypes", SearchEntityType.getV1Supported());
        response.put("totalReindexedCount", totalCount);
        response.put("durationMs", duration);
        response.put("status", "completed");
        
        return ResponseEntity.ok(response);
    }
}
