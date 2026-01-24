package com.erp.smb.gateway.config;

import com.erp.smb.gateway.filter.RateLimitFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for servlet filters.
 * Registers rate limiting filter for search endpoints.
 */
@Configuration
public class FilterConfig {
    
    @Bean
    public RateLimitFilter rateLimitFilter() {
        return new RateLimitFilter();
    }
    
    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilterRegistration() {
        FilterRegistrationBean<RateLimitFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(rateLimitFilter());
        registration.addUrlPatterns("/api/search/*", "/api/search/suggestions/*");
        registration.setOrder(1); // Execute before security filter
        return registration;
    }
}
