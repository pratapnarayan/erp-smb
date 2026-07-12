package com.erp.smb.common.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * JWT authentication filter.
 *
 * Token extraction order:
 *   1. Authorization: Bearer <token>   — API clients, internal system tokens
 *   2. HttpOnly cookie "accessToken"   — browser sessions (XSS-safe)
 *
 * Logging uses SLF4J so level is controllable via log configuration.
 * Security-relevant failures are WARN; normal debug paths are DEBUG.
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    /** Cookie name must match the name set by AuthController on login/refresh. */
    public static final String ACCESS_TOKEN_COOKIE = "accessToken";

    private final JwtUtils jwtUtils;

    public JwtAuthFilter(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Skip JWT authentication for CORS preflight requests
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractToken(request);

        if (token != null) {
            if (jwtUtils.validate(token)) {
                Claims claims = jwtUtils.parse(token).getBody();
                // Expose parsed claims to downstream request handlers
                request.setAttribute("jwtClaims", claims);

                List<String> roles = (List<String>) claims.getOrDefault("roles", List.of());
                // Strip ROLE_ prefix if present — Spring Security's hasRole() / hasAnyRole()
                // adds it automatically, so storing it twice would break role checks.
                Collection<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .toList();

                UserDetails principal = User.withUsername(claims.getSubject())
                        .password("")
                        .authorities(authorities)
                        .build();

                var authToken = new UsernamePasswordAuthenticationToken(
                        principal, null, principal.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authToken);

                log.debug("JWT authenticated: subject={}, roles={}, path={}",
                        claims.getSubject(), roles, request.getRequestURI());

            } else {
                // Warn with a short prefix of the token to aid debugging without exposing
                // the full token value in logs.
                String tokenPrefix = token.substring(0, Math.min(10, token.length()));
                log.warn("JWT validation failed: token={}..., path={}, method={}",
                        tokenPrefix, request.getRequestURI(), request.getMethod());
            }
        } else {
            log.debug("No JWT token present: path={}, method={}",
                    request.getRequestURI(), request.getMethod());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extract a JWT string from the incoming request.
     *
     * Checks the Authorization header first so that API clients and internal
     * service-to-service calls (which use Bearer tokens) are not accidentally
     * routed through the cookie path.
     *
     * @return raw JWT string, or {@code null} if not found
     */
    private String extractToken(HttpServletRequest request) {
        // 1. Authorization: Bearer <token>
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        // 2. HttpOnly cookie (browser-based sessions)
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            return Arrays.stream(cookies)
                    .filter(c -> ACCESS_TOKEN_COOKIE.equals(c.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }

        return null;
    }
}
