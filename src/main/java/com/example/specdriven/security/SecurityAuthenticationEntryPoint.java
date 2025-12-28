package com.example.specdriven.security;

import com.example.specdriven.api.model.ErrorResponse;
import com.example.specdriven.error.ErrorCode;
import com.example.specdriven.error.ErrorResponseFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * Custom authentication entry point that returns 401 with shared ErrorResponse format.
 */
@Component
@ConditionalOnProperty(name = "feature-flag.users-api", havingValue = "true")
public class SecurityAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public SecurityAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        ErrorResponse errorResponse = ErrorResponseFactory.from(
                ErrorCode.UNAUTHORIZED,
                "Missing or invalid bearer token",
                Map.of("path", request.getRequestURI())
        );

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}

