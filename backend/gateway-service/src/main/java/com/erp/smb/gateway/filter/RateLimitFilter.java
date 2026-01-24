package com.erp.smb.gateway.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Rate limiting filter for search endpoints.
 * Applies strict limits for autocomplete, moderate limits for full search.
 * Tenant-aware with IP-based fallback.
 * 
 * Note: Registered manually via FilterConfig, not as @Component
 */
public class RateLimitFilter implements Filter {
    
    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);
    
    // Rate limit buckets: key = tenantId (or IP as fallback)
    private final ConcurrentHashMap<String, AtomicInteger> autocompleteBuckets = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicInteger> searchBuckets = new ConcurrentHashMap<>();
    
    // Limits per minute
    private static final int AUTOCOMPLETE_LIMIT_PER_MINUTE = 60;  // 1 request/second - strict
    private static final int SEARCH_LIMIT_PER_MINUTE = 20;        // ~1 request/3 seconds - moderate
    
    public RateLimitFilter() {
        // Reset buckets every minute
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
            autocompleteBuckets.clear();
            searchBuckets.clear();
        }, 1, 1, TimeUnit.MINUTES);
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        
        String path = req.getRequestURI();
        
        // Only apply rate limiting to search endpoints
        if (!path.startsWith("/api/search")) {
            chain.doFilter(request, response);
            return;
        }
        
        // CRITICAL: Block indexing endpoints from external access
        if (path.startsWith("/api/search/index") || path.startsWith("/api/search/reindex")) {
            log.warn("Blocked external access to indexing endpoint: path={}, ip={}", 
                path, req.getRemoteAddr());
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write("{\"error\":\"Forbidden\"}");
            return;
        }
        
        // Determine rate limit key: tenant-aware with IP fallback
        String tenantId = req.getHeader("X-Tenant-Id");
        String rateLimitKey = (tenantId != null && !tenantId.isBlank()) 
            ? "tenant:" + tenantId 
            : "ip:" + req.getRemoteAddr();
        
        // Determine which bucket to use
        boolean isAutocomplete = path.startsWith("/api/search/suggestions");
        ConcurrentHashMap<String, AtomicInteger> bucket = isAutocomplete ? autocompleteBuckets : searchBuckets;
        int limit = isAutocomplete ? AUTOCOMPLETE_LIMIT_PER_MINUTE : SEARCH_LIMIT_PER_MINUTE;
        
        // Get or create counter
        AtomicInteger counter = bucket.computeIfAbsent(rateLimitKey, k -> new AtomicInteger(0));
        int currentCount = counter.incrementAndGet();
        
        // Check if limit exceeded
        if (currentCount > limit) {
            log.warn("Rate limit exceeded: path={}, key={}, count={}, limit={}", 
                path, rateLimitKey, currentCount, limit);
            
            resp.setStatus(429); // Too Many Requests
            resp.setHeader("X-RateLimit-Limit", String.valueOf(limit));
            resp.setHeader("X-RateLimit-Remaining", "0");
            resp.setHeader("Retry-After", "60");
            resp.getWriter().write("{\"error\":\"Rate limit exceeded\",\"retryAfter\":60}");
            return;
        }
        
        // Add rate limit headers
        resp.setHeader("X-RateLimit-Limit", String.valueOf(limit));
        resp.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, limit - currentCount)));
        
        // Log search request for observability
        log.info("Search request: path={}, tenant={}, method={}, status=allowed", 
            path, tenantId != null ? tenantId : "unknown", req.getMethod());
        
        chain.doFilter(request, response);
    }
}
