package com.erp.smb.common.search;

import java.util.regex.Pattern;

/**
 * Utility class for search operations
 */
public class SearchUtils {
    
    private static final Pattern SPECIAL_CHARS = Pattern.compile("[^a-zA-Z0-9\\s]");
    private static final Pattern MULTIPLE_SPACES = Pattern.compile("\\s+");

    // Code-like queries such as SO-1005, SKU-1005, INV-2024-001.
    // Heuristics:
    // - No spaces
    // - Contains at least one digit
    // - Only letters/digits and common separators (-, _, /)
    private static final Pattern CODE_LIKE_QUERY = Pattern.compile("^(?=.*\\d)[a-zA-Z0-9][a-zA-Z0-9\\-_/]*$");
    
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
     * Returns true if the query looks like an identifier/code (e.g. SO-1005).
     *
     * These queries should typically be treated as exact matches to avoid noisy results
     * from tokenization and fuzzy matching.
     */
    public static boolean isCodeLikeQuery(String query) {
        if (query == null) {
            return false;
        }
        String trimmed = query.trim();
        if (trimmed.isEmpty() || trimmed.contains(" ")) {
            return false;
        }
        return CODE_LIKE_QUERY.matcher(trimmed).matches();
    }

    /**
     * Normalize a code query for exact matching:
     * - Trim
     * - Lowercase
     *
     * Note: We intentionally keep separators like '-' to preserve identifiers.
     */
    public static String normalizeCodeQuery(String query) {
        if (query == null) {
            return "";
        }
        return query.trim().toLowerCase();
    }
    
    /**
     * Extract tenant id.
     *
     * Resolution order (matches gateway behavior):
     * 1) Explicit `X-Tenant-Id` header value (if non-blank)
     * 2) JWT claim: `tenantId`, `tenant_id`, or `tenant`
     *
     * Returns null if no tenant could be resolved.
     */
    public static String extractTenantId(String tenantHeader, String authorizationHeader, com.erp.smb.common.security.JwtUtils jwtUtils) {
        if (tenantHeader != null && !tenantHeader.isBlank()) {
            return tenantHeader;
        }
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ") || jwtUtils == null) {
            return null;
        }
        try {
            var claims = jwtUtils.parse(authorizationHeader.substring(7)).getBody();
            Object t = claims.get("tenantId");
            if (t == null) {
                t = claims.get("tenant_id");
            }
            if (t == null) {
                t = claims.get("tenant");
            }
            return t != null ? String.valueOf(t) : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Backwards-compatible helper: tries to parse the provided JWT and extract tenant claims.
     *
     * Note: This method does not default to any hardcoded tenant.
     */
    public static String extractTenantIdFromJwt(String jwtToken, com.erp.smb.common.security.JwtUtils jwtUtils) {
        if (jwtToken == null || jwtToken.isBlank() || jwtUtils == null) {
            return null;
        }
        try {
            var claims = jwtUtils.parse(jwtToken).getBody();
            Object t = claims.get("tenantId");
            if (t == null) {
                t = claims.get("tenant_id");
            }
            if (t == null) {
                t = claims.get("tenant");
            }
            return t != null ? String.valueOf(t) : null;
        } catch (Exception ignored) {
            return null;
        }
    }
}
