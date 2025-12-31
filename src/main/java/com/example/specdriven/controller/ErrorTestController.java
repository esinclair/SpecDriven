package com.example.specdriven.controller;

import com.example.specdriven.exception.AuthenticationException;
import com.example.specdriven.exception.ConflictException;
import com.example.specdriven.exception.ResourceNotFoundException;
import com.example.specdriven.exception.ValidationException;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Test controller for triggering various error conditions.
 * Only active in test profile to verify error handling.
 */
@RestController
@RequestMapping("/test")
@Profile("test")
public class ErrorTestController {

    /**
     * Endpoint to trigger various error conditions for testing.
     *
     * @param type the type of error to trigger (validation, notfound, conflict, authentication, database)
     * @return never returns normally, always throws exception
     */
    @GetMapping("/trigger-error")
    public ResponseEntity<String> triggerError(@RequestParam String type) {
        switch (type) {
            case "validation":
                throw new ValidationException("Test validation error");
            case "notfound":
                throw new ResourceNotFoundException("Test resource not found");
            case "conflict":
                throw new ConflictException("Test conflict error");
            case "authentication":
                throw new AuthenticationException("Test authentication error");
            case "database":
                // Simulate database access exception
                throw new org.springframework.dao.DataAccessResourceFailureException("Test database error");
            default:
                return ResponseEntity.ok("Unknown error type");
        }
    }
}
