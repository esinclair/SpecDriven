package com.example.specdriven.controller;

import com.example.specdriven.api.LoginApi;
import com.example.specdriven.api.model.LoginRequest;
import com.example.specdriven.api.model.LoginResponse;
import com.example.specdriven.service.LoginService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for user authentication endpoints.
 * Implements the generated LoginApi interface from OpenAPI contract.
 */
@RestController
public class LoginController implements LoginApi {
    
    private final LoginService loginService;
    
    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }
    
    @Override
    public ResponseEntity<LoginResponse> login(@Valid LoginRequest loginRequest) {
        LoginResponse response = loginService.login(loginRequest);
        return ResponseEntity.ok(response);
    }
}
