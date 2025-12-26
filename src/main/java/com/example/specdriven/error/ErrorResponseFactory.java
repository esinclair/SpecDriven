package com.example.specdriven.error;

import com.example.specdriven.api.model.ErrorResponse;

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
}
