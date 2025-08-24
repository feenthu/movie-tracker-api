package com.movietracker.api.security;

import com.movietracker.api.entity.User;
import com.movietracker.api.service.AuthenticationService;
import com.movietracker.api.service.JwtService;
import com.movietracker.api.service.OAuth2SessionService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Component("oauth2AuthenticationSuccessHandlerV2")
@ConditionalOnProperty(name = "app.auth.oauth2-enabled", havingValue = "true")
public class OAuth2AuthenticationSuccessHandlerV2 implements AuthenticationSuccessHandler {

    private final AuthenticationService authenticationService;
    private final JwtService jwtService;
    private final OAuth2SessionService sessionService;
    
    @Value("${app.oauth2.redirect-uri:https://movie-tracker-web-production.up.railway.app/auth/callback}")
    private String redirectUri;

    @Autowired
    public OAuth2AuthenticationSuccessHandlerV2(
            @Lazy AuthenticationService authenticationService,
            @Lazy JwtService jwtService,
            OAuth2SessionService sessionService) {
        this.authenticationService = authenticationService;
        this.jwtService = jwtService;
        this.sessionService = sessionService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                       Authentication authentication) throws IOException {
        
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        
        // Get registration ID from the OAuth2AuthenticationToken
        String registrationId = "unknown";
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            registrationId = oauthToken.getAuthorizedClientRegistrationId();
        }

        try {
            // Get session ID from cookie
            String sessionId = getSessionIdFromCookies(request);
            
            if (sessionId == null) {
                // Fallback to old behavior for backward compatibility
                handleLegacyFlow(request, response, oAuth2User, registrationId);
                return;
            }

            // Validate session exists and matches state
            OAuth2SessionService.OAuth2SessionData sessionData = sessionService.getSession(sessionId);
            if (sessionData == null) {
                throw new IllegalStateException("Invalid or expired OAuth2 session");
            }

            // Verify state parameter for CSRF protection
            String stateParam = request.getParameter("state");
            if (!sessionData.getState().equals(stateParam)) {
                throw new IllegalStateException("State parameter mismatch - possible CSRF attack");
            }

            // Process OAuth2 user and get or create user in our system
            User user = authenticationService.processOAuth2User(oAuth2User, registrationId);
            
            // Generate JWT token
            String token = jwtService.generateToken(user);
            
            // Store authentication result in session
            String userJson = String.format("{\"id\":\"%s\",\"email\":\"%s\",\"username\":\"%s\"}", 
                    user.getId(), user.getEmail(), user.getUsername());
            
            sessionService.storeAuthenticationResult(sessionId, user.getId(), token, userJson);

            // Redirect to frontend callback with clean URL (no sensitive data)
            String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                    .queryParam("success", "true")
                    .queryParam("session", sessionId)
                    .build().toUriString();
            
            System.out.println("OAuth2 Success (V2) - Redirecting to: " + targetUrl);
            System.out.println("OAuth2 Success (V2) - User: " + user.getEmail());
            System.out.println("OAuth2 Success (V2) - Session: " + sessionId);
            
            response.sendRedirect(targetUrl);

        } catch (Exception e) {
            System.err.println("OAuth2 authentication error: " + e.getMessage());
            e.printStackTrace();
            
            // Redirect to frontend with error
            String errorUrl = UriComponentsBuilder.fromUriString(redirectUri)
                    .queryParam("success", "false")
                    .queryParam("error", URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8))
                    .build().toUriString();
            
            response.sendRedirect(errorUrl);
        }
    }

    /**
     * Extract session ID from cookies
     */
    private String getSessionIdFromCookies(HttpServletRequest request) {
        if (request.getCookies() != null) {
            return Arrays.stream(request.getCookies())
                .filter(cookie -> "oauth2-session".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
        }
        return null;
    }

    /**
     * Fallback to legacy URL parameter behavior for backward compatibility
     */
    private void handleLegacyFlow(HttpServletRequest request, HttpServletResponse response,
                                 OAuth2User oAuth2User, String registrationId) throws IOException {
        
        System.out.println("OAuth2 Success - Using legacy flow (no session found)");
        
        // Process OAuth2 user and get or create user in our system
        User user = authenticationService.processOAuth2User(oAuth2User, registrationId);
        
        // Generate JWT token
        String token = jwtService.generateToken(user);
        
        // Create HTTP-only cookies for sensitive data (legacy approach)
        Cookie tokenCookie = new Cookie("auth-token", token);
        tokenCookie.setHttpOnly(true);
        tokenCookie.setSecure(true);
        tokenCookie.setPath("/");
        tokenCookie.setMaxAge(24 * 60 * 60);
        tokenCookie.setSameSite(Cookie.SameSite.LAX);
        
        String userJson = String.format("{\"id\":\"%s\",\"email\":\"%s\",\"username\":\"%s\"}", 
                user.getId(), user.getEmail(), user.getUsername());
        String encodedUserJson = URLEncoder.encode(userJson, StandardCharsets.UTF_8);
        
        Cookie userCookie = new Cookie("auth-user", encodedUserJson);
        userCookie.setHttpOnly(false);
        userCookie.setSecure(true);
        userCookie.setPath("/");
        userCookie.setMaxAge(24 * 60 * 60);
        userCookie.setSameSite(Cookie.SameSite.LAX);
        
        response.addCookie(tokenCookie);
        response.addCookie(userCookie);
        
        // Redirect to frontend callback without sensitive data in URL
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("success", "true")
                .build().toUriString();
        
        response.sendRedirect(targetUrl);
    }
}