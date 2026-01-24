package com.erp.smb.common.search;

/**
 * DTO for search autocomplete suggestions
 */
public class SearchSuggestion {
    
    private String text;
    private SearchEntityType entityType;
    private String entityId;
    private String highlight;
    private double relevanceScore;
    
    public SearchSuggestion() {
    }
    
    public SearchSuggestion(String text, SearchEntityType entityType, String entityId) {
        this.text = text;
        this.entityType = entityType;
        this.entityId = entityId;
    }
    
    // Getters and Setters
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
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
    
    public String getHighlight() {
        return highlight;
    }
    
    public void setHighlight(String highlight) {
        this.highlight = highlight;
    }
    
    public double getRelevanceScore() {
        return relevanceScore;
    }
    
    public void setRelevanceScore(double relevanceScore) {
        this.relevanceScore = relevanceScore;
    }
}
