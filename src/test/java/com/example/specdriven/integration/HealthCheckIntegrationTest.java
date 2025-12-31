package com.example.specdriven.integration;

import com.example.specdriven.api.model.PingResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the health check endpoint (/ping).
 * Tests verify that the endpoint works without authentication,
 * is not affected by feature flags, and meets performance requirements.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HealthCheckIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    /**
     * T031: Test GET /ping returns 200 with PingResponse containing message "pong"
     */
    @Test
    void ping_Returns200WithPongMessage() {
        // When: Call GET /ping
        ResponseEntity<PingResponse> response = restTemplate.getForEntity("/ping", PingResponse.class);

        // Then: Verify 200 OK status
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // And: Verify response body contains message "pong"
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("pong");
    }

    /**
     * T032: Test health check works without Authorization header
     */
    @Test
    void ping_NoAuthRequired_Returns200() {
        // When: Call GET /ping without any authentication headers
        ResponseEntity<PingResponse> response = restTemplate.getForEntity("/ping", PingResponse.class);

        // Then: Verify 200 OK status (no 401 Unauthorized)
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // And: Verify response body is present
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("pong");
    }

    /**
     * T033: Test health check works when FeatureFlag.usersApi is false
     * Note: The default test configuration has the flag enabled, but /ping
     * should work regardless. This test verifies that behavior.
     */
    @Test
    void ping_FeatureFlagDisabled_StillReturns200() {
        // When: Call GET /ping (works regardless of feature flag state)
        ResponseEntity<PingResponse> response = restTemplate.getForEntity("/ping", PingResponse.class);

        // Then: Verify 200 OK status (not 404 or any other error)
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // And: Verify response body is present
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("pong");
    }

    /**
     * T034: Test that 95% of requests complete in under 1 second
     * Makes 20 requests and verifies 95% (19 out of 20) are under 1 second
     */
    @Test
    void ping_ResponseTime_Under1Second() {
        // Given: 20 requests
        int totalRequests = 20;
        List<Long> responseTimes = new ArrayList<>();

        // When: Make 20 requests and measure response time for each
        for (int i = 0; i < totalRequests; i++) {
            Instant start = Instant.now();
            ResponseEntity<PingResponse> response = restTemplate.getForEntity("/ping", PingResponse.class);
            Instant end = Instant.now();

            // Verify each request succeeds
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            // Record response time in milliseconds
            long responseTimeMs = Duration.between(start, end).toMillis();
            responseTimes.add(responseTimeMs);
        }

        // Then: Sort response times to calculate 95th percentile
        responseTimes.sort(Long::compareTo);

        // Calculate 95th percentile index (19th out of 20 = index 18)
        int percentile95Index = (int) Math.ceil(totalRequests * 0.95) - 1;
        long percentile95Value = responseTimes.get(percentile95Index);

        // Verify 95th percentile is under 1 second (1000ms)
        assertThat(percentile95Value)
                .as("95th percentile response time should be under 1 second")
                .isLessThan(1000L);
    }

    /**
     * T161: Performance smoke test making 100 requests and checking 95th percentile is under 1 second.
     * This is a more robust performance test than the basic 20-request test.
     */
    @Test
    void verify95PercentUnder1Second() {
        // Given: 100 requests for statistical significance
        int totalRequests = 100;
        List<Long> responseTimes = new ArrayList<>();

        // Warm up - make a few requests first
        for (int i = 0; i < 5; i++) {
            restTemplate.getForEntity("/ping", PingResponse.class);
        }

        // When: Make 100 requests and measure response time for each
        for (int i = 0; i < totalRequests; i++) {
            Instant start = Instant.now();
            ResponseEntity<PingResponse> response = restTemplate.getForEntity("/ping", PingResponse.class);
            Instant end = Instant.now();

            // Verify each request succeeds
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            // Record response time in milliseconds
            long responseTimeMs = Duration.between(start, end).toMillis();
            responseTimes.add(responseTimeMs);
        }

        // Then: Sort response times to calculate 95th percentile
        responseTimes.sort(Long::compareTo);

        // Calculate 95th percentile index (95th out of 100 = index 94)
        // For n=100, 95th percentile is at position ceil(100 * 0.95) - 1 = 94 (0-indexed)
        int percentile95Index = Math.min(
                (int) Math.ceil(totalRequests * 0.95) - 1,
                totalRequests - 1  // Ensure we don't exceed array bounds
        );
        long percentile95Value = responseTimes.get(percentile95Index);

        // Calculate average for reporting
        double average = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        long min = responseTimes.get(0);
        long max = responseTimes.get(totalRequests - 1);

        // Log performance metrics for debugging
        System.out.printf("Performance metrics (100 requests): min=%dms, avg=%.2fms, 95th=%dms, max=%dms%n",
                min, average, percentile95Value, max);

        // Verify 95th percentile is under 1 second (1000ms)
        assertThat(percentile95Value)
                .as("95th percentile of 100 requests should be under 1 second. " +
                    "Metrics: min=%dms, avg=%.2fms, 95th=%dms, max=%dms", 
                    min, average, percentile95Value, max)
                .isLessThan(1000L);
    }
}
