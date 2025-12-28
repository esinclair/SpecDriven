package com.example.specdriven.users;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Utility for hashing and verifying passwords using BCrypt.
 * T026: Password hashing implementation.
 */
@Component
public class PasswordHasher {
    
    private final PasswordEncoder encoder = new BCryptPasswordEncoder();
    
    /**
     * Hash a plain text password.
     * @param plainPassword the plain text password
     * @return the hashed password
     */
    public String hash(String plainPassword) {
        return encoder.encode(plainPassword);
    }
    
    /**
     * Verify that a plain text password matches a hashed password.
     * @param plainPassword the plain text password
     * @param hashedPassword the hashed password
     * @return true if the password matches, false otherwise
     */
    public boolean matches(String plainPassword, String hashedPassword) {
        return encoder.matches(plainPassword, hashedPassword);
    }
}
