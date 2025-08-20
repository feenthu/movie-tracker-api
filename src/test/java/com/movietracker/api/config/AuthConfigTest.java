package com.movietracker.api.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class AuthConfigTest {
    
    @Autowired
    private AuthConfig authConfig;
    
    @Test
    void authConfig_ShouldLoadCorrectly() {
        // Then
        assertNotNull(authConfig);
        assertTrue(authConfig.isLocalAuthEnabled());
        assertFalse(authConfig.isOauth2Enabled());
        
        assertNotNull(authConfig.getJwt());
        assertEquals("testSecretKeyThatIsLongEnoughForHS256AlgorithmTesting", 
                     authConfig.getJwt().getSecret());
        assertEquals(1, authConfig.getJwt().getExpirationHours());
    }
    
    @Test
    void jwtConfig_ShouldHaveValidDefaults() {
        // Given
        AuthConfig.Jwt jwtConfig = new AuthConfig.Jwt();
        
        // Then
        assertNotNull(jwtConfig.getSecret());
        assertFalse(jwtConfig.getSecret().isEmpty());
        assertTrue(jwtConfig.getSecret().length() > 32); // Minimum for HS256
        assertEquals(1, jwtConfig.getExpirationHours());
    }
    
    @Test
    void authConfig_ShouldAllowFeatureToggling() {
        // Given
        AuthConfig config = new AuthConfig();
        
        // When
        config.setLocalAuthEnabled(false);
        config.setOauth2Enabled(true);
        
        // Then
        assertFalse(config.isLocalAuthEnabled());
        assertTrue(config.isOauth2Enabled());
    }
    
    @Test
    void jwtConfig_ShouldAllowCustomization() {
        // Given
        AuthConfig.Jwt jwtConfig = new AuthConfig.Jwt();
        
        // When
        jwtConfig.setSecret("customSecretKey");
        jwtConfig.setExpirationHours(24);
        
        // Then
        assertEquals("customSecretKey", jwtConfig.getSecret());
        assertEquals(24, jwtConfig.getExpirationHours());
    }
}
