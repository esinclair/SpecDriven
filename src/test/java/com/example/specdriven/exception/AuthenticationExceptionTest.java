package com.example.specdriven.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AuthenticationException.
 * Verifies exception creation and message handling.
 */
class AuthenticationExceptionTest {

    @Test
    void constructor_WithMessage_SetsMessage() {
        String message = "Invalid credentials";
        AuthenticationException exception = new AuthenticationException(message);
        
        assertEquals(message, exception.getMessage());
    }

    @Test
    void constructor_WithMessageAndCause_SetsBoth() {
        String message = "Invalid credentials";
        Throwable cause = new IllegalStateException("State error");
        AuthenticationException exception = new AuthenticationException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void isRuntimeException() {
        AuthenticationException exception = new AuthenticationException("Test");
        
        assertInstanceOf(RuntimeException.class, exception);
    }
}
