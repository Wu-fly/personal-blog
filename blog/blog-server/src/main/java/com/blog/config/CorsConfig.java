package com.blog.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * CORS configuration for cross-origin requests
 * 
 * This configuration enables the frontend applications (user portal and admin portal)
 * to make requests to the backend API from different origins.
 * 
 * Configuration is externalized in application.yml for easy environment-specific setup.
 */
@Configuration
public class CorsConfig {
    
    /**
     * CORS properties loaded from application.yml
     */
    @ConfigurationProperties(prefix = "cors")
    public static class CorsProperties {
        private List<String> allowedOrigins;
        private List<String> allowedMethods;
        private List<String> allowedHeaders;
        private List<String> exposedHeaders;
        private Boolean allowCredentials;
        private Long maxAge;

        public List<String> getAllowedOrigins() {
            return allowedOrigins;
        }

        public void setAllowedOrigins(List<String> allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
        }

        public List<String> getAllowedMethods() {
            return allowedMethods;
        }

        public void setAllowedMethods(List<String> allowedMethods) {
            this.allowedMethods = allowedMethods;
        }

        public List<String> getAllowedHeaders() {
            return allowedHeaders;
        }

        public void setAllowedHeaders(List<String> allowedHeaders) {
            this.allowedHeaders = allowedHeaders;
        }

        public List<String> getExposedHeaders() {
            return exposedHeaders;
        }

        public void setExposedHeaders(List<String> exposedHeaders) {
            this.exposedHeaders = exposedHeaders;
        }

        public Boolean getAllowCredentials() {
            return allowCredentials;
        }

        public void setAllowCredentials(Boolean allowCredentials) {
            this.allowCredentials = allowCredentials;
        }

        public Long getMaxAge() {
            return maxAge;
        }

        public void setMaxAge(Long maxAge) {
            this.maxAge = maxAge;
        }
    }
    
    @Bean
    public CorsProperties corsProperties() {
        return new CorsProperties();
    }
    
    /**
     * Configure CORS filter
     * 
     * @return CorsFilter configured with properties from application.yml
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsProperties props = corsProperties();
        CorsConfiguration config = new CorsConfiguration();
        
        // Configure allowed origins - explicitly allow frontend origins
        if (props.getAllowedOrigins() != null && !props.getAllowedOrigins().isEmpty()) {
            props.getAllowedOrigins().forEach(config::addAllowedOrigin);
        } else {
            // Fallback: explicitly allow common development origins
            config.addAllowedOrigin("http://localhost:5174");
            config.addAllowedOrigin("http://localhost:5175");
            config.addAllowedOrigin("http://localhost:5176");
            config.addAllowedOrigin("http://localhost:3000");
        }
        
        // Configure allowed methods
        if (props.getAllowedMethods() != null && !props.getAllowedMethods().isEmpty()) {
            props.getAllowedMethods().forEach(config::addAllowedMethod);
        } else {
            // Fallback to common HTTP methods
            config.addAllowedMethod("GET");
            config.addAllowedMethod("POST");
            config.addAllowedMethod("PUT");
            config.addAllowedMethod("DELETE");
            config.addAllowedMethod("OPTIONS");
            config.addAllowedMethod("PATCH");
        }
        
        // Configure allowed headers
        if (props.getAllowedHeaders() != null && !props.getAllowedHeaders().isEmpty()) {
            props.getAllowedHeaders().forEach(config::addAllowedHeader);
        } else {
            // Fallback to allow all headers
            config.addAllowedHeader("*");
        }
        
        // Configure exposed headers (headers that frontend can access)
        if (props.getExposedHeaders() != null && !props.getExposedHeaders().isEmpty()) {
            props.getExposedHeaders().forEach(config::addExposedHeader);
        }
        
        // Configure credentials support (required for cookies and authorization headers)
        config.setAllowCredentials(props.getAllowCredentials() != null ? props.getAllowCredentials() : true);
        
        // Configure max age for preflight requests (in seconds)
        config.setMaxAge(props.getMaxAge() != null ? props.getMaxAge() : 3600L);
        
        // Register CORS configuration for all endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
}