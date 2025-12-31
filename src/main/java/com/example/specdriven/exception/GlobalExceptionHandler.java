package com.example.specdriven.exception;

import com.example.specdriven.api.model.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

/**
 * Global exception handler for the application.
 * Maps exceptions to HTTP status codes and ErrorResponse objects.
 * 
 * HTTP Status Semantics for Retry Behavior:
 * - 4xx (Client Errors): Do NOT retry without changing the request
 * - 5xx (Server Errors): MAY retry with exponential backoff
 * 
 * No 'retryable' field is included in error responses.
 * Clients should infer retry behavior from HTTP status codes.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle validation exceptions (400 Bad Request).
     * Client should not retry without fixing validation errors.
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException ex, WebRequest request) {
        logger.warn("Validation failed: {}", ex.getMessage());
        ErrorResponse error = ErrorResponseFactory.validationFailed(ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle Spring validation exceptions from @Valid annotations (400 Bad Request).
     * Client should not retry without fixing validation errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, WebRequest request) {
        String message = "Validation failed: ";
        if (ex.getBindingResult().hasErrors()) {
            message += ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        }
        logger.warn("Method argument validation failed: {}", message);
        ErrorResponse error = ErrorResponseFactory.validationFailed(message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle resource not found exceptions (404 Not Found).
     * Client should not retry - resource does not exist.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex, WebRequest request) {
        logger.warn("Resource not found: {}", ex.getMessage());
        ErrorResponse error = ErrorResponseFactory.resourceNotFound(ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle conflict exceptions (409 Conflict).
     * Client should not retry without changing the request (e.g., use different email).
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex, WebRequest request) {
        logger.warn("Conflict: {}", ex.getMessage());
        ErrorResponse error = ErrorResponseFactory.conflict(ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    /**
     * Handle authentication exceptions (401 Unauthorized).
     * Client should not retry without valid credentials or token.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        logger.warn("Authentication failed: {}", ex.getMessage());
        ErrorResponse error = ErrorResponseFactory.authenticationFailed(ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle database access exceptions (503 Service Unavailable).
     * Client MAY retry with exponential backoff - transient failure.
     * Optionally includes Retry-After header.
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDataAccessException(DataAccessException ex, WebRequest request) {
        logger.error("Database access error: {}", ex.getMessage(), ex);
        ErrorResponse error = ErrorResponseFactory.serviceUnavailable("Service temporarily unavailable. Please try again later.");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .header("Retry-After", "60") // Suggest retry after 60 seconds
                .body(error);
    }

    /**
     * Handle all other unexpected exceptions (500 Internal Server Error).
     * Client MAY retry with exponential backoff.
     * Never exposes stack traces or sensitive internal details.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        logger.error("Unexpected error: {}", ex.getMessage(), ex);
        ErrorResponse error = ErrorResponseFactory.internalError("An unexpected error occurred. Please try again later.");
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
