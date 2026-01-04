package com.example.specdriven.integration;

import com.example.specdriven.api.model.*;
import com.example.specdriven.domain.UserEntity;
import com.example.specdriven.integration.support.IntegrationTestHelper;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Feature Flag functionality (User Story 9).
 * Tests that endpoints work normally when the FeatureFlag.usersApi flag is enabled.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "feature-flag.users-api=true"
})
@Transactional
class FeatureFlagEnabledIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private IntegrationTestHelper testHelper;

    private static final String AUTH_USERNAME = "flagauth";
    private static final String AUTH_PASSWORD = "AuthPassword123!";
    private static final String AUTH_EMAIL = "flagauth@example.com";

    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        authToken = testHelper.createAdminUserAndGetToken(mockMvc, AUTH_USERNAME, AUTH_PASSWORD, AUTH_EMAIL);
    }

    // T149: usersApi_FeatureFlagEnabled_ProcessesRequests
    @Test
    void usersApi_FeatureFlagEnabled_ProcessesRequests() throws Exception {
        // Test /users endpoint (requires auth)
        mockMvc.perform(get("/users")
                .param("page", "1")
                .param("pageSize", "10")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk());

        // Test POST /users endpoint (requires auth)
        CreateUserRequest createRequest = new CreateUserRequest(
                "flaguser", "Flag User", "Password123!", "flaguser@example.com");
        MvcResult createResult = mockMvc.perform(post("/users")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        User createdUser = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), User.class);

        // Test /users/{id} endpoint (requires auth)
        mockMvc.perform(get("/users/" + createdUser.getId())
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk());
    }

    // T150 (duplicate for enabled): ping still works with feature flag enabled
    @Test
    void ping_FeatureFlagEnabled_StillWorks() throws Exception {
        mockMvc.perform(get("/ping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("pong"));
    }

    // Test login works when feature flag is enabled
    @Test
    void login_FeatureFlagEnabled_Works() throws Exception {
        // Create a user for login
        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());
        user.setUsername("logintest");
        user.setName("Login Test");
        user.setEmailAddress("logintest@example.com");
        user.setPasswordHash(passwordEncoder.encode("Password123!"));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        LoginRequest loginRequest = new LoginRequest("logintest", "Password123!");
        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }
}
