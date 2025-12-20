package com.erp.smb.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtUtils jwtUtils;
    public JwtAuthFilter(JwtUtils jwtUtils){ this.jwtUtils = jwtUtils; }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            if (jwtUtils.validate(token)) {
                var claims = jwtUtils.parse(token).getBody();
                // expose claims to downstream handlers
                request.setAttribute("jwtClaims", claims);
                List<String> roles = (List<String>) claims.getOrDefault("roles", List.of());
                // Use authorities to accept roles that may already be prefixed with ROLE_
                java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> authorities = roles.stream()
                        .map(org.springframework.security.core.authority.SimpleGrantedAuthority::new)
                        .toList();
                UserDetails principal = User.withUsername(claims.getSubject())
                        .password("")
                        .authorities(authorities)
                        .build();
                var authToken = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
