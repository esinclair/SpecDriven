package com.example.specdriven.security;

import com.example.specdriven.api.model.CreateUserRequest;
import com.example.specdriven.api.model.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * T014: Tests for 401/403 error responses body shape + code mapping
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "feature-flag.users-api=true"
})
class SecurityErrorResponsesTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void missingBearerToken_returns401_withSharedErrorBody() {
        // Attempt to get a user without a bearer token (after bootstrap)
        UUID userId = UUID.randomUUID();
        ResponseEntity<ErrorResponse> response = restTemplate.getForEntity(
                "/users/" + userId,
                ErrorResponse.class
        );

        // Should return 401 with shared error shape
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("UNAUTHORIZED");
        assertThat(response.getBody().getMessage()).isNotEmpty();
    }

    @Test
    void invalidBearerToken_returns401_withSharedErrorBody() {
        // Attempt to access protected endpoint with invalid token
        UUID userId = UUID.randomUUID();
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer invalid-token-12345");
        
        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                "/users/" + userId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                ErrorResponse.class
        );

        // Should return 401 with shared error shape
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("UNAUTHORIZED");
        assertThat(response.getBody().getMessage()).isNotEmpty();
    }

    @Test
    void malformedAuthorizationHeader_returns401_withSharedErrorBody() {
        // Attempt with malformed header (not "Bearer <token>")
        UUID userId = UUID.randomUUID();
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "InvalidFormat token123");
        
        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                "/users/" + userId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                ErrorResponse.class
        );

        // Should return 401 with shared error shape
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("UNAUTHORIZED");
    }
}
