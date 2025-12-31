package com.example.specdriven.security;

import com.example.specdriven.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * JWT token provider for generating and validating JSON Web Tokens.
 * Uses the jjwt library for secure token operations.
 */
@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);
    private static final int MINIMUM_SECRET_LENGTH = 32; // 256 bits for HS256

    private final JwtConfig jwtConfig;
    private final SecretKey secretKey;

    public JwtTokenProvider(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
        String secret = jwtConfig.getSecret();
        
        // Validate secret is not empty
        if (secret == null || secret.isEmpty()) {
            throw new IllegalStateException("JWT secret must not be empty. " +
                    "Set the jwt.secret property or JWT_SECRET environment variable.");
        }
        
        // Validate secret length for security
        if (secret.length() < MINIMUM_SECRET_LENGTH) {
            String profile = System.getProperty("spring.profiles.active", "");
            boolean isProd = profile.contains("prod") || profile.contains("production");
            
            if (isProd) {
                throw new IllegalStateException(
                        String.format("JWT secret is too short (%d chars). " +
                                "Production requires at least %d characters for security. " +
                                "Set the jwt.secret property or JWT_SECRET environment variable.",
                                secret.length(), MINIMUM_SECRET_LENGTH));
            } else {
                logger.warn("JWT secret is too short ({} chars). For production, use at least {} characters. " +
                           "Set the jwt.secret property or JWT_SECRET environment variable.",
                           secret.length(), MINIMUM_SECRET_LENGTH);
            }
        }
        
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generate a JWT token for the given user ID.
     *
     * @param userId the user's unique identifier
     * @return signed JWT token string
     */
    public String generateToken(UUID userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtConfig.getExpirationMs());

        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Validate a JWT token.
     *
     * @param token the JWT token to validate
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException ex) {
            logger.warn("JWT token expired: {}", ex.getMessage());
            return false;
        } catch (JwtException | IllegalArgumentException ex) {
            logger.warn("Invalid JWT token: {}", ex.getMessage());
            return false;
        }
    }

    /**
     * Extract the user ID from a JWT token.
     *
     * @param token the JWT token
     * @return the user ID from the token's subject claim
     * @throws JwtException if the token is invalid
     */
    public UUID getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return UUID.fromString(claims.getSubject());
    }

    /**
     * Check if a token is expired.
     *
     * @param token the JWT token
     * @return true if expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException ex) {
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return true;
        }
    }
}
