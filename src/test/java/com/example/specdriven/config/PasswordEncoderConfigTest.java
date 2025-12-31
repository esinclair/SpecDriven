package com.example.specdriven.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for PasswordEncoderConfig.
 * Verifies that BCryptPasswordEncoder bean is properly configured.
 */
@SpringBootTest
class PasswordEncoderConfigTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void passwordEncoder_IsConfigured() {
        assertNotNull(passwordEncoder);
    }

    @Test
    void passwordEncoder_CanEncodePasswords() {
        String rawPassword = "testPassword123";
        
        String encoded = passwordEncoder.encode(rawPassword);
        
        assertNotNull(encoded);
        assertNotEquals(rawPassword, encoded);
        assertTrue(encoded.startsWith("$2a$") || encoded.startsWith("$2b$")); // BCrypt format
    }

    @Test
    void passwordEncoder_CanVerifyPasswords() {
        String rawPassword = "testPassword123";
        String encoded = passwordEncoder.encode(rawPassword);
        
        boolean matches = passwordEncoder.matches(rawPassword, encoded);
        
        assertTrue(matches);
    }

    @Test
    void passwordEncoder_RejectsMismatchedPasswords() {
        String rawPassword = "testPassword123";
        String encoded = passwordEncoder.encode(rawPassword);
        
        boolean matches = passwordEncoder.matches("wrongPassword", encoded);
        
        assertFalse(matches);
    }

    @Test
    void passwordEncoder_GeneratesDifferentHashesForSamePassword() {
        String rawPassword = "testPassword123";
        
        String encoded1 = passwordEncoder.encode(rawPassword);
        String encoded2 = passwordEncoder.encode(rawPassword);
        
        assertNotEquals(encoded1, encoded2); // Different salts
        assertTrue(passwordEncoder.matches(rawPassword, encoded1));
        assertTrue(passwordEncoder.matches(rawPassword, encoded2));
    }
}
