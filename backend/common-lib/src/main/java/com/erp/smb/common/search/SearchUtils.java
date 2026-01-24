package com.erp.smb.common.search;

import java.util.regex.Pattern;

/**
 * Utility class for search operations
 */
public class SearchUtils {
    
    private static final Pattern SPECIAL_CHARS = Pattern.compile("[^a-zA-Z0-9\\s]");
    private static final Pattern MULTIPLE_SPACES = Pattern.compile("\\s+");
    
    /**
     * Normalize and sanitize search query
     * - Remove special characters
     * - Trim whitespace
     * - Convert to lowercase
     * - Remove multiple spaces
     */
    public static String normalizeQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            return "";
        }
        
        String normalized = query.trim().toLowerCase();
        normalized = SPECIAL_CHARS.matcher(normalized).replaceAll(" ");
        normalized = MULTIPLE_SPACES.matcher(normalized).replaceAll(" ");
        return normalized.trim();
    }
    
    /**
     * Convert query to PostgreSQL tsquery format
     * Splits words and joins with & operator
     */
    public static String toTsQuery(String query) {
        String normalized = normalizeQuery(query);
        if (normalized.isEmpty()) {
            return "";
        }
        
        String[] words = normalized.split("\\s+");
        return String.join(" & ", words);
    }
    
    /**
     * Convert query to prefix search format for autocomplete
     * Adds :* to the last word for prefix matching
     */
    public static String toPrefixQuery(String query) {
        String normalized = normalizeQuery(query);
        if (normalized.isEmpty()) {
            return "";
        }
        
        String[] words = normalized.split("\\s+");
        if (words.length == 0) {
            return "";
        }
        
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < words.length - 1; i++) {
            result.append(words[i]).append(" & ");
        }
        result.append(words[words.length - 1]).append(":*");
        
        return result.toString();
    }
    
    /**
     * Sanitize input to prevent SQL injection
     */
    public static String sanitizeInput(String input) {
        if (input == null) {
            return "";
        }
        
        // Remove single quotes and other SQL special characters
        return input.replaceAll("['\"\\\\;]", "");
    }
    
    /**
     * Extract tenant ID from JWT token or header
     * This is a placeholder - actual implementation depends on JWT structure
     */
    public static String extractTenantId(String token) {
        // TODO: Implement JWT parsing logic
        // For now, return a default
        return "demo";
    }
}
