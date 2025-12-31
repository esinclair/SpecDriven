package com.example.specdriven.exception;

/**
 * Exception thrown when a resource conflict occurs (e.g., duplicate email).
 * Maps to HTTP 409 Conflict with CONFLICT error code.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }

    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
