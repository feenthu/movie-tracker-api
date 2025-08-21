package com.movietracker.api.security;

import com.movietracker.api.entity.User;
import com.movietracker.api.service.AuthenticationService;
import com.movietracker.api.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthenticationService authenticationService;
    private final JwtService jwtService;
    
    @Value("${app.oauth2.redirect-uri:http://localhost:3001/auth/callback}")
    private String redirectUri;

    @Autowired
    public OAuth2AuthenticationSuccessHandler(
            AuthenticationService authenticationService,
            JwtService jwtService) {
        this.authenticationService = authenticationService;
        this.jwtService = jwtService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                       Authentication authentication) throws IOException {
        
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String registrationId = extractRegistrationId(request);
        
        // Process OAuth2 user and get or create user in our system
        User user = authenticationService.processOAuth2User(oAuth2User, registrationId);
        
        // Generate JWT token
        String token = jwtService.generateToken(user);
        
        // Redirect to frontend with token
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("token", token)
                .queryParam("success", "true")
                .build().toUriString();
                
        response.sendRedirect(targetUrl);
    }
    
    private String extractRegistrationId(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        if (requestUri.contains("/oauth2/authorization/")) {
            return requestUri.substring(requestUri.lastIndexOf("/") + 1);
        }
        return "unknown";
    }
}