package com.example.specdriven.security;

import com.example.specdriven.config.JwtConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtTokenProvider.
 * Tests JWT token generation, validation, and extraction.
 */
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private JwtConfig jwtConfig;

    @BeforeEach
    void setUp() {
        jwtConfig = new JwtConfig();
        jwtConfig.setSecret("test-secret-for-unit-tests-that-is-long-enough-for-hs256");
        jwtConfig.setExpirationMs(86400000L); // 24 hours
        jwtTokenProvider = new JwtTokenProvider(jwtConfig);
    }

    @Test
    void generateToken_ValidUserId_ReturnsToken() {
        UUID userId = UUID.randomUUID();

        String token = jwtTokenProvider.generateToken(userId);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        // JWT format: header.payload.signature
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length);
    }

    @Test
    void validateToken_ValidToken_ReturnsTrue() {
        UUID userId = UUID.randomUUID();
        String token = jwtTokenProvider.generateToken(userId);

        boolean isValid = jwtTokenProvider.validateToken(token);

        assertTrue(isValid);
    }

    @Test
    void validateToken_InvalidToken_ReturnsFalse() {
        String invalidToken = "not-a-valid-jwt-token";

        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        assertFalse(isValid);
    }

    @Test
    void validateToken_TamperedToken_ReturnsFalse() {
        UUID userId = UUID.randomUUID();
        String token = jwtTokenProvider.generateToken(userId);
        // Tamper with the signature
        String tamperedToken = token.substring(0, token.length() - 5) + "xxxxx";

        boolean isValid = jwtTokenProvider.validateToken(tamperedToken);

        assertFalse(isValid);
    }

    @Test
    void validateToken_NullToken_ReturnsFalse() {
        boolean isValid = jwtTokenProvider.validateToken(null);

        assertFalse(isValid);
    }

    @Test
    void validateToken_EmptyToken_ReturnsFalse() {
        boolean isValid = jwtTokenProvider.validateToken("");

        assertFalse(isValid);
    }

    @Test
    void getUserIdFromToken_ValidToken_ReturnsUserId() {
        UUID userId = UUID.randomUUID();
        String token = jwtTokenProvider.generateToken(userId);

        UUID extractedUserId = jwtTokenProvider.getUserIdFromToken(token);

        assertEquals(userId, extractedUserId);
    }

    @Test
    void isTokenExpired_ValidToken_ReturnsFalse() {
        UUID userId = UUID.randomUUID();
        String token = jwtTokenProvider.generateToken(userId);

        boolean isExpired = jwtTokenProvider.isTokenExpired(token);

        assertFalse(isExpired);
    }

    @Test
    void isTokenExpired_InvalidToken_ReturnsTrue() {
        boolean isExpired = jwtTokenProvider.isTokenExpired("invalid-token");

        assertTrue(isExpired);
    }

    @Test
    void generateToken_DifferentUserIds_ProduceDifferentTokens() {
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();

        String token1 = jwtTokenProvider.generateToken(userId1);
        String token2 = jwtTokenProvider.generateToken(userId2);

        assertNotEquals(token1, token2);
    }

    @Test
    void generateToken_SameUserId_ExtractsSameUserId() {
        UUID userId = UUID.randomUUID();
        String token1 = jwtTokenProvider.generateToken(userId);
        String token2 = jwtTokenProvider.generateToken(userId);

        UUID extracted1 = jwtTokenProvider.getUserIdFromToken(token1);
        UUID extracted2 = jwtTokenProvider.getUserIdFromToken(token2);

        assertEquals(userId, extracted1);
        assertEquals(userId, extracted2);
    }
}
