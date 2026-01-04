package com.example.specdriven.integration;

import com.example.specdriven.api.model.*;
import com.example.specdriven.integration.support.IntegrationTestHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PermissionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IntegrationTestHelper testHelper;

    @Test
    void createUser_WithCreateUserPermission_Returns201() throws Exception {
        String token = testHelper.createAdminUserAndGetToken(mockMvc, "admin_user", "Password123!", "admin_user@example.com");

        CreateUserRequest request = new CreateUserRequest(
                "newuser_ok", "New User OK", "Password123!", "new_ok@example.com");

        mockMvc.perform(post("/users")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void createUser_WithoutCreateUserPermission_Returns403() throws Exception {
        // User with no roles
        String token = testHelper.createUserWithoutRoleAndGetToken(mockMvc, "standard_user", "Password123!", "standard_user@example.com");

        CreateUserRequest request = new CreateUserRequest(
                "newuser_fail", "New User Fail", "Password123!", "new_fail@example.com");

        mockMvc.perform(post("/users")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }
}
