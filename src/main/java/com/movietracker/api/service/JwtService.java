package com.movietracker.api.service;

import com.movietracker.api.config.AuthConfig;
import com.movietracker.api.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Service for JWT token operations including generation, validation, and parsing.
 * 
 * <p>This service provides:
 * <ul>
 *   <li>Secure JWT token generation with user claims</li>
 *   <li>Token validation using HMAC SHA-256 signature verification</li>
 *   <li>Claim extraction (username, user ID, expiration)</li>
 *   <li>Configurable token expiration based on application settings</li>
 * </ul>
 * 
 * <p>Tokens are signed using HS256 algorithm with a configurable secret key.
 * The secret key is automatically converted to the appropriate format for HMAC operations.
 * 
 * @author Movie Tracker API Team
 * @since 1.0.0
 */
@Service
public class JwtService {
    
    private final AuthConfig authConfig;
    private final SecretKey secretKey;
    
    @Autowired
    public JwtService(AuthConfig authConfig) {
        this.authConfig = authConfig;
        this.secretKey = Keys.hmacShaKeyFor(authConfig.getJwt().getSecret().getBytes());
    }
    
    /**
     * Generate JWT token for user
     */
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("user_id", user.getId());
        claims.put("email", user.getEmail());
        claims.put("username", user.getUsername());
        
        return createToken(claims, user.getEmail());
    }
    
    /**
     * Create JWT token with claims and subject
     */
    private String createToken(Map<String, Object> claims, String subject) {        Date now = new Date();
        Date expiration = Date.from(
            LocalDateTime.now()
                .plusHours(authConfig.getJwt().getExpirationHours())
                .atZone(ZoneId.systemDefault())
                .toInstant()
        );
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * Validate JWT token
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Extract username (email) from token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    /**
     * Extract user ID from token
     */
    public String extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("user_id", String.class));
    }
    
    /**
     * Extract expiration date from token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    /**
     * Extract specific claim from token
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    /**
     * Extract all claims from token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}
