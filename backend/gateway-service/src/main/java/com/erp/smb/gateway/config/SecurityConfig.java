package com.erp.smb.gateway.config;

import com.erp.smb.common.security.JwtAuthFilter;
import com.erp.smb.common.security.JwtUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class SecurityConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins:http://localhost:5173}") String allowedOrigins) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*")); // Allow all headers to support multipart/form-data
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter,
            @Qualifier("corsConfigurationSource") CorsConfigurationSource corsConfigurationSource) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Allow CORS preflight requests
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/actuator/**", "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**")
                        .permitAll()
                        // Allow proxied Swagger and OpenAPI docs via gateway.
                        // IMPORTANT: Spring's PathPattern does not allow more pattern data after a `**`
                        // segment,
                        // so patterns like `/api/**/v3/api-docs/**` cause PatternParseException.
                        // Instead, list the known service-specific doc endpoints explicitly.
                        .requestMatchers(
                                "/api/auth/v3/api-docs/**",
                                "/api/users/v3/api-docs/**",
                                "/api/products/v3/api-docs/**",
                                "/api/orders/v3/api-docs/**",
                                "/api/sales/v3/api-docs/**",
                                "/api/finance/v3/api-docs/**",
                                "/api/hrms/v3/api-docs/**",
                                "/api/enquiry/v3/api-docs/**",
                                "/api/reports/v3/api-docs/**")
                        .permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/reports/**").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public JwtAuthFilter jwtAuthFilter(JwtUtils jwtUtils) {
        return new JwtAuthFilter(jwtUtils);
    }

    @Bean
    public JwtUtils jwtUtils(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-ttl}") long accessTtl) {
        return new JwtUtils(secret, accessTtl, accessTtl * 24);
    }
}
