package com.example.specdriven.error;

/**
 * Stable error codes for API responses.
 * These codes must never change to maintain API contract stability.
 */
public enum ErrorCode {
    VALIDATION_FAILED,
    UNAUTHORIZED,
    FORBIDDEN,
    RESOURCE_NOT_FOUND,
    FEATURE_DISABLED,
    CONFLICT,
    INTERNAL_ERROR,
    SERVICE_UNAVAILABLE
}
