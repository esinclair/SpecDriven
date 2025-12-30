package com.example.specdriven.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuration for password encoding.
 * Uses BCrypt for secure password hashing.
 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * Create a BCrypt password encoder bean.
     * BCrypt is a secure adaptive hash function designed for password hashing.
     * It includes a salt to protect against rainbow table attacks.
     *
     * @return BCryptPasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
