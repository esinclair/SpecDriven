package com.example.specdriven.error;

public enum ApiErrorCode {
    INTERNAL_ERROR,
    VALIDATION_FAILED,
    RESOURCE_NOT_FOUND,
    UPSTREAM_TIMEOUT,
    RATE_LIMITED,

    // Users API specific codes
    UNAUTHORIZED,
    FORBIDDEN,
    USER_NOT_FOUND,
    RESOURCE_CONFLICT,
    INVALID_CREDENTIALS,
    FEATURE_DISABLED
}
