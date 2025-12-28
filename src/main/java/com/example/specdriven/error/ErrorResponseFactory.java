package com.example.specdriven.error;

import com.example.specdriven.api.model.ErrorResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.Map;

public final class ErrorResponseFactory {

    private ErrorResponseFactory() {
    }

    public static ErrorResponse from(ApiErrorCode code, String message) {
        return from(code, message, null);
    }

    public static ErrorResponse from(ApiErrorCode code, String message, Map<String, Object> details) {
        ErrorResponse response = new ErrorResponse();
        response.setCode(code.name());
        response.setMessage(message);
        if (details != null && !details.isEmpty()) {
            response.setDetails(details);
        }
        return response;
    }
    
    public static ResponseStatusException badRequest(ApiErrorCode code, String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
    
    public static ResponseStatusException notFound(ApiErrorCode code, String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }
    
    public static ResponseStatusException conflict(ApiErrorCode code, String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }
}
