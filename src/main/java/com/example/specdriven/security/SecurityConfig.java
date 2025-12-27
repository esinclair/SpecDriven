package com.example.specdriven.security;

import com.example.specdriven.config.FeatureFlagProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration for stateless bearer token authentication.
 * When users-api feature is disabled, permits all requests.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final FeatureFlagProperties featureFlagProperties;
    private final BearerTokenAuthFilter bearerTokenAuthFilter;
    private final BootstrapCreateUserAuthorizationManager bootstrapAuthManager;
    private final SecurityAuthenticationEntryPoint authenticationEntryPoint;
    private final SecurityAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(
            FeatureFlagProperties featureFlagProperties,
            @Autowired(required = false) BearerTokenAuthFilter bearerTokenAuthFilter,
            @Autowired(required = false) BootstrapCreateUserAuthorizationManager bootstrapAuthManager,
            @Autowired(required = false) SecurityAuthenticationEntryPoint authenticationEntryPoint,
            @Autowired(required = false) SecurityAccessDeniedHandler accessDeniedHandler) {
        this.featureFlagProperties = featureFlagProperties;
        this.bearerTokenAuthFilter = bearerTokenAuthFilter;
        this.bootstrapAuthManager = bootstrapAuthManager;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // If users-api feature is disabled, permit all requests (no authentication)
        if (!featureFlagProperties.isUsersApi()) {
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }

        // Users-api feature is enabled: apply full security configuration
        http.authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/ping").permitAll()
                        .requestMatchers("/login").permitAll()

                        // Bootstrap: POST /users allowed when zero users exist
                        .requestMatchers("POST", "/users").access(bootstrapAuthManager)

                        // All other /users endpoints require authentication
                        .requestMatchers("/users/**").authenticated()

                        // Default: permit all (let controllers handle feature flag gating)
                        .anyRequest().permitAll()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .addFilterBefore(bearerTokenAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

