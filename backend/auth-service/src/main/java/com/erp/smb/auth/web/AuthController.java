package com.erp.smb.auth.web;

import com.erp.smb.auth.domain.UserEntity;
import com.erp.smb.auth.repo.UserRepository;
import com.erp.smb.common.security.JwtAuthFilter;
import com.erp.smb.common.security.JwtUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Authentication endpoints: login, signup, refresh, logout, change-password, delete-user.
 *
 * Security model:
 *   - Access token is issued as an HttpOnly cookie ("accessToken") to prevent
 *     JavaScript access (XSS mitigation). The cookie is also returned in the
 *     response body for any non-browser API clients that need it.
 *   - Refresh token is issued as an HttpOnly cookie ("refreshToken").
 *   - SameSite=Lax prevents CSRF for state-changing requests from cross-site
 *     navigations while still allowing the cookie on top-level navigations.
 *   - All manual Authorization-header token extraction has been removed from
 *     this controller; Spring Security / JwtAuthFilter handles that elsewhere.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    /** Must match JwtAuthFilter.ACCESS_TOKEN_COOKIE */
    private static final String ACCESS_COOKIE  = JwtAuthFilter.ACCESS_TOKEN_COOKIE;
    private static final String REFRESH_COOKIE = "refreshToken";

    /** Cookie TTL in seconds — access token lives for accessTtl, refresh for 24× that. */
    private static final int REFRESH_COOKIE_MAX_AGE_SECONDS = 86400; // 24 h default; adjust as needed

    private final UserRepository users;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder encoder;
    /** Whether to set the Secure attribute on auth cookies — true behind HTTPS, false for local http://localhost dev. */
    private final boolean cookieSecure;

    public AuthController(
            UserRepository users,
            JwtUtils jwtUtils,
            PasswordEncoder encoder,
            @Value("${app.security.cookie-secure:true}") boolean cookieSecure) {
        this.users        = users;
        this.jwtUtils     = jwtUtils;
        this.encoder      = encoder;
        this.cookieSecure = cookieSecure;
    }

    // ── POST /api/auth/signup ─────────────────────────────────────────────────

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest req) {
        if (users.findByUsername(req.username()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "username_taken"));
        }
        UserEntity u = new UserEntity();
        u.setUsername(req.username());
        u.setPassword(encoder.encode(req.password()));
        u.setRole(req.role());
        users.save(u);
        log.info("User created: username={}, role={}", req.username(), req.role());
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    // ── POST /api/auth/login ──────────────────────────────────────────────────

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req, HttpServletResponse response) {
        var user = users.findByUsername(req.username()).orElse(null);
        if (user == null || !encoder.matches(req.password(), user.getPassword())) {
            log.warn("Login failed for username={}", req.username());
            return ResponseEntity.status(401).body(Map.of("error", "invalid_credentials"));
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", List.of(user.getRole()));
        claims.put("tenantId", "demo");

        String accessToken  = jwtUtils.generateAccessToken(user.getUsername(), claims);
        String refreshToken = jwtUtils.generateRefreshToken(user.getUsername(), claims);

        // Set tokens as HttpOnly cookies — the browser stores and sends these automatically.
        // JavaScript cannot read them, preventing XSS-based token theft.
        addAuthCookies(response, accessToken, refreshToken);

        log.info("Login successful: username={}", user.getUsername());

        // Return non-sensitive user info in the body. Tokens are in cookies only.
        return ResponseEntity.ok(Map.of(
                "username", user.getUsername(),
                "role",     user.getRole()
        ));
    }

    // ── POST /api/auth/refresh ────────────────────────────────────────────────

    /**
     * Refresh the access token using the refresh token cookie.
     * The browser sends the refreshToken cookie automatically.
     * On success, a new accessToken cookie is set.
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractCookie(request, REFRESH_COOKIE);
        if (refreshToken == null || !jwtUtils.validate(refreshToken)) {
            log.warn("Refresh token missing or invalid: path={}", request.getRequestURI());
            return ResponseEntity.status(401).body(Map.of("error", "invalid_refresh_token"));
        }

        var claims     = jwtUtils.parse(refreshToken).getBody();
        String username = claims.getSubject();
        var roles       = (List<String>) claims.get("roles");

        Map<String, Object> newClaims = new HashMap<>();
        newClaims.put("roles", roles != null ? roles : List.of());
        Object tenant = claims.get("tenantId");
        if (tenant != null) newClaims.put("tenantId", tenant.toString());

        String newAccessToken = jwtUtils.generateAccessToken(username, newClaims);

        // Issue new access token cookie; keep existing refresh token cookie unchanged.
        response.addHeader("Set-Cookie", buildCookie(ACCESS_COOKIE, newAccessToken, -1).toString());

        log.debug("Token refreshed: username={}", username);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    // ── POST /api/auth/logout ─────────────────────────────────────────────────

    /**
     * Clear auth cookies server-side.
     * The client should also clear any non-sensitive user data from localStorage.
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        // Expire both cookies immediately by setting max-age=0
        response.addHeader("Set-Cookie", buildCookie(ACCESS_COOKIE,  "", 0).toString());
        response.addHeader("Set-Cookie", buildCookie(REFRESH_COOKIE, "", 0).toString());
        return ResponseEntity.ok(Map.of("status", "logged_out"));
    }

    // ── POST /api/auth/change-password ────────────────────────────────────────

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            HttpServletRequest request,
            @RequestBody ChangePasswordRequest req) {

        // Read caller identity from the JWT in the cookie (or Bearer header, via filter)
        var auth = (org.springframework.security.core.Authentication)
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(401).body(Map.of("error", "unauthorized"));
        }

        String username = auth.getName();
        var user = users.findByUsername(username).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("error", "user_not_found"));
        }
        if (!encoder.matches(req.currentPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body(Map.of("error", "wrong_password"));
        }
        user.setPassword(encoder.encode(req.newPassword()));
        users.save(user);
        log.info("Password changed: username={}", username);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    // ── DELETE /api/auth/users/{username} (ADMIN only) ───────────────────────

    @DeleteMapping("/users/{username}")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<?> deleteUser(@PathVariable String username) {
        var auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "unauthorized"));
        }

        String callerUsername = auth.getName();

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        if (!isAdmin) {
            return ResponseEntity.status(403).body(Map.of("error", "forbidden",
                    "message", "Only ADMIN can delete users"));
        }

        if (callerUsername.equalsIgnoreCase(username)) {
            return ResponseEntity.badRequest().body(Map.of("error", "self_delete_forbidden",
                    "message", "You cannot delete your own account"));
        }

        if (users.findByUsername(username).isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "user_not_found",
                    "username", username));
        }
        users.deleteByUsername(username);
        log.info("User deleted: username={}, by={}", username, callerUsername);
        return ResponseEntity.noContent().build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Write both accessToken and refreshToken as HttpOnly cookies.
     */
    private void addAuthCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        response.addHeader("Set-Cookie", buildCookie(ACCESS_COOKIE,  accessToken,  -1).toString());
        response.addHeader("Set-Cookie", buildCookie(REFRESH_COOKIE, refreshToken, REFRESH_COOKIE_MAX_AGE_SECONDS).toString());
    }

    /**
     * Build an HttpOnly, SameSite=Lax cookie.
     *
     * @param name    cookie name
     * @param value   cookie value
     * @param maxAge  max-age in seconds; -1 means session cookie (no Max-Age attribute)
     */
    private ResponseCookie buildCookie(String name, String value, int maxAge) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, value)
                .httpOnly(true)
                .path("/")
                .sameSite("Lax");

        if (maxAge >= 0) {
            builder.maxAge(maxAge);
        }

        // Secure defaults to true (production/HTTPS). Local dev profiles set
        // app.security.cookie-secure=false so http://localhost still works.
        builder.secure(cookieSecure);

        return builder.build();
    }

    /**
     * Extract a named cookie value from the request.
     */
    private String extractCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        return Arrays.stream(cookies)
                .filter(c -> name.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    // ── Request records ───────────────────────────────────────────────────────

    public record SignupRequest(
            @NotBlank String username,
            @NotBlank String password,
            @NotBlank String role) {}

    public record LoginRequest(
            @NotBlank String username,
            @NotBlank String password) {}

    public record ChangePasswordRequest(
            @NotBlank String currentPassword,
            @NotBlank String newPassword) {}
}
