package com.example.specdriven.error;

/**
 * Exception thrown when request validation fails.
 * Maps to HTTP 400 Bad Request with VALIDATION_FAILED error code.
 */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
    
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
