package com.erp.smb.reporting.config;

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
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter,
                                                   @Value("${app.security.permit-all:false}") boolean permitAll) throws Exception {
        http.csrf(csrf -> csrf.disable())
           .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        if (permitAll) {
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        } else {
            http.authorizeHttpRequests(auth -> auth
                    .requestMatchers("/actuator/**", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
                    .requestMatchers(org.springframework.http.HttpMethod.GET, "/v1/reports/**").permitAll()
                    .anyRequest().authenticated()
            );
        }

        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public JwtAuthFilter jwtAuthFilter(JwtUtils utils) {
        return new JwtAuthFilter(utils);
    }

    @Bean
    public JwtUtils jwtUtils(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-ttl}") long accessTtl
    ) {
        return new JwtUtils(secret, accessTtl, accessTtl * 24);
    }
}
