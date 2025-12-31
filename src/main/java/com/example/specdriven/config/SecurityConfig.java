package com.example.specdriven.config;

import com.example.specdriven.api.model.ErrorResponse;
import com.example.specdriven.exception.ErrorResponseFactory;
import com.example.specdriven.security.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, ObjectMapper objectMapper) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.objectMapper = objectMapper;
    }

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
            
            // Configure exception handling for authentication errors
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(authenticationEntryPoint())
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
            )
            
            // Add JWT authentication filter before UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Custom authentication entry point for handling 401 responses.
     * Returns JSON error response when authentication is required but not provided.
     */
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            ErrorResponse error = ErrorResponseFactory.authenticationRequired("Authentication required");
            objectMapper.writeValue(response.getOutputStream(), error);
        };
    }
}
