package com.example.specdriven.filter;

import com.example.specdriven.api.model.ErrorResponse;
import com.example.specdriven.config.FeatureFlagProperties;
import com.example.specdriven.error.ApiErrorCode;
import com.example.specdriven.error.ErrorResponseFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * Filter to check if Users API feature is enabled before processing requests.
 * Returns 404 with shared error response if the feature is disabled.
 */
@Component
@Order(1) // Run early in the filter chain, before validation
public class UsersApiFeatureFilter implements Filter {

    private final FeatureFlagProperties featureFlags;
    private final ObjectMapper objectMapper;

    public UsersApiFeatureFilter(FeatureFlagProperties featureFlags, ObjectMapper objectMapper) {
        this.featureFlags = featureFlags;
        this.objectMapper = objectMapper;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String path = httpRequest.getRequestURI();
        
        // Check if this is a Users API or Login endpoint
        if (isUsersApiPath(path) && !featureFlags.isUsersApi()) {
            // Feature is disabled, return 404
            ErrorResponse error = ErrorResponseFactory.from(
                    ApiErrorCode.FEATURE_DISABLED,
                    "Feature not available",
                    Map.of("path", path, "feature", "usersApi")
            );
            
            httpResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
            httpResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(httpResponse.getWriter(), error);
            return;
        }
        
        // Feature is enabled or not a Users API path, continue with the filter chain
        chain.doFilter(request, response);
    }
    
    private boolean isUsersApiPath(String path) {
        return path.startsWith("/users") || path.equals("/login");
    }
}
