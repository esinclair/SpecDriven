package com.example.specdriven.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for feature flags.
 * Binds to 'feature-flag.*' properties in application.yml.
 * 
 * Feature flags allow enabling/disabling features without code changes.
 * Default values are set to 'false' (disabled) until features are validated.
 */
@Configuration
@ConfigurationProperties(prefix = "feature-flag")
@Getter
@Setter
public class FeatureFlagConfig {

    /**
     * Feature flag for Users API endpoints.
     * Default: false (disabled until validated)
     * 
     * When enabled (true):
     * - /users/** endpoints are accessible
     * - /login endpoint is accessible
     * 
     * When disabled (false):
     * - /users/** and /login return 404 (no feature disclosure)
     * - /ping endpoint is NOT affected (always accessible)
     */
    private boolean usersApi = false;
}
