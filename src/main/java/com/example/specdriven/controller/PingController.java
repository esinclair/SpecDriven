package com.example.specdriven.controller;

import com.example.specdriven.api.PingApi;
import com.example.specdriven.api.model.PingResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller implementing the health check endpoint.
 * 
 * This endpoint provides a lightweight health check that:
 * - Requires no authentication
 * - Has no database dependencies
 * - Works regardless of feature flag state
 * - Returns immediately with no processing
 */
@RestController
public class PingController implements PingApi {

    /**
     * Health check endpoint that returns "pong".
     * 
     * This endpoint is designed to be:
     * - Fast: No database queries or external calls
     * - Public: No authentication required
     * - Always available: Not gated by feature flags
     * 
     * @return ResponseEntity with PingResponse containing message "pong"
     */
    @Override
    public ResponseEntity<PingResponse> ping() {
        PingResponse response = new PingResponse();
        response.setMessage("pong");
        return ResponseEntity.ok(response);
    }
}
