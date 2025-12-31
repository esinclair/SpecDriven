package com.example.specdriven.security;

import com.example.specdriven.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

/**
 * Filter that allows user creation without authentication when system is in bootstrap mode.
 * Bootstrap mode is active when no users exist in the system.
 */
@Component
public class BootstrapModeFilter extends OncePerRequestFilter {
    
    private final UserService userService;
    
    public BootstrapModeFilter(UserService userService) {
        this.userService = userService;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // Only apply to POST /users requests
        if ("POST".equalsIgnoreCase(request.getMethod()) &&
                request.getRequestURI().matches("^/users/?$")) {
            
            // Check if system is in bootstrap mode
            if (userService.isBootstrapMode()) {
                // Allow the request by setting a dummy authentication
                // This prevents Spring Security from blocking the request
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                "bootstrap",
                                null,
                                Collections.emptyList()
                        );
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        
        filterChain.doFilter(request, response);
    }
}
