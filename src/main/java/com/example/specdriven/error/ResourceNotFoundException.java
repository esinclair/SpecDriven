package com.example.specdriven.error;

/**
 * Exception thrown when a requested resource does not exist.
 * Maps to HTTP 404 Not Found with RESOURCE_NOT_FOUND error code.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
