package com.erp.smb.auth.config;

import com.erp.smb.common.security.JwtAuthFilter;
import com.erp.smb.common.security.JwtUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    /**
     * All auth-service routes stay open at the authorization-rule level — login,
     * signup, and refresh must be reachable unauthenticated. But change-password
     * and delete-user (in AuthController) read the caller's identity from
     * SecurityContextHolder, so JwtAuthFilter must still run to populate it from
     * the accessToken cookie/Authorization header; without it those endpoints
     * always see an anonymous principal. CSRF is disabled because this is a
     * stateless REST API using JWT, not session cookies for CSRF protection.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public JwtAuthFilter jwtAuthFilter(JwtUtils jwtUtils) {
        return new JwtAuthFilter(jwtUtils);
    }

    /**
     * Shared password encoder — BCrypt with the default strength (10 rounds).
     * Exposed as a bean so AuthController can inject it rather than constructing
     * its own instance inline, keeping encoder configuration in one place.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Shared JwtUtils — constructed once with the configured secret and TTLs.
     * AuthController injects this bean instead of creating its own JwtUtils,
     * guaranteeing that the signing key is consistent across the service.
     */
    @Bean
    public JwtUtils jwtUtils(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-ttl}") long accessTtl) {
        return new JwtUtils(secret, accessTtl, accessTtl * 24L);
    }
}
