package com.example.specdriven.integration;

import com.example.specdriven.api.model.PingResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for health check endpoint with feature flag disabled.
 * Verifies that /ping always works regardless of feature flag state.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"feature-flag.users-api=false"})
class HealthCheckWithFeatureFlagDisabledTest {

    @Autowired
    private TestRestTemplate restTemplate;

    /**
     * T033: Test health check works when FeatureFlag.usersApi is false
     * This test explicitly disables the feature flag to verify /ping still works
     */
    @Test
    void ping_FeatureFlagDisabled_StillReturns200() {
        // When: Call GET /ping with feature flag disabled
        ResponseEntity<PingResponse> response = restTemplate.getForEntity("/ping", PingResponse.class);

        // Then: Verify 200 OK status (not 404 or any other error)
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // And: Verify response body is present
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("pong");
    }
}
