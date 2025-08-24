package com.movietracker.api.service;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class OAuth2SessionService {

    private final Map<String, OAuth2SessionData> sessions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    // Session expires after 10 minutes
    private static final long SESSION_TIMEOUT_MINUTES = 10;

    public OAuth2SessionService() {
        // Clean up expired sessions every 5 minutes
        scheduler.scheduleAtFixedRate(this::cleanupExpiredSessions, 5, 5, TimeUnit.MINUTES);
    }

    /**
     * Create a new OAuth2 session for the authorization flow
     */
    public String createSession(String state, String codeVerifier, String provider) {
        String sessionId = UUID.randomUUID().toString();
        OAuth2SessionData sessionData = new OAuth2SessionData(
            sessionId,
            state, 
            codeVerifier,
            provider,
            System.currentTimeMillis()
        );
        
        sessions.put(sessionId, sessionData);
        return sessionId;
    }

    /**
     * Store authentication result in session after OAuth2 callback
     */
    public void storeAuthenticationResult(String sessionId, String userId, String token, String userJson) {
        OAuth2SessionData session = sessions.get(sessionId);
        if (session != null && !session.isExpired()) {
            session.setUserId(userId);
            session.setToken(token);
            session.setUserData(userJson);
            session.setAuthenticated(true);
        }
    }

    /**
     * Exchange session for authentication data (one-time use)
     */
    public OAuth2SessionData exchangeSession(String sessionId) {
        OAuth2SessionData session = sessions.remove(sessionId); // Remove on exchange
        if (session != null && !session.isExpired() && session.isAuthenticated()) {
            return session;
        }
        return null;
    }

    /**
     * Validate session for OAuth2 callback
     */
    public OAuth2SessionData getSession(String sessionId) {
        OAuth2SessionData session = sessions.get(sessionId);
        if (session != null && !session.isExpired()) {
            return session;
        }
        return null;
    }

    /**
     * Clean up expired sessions
     */
    private void cleanupExpiredSessions() {
        long now = System.currentTimeMillis();
        sessions.entrySet().removeIf(entry -> 
            now - entry.getValue().getCreatedAt() > TimeUnit.MINUTES.toMillis(SESSION_TIMEOUT_MINUTES)
        );
    }

    /**
     * OAuth2 session data container
     */
    public static class OAuth2SessionData {
        private final String sessionId;
        private final String state;
        private final String codeVerifier;
        private final String provider;
        private final long createdAt;
        
        // Set after authentication
        private String userId;
        private String token;
        private String userData;
        private boolean authenticated = false;

        public OAuth2SessionData(String sessionId, String state, String codeVerifier, 
                               String provider, long createdAt) {
            this.sessionId = sessionId;
            this.state = state;
            this.codeVerifier = codeVerifier;
            this.provider = provider;
            this.createdAt = createdAt;
        }

        // Getters
        public String getSessionId() { return sessionId; }
        public String getState() { return state; }
        public String getCodeVerifier() { return codeVerifier; }
        public String getProvider() { return provider; }
        public long getCreatedAt() { return createdAt; }
        public String getUserId() { return userId; }
        public String getToken() { return token; }
        public String getUserData() { return userData; }
        public boolean isAuthenticated() { return authenticated; }

        // Setters
        public void setUserId(String userId) { this.userId = userId; }
        public void setToken(String token) { this.token = token; }
        public void setUserData(String userData) { this.userData = userData; }
        public void setAuthenticated(boolean authenticated) { this.authenticated = authenticated; }

        public boolean isExpired() {
            return System.currentTimeMillis() - createdAt > TimeUnit.MINUTES.toMillis(SESSION_TIMEOUT_MINUTES);
        }
    }
}