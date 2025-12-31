package com.example.specdriven.service;

import com.example.specdriven.config.FeatureFlagConfig;
import org.springframework.stereotype.Service;

/**
 * Service for checking feature flag states.
 * Provides centralized access to feature flag configuration.
 */
@Service
public class FeatureFlagService {

    private final FeatureFlagConfig featureFlagConfig;

    public FeatureFlagService(FeatureFlagConfig featureFlagConfig) {
        this.featureFlagConfig = featureFlagConfig;
    }

    /**
     * Check if the Users API feature is enabled.
     * Controls access to /users/** and /login endpoints.
     * 
     * @return true if Users API is enabled, false otherwise
     */
    public boolean isUsersApiEnabled() {
        return featureFlagConfig.isUsersApi();
    }

    /**
     * Check if a specific request path requires a feature flag.
     * /ping is explicitly NOT gated by any feature flag.
     * 
     * @param requestPath the request path to check
     * @return true if the path requires a feature flag, false otherwise
     */
    public boolean requiresFeatureFlag(String requestPath) {
        // Health check endpoint is never gated
        if (requestPath != null && requestPath.startsWith("/ping")) {
            return false;
        }
        
        // Users API endpoints and login require the usersApi feature flag
        if (requestPath != null && (requestPath.startsWith("/users") || requestPath.startsWith("/login"))) {
            return true;
        }
        
        // Other endpoints don't require feature flags (yet)
        return false;
    }

    /**
     * Check if a request should be blocked due to feature flag.
     * Returns true if the request requires a feature flag that is disabled.
     * 
     * @param requestPath the request path to check
     * @return true if the request should be blocked, false otherwise
     */
    public boolean shouldBlockRequest(String requestPath) {
        // Health check is never blocked
        if (requestPath != null && requestPath.startsWith("/ping")) {
            return false;
        }
        
        // Check if Users API endpoints are blocked
        if (requestPath != null && (requestPath.startsWith("/users") || requestPath.startsWith("/login"))) {
            return !isUsersApiEnabled();
        }
        
        // Other endpoints are not blocked
        return false;
    }
}
