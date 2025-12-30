package com.example.specdriven.exception;

import com.example.specdriven.api.model.ErrorResponse;

/**
 * Factory class for creating consistent ErrorResponse objects with stable error codes.
 * Ensures all error responses follow the contract-defined structure.
 */
public class ErrorResponseFactory {

    // Stable error codes as defined in the OpenAPI contract
    public static final String VALIDATION_FAILED = "VALIDATION_FAILED";
    public static final String RESOURCE_NOT_FOUND = "RESOURCE_NOT_FOUND";
    public static final String CONFLICT = "CONFLICT";
    public static final String AUTHENTICATION_REQUIRED = "AUTHENTICATION_REQUIRED";
    public static final String AUTHENTICATION_FAILED = "AUTHENTICATION_FAILED";
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";
    public static final String SERVICE_UNAVAILABLE = "SERVICE_UNAVAILABLE";

    /**
     * Create an error response for validation failures.
     *
     * @param message user-friendly error message
     * @return ErrorResponse with VALIDATION_FAILED code
     */
    public static ErrorResponse validationFailed(String message) {
        ErrorResponse error = new ErrorResponse();
        error.setCode(VALIDATION_FAILED);
        error.setMessage(message);
        return error;
    }

    /**
     * Create an error response for resource not found.
     *
     * @param message user-friendly error message
     * @return ErrorResponse with RESOURCE_NOT_FOUND code
     */
    public static ErrorResponse resourceNotFound(String message) {
        ErrorResponse error = new ErrorResponse();
        error.setCode(RESOURCE_NOT_FOUND);
        error.setMessage(message);
        return error;
    }

    /**
     * Create an error response for resource conflicts.
     *
     * @param message user-friendly error message
     * @return ErrorResponse with CONFLICT code
     */
    public static ErrorResponse conflict(String message) {
        ErrorResponse error = new ErrorResponse();
        error.setCode(CONFLICT);
        error.setMessage(message);
        return error;
    }

    /**
     * Create an error response for missing authentication.
     *
     * @param message user-friendly error message
     * @return ErrorResponse with AUTHENTICATION_REQUIRED code
     */
    public static ErrorResponse authenticationRequired(String message) {
        ErrorResponse error = new ErrorResponse();
        error.setCode(AUTHENTICATION_REQUIRED);
        error.setMessage(message);
        return error;
    }

    /**
     * Create an error response for failed authentication.
     *
     * @param message user-friendly error message
     * @return ErrorResponse with AUTHENTICATION_FAILED code
     */
    public static ErrorResponse authenticationFailed(String message) {
        ErrorResponse error = new ErrorResponse();
        error.setCode(AUTHENTICATION_FAILED);
        error.setMessage(message);
        return error;
    }

    /**
     * Create an error response for internal server errors.
     *
     * @param message user-friendly error message (no sensitive data)
     * @return ErrorResponse with INTERNAL_ERROR code
     */
    public static ErrorResponse internalError(String message) {
        ErrorResponse error = new ErrorResponse();
        error.setCode(INTERNAL_ERROR);
        error.setMessage(message);
        return error;
    }

    /**
     * Create an error response for service unavailability (transient failures).
     *
     * @param message user-friendly error message
     * @return ErrorResponse with SERVICE_UNAVAILABLE code
     */
    public static ErrorResponse serviceUnavailable(String message) {
        ErrorResponse error = new ErrorResponse();
        error.setCode(SERVICE_UNAVAILABLE);
        error.setMessage(message);
        return error;
    }
}
