package com.example.specdriven.service;

import com.example.specdriven.domain.RoleEntity;
import com.example.specdriven.domain.UserEntity;
import com.example.specdriven.exception.AuthenticationException;
import com.example.specdriven.exception.ValidationException;
import com.example.specdriven.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for handling user login and authentication.
 */
@Service
public class LoginService {
    
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    
    public LoginService(UserService userService,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }
    
    public String login(String username, String password) {
        // Validate inputs
        if (username == null || username.isBlank()) {
            throw new ValidationException("Username is required");
        }
        if (password == null || password.isBlank()) {
            throw new ValidationException("Password is required");
        }
        
        try {
            // Find user
            UserEntity user = userService.findByUsername(username);
            
            // Verify password
            if (!passwordEncoder.matches(password, user.getPasswordHash())) {
                // Non-enumerating error message
                throw new AuthenticationException("Invalid username or password");
            }
            
            // Load roles
            List<String> roles;
            if (user.getRoles() != null && !user.getRoles().isEmpty()) {
                roles = user.getRoles().stream()
                        .map(RoleEntity::getName)
                        .collect(Collectors.toList());
            } else {
                roles = List.of("USER");  // Default role
            }
            
            // Generate token
            return tokenProvider.generateToken(username, roles);
            
        } catch (com.example.specdriven.exception.ResourceNotFoundException e) {
            // Non-enumerating error message (same as invalid password)
            throw new AuthenticationException("Invalid username or password");
        }
    }
}
