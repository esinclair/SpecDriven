package com.example.specdriven.exception;

import com.example.specdriven.api.model.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for GlobalExceptionHandler.
 * Verifies exception to HTTP status code and ErrorResponse mapping.
 */
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    private final WebRequest webRequest = mock(WebRequest.class);

    @Test
    void handleValidationException_Returns400WithValidationFailedCode() {
        ValidationException exception = new ValidationException("Invalid input");
        
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handleValidationException(exception, webRequest);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorResponseFactory.VALIDATION_FAILED, response.getBody().getCode());
        assertEquals("Invalid input", response.getBody().getMessage());
    }

    @Test
    void handleMethodArgumentNotValid_Returns400WithValidationFailedCode() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        ObjectError objectError = new FieldError("object", "field", "Field is required");
        
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getAllErrors()).thenReturn(Collections.singletonList(objectError));
        
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handleMethodArgumentNotValid(exception, webRequest);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorResponseFactory.VALIDATION_FAILED, response.getBody().getCode());
        assertTrue(response.getBody().getMessage().contains("Validation failed"));
    }

    @Test
    void handleMethodArgumentNotValid_NoErrors_ReturnsGenericMessage() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.hasErrors()).thenReturn(false);
        
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handleMethodArgumentNotValid(exception, webRequest);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Validation failed: ", response.getBody().getMessage());
    }

    @Test
    void handleHandlerMethodValidation_WithErrors_Returns400() {
        HandlerMethodValidationException exception = mock(HandlerMethodValidationException.class);
        ObjectError objectError = new ObjectError("object", "Field validation failed");
        List<? extends org.springframework.context.MessageSourceResolvable> errors = List.of(objectError);
        
        when(exception.getAllErrors()).thenAnswer(invocation -> errors);
        
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handleHandlerMethodValidation(exception, webRequest);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorResponseFactory.VALIDATION_FAILED, response.getBody().getCode());
        assertTrue(response.getBody().getMessage().contains("Validation failed"));
    }

    @Test
    void handleHandlerMethodValidation_NoErrors_ReturnsGenericMessage() {
        HandlerMethodValidationException exception = mock(HandlerMethodValidationException.class);
        
        when(exception.getAllErrors()).thenReturn(Collections.emptyList());
        
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handleHandlerMethodValidation(exception, webRequest);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Validation failed", response.getBody().getMessage());
    }

    @Test
    void handleResourceNotFound_Returns404WithResourceNotFoundCode() {
        ResourceNotFoundException exception = new ResourceNotFoundException("User not found");
        
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handleResourceNotFound(exception, webRequest);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorResponseFactory.RESOURCE_NOT_FOUND, response.getBody().getCode());
        assertEquals("User not found", response.getBody().getMessage());
    }

    @Test
    void handleConflict_Returns409WithConflictCode() {
        ConflictException exception = new ConflictException("Email already exists");
        
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handleConflict(exception, webRequest);
        
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorResponseFactory.CONFLICT, response.getBody().getCode());
        assertEquals("Email already exists", response.getBody().getMessage());
    }

    @Test
    void handleAuthenticationException_Returns401WithAuthenticationFailedCode() {
        AuthenticationException exception = new AuthenticationException("Invalid credentials");
        
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handleAuthenticationException(exception, webRequest);
        
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorResponseFactory.AUTHENTICATION_FAILED, response.getBody().getCode());
        assertEquals("Invalid credentials", response.getBody().getMessage());
    }

    @Test
    void handleDataAccessException_Returns503WithServiceUnavailableCode() {
        DataAccessResourceFailureException exception = 
                new DataAccessResourceFailureException("Database connection failed");
        
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handleDataAccessException(exception, webRequest);
        
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorResponseFactory.SERVICE_UNAVAILABLE, response.getBody().getCode());
        assertTrue(response.getBody().getMessage().contains("temporarily unavailable"));
        assertEquals("60", response.getHeaders().getFirst("Retry-After"));
    }

    @Test
    void handleGenericException_Returns500WithInternalErrorCode() {
        Exception exception = new RuntimeException("Unexpected error");
        
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handleGenericException(exception, webRequest);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorResponseFactory.INTERNAL_ERROR, response.getBody().getCode());
        assertTrue(response.getBody().getMessage().contains("unexpected error"));
    }

    @Test
    void handleGenericException_DoesNotExposeStackTrace() {
        Exception exception = new RuntimeException("Sensitive internal error");
        
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handleGenericException(exception, webRequest);
        
        assertNotNull(response.getBody());
        // Verify the response message doesn't contain sensitive internal details
        assertFalse(response.getBody().getMessage().contains("Sensitive internal error"));
        assertTrue(response.getBody().getMessage().contains("unexpected error"));
    }

    @Test
    void allErrorResponses_HaveStableCodeAndMessage() {
        // Test that all handler methods return responses with both code and message fields
        ValidationException validationEx = new ValidationException("Test");
        ResponseEntity<ErrorResponse> validationResponse = globalExceptionHandler
                .handleValidationException(validationEx, webRequest);
        assertNotNull(validationResponse.getBody().getCode());
        assertNotNull(validationResponse.getBody().getMessage());
        
        ResourceNotFoundException notFoundEx = new ResourceNotFoundException("Test");
        ResponseEntity<ErrorResponse> notFoundResponse = globalExceptionHandler
                .handleResourceNotFound(notFoundEx, webRequest);
        assertNotNull(notFoundResponse.getBody().getCode());
        assertNotNull(notFoundResponse.getBody().getMessage());
        
        ConflictException conflictEx = new ConflictException("Test");
        ResponseEntity<ErrorResponse> conflictResponse = globalExceptionHandler
                .handleConflict(conflictEx, webRequest);
        assertNotNull(conflictResponse.getBody().getCode());
        assertNotNull(conflictResponse.getBody().getMessage());
        
        AuthenticationException authEx = new AuthenticationException("Test");
        ResponseEntity<ErrorResponse> authResponse = globalExceptionHandler
                .handleAuthenticationException(authEx, webRequest);
        assertNotNull(authResponse.getBody().getCode());
        assertNotNull(authResponse.getBody().getMessage());
    }

    @Test
    void handleMethodArgumentTypeMismatch_NonEnumType_ReturnsBasicMessage() {
        org.springframework.web.method.annotation.MethodArgumentTypeMismatchException exception = 
                mock(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class);
        
        when(exception.getName()).thenReturn("id");
        when(exception.getRequiredType()).thenReturn(null);
        
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handleMethodArgumentTypeMismatch(exception, webRequest);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("Invalid value for parameter 'id'"));
        assertFalse(response.getBody().getMessage().contains("Valid values are"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void handleMethodArgumentTypeMismatch_NonEnumRequiredType_ReturnsBasicMessage() {
        org.springframework.web.method.annotation.MethodArgumentTypeMismatchException exception = 
                mock(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class);
        
        when(exception.getName()).thenReturn("page");
        when(exception.getRequiredType()).thenReturn((Class) Integer.class);
        
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handleMethodArgumentTypeMismatch(exception, webRequest);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("Invalid value for parameter 'page'"));
        assertFalse(response.getBody().getMessage().contains("Valid values are"));
    }
}
