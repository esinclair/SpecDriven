package com.example.specdriven.service;

import com.example.specdriven.api.model.LoginRequest;
import com.example.specdriven.api.model.LoginResponse;
import com.example.specdriven.domain.UserEntity;
import com.example.specdriven.exception.AuthenticationException;
import com.example.specdriven.repository.UserRepository;
import com.example.specdriven.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service for handling user authentication and login operations.
 * Implements non-enumeration pattern to prevent username enumeration attacks.
 */
@Service
@Transactional
public class LoginService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    
    public LoginService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }
    
    /**
     * Authenticate a user with username and password, returning a JWT token.
     * Implements non-enumeration: returns same error for unknown username and wrong password.
     * 
     * @param request the login request with username and password
     * @return login response with JWT token
     * @throws AuthenticationException if credentials are invalid
     */
    public LoginResponse login(LoginRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();
        
        // Look up user by email address (username is email)
        Optional<UserEntity> userOpt = userRepository.findByEmailAddress(username);
        
        // For non-enumeration: always hash the password even if user not found
        // This prevents timing attacks that could reveal if a username exists
        String dummyHash = "$2a$10$dummyhashfordummyuser000000000000000000000000000000";
        String hashToCheck = userOpt.map(UserEntity::getPasswordHash).orElse(dummyHash);
        
        // Always check password (either real hash or dummy hash)
        boolean passwordMatches = passwordEncoder.matches(password, hashToCheck);
        
        // Only succeed if user exists AND password matches
        if (userOpt.isPresent() && passwordMatches) {
            UserEntity user = userOpt.get();
            String token = jwtTokenProvider.generateToken(user.getId());
            
            LoginResponse response = new LoginResponse();
            response.setToken(token);
            response.setTokenType(LoginResponse.TokenTypeEnum.BEARER);
            return response;
        }
        
        // Return same error message for unknown username and wrong password
        throw new AuthenticationException("Invalid username or password");
    }
}
