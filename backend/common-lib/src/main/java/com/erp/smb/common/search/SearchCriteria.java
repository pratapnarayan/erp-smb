package com.erp.smb.common.search;

import java.util.List;

/**
 * DTO for search criteria
 */
public class SearchCriteria {
    
    private String query;
    private SearchEntityType entityType;
    private List<String> searchableFields;
    private String tenantId;
    private int page = 0;
    private int size = 20;
    private boolean fuzzyMatch = true;
    
    public SearchCriteria() {
    }
    
    public SearchCriteria(String query, SearchEntityType entityType, String tenantId) {
        this.query = query;
        this.entityType = entityType;
        this.tenantId = tenantId;
    }
    
    // Getters and Setters
    public String getQuery() {
        return query;
    }
    
    public void setQuery(String query) {
        this.query = query;
    }
    
    public SearchEntityType getEntityType() {
        return entityType;
    }
    
    public void setEntityType(SearchEntityType entityType) {
        this.entityType = entityType;
    }
    
    public List<String> getSearchableFields() {
        return searchableFields;
    }
    
    public void setSearchableFields(List<String> searchableFields) {
        this.searchableFields = searchableFields;
    }
    
    public String getTenantId() {
        return tenantId;
    }
    
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
    
    public int getPage() {
        return page;
    }
    
    public void setPage(int page) {
        this.page = page;
    }
    
    public int getSize() {
        return size;
    }
    
    public void setSize(int size) {
        this.size = size;
    }
    
    public boolean isFuzzyMatch() {
        return fuzzyMatch;
    }
    
    public void setFuzzyMatch(boolean fuzzyMatch) {
        this.fuzzyMatch = fuzzyMatch;
    }
}
