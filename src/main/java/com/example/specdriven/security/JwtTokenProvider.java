package com.example.specdriven.security;

import com.example.specdriven.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * Provider for generating and validating JWT bearer tokens.
 * Uses JJWT library with HS256 algorithm.
 */
@Component
public class JwtTokenProvider {
    
    private final SecretKey key;
    private final long expirationMs;
    
    public JwtTokenProvider(JwtConfig jwtConfig) {
        // Create signing key from configured secret
        this.key = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
        this.expirationMs = jwtConfig.getExpirationMs();
    }
    
    /**
     * Generate a JWT token for the given user ID.
     * 
     * @param userId the user's unique identifier
     * @return signed JWT token string
     */
    public String generateToken(UUID userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);
        
        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }
    
    /**
     * Validate a JWT token and return true if valid.
     * 
     * @param token the JWT token to validate
     * @return true if token is valid and not expired
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            // Token expired
            return false;
        } catch (UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
            // Invalid token
            return false;
        }
    }
    
    /**
     * Extract the user ID from a valid JWT token.
     * 
     * @param token the JWT token
     * @return the user ID
     * @throws io.jsonwebtoken.JwtException if token is invalid
     */
    public UUID getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        
        return UUID.fromString(claims.getSubject());
    }
}
