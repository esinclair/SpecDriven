package com.example.specdriven.config;

import com.example.specdriven.repository.UserRepository;
import com.example.specdriven.security.FeatureFlagSecurityFilter;
import com.example.specdriven.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration for the application.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final FeatureFlagSecurityFilter featureFlagSecurityFilter;
    private final UserRepository userRepository;
    
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                         FeatureFlagSecurityFilter featureFlagSecurityFilter,
                         UserRepository userRepository) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.featureFlagSecurityFilter = featureFlagSecurityFilter;
        this.userRepository = userRepository;
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/ping").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/login").permitAll()
                        // Bootstrap mode: allow POST /users without auth when no users exist
                        .requestMatchers(HttpMethod.POST, "/users").access((authentication, context) -> {
                            long userCount = userRepository.count();
                            if (userCount == 0) {
                                return new org.springframework.security.authorization.AuthorizationDecision(true);
                            }
                            return new org.springframework.security.authorization.AuthorizationDecision(
                                    authentication.get().isAuthenticated());
                        })
                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .addFilterBefore(featureFlagSecurityFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        // Allow H2 console frames
        http.headers(headers -> headers.frameOptions(frame -> frame.disable()));
        
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
