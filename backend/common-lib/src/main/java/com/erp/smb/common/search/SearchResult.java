package com.erp.smb.common.search;

import java.util.List;

/**
 * Generic search result container
 * @param <T> The type of search result entity
 */
public class SearchResult<T> {
    
    private List<T> results;
    private long totalCount;
    private int page;
    private int size;
    private long searchTimeMs;
    private String query;
    
    public SearchResult() {
    }
    
    public SearchResult(List<T> results, long totalCount, int page, int size) {
        this.results = results;
        this.totalCount = totalCount;
        this.page = page;
        this.size = size;
    }
    
    // Getters and Setters
    public List<T> getResults() {
        return results;
    }
    
    public void setResults(List<T> results) {
        this.results = results;
    }
    
    public long getTotalCount() {
        return totalCount;
    }
    
    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
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
    
    public long getSearchTimeMs() {
        return searchTimeMs;
    }
    
    public void setSearchTimeMs(long searchTimeMs) {
        this.searchTimeMs = searchTimeMs;
    }
    
    public String getQuery() {
        return query;
    }
    
    public void setQuery(String query) {
        this.query = query;
    }
    
    public int getTotalPages() {
        return (int) Math.ceil((double) totalCount / size);
    }
    
    public boolean hasNext() {
        return page < getTotalPages() - 1;
    }
    
    public boolean hasPrevious() {
        return page > 0;
    }
}
