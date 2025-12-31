package com.example.specdriven.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for JwtConfig.
 * Verifies JWT configuration properties are properly loaded from application.yml.
 */
@SpringBootTest
@TestPropertySource(properties = {
    "jwt.secret=test-jwt-secret-key-for-testing-minimum-256-bits-required-for-hmac-sha256-algorithm",
    "jwt.expirationMs=3600000"
})
class JwtConfigTest {

    @Autowired
    private JwtConfig jwtConfig;

    @Test
    void jwtConfig_IsConfigured() {
        assertNotNull(jwtConfig);
    }

    @Test
    void jwtSecret_IsLoaded() {
        assertNotNull(jwtConfig.getSecret());
        assertEquals("test-jwt-secret-key-for-testing-minimum-256-bits-required-for-hmac-sha256-algorithm", jwtConfig.getSecret());
    }

    @Test
    void jwtExpirationMs_IsLoaded() {
        assertNotNull(jwtConfig.getExpirationMs());
        assertEquals(3600000L, jwtConfig.getExpirationMs());
    }
}
