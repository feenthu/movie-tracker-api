package com.movietracker.api.service;

import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class PKCEService {

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generate a cryptographically secure code verifier for PKCE
     * @return Base64-URL encoded random string (43-128 characters)
     */
    public String generateCodeVerifier() {
        byte[] randomBytes = new byte[32]; // 32 bytes = 256 bits
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(randomBytes);
    }

    /**
     * Generate code challenge from code verifier using SHA256
     * @param codeVerifier The code verifier
     * @return Base64-URL encoded SHA256 hash of the code verifier
     */
    public String generateCodeChallenge(String codeVerifier) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Generate a secure state parameter for CSRF protection
     * @return Base64-URL encoded random string
     */
    public String generateState() {
        byte[] randomBytes = new byte[16]; // 16 bytes = 128 bits
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(randomBytes);
    }

    /**
     * PKCE parameters container
     */
    public static class PKCEParams {
        private final String codeVerifier;
        private final String codeChallenge;
        private final String state;

        public PKCEParams(String codeVerifier, String codeChallenge, String state) {
            this.codeVerifier = codeVerifier;
            this.codeChallenge = codeChallenge;
            this.state = state;
        }

        public String getCodeVerifier() { return codeVerifier; }
        public String getCodeChallenge() { return codeChallenge; }
        public String getState() { return state; }
    }

    /**
     * Generate complete PKCE parameters
     */
    public PKCEParams generatePKCEParams() {
        String codeVerifier = generateCodeVerifier();
        String codeChallenge = generateCodeChallenge(codeVerifier);
        String state = generateState();
        
        return new PKCEParams(codeVerifier, codeChallenge, state);
    }
}