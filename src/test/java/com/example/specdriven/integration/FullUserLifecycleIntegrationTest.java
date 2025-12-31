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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for complete user lifecycle (Phase 11).
 * Tests the full flow: create → login → get token → assign role → update → list → delete → verify.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class FullUserLifecycleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String AUTH_USERNAME = "lifecycleauth";
    private static final String AUTH_PASSWORD = "AuthPassword123!";
    private static final String AUTH_EMAIL = "lifecycleauth@example.com";

    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        authToken = createAuthUserAndGetToken();
    }

    private String createAuthUserAndGetToken() throws Exception {
        UserEntity authUser = new UserEntity();
        authUser.setId(UUID.randomUUID());
        authUser.setUsername(AUTH_USERNAME);
        authUser.setName("Lifecycle Auth User");
        authUser.setEmailAddress(AUTH_EMAIL);
        authUser.setPasswordHash(passwordEncoder.encode(AUTH_PASSWORD));
        authUser.setCreatedAt(LocalDateTime.now());
        authUser.setUpdatedAt(LocalDateTime.now());
        userRepository.save(authUser);

        LoginRequest loginRequest = new LoginRequest(AUTH_USERNAME, AUTH_PASSWORD);
        MvcResult result = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        LoginResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), LoginResponse.class);
        return response.getToken();
    }

    /**
     * T159: Test complete user lifecycle flow:
     * create user → login → get token → assign role → update user → list users → delete user → verify deleted
     */
    @Test
    void fullUserLifecycle_CompleteFlow() throws Exception {
        // Step 1: Create a new user
        String newUsername = "lifecycleuser";
        String newPassword = "LifecyclePass123!";
        String newEmail = "lifecycle@example.com";
        
        CreateUserRequest createRequest = new CreateUserRequest(
                newUsername, "Lifecycle Test User", newPassword, newEmail);

        MvcResult createResult = mockMvc.perform(post("/users")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.username").value(newUsername))
                .andExpect(jsonPath("$.emailAddress").value(newEmail))
                .andReturn();

        User createdUser = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), User.class);
        UUID userId = createdUser.getId();
        assertNotNull(userId, "User ID should not be null");

        // Step 2: Login with the new user and get token
        LoginRequest newUserLogin = new LoginRequest(newUsername, newPassword);
        MvcResult loginResult = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUserLogin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        LoginResponse loginResponse = objectMapper.readValue(
                loginResult.getResponse().getContentAsString(), LoginResponse.class);
        String newUserToken = loginResponse.getToken();
        assertNotNull(newUserToken, "Token should not be null");

        // Step 3: Get user by ID (verify retrieval works)
        mockMvc.perform(get("/users/" + userId)
                .header("Authorization", "Bearer " + newUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value(newUsername));

        // Step 4: Assign role to user
        mockMvc.perform(put("/users/" + userId + "/roles/USER")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());

        // Verify role was assigned
        mockMvc.perform(get("/users/" + userId)
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles[?(@.roleName == 'USER')]").exists());

        // Step 5: Update user information
        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setName("Updated Lifecycle User");

        mockMvc.perform(put("/users/" + userId)
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Lifecycle User"));

        // Step 6: List users and verify the user appears
        mockMvc.perform(get("/users")
                .param("page", "1")
                .param("pageSize", "100")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[?(@.id == '" + userId + "')]").exists());

        // Step 7: Delete the user
        mockMvc.perform(delete("/users/" + userId)
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());

        // Step 8: Verify user is deleted (GET returns 404)
        mockMvc.perform(get("/users/" + userId)
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    /**
     * T160: Test that multiple users can perform independent operations without interference.
     * Note: This test doesn't use @Transactional concurrent threads because transactions
     * don't propagate across threads. Instead, it performs sequential independent operations.
     */
    @Test
    void multipleUsers_IndependentOperations() throws Exception {
        // Create multiple users
        List<UUID> userIds = new ArrayList<>();
        
        for (int i = 0; i < 3; i++) {
            CreateUserRequest createRequest = new CreateUserRequest(
                    "multiuser" + i, "Multi User " + i, "Password123!", 
                    "multiuser" + i + "@example.com");

            MvcResult createResult = mockMvc.perform(post("/users")
                    .header("Authorization", "Bearer " + authToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated())
                    .andReturn();

            User createdUser = objectMapper.readValue(
                    createResult.getResponse().getContentAsString(), User.class);
            userIds.add(createdUser.getId());
        }

        // Perform operations on each user independently (sequential within single transaction)
        for (int i = 0; i < userIds.size(); i++) {
            final int index = i;
            final UUID userId = userIds.get(index);
            
            // Update user
            UpdateUserRequest updateRequest = new UpdateUserRequest();
            updateRequest.setName("Updated Multi User " + index);

            mockMvc.perform(put("/users/" + userId)
                    .header("Authorization", "Bearer " + authToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk());

            // Assign role
            mockMvc.perform(put("/users/" + userId + "/roles/USER")
                    .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isNoContent());

            // Verify user state
            mockMvc.perform(get("/users/" + userId)
                    .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Updated Multi User " + index))
                    .andExpect(jsonPath("$.roles[?(@.roleName == 'USER')]").exists());
        }

        // Verify each user maintains independent state (not affected by other users)
        for (int i = 0; i < userIds.size(); i++) {
            UUID userId = userIds.get(i);
            mockMvc.perform(get("/users/" + userId)
                    .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Updated Multi User " + i))
                    .andExpect(jsonPath("$.emailAddress").value("multiuser" + i + "@example.com"));
        }
        
        // Test that modifying one user doesn't affect others
        UpdateUserRequest updateFirst = new UpdateUserRequest();
        updateFirst.setName("Special First User");
        mockMvc.perform(put("/users/" + userIds.get(0))
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateFirst)))
                .andExpect(status().isOk());
        
        // Verify other users are not affected
        mockMvc.perform(get("/users/" + userIds.get(1))
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Multi User 1"));
        
        mockMvc.perform(get("/users/" + userIds.get(2))
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Multi User 2"));
    }
}
