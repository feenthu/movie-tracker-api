package com.movietracker.api.controller;

import com.movietracker.api.service.OAuth2SessionService;
import com.movietracker.api.service.PKCEService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/oauth2")
@CrossOrigin(origins = {"http://localhost:3001", "https://movie-tracker-web-production.up.railway.app"}, allowCredentials = "true")
@ConditionalOnProperty(name = "app.auth.oauth2-enabled", havingValue = "true", matchIfMissing = false)
public class OAuth2Controller {

    private final OAuth2SessionService sessionService;
    private final PKCEService pkceService;

    @Value("${spring.security.oauth2.client.registration.google.client-id:}")
    private String googleClientId;

    @Value("${app.api-base-url:http://localhost:8081}")
    private String apiBaseUrl;

    @Autowired
    public OAuth2Controller(OAuth2SessionService sessionService, PKCEService pkceService) {
        this.sessionService = sessionService;
        this.pkceService = pkceService;
    }

    /**
     * Initiate OAuth2 authorization flow with PKCE
     * GET /oauth2/authorize/{provider}
     */
    @GetMapping("/authorize/{provider}")
    public ResponseEntity<Map<String, String>> initiateOAuth2Flow(
            @PathVariable String provider, 
            HttpServletResponse response) {
        
        System.out.println("=== NEW PKCE OAUTH2 FLOW INITIATED ===");
        System.out.println("Provider: " + provider);
        System.out.println("Session service available: " + (sessionService != null));
        System.out.println("PKCE service available: " + (pkceService != null));
        
        try {
            // Generate PKCE parameters
            PKCEService.PKCEParams pkceParams = pkceService.generatePKCEParams();
            
            // Create session to track OAuth2 flow
            String sessionId = sessionService.createSession(
                pkceParams.getState(), 
                pkceParams.getCodeVerifier(), 
                provider
            );

            // Set session cookie
            Cookie sessionCookie = new Cookie("oauth2-session", sessionId);
            sessionCookie.setHttpOnly(true);
            sessionCookie.setSecure(true); 
            sessionCookie.setPath("/");
            sessionCookie.setMaxAge(10 * 60); // 10 minutes
            // Remove domain setting - let browser handle it automatically
            response.addCookie(sessionCookie);

            // Build OAuth2 authorization URL
            String authorizationUrl = buildAuthorizationUrl(provider, pkceParams);
            
            Map<String, String> result = new HashMap<>();
            result.put("authorizationUrl", authorizationUrl);
            result.put("state", pkceParams.getState());
            
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to initiate OAuth2 flow: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Exchange session for authentication data
     * POST /oauth2/session/exchange
     */
    @PostMapping("/session/exchange")
    public ResponseEntity<Map<String, Object>> exchangeSession(
            @CookieValue(value = "oauth2-session", required = false) String sessionId,
            HttpServletResponse response) {
        
        if (sessionId == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "No session found");
            return ResponseEntity.badRequest().body(error);
        }

        // Exchange session for auth data (one-time use)
        OAuth2SessionService.OAuth2SessionData sessionData = sessionService.exchangeSession(sessionId);
        
        if (sessionData == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Invalid or expired session");
            return ResponseEntity.badRequest().body(error);
        }

        // Set HTTP-only authentication cookie
        Cookie authCookie = new Cookie("auth-token", sessionData.getToken());
        authCookie.setHttpOnly(true);
        authCookie.setSecure(true);
        authCookie.setPath("/");
        authCookie.setMaxAge(24 * 60 * 60); // 24 hours
        // Note: SameSite=Lax is set by browser default for secure cookies
        response.addCookie(authCookie);

        // Clear session cookie
        Cookie clearSessionCookie = new Cookie("oauth2-session", "");
        clearSessionCookie.setHttpOnly(true);
        clearSessionCookie.setPath("/");
        clearSessionCookie.setMaxAge(0);
        response.addCookie(clearSessionCookie);

        // Return user data (safe to send to frontend)
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("user", sessionData.getUserData());
        
        return ResponseEntity.ok(result);
    }

    /**
     * Build provider-specific authorization URL
     */
    private String buildAuthorizationUrl(String provider, PKCEService.PKCEParams pkceParams) {
        String redirectUri = apiBaseUrl + "/login/oauth2/code/" + provider;
        
        switch (provider.toLowerCase()) {
            case "google":
                return UriComponentsBuilder
                    .fromUriString("https://accounts.google.com/o/oauth2/v2/auth")
                    .queryParam("client_id", googleClientId)
                    .queryParam("redirect_uri", redirectUri)
                    .queryParam("scope", "openid email profile")
                    .queryParam("response_type", "code")
                    .queryParam("state", pkceParams.getState())
                    .queryParam("code_challenge", pkceParams.getCodeChallenge())
                    .queryParam("code_challenge_method", "S256")
                    .build().toUriString();
                    
            default:
                throw new IllegalArgumentException("Unsupported OAuth2 provider: " + provider);
        }
    }
}