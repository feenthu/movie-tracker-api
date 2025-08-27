package com.movietracker.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class OAuth2SessionServiceTest {

    private OAuth2SessionService sessionService;

    @BeforeEach
    void setUp() {
        sessionService = new OAuth2SessionService();
    }

    @Test
    void shouldCreateSessionWithValidParameters() {
        String state = "test-state";
        String codeVerifier = "test-code-verifier";
        String provider = "google";

        String sessionId = sessionService.createSession(state, codeVerifier, provider);

        assertNotNull(sessionId);
        assertFalse(sessionId.isEmpty());

        OAuth2SessionService.OAuth2SessionData sessionData = sessionService.getSession(sessionId);
        assertNotNull(sessionData);
        assertEquals(state, sessionData.getState());
        assertEquals(codeVerifier, sessionData.getCodeVerifier());
        assertEquals(provider, sessionData.getProvider());
        assertFalse(sessionData.isAuthenticated());
    }

    @Test
    void shouldStoreAuthenticationResult() {
        String sessionId = sessionService.createSession("state", "verifier", "google");
        String userId = "user123";
        String token = "jwt-token";
        String userJson = "{\"id\":\"user123\",\"email\":\"test@example.com\"}";

        sessionService.storeAuthenticationResult(sessionId, userId, token, userJson);

        OAuth2SessionService.OAuth2SessionData sessionData = sessionService.getSession(sessionId);
        assertNotNull(sessionData);
        assertEquals(userId, sessionData.getUserId());
        assertEquals(token, sessionData.getToken());
        assertEquals(userJson, sessionData.getUserData());
        assertTrue(sessionData.isAuthenticated());
    }

    @Test
    void shouldExchangeSessionOneTimeOnly() {
        String sessionId = sessionService.createSession("state", "verifier", "google");
        sessionService.storeAuthenticationResult(sessionId, "user123", "token", "userdata");

        // First exchange should succeed
        OAuth2SessionService.OAuth2SessionData sessionData = sessionService.exchangeSession(sessionId);
        assertNotNull(sessionData);
        assertTrue(sessionData.isAuthenticated());

        // Second exchange should fail (one-time use)
        OAuth2SessionService.OAuth2SessionData secondExchange = sessionService.exchangeSession(sessionId);
        assertNull(secondExchange);
    }

    @Test
    void shouldReturnNullForInvalidSession() {
        OAuth2SessionService.OAuth2SessionData sessionData = sessionService.getSession("invalid-session-id");
        assertNull(sessionData);
    }

    @Test
    void shouldReturnNullForExchangeOfUnauthenticatedSession() {
        String sessionId = sessionService.createSession("state", "verifier", "google");
        // Don't store authentication result

        OAuth2SessionService.OAuth2SessionData sessionData = sessionService.exchangeSession(sessionId);
        assertNull(sessionData);
    }

    @Test
    void shouldHandleSessionExpiration() throws InterruptedException {
        // This test would require modifying the timeout for testing purposes
        // or using dependency injection to provide a test-specific timeout
        // For now, we'll just test the isExpired method logic
        
        String sessionId = sessionService.createSession("state", "verifier", "google");
        OAuth2SessionService.OAuth2SessionData sessionData = sessionService.getSession(sessionId);
        
        assertNotNull(sessionData);
        assertFalse(sessionData.isExpired());
    }
}