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
        
        // Create secure HTTP-only cookies for sensitive data
        Cookie tokenCookie = new Cookie("auth-token", token);
        tokenCookie.setHttpOnly(true);
        tokenCookie.setSecure(true); // HTTPS only
        tokenCookie.setPath("/");
        tokenCookie.setMaxAge(24 * 60 * 60); // 24 hours
        // Note: SameSite=Lax is set by browser default for secure cookies
        
        String userJson = String.format("{\"id\":\"%s\",\"email\":\"%s\",\"username\":\"%s\"}", 
                user.getId(), user.getEmail(), user.getUsername());
        String encodedUserJson = URLEncoder.encode(userJson, StandardCharsets.UTF_8);
        
        Cookie userCookie = new Cookie("auth-user", encodedUserJson);
        userCookie.setHttpOnly(false); // Frontend needs to read this
        userCookie.setSecure(true); // HTTPS only
        userCookie.setPath("/");
        userCookie.setMaxAge(24 * 60 * 60); // 24 hours
        // Note: SameSite=Lax is set by browser default for secure cookies
        
        response.addCookie(tokenCookie);
        response.addCookie(userCookie);
        
        // Redirect to frontend callback without sensitive data in URL
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("success", "true")
                .build().toUriString();
        
        // Debug logging
        System.out.println("OAuth2 Success - Redirecting to: " + targetUrl);
        System.out.println("OAuth2 Success - User: " + user.getEmail());
        System.out.println("OAuth2 Success - Registration ID: " + registrationId);
        
        // Clear any existing response content and redirect
        response.reset();
        response.addCookie(tokenCookie); // Re-add cookies after reset
        response.addCookie(userCookie);
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