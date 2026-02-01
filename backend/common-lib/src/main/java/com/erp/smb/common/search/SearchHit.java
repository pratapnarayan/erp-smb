package com.erp.smb.common.search;

import java.util.Map;

/**
 * Unified search result DTO for frontend consumption.
 * Abstracts entity-specific details into a consistent format.
 * 
 * Frontend must NOT branch on entityType for rendering.
 * All necessary display data is in title/subtitle/highlight fields.
 */
public class SearchHit {
    
    private SearchEntityType entityType;
    private String entityId;
    private String title;           // Primary display field (e.g., product name, customer name)
    private String subtitle;        // Secondary info (e.g., SKU, email, order date)
    private String highlight;       // Matched text fragment for display
    private Double relevanceScore;  // Search ranking score
    private Map<String, Object> rawData;  // Optional minimal metadata
    
    public SearchHit() {
    }
    
    public SearchHit(SearchEntityType entityType, String entityId, String title) {
        this.entityType = entityType;
        this.entityId = entityId;
        this.title = title;
    }
    
    // Getters and Setters
    public SearchEntityType getEntityType() {
        return entityType;
    }
    
    public void setEntityType(SearchEntityType entityType) {
        this.entityType = entityType;
    }
    
    public String getEntityId() {
        return entityId;
    }
    
    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getSubtitle() {
        return subtitle;
    }
    
    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }
    
    public String getHighlight() {
        return highlight;
    }
    
    public void setHighlight(String highlight) {
        this.highlight = highlight;
    }
    
    public Double getRelevanceScore() {
        return relevanceScore;
    }
    
    public void setRelevanceScore(Double relevanceScore) {
        this.relevanceScore = relevanceScore;
    }
    
    public Map<String, Object> getRawData() {
        return rawData;
    }
    
    public void setRawData(Map<String, Object> rawData) {
        this.rawData = rawData;
    }
}
