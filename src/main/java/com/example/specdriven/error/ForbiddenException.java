package com.example.specdriven.error;

/**
 * Exception thrown when user is authenticated but lacks required permissions.
 * Maps to HTTP 403 Forbidden with FORBIDDEN error code.
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
    
    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}
