package com.erp.smb.gateway.config;

import com.erp.smb.common.security.JwtAuthFilter;
import com.erp.smb.common.security.JwtUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/actuator/**", "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
                // Allow proxied Swagger and OpenAPI docs via gateway
                .requestMatchers("/api/**/v3/api-docs/**", "/api/**/swagger-ui.html", "/api/**/swagger-ui/**").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/reports/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public JwtAuthFilter jwtAuthFilter(JwtUtils jwtUtils){
        return new JwtAuthFilter(jwtUtils);
    }

    @Bean
    public JwtUtils jwtUtils(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-ttl}") long accessTtl
    ) {
        return new JwtUtils(secret, accessTtl, accessTtl * 24);
    }
}
