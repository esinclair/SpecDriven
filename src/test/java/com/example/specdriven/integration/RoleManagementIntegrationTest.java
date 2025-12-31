package com.example.specdriven.integration;

import com.example.specdriven.api.model.*;
import com.example.specdriven.domain.RoleEntity;
import com.example.specdriven.domain.UserEntity;
import com.example.specdriven.domain.UserRoleEntity;
import com.example.specdriven.repository.RoleRepository;
import com.example.specdriven.repository.UserRepository;
import com.example.specdriven.repository.UserRoleRepository;
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
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Role Management functionality (User Story 6).
 * Tests PUT /users/{id}/roles/{roleName} and DELETE /users/{id}/roles/{roleName}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RoleManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String AUTH_USERNAME = "roleauth";
    private static final String AUTH_PASSWORD = "AuthPassword123!";
    private static final String AUTH_EMAIL = "roleauth@example.com";

    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        authToken = createAuthUserAndGetToken();
    }

    private String createAuthUserAndGetToken() throws Exception {
        UserEntity authUser = new UserEntity();
        authUser.setId(UUID.randomUUID());
        authUser.setUsername(AUTH_USERNAME);
        authUser.setName("Role Auth User");
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

    private UserEntity createTestUser(String username, String name, String email) {
        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());
        user.setUsername(username);
        user.setName(name);
        user.setEmailAddress(email);
        user.setPasswordHash(passwordEncoder.encode("Password123!"));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    // T130: assignRole_ValidRole_Returns204
    @Test
    void assignRole_ValidRole_Returns204() throws Exception {
        UserEntity user = createTestUser("roleuser1", "Role User 1", "role1@example.com");

        mockMvc.perform(put("/users/" + user.getId() + "/roles/ADMIN")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());
    }

    // T131: assignRole_VerifyAssigned_RoleAppearsInUser
    @Test
    void assignRole_VerifyAssigned_RoleAppearsInUser() throws Exception {
        UserEntity user = createTestUser("roleuser2", "Role User 2", "role2@example.com");

        // Assign role
        mockMvc.perform(put("/users/" + user.getId() + "/roles/USER")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());

        // Verify role appears in user
        mockMvc.perform(get("/users/" + user.getId())
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(jsonPath("$.roles[?(@.roleName == 'USER')]").exists());
    }

    // T132: assignRole_AlreadyAssigned_IdempotentReturns204
    @Test
    void assignRole_AlreadyAssigned_IdempotentReturns204() throws Exception {
        UserEntity user = createTestUser("roleuser3", "Role User 3", "role3@example.com");

        // Assign role first time
        mockMvc.perform(put("/users/" + user.getId() + "/roles/ADMIN")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());

        // Assign same role second time (should be idempotent)
        mockMvc.perform(put("/users/" + user.getId() + "/roles/ADMIN")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());

        // Verify role appears only once
        MvcResult result = mockMvc.perform(get("/users/" + user.getId())
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn();

        User userResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(), User.class);
        
        long adminRoleCount = userResponse.getRoles().stream()
                .filter(r -> "ADMIN".equals(r.getRoleName().getValue()))
                .count();
        assertEquals(1, adminRoleCount, "User should have exactly one ADMIN role");
    }

    // T133: removeRole_ValidRole_Returns204
    @Test
    void removeRole_ValidRole_Returns204() throws Exception {
        UserEntity user = createTestUser("roleuser4", "Role User 4", "role4@example.com");

        // First assign a role
        mockMvc.perform(put("/users/" + user.getId() + "/roles/USER")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());

        // Then remove it
        mockMvc.perform(delete("/users/" + user.getId() + "/roles/USER")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());
    }

    // T134: removeRole_VerifyRemoved_RoleNotInUser
    @Test
    void removeRole_VerifyRemoved_RoleNotInUser() throws Exception {
        UserEntity user = createTestUser("roleuser5", "Role User 5", "role5@example.com");

        // Assign role
        mockMvc.perform(put("/users/" + user.getId() + "/roles/ADMIN")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());

        // Verify role is assigned
        mockMvc.perform(get("/users/" + user.getId())
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles[?(@.roleName == 'ADMIN')]").exists());

        // Remove role
        mockMvc.perform(delete("/users/" + user.getId() + "/roles/ADMIN")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());

        // Verify role is removed
        mockMvc.perform(get("/users/" + user.getId())
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles[?(@.roleName == 'ADMIN')]").doesNotExist());
    }

    // T135: removeRole_NotAssigned_IdempotentReturns204
    @Test
    void removeRole_NotAssigned_IdempotentReturns204() throws Exception {
        UserEntity user = createTestUser("roleuser6", "Role User 6", "role6@example.com");

        // Remove role that was never assigned (should be idempotent)
        mockMvc.perform(delete("/users/" + user.getId() + "/roles/ADMIN")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());
    }

    // T136: assignRole_UserNotFound_Returns404
    @Test
    void assignRole_UserNotFound_Returns404() throws Exception {
        UUID nonExistentUserId = UUID.randomUUID();

        mockMvc.perform(put("/users/" + nonExistentUserId + "/roles/ADMIN")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    // T137: assignRole_InvalidRoleName_Returns400
    @Test
    void assignRole_InvalidRoleName_Returns400() throws Exception {
        UserEntity user = createTestUser("roleuser7", "Role User 7", "role7@example.com");

        // Try to assign an invalid role name (not in the RoleName enum)
        mockMvc.perform(put("/users/" + user.getId() + "/roles/INVALID_ROLE")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isBadRequest());
    }

    // T138: assignRole_NoAuth_Returns401
    @Test
    void assignRole_NoAuth_Returns401() throws Exception {
        UserEntity user = createTestUser("roleuser8", "Role User 8", "role8@example.com");

        mockMvc.perform(put("/users/" + user.getId() + "/roles/ADMIN"))
                .andExpect(status().isUnauthorized());
    }

    // T139: removeRole_NoAuth_Returns401
    @Test
    void removeRole_NoAuth_Returns401() throws Exception {
        UserEntity user = createTestUser("roleuser9", "Role User 9", "role9@example.com");

        mockMvc.perform(delete("/users/" + user.getId() + "/roles/ADMIN"))
                .andExpect(status().isUnauthorized());
    }

    // Additional test: Multiple roles can be assigned to a user
    @Test
    void multipleRoles_CanBeAssignedToUser() throws Exception {
        UserEntity user = createTestUser("roleuser10", "Role User 10", "role10@example.com");

        // Assign multiple roles
        mockMvc.perform(put("/users/" + user.getId() + "/roles/ADMIN")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(put("/users/" + user.getId() + "/roles/USER")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());

        // Verify both roles appear
        mockMvc.perform(get("/users/" + user.getId())
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(jsonPath("$.roles.length()").value(2));
    }

    // Additional test: Remove role from non-existent user returns 404
    @Test
    void removeRole_UserNotFound_Returns404() throws Exception {
        UUID nonExistentUserId = UUID.randomUUID();

        mockMvc.perform(delete("/users/" + nonExistentUserId + "/roles/ADMIN")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }
}
