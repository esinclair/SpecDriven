package com.example.specdriven.controller;

import com.example.specdriven.api.LoginApi;
import com.example.specdriven.api.model.LoginRequest;
import com.example.specdriven.api.model.LoginResponse;
import com.example.specdriven.service.LoginService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller implementing the login endpoint.
 */
@RestController
public class LoginController implements LoginApi {
    
    private final LoginService loginService;
    
    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }
    
    @Override
    public ResponseEntity<LoginResponse> login(LoginRequest loginRequest) {
        String token = loginService.login(
                loginRequest.getUsername(), 
                loginRequest.getPassword()
        );
        
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        
        return ResponseEntity.ok(response);
    }
}
