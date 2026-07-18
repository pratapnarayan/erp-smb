package com.erp.smb.gateway.filter;

import jakarta.annotation.PreDestroy;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rate limiting filter for search endpoints.
 *
 * Strategy:
 *   - Autocomplete (/api/search/suggestions): 60 req/min per key (1 rps, strict)
 *   - Full search  (/api/search):             20 req/min per key (~1 req/3s, moderate)
 *
 * Rate limit key is tenant-aware (X-Tenant-Id header) with an IP-based fallback
 * for unauthenticated or header-less requests.
 *
 * Limitations (acceptable for a single-instance deployment):
 *   - Counters are in-process; they reset on restart and are not shared across
 *     multiple gateway instances. Migrate to Redis + token-bucket algorithm if
 *     the gateway ever scales horizontally.
 *
 * Lifecycle:
 *   - The bucket-reset scheduler is created once and stopped via @PreDestroy,
 *     preventing thread leaks on application shutdown or test teardown.
 *
 * Note: Registered manually via FilterConfig, not as @Component.
 */
public class RateLimitFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    private static final int AUTOCOMPLETE_LIMIT_PER_MINUTE = 60;
    private static final int SEARCH_LIMIT_PER_MINUTE       = 20;

    /** Separate buckets per endpoint type to allow independent limits. */
    private final ConcurrentHashMap<String, AtomicInteger> autocompleteBuckets = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicInteger> searchBuckets       = new ConcurrentHashMap<>();

    /**
     * Single-thread scheduler for periodic bucket resets.
     * Stored as a field so @PreDestroy can shut it down cleanly.
     */
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "rate-limit-reset");
        t.setDaemon(true); // don't block JVM shutdown
        return t;
    });

    public RateLimitFilter() {
        // Reset all counters once per minute to implement a fixed-window strategy.
        scheduler.scheduleAtFixedRate(() -> {
            autocompleteBuckets.clear();
            searchBuckets.clear();
            log.debug("Rate limit buckets reset");
        }, 1, 1, TimeUnit.MINUTES);
    }

    /**
     * Gracefully shut down the reset scheduler when the Spring context closes.
     * Without this, the thread pool would outlive the application context in tests
     * and during hot-reload scenarios.
     */
    @PreDestroy
    public void shutdown() {
        log.info("Shutting down rate limit scheduler");
        scheduler.shutdownNow();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  req  = (HttpServletRequest)  request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String path = req.getRequestURI();

        // Only apply rate limiting to search endpoints
        if (!path.startsWith("/api/search")) {
            chain.doFilter(request, response);
            return;
        }

        // Guard: block indexing/reindex paths — defence-in-depth alongside SecurityConfig.denyAll()
        if (path.startsWith("/api/search/index") || path.startsWith("/api/search/reindex")) {
            log.warn("Blocked external access to internal endpoint: path={}, ip={}",
                    path, req.getRemoteAddr());
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"error\":\"Forbidden\"}");
            return;
        }

        // Build rate-limit key: prefer tenant ID (stable across IPs) over raw IP
        String tenantId      = req.getHeader("X-Tenant-Id");
        String rateLimitKey  = (tenantId != null && !tenantId.isBlank())
                ? "tenant:" + tenantId
                : "ip:"     + req.getRemoteAddr();

        boolean isAutocomplete = path.startsWith("/api/search/suggestions");
        ConcurrentHashMap<String, AtomicInteger> bucket = isAutocomplete ? autocompleteBuckets : searchBuckets;
        int limit = isAutocomplete ? AUTOCOMPLETE_LIMIT_PER_MINUTE : SEARCH_LIMIT_PER_MINUTE;

        int currentCount = bucket.computeIfAbsent(rateLimitKey, k -> new AtomicInteger(0))
                                 .incrementAndGet();

        resp.setHeader("X-RateLimit-Limit",     String.valueOf(limit));
        resp.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, limit - currentCount)));

        if (currentCount > limit) {
            log.warn("Rate limit exceeded: path={}, key={}, count={}, limit={}",
                    path, rateLimitKey, currentCount, limit);
            resp.setStatus(429);
            resp.setHeader("Retry-After", "60");
            resp.setContentType("application/json");
            resp.getWriter().write("{\"error\":\"Rate limit exceeded\",\"retryAfter\":60}");
            return;
        }

        log.debug("Search request allowed: path={}, tenant={}, method={}",
                path, tenantId != null ? tenantId : "unknown", req.getMethod());

        chain.doFilter(request, response);
    }
}
