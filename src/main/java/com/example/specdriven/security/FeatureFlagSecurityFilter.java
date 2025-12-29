package com.example.specdriven.security;

import com.example.specdriven.config.FeatureFlagConfig;
import com.example.specdriven.exception.ErrorResponseFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter that checks feature flags and returns 404 for disabled features.
 */
@Component
public class FeatureFlagSecurityFilter extends OncePerRequestFilter {
    
    private final FeatureFlagConfig featureFlagConfig;
    private final ObjectMapper objectMapper;
    
    public FeatureFlagSecurityFilter(FeatureFlagConfig featureFlagConfig, ObjectMapper objectMapper) {
        this.featureFlagConfig = featureFlagConfig;
        this.objectMapper = objectMapper;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        
        String path = request.getRequestURI();
        
        // Check if users API is disabled and path is for users API
        if (!featureFlagConfig.isUsersApi() && isUsersApiPath(path)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getWriter(), 
                    ErrorResponseFactory.createResourceNotFoundError("Resource not found"));
            return;
        }
        
        filterChain.doFilter(request, response);
    }
    
    private boolean isUsersApiPath(String path) {
        // Feature flag gates user management endpoints but NOT the ping endpoint
        return (path.startsWith("/users") || path.startsWith("/login")) && !path.startsWith("/ping");
    }
}
