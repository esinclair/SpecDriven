package com.example.specdriven.exception;

/**
 * Exception thrown when a conflict occurs (e.g., duplicate resource).
 */
public class ConflictException extends RuntimeException {
    
    public ConflictException(String message) {
        super(message);
    }
    
    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
