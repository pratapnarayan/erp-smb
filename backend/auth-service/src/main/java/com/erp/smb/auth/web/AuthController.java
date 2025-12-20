package com.erp.smb.auth.web;

import com.erp.smb.auth.domain.UserEntity;
import com.erp.smb.auth.repo.UserRepository;
import com.erp.smb.common.security.JwtUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository users;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthController(UserRepository users, @Value("${app.jwt.secret}") String secret, @Value("${app.jwt.access-ttl}") long ttl) {
        this.users = users;
        this.jwtUtils = new JwtUtils(secret, ttl, ttl * 24);
    }

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
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        var user = users.findByUsername(req.username()).orElse(null);
        if (user == null || !encoder.matches(req.password(), user.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("error", "invalid_credentials"));
        }
        java.util.Map<String, Object> claims = new java.util.HashMap<>();
        claims.put("roles", java.util.List.of(user.getRole()));
        // Include a demo tenantId for local testing; adjust as needed for multi-tenant setups
        claims.put("tenantId", "demo");
        String access = jwtUtils.generateAccessToken(user.getUsername(), claims);
        String refresh = jwtUtils.generateRefreshToken(user.getUsername(), claims);
        return ResponseEntity.ok(Map.of("accessToken", access, "refreshToken", refresh, "username", user.getUsername(), "role", user.getRole()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String,String> body) {
        String token = body.get("refreshToken");
        if (token == null || !jwtUtils.validate(token)) return ResponseEntity.status(401).build();
        var claims = jwtUtils.parse(token).getBody();
        String username = claims.getSubject();
        var roles = (java.util.List<String>) claims.get("roles");
        java.util.Map<String, Object> newClaims = new java.util.HashMap<>();
        newClaims.put("roles", roles);
        // Preserve tenantId in the refreshed access token
        Object tenant = claims.get("tenantId");
        if (tenant != null) newClaims.put("tenantId", tenant.toString());
        String access = jwtUtils.generateAccessToken(username, newClaims);
        return ResponseEntity.ok(Map.of("accessToken", access));
    }

    public record SignupRequest(@NotBlank String username, @NotBlank String password, @NotBlank String role) {}
    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}
}
