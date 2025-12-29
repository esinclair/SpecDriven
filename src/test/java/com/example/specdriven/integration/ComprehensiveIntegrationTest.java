package com.example.specdriven.integration;

import com.example.specdriven.api.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Comprehensive integration tests covering all user stories.
 * Tests are ordered to ensure proper dependencies (e.g., create before retrieve).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ComprehensiveIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private static String authToken;
    private static String firstUserId;
    
    /**
     * User Story 1: Health Check
     * Test the /ping endpoint without authentication
     */
    @Test
    @Order(1)
    public void testHealthCheckPing() throws Exception {
        mockMvc.perform(get("/ping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("pong"));
    }
    
    /**
     * User Story 3: Create First User (Bootstrap Mode)
     * Test creating the first user without authentication
     */
    @Test
    @Order(2)
    public void testCreateFirstUserWithoutAuth() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("admin");
        request.setName("Admin User");
        request.setEmailAddress("admin@example.com");
        request.setPassword("SecurePassword123!");
        
        MvcResult result = mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.name").value("Admin User"))
                .andExpect(jsonPath("$.emailAddress").value("admin@example.com"))
                .andReturn();
        
        String responseBody = result.getResponse().getContentAsString();
        User createdUser = objectMapper.readValue(responseBody, User.class);
        firstUserId = createdUser.getId().toString();
    }
    
    /**
     * User Story 2: Authentication
     * Test logging in with valid credentials
     */
    @Test
    @Order(3)
    public void testLoginWithValidCredentials() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("SecurePassword123!");
        
        MvcResult result = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();
        
        String responseBody = result.getResponse().getContentAsString();
        LoginResponse loginResponse = objectMapper.readValue(responseBody, LoginResponse.class);
        authToken = loginResponse.getToken();
    }
    
    /**
     * User Story 2: Authentication Failure
     * Test login with invalid credentials
     */
    @Test
    @Order(4)
    public void testLoginWithInvalidCredentials() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("WrongPassword");
        
        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_FAILED"));
    }
    
    /**
     * User Story 3: Retrieve User by ID
     * Test retrieving a user with authentication
     */
    @Test
    @Order(5)
    public void testGetUserById() throws Exception {
        mockMvc.perform(get("/users/" + firstUserId)
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(firstUserId))
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.emailAddress").value("admin@example.com"));
    }
    
    /**
     * User Story 3: Create User with Auth Required
     * Test that creating a user requires authentication after bootstrap
     */
    @Test
    @Order(6)
    public void testCreateUserRequiresAuth() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("testuser");
        request.setName("Test User");
        request.setEmailAddress("test@example.com");
        request.setPassword("TestPassword123!");
        
        // Without auth - should fail
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
        
        // With auth - should succeed
        mockMvc.perform(post("/users")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testuser"));
    }
    
    /**
     * User Story 3: Duplicate Email Validation
     * Test that duplicate email addresses are rejected
     */
    @Test
    @Order(7)
    public void testCreateUserWithDuplicateEmail() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("another");
        request.setName("Another User");
        request.setEmailAddress("admin@example.com"); // Duplicate
        request.setPassword("Password123!");
        
        mockMvc.perform(post("/users")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }
    
    /**
     * User Story 4: Update User
     * Test updating user information
     */
    @Test
    @Order(8)
    public void testUpdateUser() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("Updated Admin User");
        
        mockMvc.perform(put("/users/" + firstUserId)
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Admin User"));
    }
    
    /**
     * User Story 5: List Users with Pagination
     * Test listing users with pagination
     */
    @Test
    @Order(9)
    public void testListUsers() throws Exception {
        mockMvc.perform(get("/users")
                .header("Authorization", "Bearer " + authToken)
                .param("page", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.pageSize").value(10))
                .andExpect(jsonPath("$.totalItems").exists())
                .andExpect(jsonPath("$.totalPages").exists());
    }
    
    /**
     * User Story 5: Filter Users by Username
     * Test filtering users by username
     */
    @Test
    @Order(10)
    public void testFilterUsersByUsername() throws Exception {
        mockMvc.perform(get("/users")
                .header("Authorization", "Bearer " + authToken)
                .param("page", "1")
                .param("pageSize", "10")
                .param("username", "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].username").value("admin"));
    }
    
    /**
     * User Story 6: Assign Role to User
     * Test assigning a role to a user
     */
    @Test
    @Order(11)
    public void testAssignRoleToUser() throws Exception {
        mockMvc.perform(post("/users/" + firstUserId + "/roles/ADMIN")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());
        
        // Verify role was assigned
        mockMvc.perform(get("/users/" + firstUserId)
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(jsonPath("$.roles[0].roleName").value("ADMIN"));
    }
    
    /**
     * User Story 6: Remove Role from User
     * Test removing a role from a user
     */
    @Test
    @Order(12)
    public void testRemoveRoleFromUser() throws Exception {
        mockMvc.perform(delete("/users/" + firstUserId + "/roles/ADMIN")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());
    }
    
    /**
     * User Story 7: Consistent Error Format - Not Found
     * Test that 404 errors have consistent format
     */
    @Test
    @Order(13)
    public void testNotFoundErrorFormat() throws Exception {
        mockMvc.perform(get("/users/00000000-0000-0000-0000-000000000099")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").exists());
    }
    
    /**
     * User Story 7: Consistent Error Format - Validation
     * Test that validation errors have consistent format
     */
    @Test
    @Order(14)
    public void testValidationErrorFormat() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername(""); // Empty username
        request.setPassword("pass");
        
        mockMvc.perform(post("/users")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.message").exists());
    }
    
    /**
     * User Story 8: Retry Behavior - Validation Error (4xx)
     * Test that validation errors return 4xx indicating no retry
     */
    @Test
    @Order(15)
    public void testValidationErrorNoRetry() throws Exception {
        mockMvc.perform(get("/users")
                .header("Authorization", "Bearer " + authToken)
                .param("page", "-1") // Invalid page
                .param("pageSize", "10"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }
    
    /**
     * User Story 9: Feature Flag - Ping Always Available
     * Test that /ping works regardless of feature flags
     * (Feature flag is enabled in test config, but ping should work even if disabled)
     */
    @Test
    @Order(16)
    public void testPingAlwaysAvailable() throws Exception {
        mockMvc.perform(get("/ping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("pong"));
    }
    
    /**
     * User Story 4: Delete User
     * Test deleting a user (run last to avoid breaking other tests)
     */
    @Test
    @Order(100)
    public void testDeleteUser() throws Exception {
        // Create a user to delete
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("tobedeleted");
        request.setName("Delete Me");
        request.setEmailAddress("delete@example.com");
        request.setPassword("Password123!");
        
        MvcResult createResult = mockMvc.perform(post("/users")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        
        String responseBody = createResult.getResponse().getContentAsString();
        User createdUser = objectMapper.readValue(responseBody, User.class);
        String userIdToDelete = createdUser.getId().toString();
        
        // Delete the user
        mockMvc.perform(delete("/users/" + userIdToDelete)
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());
        
        // Verify user is deleted
        mockMvc.perform(get("/users/" + userIdToDelete)
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }
}
