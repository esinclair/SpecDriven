package com.example.specdriven.users;

import com.example.specdriven.api.model.CreateUserRequest;
import com.example.specdriven.api.model.ErrorResponse;
import com.example.specdriven.api.model.LoginRequest;
import com.example.specdriven.api.model.RoleName;
import com.example.specdriven.api.model.UpdateUserRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * T007: Tests that all Users API endpoints (including /login) return 404
 * when feature-flag.users-api=false
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "feature-flag.users-api=false"
})
class UsersApiFeatureFlagDisabledTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void postUsers_whenFeatureDisabled_returns404() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("testuser");
        request.setName("Test User");
        request.setEmailAddress("test@example.com");
        request.setPassword("password123");

        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                "/users",
                request,
                ErrorResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("FEATURE_DISABLED");
    }

    @Test
    void getUsers_whenFeatureDisabled_returns404() {
        ResponseEntity<ErrorResponse> response = restTemplate.getForEntity(
                "/users?page=0&pageSize=10",
                ErrorResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("FEATURE_DISABLED");
    }

    @Test
    void getUserById_whenFeatureDisabled_returns404() {
        UUID userId = UUID.randomUUID();
        ResponseEntity<ErrorResponse> response = restTemplate.getForEntity(
                "/users/" + userId,
                ErrorResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("FEATURE_DISABLED");
    }

    @Test
    void updateUser_whenFeatureDisabled_returns404() {
        UUID userId = UUID.randomUUID();
        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("Updated Name");

        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                "/users/" + userId,
                HttpMethod.PUT,
                new HttpEntity<>(request),
                ErrorResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("FEATURE_DISABLED");
    }

    @Test
    void deleteUser_whenFeatureDisabled_returns404() {
        UUID userId = UUID.randomUUID();
        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                "/users/" + userId,
                HttpMethod.DELETE,
                null,
                ErrorResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("FEATURE_DISABLED");
    }

    @Test
    void assignRole_whenFeatureDisabled_returns404() {
        UUID userId = UUID.randomUUID();
        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                "/users/" + userId + "/roles/" + RoleName.ADMIN.getValue(),
                HttpMethod.PUT,
                null,
                ErrorResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("FEATURE_DISABLED");
    }

    @Test
    void removeRole_whenFeatureDisabled_returns404() {
        UUID userId = UUID.randomUUID();
        ResponseEntity<ErrorResponse> response = restTemplate.exchange(
                "/users/" + userId + "/roles/" + RoleName.ADMIN.getValue(),
                HttpMethod.DELETE,
                null,
                ErrorResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("FEATURE_DISABLED");
    }

    @Test
    void login_whenFeatureDisabled_returns404() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                "/login",
                request,
                ErrorResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("FEATURE_DISABLED");
    }
}
