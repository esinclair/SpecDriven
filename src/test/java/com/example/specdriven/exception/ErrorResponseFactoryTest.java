package com.example.specdriven.exception;

import com.example.specdriven.api.model.ErrorResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ErrorResponseFactory.
 * Verifies that all error response factory methods create consistent ErrorResponse objects
 * with correct stable error codes and messages.
 */
class ErrorResponseFactoryTest {

    @Test
    void validationFailed_CreatesResponseWithCorrectCode() {
        String message = "Invalid input data";
        ErrorResponse response = ErrorResponseFactory.validationFailed(message);
        
        assertNotNull(response);
        assertEquals(ErrorResponseFactory.VALIDATION_FAILED, response.getCode());
        assertEquals(message, response.getMessage());
    }

    @Test
    void resourceNotFound_CreatesResponseWithCorrectCode() {
        String message = "User not found";
        ErrorResponse response = ErrorResponseFactory.resourceNotFound(message);
        
        assertNotNull(response);
        assertEquals(ErrorResponseFactory.RESOURCE_NOT_FOUND, response.getCode());
        assertEquals(message, response.getMessage());
    }

    @Test
    void conflict_CreatesResponseWithCorrectCode() {
        String message = "Email already exists";
        ErrorResponse response = ErrorResponseFactory.conflict(message);
        
        assertNotNull(response);
        assertEquals(ErrorResponseFactory.CONFLICT, response.getCode());
        assertEquals(message, response.getMessage());
    }

    @Test
    void authenticationRequired_CreatesResponseWithCorrectCode() {
        String message = "Authentication required";
        ErrorResponse response = ErrorResponseFactory.authenticationRequired(message);
        
        assertNotNull(response);
        assertEquals(ErrorResponseFactory.AUTHENTICATION_REQUIRED, response.getCode());
        assertEquals(message, response.getMessage());
    }

    @Test
    void authenticationFailed_CreatesResponseWithCorrectCode() {
        String message = "Invalid credentials";
        ErrorResponse response = ErrorResponseFactory.authenticationFailed(message);
        
        assertNotNull(response);
        assertEquals(ErrorResponseFactory.AUTHENTICATION_FAILED, response.getCode());
        assertEquals(message, response.getMessage());
    }

    @Test
    void internalError_CreatesResponseWithCorrectCode() {
        String message = "An unexpected error occurred";
        ErrorResponse response = ErrorResponseFactory.internalError(message);
        
        assertNotNull(response);
        assertEquals(ErrorResponseFactory.INTERNAL_ERROR, response.getCode());
        assertEquals(message, response.getMessage());
    }

    @Test
    void serviceUnavailable_CreatesResponseWithCorrectCode() {
        String message = "Service temporarily unavailable";
        ErrorResponse response = ErrorResponseFactory.serviceUnavailable(message);
        
        assertNotNull(response);
        assertEquals(ErrorResponseFactory.SERVICE_UNAVAILABLE, response.getCode());
        assertEquals(message, response.getMessage());
    }

    @Test
    void errorCodes_AreStableConstants() {
        // Verify error codes are defined as expected stable values
        assertEquals("VALIDATION_FAILED", ErrorResponseFactory.VALIDATION_FAILED);
        assertEquals("RESOURCE_NOT_FOUND", ErrorResponseFactory.RESOURCE_NOT_FOUND);
        assertEquals("CONFLICT", ErrorResponseFactory.CONFLICT);
        assertEquals("AUTHENTICATION_REQUIRED", ErrorResponseFactory.AUTHENTICATION_REQUIRED);
        assertEquals("AUTHENTICATION_FAILED", ErrorResponseFactory.AUTHENTICATION_FAILED);
        assertEquals("INTERNAL_ERROR", ErrorResponseFactory.INTERNAL_ERROR);
        assertEquals("SERVICE_UNAVAILABLE", ErrorResponseFactory.SERVICE_UNAVAILABLE);
    }

    @Test
    void validationFailed_HandlesNullMessage() {
        ErrorResponse response = ErrorResponseFactory.validationFailed(null);
        
        assertNotNull(response);
        assertEquals(ErrorResponseFactory.VALIDATION_FAILED, response.getCode());
        assertNull(response.getMessage());
    }

    @Test
    void validationFailed_HandlesEmptyMessage() {
        String message = "";
        ErrorResponse response = ErrorResponseFactory.validationFailed(message);
        
        assertNotNull(response);
        assertEquals(ErrorResponseFactory.VALIDATION_FAILED, response.getCode());
        assertEquals(message, response.getMessage());
    }
}
