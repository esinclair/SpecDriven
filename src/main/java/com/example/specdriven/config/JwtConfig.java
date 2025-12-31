package com.example.specdriven.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for JWT token generation and validation.
 * Binds to 'jwt.*' properties in application.yml.
 */
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    /**
     * Secret key for signing JWT tokens.
     * MUST be overridden in production via environment variable: JWT_SECRET
     * Default value is only for development/testing.
     */
    private String secret = "default-secret-change-in-production-this-is-not-secure";

    /**
     * JWT token expiration time in milliseconds.
     * Default: 86400000 ms = 24 hours
     */
    private long expirationMs = 86400000L;

    // Getters and Setters
    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    public void setExpirationMs(long expirationMs) {
        this.expirationMs = expirationMs;
    }
}
