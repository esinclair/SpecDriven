package com.example.specdriven.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ResourceNotFoundException.
 * Verifies exception creation and message handling.
 */
class ResourceNotFoundExceptionTest {

    @Test
    void constructor_WithMessage_SetsMessage() {
        String message = "User not found";
        ResourceNotFoundException exception = new ResourceNotFoundException(message);
        
        assertEquals(message, exception.getMessage());
    }

    @Test
    void constructor_WithMessageAndCause_SetsBoth() {
        String message = "User not found";
        Throwable cause = new IllegalStateException("State error");
        ResourceNotFoundException exception = new ResourceNotFoundException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void isRuntimeException() {
        ResourceNotFoundException exception = new ResourceNotFoundException("Test");
        
        assertInstanceOf(RuntimeException.class, exception);
    }
}
