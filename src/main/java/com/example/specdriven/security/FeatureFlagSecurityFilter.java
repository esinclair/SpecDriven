package com.example.specdriven.security;

import com.example.specdriven.api.model.ErrorResponse;
import com.example.specdriven.config.FeatureFlagConfig;
import com.example.specdriven.exception.ErrorResponseFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Security filter that gates user management endpoints based on feature flag.
 * 
 * When FeatureFlag.usersApi is disabled (false):
 * - /users/** endpoints return 404 Not Found
 * - /login endpoint returns 404 Not Found
 * - /ping endpoint is ALWAYS accessible (bypasses feature flag)
 * 
 * The 404 response does not reveal that the feature exists or is disabled,
 * to prevent information disclosure.
 */
@Component
public class FeatureFlagSecurityFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(FeatureFlagSecurityFilter.class);

    private final FeatureFlagConfig featureFlagConfig;
    private final ObjectMapper objectMapper;

    public FeatureFlagSecurityFilter(FeatureFlagConfig featureFlagConfig, ObjectMapper objectMapper) {
        this.featureFlagConfig = featureFlagConfig;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        // /ping is ALWAYS accessible regardless of feature flag
        if (path.equals("/ping")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Check if request is to a gated endpoint
        if (isGatedEndpoint(path)) {
            // If feature flag is disabled, return 404
            if (!featureFlagConfig.isUsersApi()) {
                logger.debug("Feature flag disabled, returning 404 for path: {}", path);
                sendNotFoundResponse(response);
                return;
            }
        }

        // Feature flag is enabled or endpoint is not gated, proceed with request
        filterChain.doFilter(request, response);
    }

    /**
     * Check if the request path is gated by the feature flag.
     * 
     * @param path the request URI path
     * @return true if the endpoint requires the feature flag to be enabled
     */
    private boolean isGatedEndpoint(String path) {
        return path.startsWith("/users") || path.equals("/login");
    }

    /**
     * Send a 404 Not Found response without revealing feature information.
     */
    private void sendNotFoundResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        
        // Generic 404 message - does not reveal feature flag status
        ErrorResponse error = ErrorResponseFactory.resourceNotFound("Resource not found");
        objectMapper.writeValue(response.getOutputStream(), error);
    }
}
