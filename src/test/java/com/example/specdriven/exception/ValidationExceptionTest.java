package com.example.specdriven.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ValidationException.
 * Verifies exception creation and message handling.
 */
class ValidationExceptionTest {

    @Test
    void constructor_WithMessage_SetsMessage() {
        String message = "Validation failed";
        ValidationException exception = new ValidationException(message);
        
        assertEquals(message, exception.getMessage());
    }

    @Test
    void constructor_WithMessageAndCause_SetsBoth() {
        String message = "Validation failed";
        Throwable cause = new IllegalArgumentException("Invalid argument");
        ValidationException exception = new ValidationException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void isRuntimeException() {
        ValidationException exception = new ValidationException("Test");
        
        assertInstanceOf(RuntimeException.class, exception);
    }
}
