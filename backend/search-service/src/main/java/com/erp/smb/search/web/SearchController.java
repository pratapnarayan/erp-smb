package com.erp.smb.search.web;

import com.erp.smb.common.search.*;
import com.erp.smb.search.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@Tag(name = "Search", description = "Global search across products, customers, and orders")
@SecurityRequirement(name = "bearer-jwt")
public class SearchController {
    
    private final SearchService searchService;
    
    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'USER')")
    @Operation(summary = "Global search", description = "Search across v1 entity types (PRODUCT, CUSTOMER, ORDER)")
    public ResponseEntity<SearchResult<SearchHit>> search(
            @RequestParam("q") String query,
            @RequestParam(value = "type", required = false) SearchEntityType entityType,
            @RequestParam(value = "fuzzy", required = false, defaultValue = "true") boolean fuzzyMatch,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "20") int size,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        SearchCriteria criteria = new SearchCriteria(query, entityType, tenantId);
        criteria.setPage(page);
        criteria.setSize(size);
        criteria.setFuzzyMatch(fuzzyMatch);
        
        try {
            SearchResult<SearchHit> result = searchService.globalSearch(criteria);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            // Entity type not supported in v1
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/suggestions")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER', 'USER')")
    @Operation(summary = "Autocomplete suggestions", description = "Get search suggestions for autocomplete")
    public ResponseEntity<List<SearchSuggestion>> suggestions(
            @RequestParam("q") String query,
            @RequestParam(value = "type", required = false) SearchEntityType entityType,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        
        if (query == null || query.trim().length() < 2) {
            return ResponseEntity.ok(List.of());
        }
        
        List<SearchSuggestion> suggestions = searchService.getSuggestions(tenantId, query, entityType);
        return ResponseEntity.ok(suggestions);
    }
}
