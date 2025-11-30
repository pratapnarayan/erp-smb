package com.erp.smb.order.config;

import com.erp.smb.common.security.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Configuration
public class SecurityConfig {
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
    http.csrf(csrf->csrf.disable())
       .sessionManagement(sm->sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
       .authorizeHttpRequests(auth->auth.anyRequest().authenticated())
       .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }
  @Bean
  public JwtAuthFilter jwtAuthFilter(JwtUtils utils){ return new JwtAuthFilter(utils);} 
  @Bean
  public JwtUtils jwtUtils(@Value("${app.jwt.secret}") String secret, @Value("${app.jwt.access-ttl}") long accessTtl){ return new JwtUtils(secret, accessTtl, accessTtl*24);} 
  static class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtUtils jwtUtils; JwtAuthFilter(JwtUtils jwtUtils){this.jwtUtils=jwtUtils;}
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
      String auth = request.getHeader("Authorization");
      if (auth!=null && auth.startsWith("Bearer ")) {
        String token = auth.substring(7);
        if (jwtUtils.validate(token)) {
          var claims = jwtUtils.parse(token).getBody();
          List<String> roles = (List<String>) claims.getOrDefault("roles", List.of());
          UserDetails principal = User.withUsername(claims.getSubject()).password("").roles(roles.toArray(String[]::new)).build();
          var authToken = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
          SecurityContextHolder.getContext().setAuthentication(authToken);
        }
      }
      filterChain.doFilter(request, response);
    }
  }
}
