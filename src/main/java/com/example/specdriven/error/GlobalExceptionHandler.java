package com.example.specdriven.error;

import com.example.specdriven.api.model.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for consistent error responses across all endpoints.
 * Maps exceptions to HTTP status codes and stable error codes.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException ex, HttpServletRequest request) {
        Map<String, Object> details = Map.of("path", request.getRequestURI());
        ErrorResponse body = ErrorResponseFactory.from(
                ErrorCode.VALIDATION_FAILED,
                ex.getMessage(),
                details
        );
        log.warn("Validation failed: path={} message={}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, Object> details = new HashMap<>();
        details.put("path", request.getRequestURI());
        details.put("errorCount", ex.getBindingResult().getErrorCount());

        ErrorResponse body = ErrorResponseFactory.from(
                ErrorCode.VALIDATION_FAILED,
                "Request validation failed",
                details
        );

        log.warn("Validation failed: path={} errorCount={}", request.getRequestURI(), ex.getBindingResult().getErrorCount());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex, HttpServletRequest request) {
        Map<String, Object> details = Map.of("path", request.getRequestURI());
        ErrorResponse body = ErrorResponseFactory.from(
                ErrorCode.UNAUTHORIZED,
                ex.getMessage(),
                details
        );
        log.warn("Unauthorized access: path={} message={}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenException ex, HttpServletRequest request) {
        Map<String, Object> details = Map.of("path", request.getRequestURI());
        ErrorResponse body = ErrorResponseFactory.from(
                ErrorCode.FORBIDDEN,
                ex.getMessage(),
                details
        );
        log.warn("Forbidden access: path={} message={}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        Map<String, Object> details = Map.of("path", request.getRequestURI());
        ErrorResponse body = ErrorResponseFactory.from(
                ErrorCode.RESOURCE_NOT_FOUND,
                ex.getMessage(),
                details
        );
        log.warn("Resource not found: path={} message={}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(FeatureDisabledException.class)
    public ResponseEntity<ErrorResponse> handleFeatureDisabled(FeatureDisabledException ex, HttpServletRequest request) {
        Map<String, Object> details = Map.of("path", request.getRequestURI());
        ErrorResponse body = ErrorResponseFactory.from(
                ErrorCode.FEATURE_DISABLED,
                ex.getMessage(),
                details
        );
        log.warn("Feature disabled: path={} message={}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex, HttpServletRequest request) {
        Map<String, Object> details = Map.of("path", request.getRequestURI());
        ErrorResponse body = ErrorResponseFactory.from(
                ErrorCode.CONFLICT,
                ex.getMessage(),
                details
        );
        log.warn("Conflict: path={} message={}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnhandled(Exception ex, HttpServletRequest request) {
        Map<String, Object> details = Map.of(
                "path", request.getRequestURI(),
                "hint", "If this persists, contact support with timestamp"
        );

        ErrorResponse body = ErrorResponseFactory.from(
                ErrorCode.INTERNAL_ERROR,
                "Unexpected server error",
                details
        );

        // Don't log secrets; keep exception stack trace server-side.
        log.error("Unhandled exception: path={}", request.getRequestURI(), ex);

        // Keep response quick; do not perform expensive work here.
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}

