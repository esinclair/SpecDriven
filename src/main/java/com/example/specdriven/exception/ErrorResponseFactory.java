package com.example.specdriven.exception;

import com.example.specdriven.api.model.ErrorResponse;

/**
 * Factory for creating standardized error responses with stable error codes.
 */
public class ErrorResponseFactory {
    
    public static ErrorResponse createValidationError(String message) {
        return new ErrorResponse()
                .code("VALIDATION_FAILED")
                .message(message);
    }
    
    public static ErrorResponse createResourceNotFoundError(String message) {
        return new ErrorResponse()
                .code("RESOURCE_NOT_FOUND")
                .message(message);
    }
    
    public static ErrorResponse createConflictError(String message) {
        return new ErrorResponse()
                .code("CONFLICT")
                .message(message);
    }
    
    public static ErrorResponse createAuthenticationFailedError(String message) {
        return new ErrorResponse()
                .code("AUTHENTICATION_FAILED")
                .message(message);
    }
    
    public static ErrorResponse createAuthenticationRequiredError(String message) {
        return new ErrorResponse()
                .code("AUTHENTICATION_REQUIRED")
                .message(message);
    }
    
    public static ErrorResponse createInternalError(String message) {
        return new ErrorResponse()
                .code("INTERNAL_ERROR")
                .message(message);
    }
    
    public static ErrorResponse createServiceUnavailableError(String message) {
        return new ErrorResponse()
                .code("SERVICE_UNAVAILABLE")
                .message(message);
    }
}
