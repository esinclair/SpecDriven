package com.example.specdriven.controller;

import com.example.specdriven.api.PingApi;
import com.example.specdriven.api.model.PingResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller implementing the health check ping endpoint.
 */
@RestController
public class PingController implements PingApi {
    
    @Override
    public ResponseEntity<PingResponse> ping() {
        PingResponse response = new PingResponse();
        response.setMessage("pong");
        return ResponseEntity.ok(response);
    }
}
