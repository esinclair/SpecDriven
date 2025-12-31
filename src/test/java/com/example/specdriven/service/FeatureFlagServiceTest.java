package com.example.specdriven.service;

import com.example.specdriven.config.FeatureFlagConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for FeatureFlagService.
 * Verifies feature flag checking logic and request path gating behavior.
 */
@ExtendWith(MockitoExtension.class)
class FeatureFlagServiceTest {

    @Mock
    private FeatureFlagConfig featureFlagConfig;

    private FeatureFlagService featureFlagService;

    @BeforeEach
    void setUp() {
        featureFlagService = new FeatureFlagService(featureFlagConfig);
    }

    @Test
    void isUsersApiEnabled_ReturnsTrue_WhenFlagIsEnabled() {
        when(featureFlagConfig.isUsersApi()).thenReturn(true);
        
        assertTrue(featureFlagService.isUsersApiEnabled());
    }

    @Test
    void isUsersApiEnabled_ReturnsFalse_WhenFlagIsDisabled() {
        when(featureFlagConfig.isUsersApi()).thenReturn(false);
        
        assertFalse(featureFlagService.isUsersApiEnabled());
    }

    @Test
    void requiresFeatureFlag_ReturnsFalse_ForPingEndpoint() {
        assertFalse(featureFlagService.requiresFeatureFlag("/ping"));
    }

    @Test
    void requiresFeatureFlag_ReturnsTrue_ForUsersEndpoint() {
        assertTrue(featureFlagService.requiresFeatureFlag("/users"));
    }

    @Test
    void requiresFeatureFlag_ReturnsTrue_ForUsersSubPaths() {
        assertTrue(featureFlagService.requiresFeatureFlag("/users/123"));
        assertTrue(featureFlagService.requiresFeatureFlag("/users/123/roles"));
    }

    @Test
    void requiresFeatureFlag_ReturnsTrue_ForLoginEndpoint() {
        assertTrue(featureFlagService.requiresFeatureFlag("/login"));
    }

    @Test
    void requiresFeatureFlag_ReturnsFalse_ForOtherEndpoints() {
        assertFalse(featureFlagService.requiresFeatureFlag("/health"));
        assertFalse(featureFlagService.requiresFeatureFlag("/api/something"));
    }

    @Test
    void requiresFeatureFlag_ReturnsFalse_ForNullPath() {
        assertFalse(featureFlagService.requiresFeatureFlag(null));
    }

    @Test
    void shouldBlockRequest_ReturnsFalse_ForPingEndpoint() {
        // No feature flag configuration needed for ping endpoint
        assertFalse(featureFlagService.shouldBlockRequest("/ping"));
    }

    @Test
    void shouldBlockRequest_ReturnsTrue_ForUsersEndpoint_WhenFlagDisabled() {
        when(featureFlagConfig.isUsersApi()).thenReturn(false);
        
        assertTrue(featureFlagService.shouldBlockRequest("/users"));
    }

    @Test
    void shouldBlockRequest_ReturnsFalse_ForUsersEndpoint_WhenFlagEnabled() {
        when(featureFlagConfig.isUsersApi()).thenReturn(true);
        
        assertFalse(featureFlagService.shouldBlockRequest("/users"));
    }

    @Test
    void shouldBlockRequest_ReturnsTrue_ForLoginEndpoint_WhenFlagDisabled() {
        when(featureFlagConfig.isUsersApi()).thenReturn(false);
        
        assertTrue(featureFlagService.shouldBlockRequest("/login"));
    }

    @Test
    void shouldBlockRequest_ReturnsFalse_ForLoginEndpoint_WhenFlagEnabled() {
        when(featureFlagConfig.isUsersApi()).thenReturn(true);
        
        assertFalse(featureFlagService.shouldBlockRequest("/login"));
    }

    @Test
    void shouldBlockRequest_ReturnsFalse_ForOtherEndpoints() {
        assertFalse(featureFlagService.shouldBlockRequest("/health"));
        assertFalse(featureFlagService.shouldBlockRequest("/api/something"));
    }

    @Test
    void shouldBlockRequest_ReturnsFalse_ForNullPath() {
        assertFalse(featureFlagService.shouldBlockRequest(null));
    }
}
