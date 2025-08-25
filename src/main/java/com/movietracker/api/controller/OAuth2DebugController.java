package com.movietracker.api.controller;

import com.movietracker.api.service.OAuth2SessionService;
import com.movietracker.api.service.PKCEService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/debug/oauth2")
@CrossOrigin(origins = {"http://localhost:3001", "https://movie-tracker-web-production.up.railway.app"}, allowCredentials = "true")
public class OAuth2DebugController {

    private final OAuth2SessionService sessionService;
    private final PKCEService pkceService;

    @Value("${app.auth.oauth2-enabled:false}")
    private boolean oauth2Enabled;

    @Value("${spring.security.oauth2.client.registration.google.client-id:}")
    private String googleClientId;

    @Value("${app.api-base-url:http://localhost:8081}")
    private String apiBaseUrl;

    @Autowired(required = false)
    public OAuth2DebugController(OAuth2SessionService sessionService, PKCEService pkceService) {
        this.sessionService = sessionService;
        this.pkceService = pkceService;
    }

    /**
     * Debug endpoint to check OAuth2 configuration status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getOAuth2Status() {
        Map<String, Object> status = new HashMap<>();
        
        status.put("oauth2Enabled", oauth2Enabled);
        status.put("hasGoogleClientId", !googleClientId.isEmpty());
        status.put("googleClientId", googleClientId.isEmpty() ? "NOT_SET" : googleClientId.substring(0, Math.min(10, googleClientId.length())) + "...");
        status.put("apiBaseUrl", apiBaseUrl);
        status.put("sessionServiceAvailable", sessionService != null);
        status.put("pkceServiceAvailable", pkceService != null);
        
        // Check which endpoints are available
        status.put("newPkceEndpoint", apiBaseUrl + "/oauth2/authorize/google");
        status.put("oldSpringEndpoint", apiBaseUrl + "/oauth2/authorization/google");
        
        return ResponseEntity.ok(status);
    }

    /**
     * Test the new PKCE endpoint directly
     */
    @GetMapping("/test-pkce/{provider}")
    public ResponseEntity<Map<String, Object>> testPkceEndpoint(@PathVariable String provider) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (sessionService == null) {
                result.put("error", "OAuth2SessionService not available");
                return ResponseEntity.badRequest().body(result);
            }
            
            if (pkceService == null) {
                result.put("error", "PKCEService not available");
                return ResponseEntity.badRequest().body(result);
            }
            
            // Generate PKCE parameters
            PKCEService.PKCEParams pkceParams = pkceService.generatePKCEParams();
            
            // Create session
            String sessionId = sessionService.createSession(
                pkceParams.getState(), 
                pkceParams.getCodeVerifier(), 
                provider
            );
            
            result.put("success", true);
            result.put("sessionId", sessionId);
            result.put("state", pkceParams.getState());
            result.put("codeChallenge", pkceParams.getCodeChallenge());
            result.put("message", "PKCE flow components are working correctly");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            result.put("error", "PKCE test failed: " + e.getMessage());
            result.put("stackTrace", e.getClass().getSimpleName());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Check what GraphQL OAuth2DataFetcher returns
     */
    @GetMapping("/graphql-endpoint-check")
    public ResponseEntity<Map<String, Object>> checkGraphQLEndpoint() {
        Map<String, Object> result = new HashMap<>();
        
        // Simulate what OAuth2DataFetcher returns
        String loginUrl = apiBaseUrl + "/oauth2/authorize/google";
        
        result.put("graphqlReturnsUrl", loginUrl);
        result.put("isNewPkceEndpoint", loginUrl.contains("/oauth2/authorize/"));
        result.put("isOldSpringEndpoint", loginUrl.contains("/oauth2/authorization/"));
        result.put("apiBaseUrl", apiBaseUrl);
        
        return ResponseEntity.ok(result);
    }
}