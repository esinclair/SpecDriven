package com.example.specdriven.config;

import com.example.specdriven.api.model.ErrorResponse;
import com.example.specdriven.exception.ErrorResponseFactory;
import com.example.specdriven.security.FeatureFlagSecurityFilter;
import com.example.specdriven.security.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration for the application.
 * Configures authentication, authorization, and security filters.
 * 
 * Filter Chain Order:
 * 1. FeatureFlagSecurityFilter - Gates endpoints based on feature flag
 * 2. JwtAuthenticationFilter - Validates JWT tokens
 * 3. UsernamePasswordAuthenticationFilter (Spring's default)
 * 
 * Security Rules:
 * - /ping: Always accessible (no authentication required, bypasses feature flag)
 * - /login: Accessible without authentication (gated by feature flag)
 * - /users/**: Requires authentication (JWT bearer token), gated by feature flag
 * - All other endpoints: Require authentication by default
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final FeatureFlagSecurityFilter featureFlagSecurityFilter;
    private final ObjectMapper objectMapper;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          FeatureFlagSecurityFilter featureFlagSecurityFilter,
                          ObjectMapper objectMapper) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.featureFlagSecurityFilter = featureFlagSecurityFilter;
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
                .accessDeniedHandler(accessDeniedHandler())
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
            
            // Add FeatureFlagSecurityFilter first (before JWT filter)
            .addFilterBefore(featureFlagSecurityFilter, UsernamePasswordAuthenticationFilter.class)
            
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

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            ErrorResponse error = new ErrorResponse();
            error.setCode("FORBIDDEN");
            error.setMessage("Access denied");
            objectMapper.writeValue(response.getOutputStream(), error);
        };
    }
}
