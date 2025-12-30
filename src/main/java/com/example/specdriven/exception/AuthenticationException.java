package com.example.specdriven.exception;

/**
 * Exception thrown when authentication fails or is required.
 * Maps to HTTP 401 Unauthorized with AUTHENTICATION_REQUIRED or AUTHENTICATION_FAILED error code.
 */
public class AuthenticationException extends RuntimeException {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
