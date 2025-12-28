package com.example.specdriven.error;

/**
 * Exception thrown when authentication is required but not provided or invalid.
 * Maps to HTTP 401 Unauthorized with UNAUTHORIZED error code.
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
    
    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
