package com.erp.smb.common.search;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Generic search document for indexing
 */
public class SearchDocument {
    
    private String entityId;
    private String tenantId;
    private SearchEntityType entityType;
    private Map<String, Object> searchableFields;
    private String searchText; // Combined text for full-text search
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public SearchDocument() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public String getEntityId() {
        return entityId;
    }
    
    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }
    
    public String getTenantId() {
        return tenantId;
    }
    
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
    
    public SearchEntityType getEntityType() {
        return entityType;
    }
    
    public void setEntityType(SearchEntityType entityType) {
        this.entityType = entityType;
    }
    
    public Map<String, Object> getSearchableFields() {
        return searchableFields;
    }
    
    public void setSearchableFields(Map<String, Object> searchableFields) {
        this.searchableFields = searchableFields;
    }
    
    public String getSearchText() {
        return searchText;
    }
    
    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
