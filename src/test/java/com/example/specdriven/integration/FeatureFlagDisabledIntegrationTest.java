package com.example.specdriven.integration;

import com.example.specdriven.api.model.*;
import com.example.specdriven.domain.UserEntity;
import com.example.specdriven.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Feature Flag functionality (User Story 9).
 * Tests that endpoints are properly gated by the FeatureFlag.usersApi flag.
 * 
 * This test class runs with feature flag DISABLED to verify endpoints return 404.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "feature-flag.users-api=false"
})
@Transactional
class FeatureFlagDisabledIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // T148: usersApi_FeatureFlagDisabled_Returns404
    @Test
    void usersApi_FeatureFlagDisabled_Returns404() throws Exception {
        // Test /users endpoint
        mockMvc.perform(get("/users")
                .param("page", "1")
                .param("pageSize", "10"))
                .andExpect(status().isNotFound());

        // Test /users/{id} endpoint
        mockMvc.perform(get("/users/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());

        // Test POST /users endpoint
        CreateUserRequest createRequest = new CreateUserRequest(
                "testuser", "Test User", "Password123!", "test@example.com");
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isNotFound());

        // Test /login endpoint
        LoginRequest loginRequest = new LoginRequest("user", "password");
        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isNotFound());
    }

    // T150: ping_FeatureFlagDisabled_StillWorks
    @Test
    void ping_FeatureFlagDisabled_StillWorks() throws Exception {
        // /ping should always work regardless of feature flag
        mockMvc.perform(get("/ping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("pong"));
    }

    // T151: featureFlagDisabled_ErrorDoesNotRevealFeature
    @Test
    void featureFlagDisabled_ErrorDoesNotRevealFeature() throws Exception {
        MvcResult result = mockMvc.perform(get("/users")
                .param("page", "1")
                .param("pageSize", "10"))
                .andExpect(status().isNotFound())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        
        // Verify the error message doesn't reveal that the feature is disabled
        assertFalse(responseBody.toLowerCase().contains("feature"), 
                "Error should not reveal feature existence");
        assertFalse(responseBody.toLowerCase().contains("disabled"), 
                "Error should not reveal feature is disabled");
        assertFalse(responseBody.toLowerCase().contains("flag"), 
                "Error should not reveal feature flags");
    }
}
