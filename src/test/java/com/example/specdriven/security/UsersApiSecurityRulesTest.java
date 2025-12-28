package com.example.specdriven.security;

import com.example.specdriven.api.model.CreateUserRequest;
import com.example.specdriven.api.model.ErrorResponse;
import com.example.specdriven.api.model.LoginRequest;
import com.example.specdriven.api.model.LoginResponse;
import com.example.specdriven.api.model.User;
import com.example.specdriven.users.persistence.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * T019: Security integration tests:
 * - /login is public
 * - protected endpoints require bearer token
 * - bootstrap create-user behavior (0 users → allow, >0 users → deny)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "feature-flag.users-api=true"
})
class UsersApiSecurityRulesTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void clearUsers() {
        // Clean slate for each test
        userRepository.deleteAll();
    }

    @Test
    void login_isPublic_noAuthRequired() {
        // Create a user first (bootstrap allowed when count=0)
        CreateUserRequest createReq = new CreateUserRequest();
        createReq.setUsername("logintest");
        createReq.setName("Login Test");
        createReq.setEmailAddress("login@example.com");
        createReq.setPassword("password123");
        
        ResponseEntity<User> createResp = restTemplate.postForEntity("/users", createReq, User.class);
        assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Login without bearer token should succeed
        LoginRequest loginReq = new LoginRequest();
        loginReq.setUsername("logintest");
        loginReq.setPassword("password123");

        ResponseEntity<LoginResponse> loginResp = restTemplate.postForEntity("/login", loginReq, LoginResponse.class);
        
        assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResp.getBody()).isNotNull();
        assertThat(loginResp.getBody().getToken()).isNotEmpty();
    }

    @Test
    void bootstrapMode_allowsUnauthenticatedCreateUser_whenUserCountIsZero() {
        // Given zero users
        assertThat(userRepository.count()).isEqualTo(0);

        // When creating first user without auth
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("firstuser");
        request.setName("First User");
        request.setEmailAddress("first@example.com");
        request.setPassword("password123");

        ResponseEntity<User> response = restTemplate.postForEntity("/users", request, User.class);

        // Then it succeeds
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUsername()).isEqualTo("firstuser");
    }

    @Test
    void afterBootstrap_requiresAuth_forCreateUser_whenUsersExist() {
        // Given at least one user exists (bootstrap complete)
        CreateUserRequest firstUser = new CreateUserRequest();
        firstUser.setUsername("existing");
        firstUser.setName("Existing User");
        firstUser.setEmailAddress("existing@example.com");
        firstUser.setPassword("password123");
        restTemplate.postForEntity("/users", firstUser, User.class);

        // When trying to create another user without auth
        CreateUserRequest secondUser = new CreateUserRequest();
        secondUser.setUsername("second");
        secondUser.setName("Second User");
        secondUser.setEmailAddress("second@example.com");
        secondUser.setPassword("password123");

        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity("/users", secondUser, ErrorResponse.class);

        // Then it fails with 401
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("UNAUTHORIZED");
    }

    @Test
    void protectedEndpoints_requireBearerToken() {
        // Create and login to get a token
        CreateUserRequest createReq = new CreateUserRequest();
        createReq.setUsername("tokentest");
        createReq.setName("Token Test");
        createReq.setEmailAddress("token@example.com");
        createReq.setPassword("password123");
        ResponseEntity<User> createResp = restTemplate.postForEntity("/users", createReq, User.class);
        UUID userId = createResp.getBody().getId();

        LoginRequest loginReq = new LoginRequest();
        loginReq.setUsername("tokentest");
        loginReq.setPassword("password123");
        ResponseEntity<LoginResponse> loginResp = restTemplate.postForEntity("/login", loginReq, LoginResponse.class);
        String token = loginResp.getBody().getToken();

        // Without token - should fail
        ResponseEntity<ErrorResponse> noTokenResp = restTemplate.getForEntity("/users/" + userId, ErrorResponse.class);
        assertThat(noTokenResp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        // With valid token - should succeed
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        ResponseEntity<User> withTokenResp = restTemplate.exchange(
                "/users/" + userId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                User.class
        );
        assertThat(withTokenResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(withTokenResp.getBody()).isNotNull();
        assertThat(withTokenResp.getBody().getUsername()).isEqualTo("tokentest");
    }
}
