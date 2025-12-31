package com.example.specdriven.integration;

import com.example.specdriven.api.model.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for consistent error handling across the application.
 * Tests verify that:
 * - All errors return ErrorResponse with code and message fields
 * - HTTP status codes correctly indicate retry behavior (4xx = don't retry, 5xx = may retry)
 * - No 'retryable' field is present in error responses
 * - Error codes are stable and documented
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.profiles.active=test"})
class ErrorHandlingIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    /**
     * T038: Test validation error returns 400 with VALIDATION_FAILED code
     */
    @Test
    void validationError_Returns400WithValidationFailedCode() {
        // When: Trigger validation error via test endpoint
        ResponseEntity<ErrorResponse> response = restTemplate.getForEntity(
                "/test/trigger-error?type=validation",
                ErrorResponse.class
        );

        // Then: Verify 400 Bad Request status
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // And: Verify error response structure
        ErrorResponse error = response.getBody();
        assertThat(error).isNotNull();
        assertThat(error.getCode()).isEqualTo("VALIDATION_FAILED");
        assertThat(error.getMessage()).isNotBlank();

        // And: Verify no retryable field (must use HTTP status for retry logic)
        assertThat(error.toString()).doesNotContain("retryable");
    }

    /**
     * T039: Test not found returns 404 with RESOURCE_NOT_FOUND code
     */
    @Test
    void notFound_Returns404WithResourceNotFoundCode() {
        // When: Trigger not found error via test endpoint
        ResponseEntity<ErrorResponse> response = restTemplate.getForEntity(
                "/test/trigger-error?type=notfound",
                ErrorResponse.class
        );

        // Then: Verify 404 Not Found status
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // And: Verify error response structure
        ErrorResponse error = response.getBody();
        assertThat(error).isNotNull();
        assertThat(error.getCode()).isEqualTo("RESOURCE_NOT_FOUND");
        assertThat(error.getMessage()).isNotBlank();

        // And: Verify no retryable field
        assertThat(error.toString()).doesNotContain("retryable");
    }

    /**
     * T040: Test conflict returns 409 with CONFLICT code
     */
    @Test
    void conflict_Returns409WithConflictCode() {
        // When: Trigger conflict error via test endpoint
        ResponseEntity<ErrorResponse> response = restTemplate.getForEntity(
                "/test/trigger-error?type=conflict",
                ErrorResponse.class
        );

        // Then: Verify 409 Conflict status
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        // And: Verify error response structure
        ErrorResponse error = response.getBody();
        assertThat(error).isNotNull();
        assertThat(error.getCode()).isEqualTo("CONFLICT");
        assertThat(error.getMessage()).isNotBlank();

        // And: Verify no retryable field
        assertThat(error.toString()).doesNotContain("retryable");
    }

    /**
     * T041: Test authentication failure returns 401 with AUTHENTICATION_FAILED code
     */
    @Test
    void authenticationFailure_Returns401WithAuthenticationFailedCode() {
        // When: Trigger authentication error via test endpoint
        ResponseEntity<ErrorResponse> response = restTemplate.getForEntity(
                "/test/trigger-error?type=authentication",
                ErrorResponse.class
        );

        // Then: Verify 401 Unauthorized status
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        // And: Verify error response structure
        ErrorResponse error = response.getBody();
        assertThat(error).isNotNull();
        assertThat(error.getCode()).isEqualTo("AUTHENTICATION_FAILED");
        assertThat(error.getMessage()).isNotBlank();

        // And: Verify no retryable field
        assertThat(error.toString()).doesNotContain("retryable");
    }

    /**
     * T042: Test error responses don't contain retryable field
     * This test verifies the general contract across all error types
     */
    @Test
    void errorResponse_NoRetryableField() {
        // Given: Test multiple error scenarios
        String[] errorTypes = {"validation", "notfound", "conflict", "authentication", "database"};

        for (String errorType : errorTypes) {
            // When: Trigger error
            ResponseEntity<ErrorResponse> response = restTemplate.getForEntity(
                    "/test/trigger-error?type=" + errorType,
                    ErrorResponse.class
            );

            // Then: Verify error response exists
            ErrorResponse error = response.getBody();
            assertThat(error).isNotNull();

            // And: Verify no retryable field in response
            // ErrorResponse should only have 'code' and 'message' fields
            assertThat(error.getCode()).isNotNull();
            assertThat(error.getMessage()).isNotNull();
            assertThat(error.toString()).doesNotContain("retryable");
        }
    }

    /**
     * T043: Test service unavailable returns 503 with SERVICE_UNAVAILABLE code
     */
    @Test
    void serviceUnavailable_Returns503() {
        // When: Trigger database/transient error via test endpoint
        ResponseEntity<ErrorResponse> response = restTemplate.getForEntity(
                "/test/trigger-error?type=database",
                ErrorResponse.class
        );

        // Then: Verify 503 Service Unavailable status
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);

        // And: Verify error response structure
        ErrorResponse error = response.getBody();
        assertThat(error).isNotNull();
        assertThat(error.getCode()).isEqualTo("SERVICE_UNAVAILABLE");
        assertThat(error.getMessage()).isNotBlank();

        // And: Verify Retry-After header is present
        assertThat(response.getHeaders().get("Retry-After")).isNotNull();

        // And: Verify no retryable field
        assertThat(error.toString()).doesNotContain("retryable");
    }
}
