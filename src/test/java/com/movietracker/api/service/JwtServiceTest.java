package com.movietracker.api.service;

import com.movietracker.api.config.AuthConfig;
import com.movietracker.api.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {
    
    private JwtService jwtService;
    private AuthConfig authConfig;
    private User testUser;
    
    @BeforeEach
    void setUp() {
        // Set up test configuration
        authConfig = new AuthConfig();
        authConfig.setLocalAuthEnabled(true);
        AuthConfig.Jwt jwtConfig = new AuthConfig.Jwt();
        jwtConfig.setSecret("testSecretKeyThatIsLongEnoughForHS256AlgorithmTesting");
        jwtConfig.setExpirationHours(1);
        authConfig.setJwt(jwtConfig);
        
        jwtService = new JwtService(authConfig);
        
        // Set up test user
        testUser = new User();
        testUser.setId("test-user-id");
        testUser.setEmail("test@example.com");
        testUser.setUsername("testuser");
        testUser.setPasswordHash("hashedPassword");
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
        testUser.setIsActive(true);
    }
    
    @Test
    void generateToken_ShouldCreateValidToken() {
        // When
        String token = jwtService.generateToken(testUser);
        
        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.contains("."));
        
        // Verify token is valid
        assertTrue(jwtService.validateToken(token));
    }
    
    @Test
    void validateToken_WithValidToken_ShouldReturnTrue() {
        // Given
        String token = jwtService.generateToken(testUser);
        
        // When
        boolean isValid = jwtService.validateToken(token);
        
        // Then
        assertTrue(isValid);
    }
    
    @Test
    void validateToken_WithInvalidToken_ShouldReturnFalse() {
        // Given
        String invalidToken = "invalid.token.here";
        
        // When
        boolean isValid = jwtService.validateToken(invalidToken);
        
        // Then
        assertFalse(isValid);
    }
    
    @Test
    void validateToken_WithNullToken_ShouldReturnFalse() {
        // When
        boolean isValid = jwtService.validateToken(null);
        
        // Then
        assertFalse(isValid);
    }
    
    @Test
    void extractUsername_ShouldReturnCorrectEmail() {
        // Given
        String token = jwtService.generateToken(testUser);
        
        // When
        String extractedUsername = jwtService.extractUsername(token);
        
        // Then
        assertEquals(testUser.getEmail(), extractedUsername);
    }
    
    @Test
    void extractUserId_ShouldReturnCorrectUserId() {
        // Given
        String token = jwtService.generateToken(testUser);
        
        // When
        String extractedUserId = jwtService.extractUserId(token);
        
        // Then
        assertEquals(testUser.getId(), extractedUserId);
    }
    
    @Test
    void isTokenExpired_WithNewToken_ShouldReturnFalse() {
        // Given
        String token = jwtService.generateToken(testUser);
        
        // When
        boolean isExpired = jwtService.isTokenExpired(token);
        
        // Then
        assertFalse(isExpired);
    }
    
    @Test
    void generateToken_ShouldIncludeRequiredClaims() {
        // When
        String token = jwtService.generateToken(testUser);
        
        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.contains("."));
        
        // Verify we can extract the claims we put in
        assertEquals(testUser.getEmail(), jwtService.extractUsername(token));
        assertEquals(testUser.getId(), jwtService.extractUserId(token));
        assertFalse(jwtService.isTokenExpired(token));
    }
}
