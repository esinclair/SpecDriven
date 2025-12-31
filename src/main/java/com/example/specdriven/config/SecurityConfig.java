package com.example.specdriven.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security configuration for the application.
 * Configures authentication, authorization, and security filters.
 * 
 * Security Rules:
 * - /ping: Always accessible (no authentication required)
 * - /login: Accessible without authentication
 * - /users/**: Requires authentication (JWT bearer token) for all operations
 * - All other endpoints: Require authentication by default
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configure the security filter chain.
     * Defines which endpoints require authentication and session management.
     *
     * @param http the HttpSecurity to configure
     * @return configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for stateless REST API
            .csrf(csrf -> csrf.disable())
            
            // Configure authorization rules
            .authorizeHttpRequests(authz -> authz
                // Public endpoints (no authentication required)
                .requestMatchers("/ping").permitAll()
                .requestMatchers("/login").permitAll()
                .requestMatchers("/test/**").permitAll() // Test endpoints for integration tests
                .requestMatchers("/h2-console/**").permitAll() // H2 console for development
                
                // Protected endpoints (authentication required)
                .requestMatchers("/users/**").authenticated()
                
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            
            // Stateless session management (no server-side sessions)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Disable default login form
            .formLogin(form -> form.disable())
            
            // Disable HTTP Basic authentication
            .httpBasic(basic -> basic.disable())
            
            // Allow H2 console frames (for development only)
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
            );

        return http.build();
    }
}
