package com.example.specdriven.error;

/**
 * Exception thrown when a feature is disabled via feature flag.
 * Maps to HTTP 404 Not Found with FEATURE_DISABLED error code.
 */
public class FeatureDisabledException extends RuntimeException {
    public FeatureDisabledException(String message) {
        super(message);
    }
    
    public FeatureDisabledException(String message, Throwable cause) {
        super(message, cause);
    }
}
