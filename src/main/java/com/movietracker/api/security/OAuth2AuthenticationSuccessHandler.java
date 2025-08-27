package com.movietracker.api.security;

import com.movietracker.api.entity.User;
import com.movietracker.api.service.AuthenticationService;
import com.movietracker.api.service.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@ConditionalOnProperty(name = "app.auth.oauth2-enabled", havingValue = "true")
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthenticationService authenticationService;
    private final JwtService jwtService;
    
    @Value("${app.oauth2.redirect-uri:http://localhost:3001/auth/callback}")
    private String redirectUri;

    @Autowired
    public OAuth2AuthenticationSuccessHandler(
            @Lazy AuthenticationService authenticationService,
            @Lazy JwtService jwtService) {
        this.authenticationService = authenticationService;
        this.jwtService = jwtService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                       Authentication authentication) throws IOException {
        
        System.out.println("=== OAUTH2 SUCCESS HANDLER V1 (LEGACY) CALLED ===");
        System.out.println("This means V2 handler is not being used or session cookie missing");
        
        try {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            
            // Get registration ID from the OAuth2AuthenticationToken
            String registrationId = "unknown";
            if (authentication instanceof OAuth2AuthenticationToken) {
                OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
                registrationId = oauthToken.getAuthorizedClientRegistrationId();
            }
            
            System.out.println("OAuth2 Success Handler - Processing authentication for: " + registrationId);
            
            // Process OAuth2 user and get or create user in our system
            User user = authenticationService.processOAuth2User(oAuth2User, registrationId);
        
        // Generate JWT token
        String token = jwtService.generateToken(user);
        
        // RAILWAY FIX: Pass auth data via URL parameters instead of cookies
        // Cross-domain cookies don't work between api.railway.app and web.railway.app subdomains
        String userJson = String.format("{\"id\":\"%s\",\"email\":\"%s\",\"username\":\"%s\"}", 
                user.getId(), user.getEmail(), user.getUsername());
        String encodedUserJson = URLEncoder.encode(userJson, StandardCharsets.UTF_8);
        String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);
        
        System.out.println("OAuth2 V1 Railway Fix: Passing auth data via URL parameters");
        System.out.println("Token length: " + token.length());
        System.out.println("User data: " + userJson);
        
        // Redirect to frontend callback with auth data in URL parameters (Railway fix)
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("success", "true")
                .queryParam("token", encodedToken)
                .queryParam("user", encodedUserJson)
                .build().toUriString();
        
        // Debug logging
        System.out.println("OAuth2 Success - Redirecting to: " + targetUrl);
        System.out.println("OAuth2 Success - User: " + user.getEmail());
        System.out.println("OAuth2 Success - Registration ID: " + registrationId);
        
        // Clear any existing response content and redirect
        response.reset();
        response.setStatus(HttpServletResponse.SC_FOUND);
        response.setHeader("Location", targetUrl);
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.flushBuffer();
        
        } catch (Exception e) {
            System.err.println("OAuth2 authentication error: " + e.getMessage());
            e.printStackTrace();
            
            // Redirect to frontend with error
            String errorUrl = UriComponentsBuilder.fromUriString(redirectUri)
                    .queryParam("success", "false")
                    .queryParam("error", URLEncoder.encode("Authentication processing failed: " + e.getMessage(), StandardCharsets.UTF_8))
                    .build().toUriString();
            
            response.sendRedirect(errorUrl);
        }
    }
}