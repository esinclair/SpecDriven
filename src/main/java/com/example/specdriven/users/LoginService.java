package com.example.specdriven.users;

import com.example.specdriven.api.model.LoginRequest;
import com.example.specdriven.api.model.LoginResponse;
import com.example.specdriven.error.ApiErrorCode;
import com.example.specdriven.error.ErrorResponseFactory;
import com.example.specdriven.security.TokenService;
import com.example.specdriven.users.persistence.UserEntity;
import org.springframework.stereotype.Service;

/**
 * Business logic for user authentication.
 * T035: Login service with credential verification.
 */
@Service
public class LoginService {
    
    private final UsersService usersService;
    private final PasswordHasher passwordHasher;
    private final TokenService tokenService;
    
    public LoginService(UsersService usersService,
                       PasswordHasher passwordHasher,
                       TokenService tokenService) {
        this.usersService = usersService;
        this.passwordHasher = passwordHasher;
        this.tokenService = tokenService;
    }
    
    /**
     * Authenticate a user and return a JWT token.
     * T035: Credential verification and token generation.
     * Returns a generic error for both invalid username and invalid password
     * to avoid leaking information about user existence.
     * 
     * @param request the login request
     * @return the login response with token
     * @throws org.springframework.web.server.ResponseStatusException 400 if credentials invalid
     */
    public LoginResponse login(LoginRequest request) {
        // Validate request
        if (request.getUsername() == null || request.getUsername().isEmpty()) {
            throw ErrorResponseFactory.badRequest(ApiErrorCode.INVALID_CREDENTIALS,
                "Invalid credentials");
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw ErrorResponseFactory.badRequest(ApiErrorCode.INVALID_CREDENTIALS,
                "Invalid credentials");
        }
        
        // Look up user by username
        UserEntity user = usersService.getUserEntityByUsername(request.getUsername())
            .orElseThrow(() -> ErrorResponseFactory.badRequest(ApiErrorCode.INVALID_CREDENTIALS,
                "Invalid credentials")); // Generic error to not leak user existence
        
        // Verify password
        if (!passwordHasher.matches(request.getPassword(), user.getPasswordHash())) {
            throw ErrorResponseFactory.badRequest(ApiErrorCode.INVALID_CREDENTIALS,
                "Invalid credentials"); // Generic error
        }
        
        // Generate JWT token
        String token = tokenService.mintToken(user.getId(), user.getUsername());
        
        // Return response
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        return response;
    }
}
