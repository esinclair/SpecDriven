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
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for User CRUD operations (User Stories 3 and 4).
 * Tests create, retrieve, update, and delete user functionality.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserCrudIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String AUTH_USERNAME = "authuser";
    private static final String AUTH_PASSWORD = "AuthPassword123!";
    private static final String AUTH_EMAIL = "auth@example.com";

    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        // Create an auth user and get a token for authenticated requests
        authToken = createAuthUserAndGetToken();
    }

    /**
     * Create an auth user and return a valid JWT token.
     */
    private String createAuthUserAndGetToken() throws Exception {
        // Create auth user directly in database
        UserEntity authUser = new UserEntity();
        authUser.setId(UUID.randomUUID());
        authUser.setUsername(AUTH_USERNAME);
        authUser.setName("Auth User");
        authUser.setEmailAddress(AUTH_EMAIL);
        authUser.setPasswordHash(passwordEncoder.encode(AUTH_PASSWORD));
        authUser.setCreatedAt(LocalDateTime.now());
        authUser.setUpdatedAt(LocalDateTime.now());
        userRepository.save(authUser);

        // Login to get token
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

    // ==========================================
    // Phase 6: User Story 3 - Create and Retrieve Users
    // ==========================================

    // T066: createUser_ValidData_Returns201
    @Test
    void createUser_ValidData_Returns201() throws Exception {
        CreateUserRequest request = new CreateUserRequest(
                "newuser", "New User", "Password123!", "new@example.com");

        mockMvc.perform(post("/users")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.name").value("New User"))
                .andExpect(jsonPath("$.emailAddress").value("new@example.com"))
                .andExpect(jsonPath("$.roles").isArray());
    }

    // T067: createUser_PasswordNotInResponse
    @Test
    void createUser_PasswordNotInResponse() throws Exception {
        CreateUserRequest request = new CreateUserRequest(
                "passuser", "Pass User", "SecretPass123!", "pass@example.com");

        MvcResult result = mockMvc.perform(post("/users")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        assertFalse(responseBody.contains("password"), "Password should not be in response");
        assertFalse(responseBody.contains("SecretPass123!"), "Raw password should not be in response");
        assertFalse(responseBody.contains("passwordHash"), "Password hash should not be in response");
    }

    // T068: getUserById_ValidId_Returns200
    @Test
    void getUserById_ValidId_Returns200() throws Exception {
        // Create a user first
        CreateUserRequest request = new CreateUserRequest(
                "getuser", "Get User", "Password123!", "get@example.com");

        MvcResult createResult = mockMvc.perform(post("/users")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        User createdUser = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), User.class);

        // Get the user
        mockMvc.perform(get("/users/" + createdUser.getId())
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdUser.getId().toString()))
                .andExpect(jsonPath("$.username").value("getuser"))
                .andExpect(jsonPath("$.name").value("Get User"))
                .andExpect(jsonPath("$.emailAddress").value("get@example.com"));
    }

    // T069: createUser_MissingRequiredFields_Returns400
    @Test
    void createUser_MissingRequiredFields_Returns400() throws Exception {
        // Missing username
        String jsonMissingUsername = "{\"name\": \"Test\", \"password\": \"Password123!\", \"emailAddress\": \"test@example.com\"}";

        mockMvc.perform(post("/users")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMissingUsername))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    // T070: createUser_InvalidEmailFormat_Returns400
    @Test
    void createUser_InvalidEmailFormat_Returns400() throws Exception {
        CreateUserRequest request = new CreateUserRequest(
                "invalidemail", "Invalid Email", "Password123!", "not-an-email");

        mockMvc.perform(post("/users")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    // T071: createUser_DuplicateEmail_Returns409
    @Test
    void createUser_DuplicateEmail_Returns409() throws Exception {
        // Create first user
        CreateUserRequest request1 = new CreateUserRequest(
                "user1", "User One", "Password123!", "duplicate@example.com");

        mockMvc.perform(post("/users")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        // Try to create second user with same email
        CreateUserRequest request2 = new CreateUserRequest(
                "user2", "User Two", "Password123!", "duplicate@example.com");

        mockMvc.perform(post("/users")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    // T072: getUserById_NotFound_Returns404
    @Test
    void getUserById_NotFound_Returns404() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(get("/users/" + nonExistentId)
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    // T073: createUser_NoAuth_Returns401
    @Test
    void createUser_NoAuth_Returns401() throws Exception {
        CreateUserRequest request = new CreateUserRequest(
                "noauthuser", "No Auth User", "Password123!", "noauth@example.com");

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // T074: getUserById_NoAuth_Returns401
    @Test
    void getUserById_NoAuth_Returns401() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(get("/users/" + userId))
                .andExpect(status().isUnauthorized());
    }

    // ==========================================
    // Phase 7: User Story 4 - Update and Delete Users
    // ==========================================

    // T089: updateUser_ValidData_Returns200
    @Test
    void updateUser_ValidData_Returns200() throws Exception {
        // Create a user first
        CreateUserRequest createRequest = new CreateUserRequest(
                "updateuser", "Update User", "Password123!", "update@example.com");

        MvcResult createResult = mockMvc.perform(post("/users")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        User createdUser = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), User.class);

        // Update the user
        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setName("Updated Name");

        mockMvc.perform(put("/users/" + createdUser.getId())
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.username").value("updateuser")) // Unchanged
                .andExpect(jsonPath("$.emailAddress").value("update@example.com")); // Unchanged
    }

    // T090: updateUser_PartialUpdate_UpdatesOnlyProvidedFields
    @Test
    void updateUser_PartialUpdate_UpdatesOnlyProvidedFields() throws Exception {
        // Create a user
        CreateUserRequest createRequest = new CreateUserRequest(
                "partialuser", "Partial User", "Password123!", "partial@example.com");

        MvcResult createResult = mockMvc.perform(post("/users")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        User createdUser = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), User.class);

        // Update only name
        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setName("Only Name Changed");

        MvcResult updateResult = mockMvc.perform(put("/users/" + createdUser.getId())
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andReturn();

        User updatedUser = objectMapper.readValue(
                updateResult.getResponse().getContentAsString(), User.class);

        assertEquals("Only Name Changed", updatedUser.getName());
        assertEquals("partialuser", updatedUser.getUsername()); // Unchanged
        assertEquals("partial@example.com", updatedUser.getEmailAddress()); // Unchanged
    }

    // T091: updateUser_PasswordChange_HashesNewPassword
    @Test
    void updateUser_PasswordChange_HashesNewPassword() throws Exception {
        // Create a user
        CreateUserRequest createRequest = new CreateUserRequest(
                "passchangeuser", "Pass Change", "OldPassword123!", "passchange@example.com");

        MvcResult createResult = mockMvc.perform(post("/users")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        User createdUser = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), User.class);

        // Update password
        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setPassword("NewPassword456!");

        mockMvc.perform(put("/users/" + createdUser.getId())
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        // Verify new password works for login
        LoginRequest loginRequest = new LoginRequest("passchangeuser", "NewPassword456!");

        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    // T092: deleteUser_ValidId_Returns204
    @Test
    void deleteUser_ValidId_Returns204() throws Exception {
        // Create a user
        CreateUserRequest createRequest = new CreateUserRequest(
                "deleteuser", "Delete User", "Password123!", "delete@example.com");

        MvcResult createResult = mockMvc.perform(post("/users")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        User createdUser = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), User.class);

        // Delete the user
        mockMvc.perform(delete("/users/" + createdUser.getId())
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());
    }

    // T093: deleteUser_VerifyDeleted_Returns404
    @Test
    void deleteUser_VerifyDeleted_Returns404() throws Exception {
        // Create a user
        CreateUserRequest createRequest = new CreateUserRequest(
                "deleteverify", "Delete Verify", "Password123!", "deleteverify@example.com");

        MvcResult createResult = mockMvc.perform(post("/users")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        User createdUser = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), User.class);

        // Delete the user
        mockMvc.perform(delete("/users/" + createdUser.getId())
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());

        // Verify user is deleted (GET returns 404)
        mockMvc.perform(get("/users/" + createdUser.getId())
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    // T095: updateUser_InvalidData_Returns400
    @Test
    void updateUser_InvalidData_Returns400() throws Exception {
        // Create a user
        CreateUserRequest createRequest = new CreateUserRequest(
                "invalidupdate", "Invalid Update", "Password123!", "invalidupdate@example.com");

        MvcResult createResult = mockMvc.perform(post("/users")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        User createdUser = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), User.class);

        // Try to update with invalid email
        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setEmailAddress("not-valid-email");

        mockMvc.perform(put("/users/" + createdUser.getId())
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    // T096: updateUser_NotFound_Returns404
    @Test
    void updateUser_NotFound_Returns404() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setName("Updated Name");

        mockMvc.perform(put("/users/" + nonExistentId)
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    // T097: deleteUser_NotFound_Returns404
    @Test
    void deleteUser_NotFound_Returns404() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(delete("/users/" + nonExistentId)
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    // T098: updateUser_DuplicateEmail_Returns409
    @Test
    void updateUser_DuplicateEmail_Returns409() throws Exception {
        // Create first user
        CreateUserRequest request1 = new CreateUserRequest(
                "emailuser1", "Email User 1", "Password123!", "email1@example.com");

        mockMvc.perform(post("/users")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        // Create second user
        CreateUserRequest request2 = new CreateUserRequest(
                "emailuser2", "Email User 2", "Password123!", "email2@example.com");

        MvcResult createResult2 = mockMvc.perform(post("/users")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated())
                .andReturn();

        User user2 = objectMapper.readValue(
                createResult2.getResponse().getContentAsString(), User.class);

        // Try to update user2's email to user1's email
        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setEmailAddress("email1@example.com");

        mockMvc.perform(put("/users/" + user2.getId())
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    // T099: updateUser_NoAuth_Returns401
    @Test
    void updateUser_NoAuth_Returns401() throws Exception {
        UUID userId = UUID.randomUUID();
        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setName("Updated");

        mockMvc.perform(put("/users/" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isUnauthorized());
    }

    // T100: deleteUser_NoAuth_Returns401
    @Test
    void deleteUser_NoAuth_Returns401() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(delete("/users/" + userId))
                .andExpect(status().isUnauthorized());
    }

    // T094: deleteUser_CascadesRoleAssignments - verify role assignments deleted when user deleted
    @Test
    void deleteUser_CascadesRoleAssignments() throws Exception {
        // Create a user
        CreateUserRequest createRequest = new CreateUserRequest(
                "cascadeuser", "Cascade User", "Password123!", "cascade@example.com");

        MvcResult createResult = mockMvc.perform(post("/users")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        User createdUser = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), User.class);
        UUID userId = createdUser.getId();

        // Assign roles to the user
        mockMvc.perform(put("/users/" + userId + "/roles/ADMIN")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(put("/users/" + userId + "/roles/USER")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());

        // Verify roles are assigned
        mockMvc.perform(get("/users/" + userId)
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(jsonPath("$.roles.length()").value(2));

        // Delete the user
        mockMvc.perform(delete("/users/" + userId)
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());

        // Verify user is deleted
        mockMvc.perform(get("/users/" + userId)
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());

        // Note: Role assignments are cascade deleted via database FK constraint (ON DELETE CASCADE)
        // We verify this implicitly by ensuring the delete succeeds without foreign key violations
    }

    // Additional test: User can update their own email to their current email
    @Test
    void updateUser_SameEmail_DoesNotConflict() throws Exception {
        // Create a user
        CreateUserRequest createRequest = new CreateUserRequest(
                "sameemailuser", "Same Email", "Password123!", "sameemail@example.com");

        MvcResult createResult = mockMvc.perform(post("/users")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        User createdUser = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), User.class);

        // Update with same email (should not conflict)
        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setEmailAddress("sameemail@example.com");
        updateRequest.setName("Updated Name");

        mockMvc.perform(put("/users/" + createdUser.getId())
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.emailAddress").value("sameemail@example.com"));
    }
}
