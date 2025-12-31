package com.example.specdriven.controller;

import com.example.specdriven.api.LoginApi;
import com.example.specdriven.api.model.LoginRequest;
import com.example.specdriven.api.model.LoginResponse;
import com.example.specdriven.service.LoginService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller implementing the Login API for user authentication.
 * Delegates to LoginService for business logic.
 */
@RestController
public class LoginController implements LoginApi {

    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    /**
     * Authenticate user and return JWT token.
     *
     * @param loginRequest the login credentials
     * @return 200 OK with LoginResponse containing the token
     */
    @Override
    public ResponseEntity<LoginResponse> login(LoginRequest loginRequest) {
        LoginResponse response = loginService.login(loginRequest);
        return ResponseEntity.ok(response);
    }
}
