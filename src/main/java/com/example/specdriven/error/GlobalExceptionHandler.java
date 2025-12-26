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

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, Object> details = new HashMap<>();
        details.put("path", request.getRequestURI());
        details.put("errorCount", ex.getBindingResult().getErrorCount());

        ErrorResponse body = ErrorResponseFactory.from(
                ApiErrorCode.VALIDATION_FAILED,
                "Request validation failed",
                details
        );

        log.warn("Validation failed: path={} errorCount={}", request.getRequestURI(), ex.getBindingResult().getErrorCount());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnhandled(Exception ex, HttpServletRequest request) {
        Map<String, Object> details = Map.of(
                "path", request.getRequestURI(),
                "hint", "If this persists, contact support with timestamp"
        );

        ErrorResponse body = ErrorResponseFactory.from(
                ApiErrorCode.INTERNAL_ERROR,
                "Unexpected server error",
                details
        );

        // Don't log secrets; keep exception stack trace server-side.
        log.error("Unhandled exception: path={}", request.getRequestURI(), ex);

        // Keep response quick; do not perform expensive work here.
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}

