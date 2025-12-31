package com.example.specdriven.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ConflictException.
 * Verifies exception creation and message handling.
 */
class ConflictExceptionTest {

    @Test
    void constructor_WithMessage_SetsMessage() {
        String message = "Email already exists";
        ConflictException exception = new ConflictException(message);
        
        assertEquals(message, exception.getMessage());
    }

    @Test
    void constructor_WithMessageAndCause_SetsBoth() {
        String message = "Email already exists";
        Throwable cause = new IllegalStateException("State error");
        ConflictException exception = new ConflictException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void isRuntimeException() {
        ConflictException exception = new ConflictException("Test");
        
        assertInstanceOf(RuntimeException.class, exception);
    }
}
