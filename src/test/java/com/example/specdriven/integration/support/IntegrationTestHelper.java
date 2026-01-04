package com.example.specdriven.integration.support;

import com.example.specdriven.api.model.LoginRequest;
import com.example.specdriven.api.model.LoginResponse;
import com.example.specdriven.domain.UserEntity;
import com.example.specdriven.domain.UserRoleEntity;
import com.example.specdriven.repository.UserRepository;
import com.example.specdriven.repository.UserRoleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Shared helper for integration tests.
 * Provides utilities for creating authenticated users with appropriate permissions.
 */
@Component
public class IntegrationTestHelper {

    /** Predefined ADMIN role ID from V002__roles_permissions.sql */
    public static final UUID ADMIN_ROLE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    /** Predefined USER role ID from V002__roles_permissions.sql */
    public static final UUID USER_ROLE_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    /** Predefined GUEST role ID from V002__roles_permissions.sql */
    public static final UUID GUEST_ROLE_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    public IntegrationTestHelper(UserRepository userRepository,
                                  UserRoleRepository userRoleRepository,
                                  PasswordEncoder passwordEncoder,
                                  ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
    }

    /**
     * Create a user with the ADMIN role and return a valid JWT token.
     * The ADMIN role grants all user management permissions (users:read, users:write, users:delete, roles:assign).
     *
     * @param mockMvc the MockMvc instance for performing login
     * @param username the username for the auth user
     * @param password the password for the auth user
     * @param email the email for the auth user
     * @return a valid JWT token for the created admin user
     * @throws Exception if user creation or login fails
     */
    public String createAdminUserAndGetToken(MockMvc mockMvc, String username, String password, String email) throws Exception {
        return createUserWithRoleAndGetToken(mockMvc, username, password, email, ADMIN_ROLE_ID);
    }

    /**
     * Create a user with a specific role and return a valid JWT token.
     *
     * @param mockMvc the MockMvc instance for performing login
     * @param username the username for the auth user
     * @param password the password for the auth user
     * @param email the email for the auth user
     * @param roleId the role ID to assign (use constants like ADMIN_ROLE_ID, USER_ROLE_ID, GUEST_ROLE_ID)
     * @return a valid JWT token for the created user
     * @throws Exception if user creation or login fails
     */
    public String createUserWithRoleAndGetToken(MockMvc mockMvc, String username, String password, String email, UUID roleId) throws Exception {
        UserEntity user = createUser(username, username, email, password);

        if (roleId != null) {
            assignRoleToUser(user.getId(), roleId);
        }

        return loginAndGetToken(mockMvc, username, password);
    }

    /**
     * Create a user without any roles and return a valid JWT token.
     * Useful for testing permission-denied scenarios.
     *
     * @param mockMvc the MockMvc instance for performing login
     * @param username the username for the user
     * @param password the password for the user
     * @param email the email for the user
     * @return a valid JWT token for the created user (with no permissions)
     * @throws Exception if user creation or login fails
     */
    public String createUserWithoutRoleAndGetToken(MockMvc mockMvc, String username, String password, String email) throws Exception {
        createUser(username, username, email, password);
        return loginAndGetToken(mockMvc, username, password);
    }

    /**
     * Create a user entity in the database.
     *
     * @param username the username
     * @param name the display name
     * @param email the email address
     * @param password the plain-text password (will be hashed)
     * @return the saved UserEntity
     */
    public UserEntity createUser(String username, String name, String email, String password) {
        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());
        user.setUsername(username);
        user.setName(name);
        user.setEmailAddress(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    /**
     * Assign a role to a user.
     *
     * @param userId the user ID
     * @param roleId the role ID
     */
    public void assignRoleToUser(UUID userId, UUID roleId) {
        UserRoleEntity userRole = new UserRoleEntity(userId, roleId, LocalDateTime.now());
        userRoleRepository.save(userRole);
    }

    /**
     * Login and return a JWT token.
     *
     * @param mockMvc the MockMvc instance
     * @param username the username
     * @param password the password
     * @return the JWT token
     * @throws Exception if login fails
     */
    public String loginAndGetToken(MockMvc mockMvc, String username, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest(username, password);
        MvcResult result = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        LoginResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(), LoginResponse.class);
        return response.getToken();
    }
}

