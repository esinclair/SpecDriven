package com.example.specdriven.service;

import com.example.specdriven.api.model.LoginRequest;
import com.example.specdriven.api.model.LoginResponse;
import com.example.specdriven.domain.UserEntity;
import com.example.specdriven.exception.AuthenticationException;
import com.example.specdriven.repository.UserRepository;
import com.example.specdriven.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;

/**
 * Service for handling user authentication (login).
 * Implements non-enumeration by returning the same error for unknown username and wrong password.
 */
@Service
public class LoginService {

    private static final Logger logger = LoggerFactory.getLogger(LoginService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    
    // Pre-computed dummy hash using the same encoder for consistent timing
    private final String dummyHash;

    public LoginService(UserRepository userRepository, 
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        // Generate dummy hash with a secure random password for timing attack prevention
        this.dummyHash = passwordEncoder.encode(generateSecureRandomPassword());
    }

    /**
     * Generate a secure random password for dummy hash creation.
     * This prevents timing analysis attacks by ensuring password comparison always occurs.
     */
    private static String generateSecureRandomPassword() {
        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getEncoder().encodeToString(randomBytes);
    }

    /**
     * Authenticate a user and return a JWT token.
     * Implements non-enumeration: always performs password hash comparison even if user not found.
     *
     * @param loginRequest the login credentials
     * @return LoginResponse containing the JWT token
     * @throws AuthenticationException if credentials are invalid
     */
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest loginRequest) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        // Find user by username
        Optional<UserEntity> userOptional = userRepository.findByUsername(username);

        // Get the password hash to compare (use dummy hash if user not found for non-enumeration)
        String storedHash = userOptional
                .map(UserEntity::getPasswordHash)
                .orElse(dummyHash);

        // Always perform password comparison to prevent timing attacks
        boolean passwordMatches = passwordEncoder.matches(password, storedHash);

        // If user not found or password doesn't match, throw same error (non-enumeration)
        if (userOptional.isEmpty() || !passwordMatches) {
            logger.warn("Authentication failed for username: {}", username);
            throw new AuthenticationException("Invalid username or password");
        }

        UserEntity user = userOptional.get();
        
        // Generate JWT token
        String token = jwtTokenProvider.generateToken(user.getId());

        logger.info("User {} logged in successfully", username);

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setTokenType(LoginResponse.TokenTypeEnum.BEARER);
        return response;
    }
}
