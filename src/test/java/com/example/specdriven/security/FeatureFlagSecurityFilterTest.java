package com.example.specdriven.security;

import com.example.specdriven.config.FeatureFlagConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FeatureFlagSecurityFilter.
 * Tests the feature flag gating logic for endpoints.
 */
@ExtendWith(MockitoExtension.class)
class FeatureFlagSecurityFilterTest {

    @Mock
    private FeatureFlagConfig featureFlagConfig;

    @Mock
    private FilterChain filterChain;

    private FeatureFlagSecurityFilter filter;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        filter = new FeatureFlagSecurityFilter(featureFlagConfig, objectMapper);
    }

    // Test: /ping always passes through regardless of feature flag
    @Test
    void doFilterInternal_PingEndpoint_AlwaysAllowed() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/ping");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Feature flag is not checked for /ping, so no stubbing needed
        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    }

    // Test: /users returns 404 when feature flag disabled
    @Test
    void doFilterInternal_UsersEndpoint_FeatureDisabled_Returns404() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/users");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(featureFlagConfig.isUsersApi()).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain, never()).doFilter(request, response);
        assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());
    }

    // Test: /users/{id} returns 404 when feature flag disabled
    @Test
    void doFilterInternal_UserByIdEndpoint_FeatureDisabled_Returns404() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/users/123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(featureFlagConfig.isUsersApi()).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain, never()).doFilter(request, response);
        assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());
    }

    // Test: /login returns 404 when feature flag disabled
    @Test
    void doFilterInternal_LoginEndpoint_FeatureDisabled_Returns404() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/login");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(featureFlagConfig.isUsersApi()).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain, never()).doFilter(request, response);
        assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());
    }

    // Test: /users passes through when feature flag enabled
    @Test
    void doFilterInternal_UsersEndpoint_FeatureEnabled_AllowsThrough() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/users");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(featureFlagConfig.isUsersApi()).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    // Test: /login passes through when feature flag enabled
    @Test
    void doFilterInternal_LoginEndpoint_FeatureEnabled_AllowsThrough() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/login");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(featureFlagConfig.isUsersApi()).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    // Test: Non-gated endpoint passes through regardless of feature flag
    @Test
    void doFilterInternal_NonGatedEndpoint_AlwaysAllowed() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/health");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // /health is not a gated endpoint, so no stubbing needed
        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    // Test: 404 response does not reveal feature information
    @Test
    void doFilterInternal_Returns404_DoesNotRevealFeature() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/users");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(featureFlagConfig.isUsersApi()).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        String responseBody = response.getContentAsString();
        
        assertFalse(responseBody.toLowerCase().contains("feature"));
        assertFalse(responseBody.toLowerCase().contains("disabled"));
        assertFalse(responseBody.toLowerCase().contains("flag"));
        assertTrue(responseBody.contains("RESOURCE_NOT_FOUND"));
    }

    // Test: /users/roles path returns 404 when feature flag disabled
    @Test
    void doFilterInternal_UserRolesEndpoint_FeatureDisabled_Returns404() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/users/123/roles/ADMIN");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(featureFlagConfig.isUsersApi()).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain, never()).doFilter(request, response);
        assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());
    }
}
