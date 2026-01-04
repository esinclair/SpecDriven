package com.example.specdriven.security;

import com.example.specdriven.api.model.ErrorResponse;
import com.example.specdriven.exception.ErrorResponseFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.stream.Collectors;

import com.example.specdriven.domain.PermissionEntity;
import com.example.specdriven.repository.PermissionRepository;

/**
 * JWT authentication filter that extracts and validates Bearer tokens from requests.
 * Sets the Spring Security authentication context for valid tokens.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;
    private final PermissionRepository permissionRepository;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider,
                                   ObjectMapper objectMapper,
                                   PermissionRepository permissionRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.objectMapper = objectMapper;
        this.permissionRepository = permissionRepository;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String requestPath = request.getRequestURI();
        
        // Skip JWT validation for public endpoints
        if (isPublicEndpoint(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwt = extractJwtFromRequest(request);

            if (jwt == null) {
                // No token provided - let Spring Security handle unauthorized access
                filterChain.doFilter(request, response);
                return;
            }

            if (!jwtTokenProvider.validateToken(jwt)) {
                // Invalid or expired token
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                        ErrorResponseFactory.authenticationFailed("Invalid or expired token"));
                return;
            }

            // Valid token - set authentication in security context
            UUID userId = jwtTokenProvider.getUserIdFromToken(jwt);

            // Load permissions for the user and map to Spring Security authorities
            var permissions = permissionRepository.findByUserId(userId);
            var authorities = permissions.stream()
                    .map(PermissionEntity::getPermission)
                    .map(SimpleGrantedAuthority::new)
                    .map(GrantedAuthority.class::cast)
                    .collect(Collectors.toList());

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);

        } catch (Exception ex) {
            logger.error("Error processing JWT authentication: {}", ex.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    ErrorResponseFactory.authenticationFailed("Authentication failed"));
        }
    }

    /**
     * Check if the request path is a public endpoint that doesn't require authentication.
     * H2 console and test endpoints are only accessible in non-production environments.
     */
    private boolean isPublicEndpoint(String requestPath) {
        // Always public endpoints
        if (requestPath.equals("/ping") || requestPath.equals("/login")) {
            return true;
        }
        
        // Development/test only endpoints - disabled in production
        String profile = System.getProperty("spring.profiles.active", "");
        boolean isProd = profile.contains("prod") || profile.contains("production");
        
        if (!isProd) {
            return requestPath.startsWith("/test/") || requestPath.startsWith("/h2-console");
        }
        
        return false;
    }

    /**
     * Extract JWT token from the Authorization header.
     *
     * @param request the HTTP request
     * @return the JWT token, or null if not present
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    /**
     * Send a JSON error response.
     */
    private void sendErrorResponse(HttpServletResponse response, int status, ErrorResponse error) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), error);
    }
}
