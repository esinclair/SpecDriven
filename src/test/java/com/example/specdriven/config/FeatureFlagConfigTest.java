package com.example.specdriven.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FeatureFlagConfig.
 * Verifies default values and getter/setter behavior for feature flags.
 */
class FeatureFlagConfigTest {

    @Test
    void usersApi_DefaultValue_IsFalse() {
        FeatureFlagConfig config = new FeatureFlagConfig();
        
        assertFalse(config.isUsersApi(), "usersApi should default to false (disabled)");
    }

    @Test
    void setUsersApi_UpdatesValue() {
        FeatureFlagConfig config = new FeatureFlagConfig();
        
        config.setUsersApi(true);
        assertTrue(config.isUsersApi());
        
        config.setUsersApi(false);
        assertFalse(config.isUsersApi());
    }
}
