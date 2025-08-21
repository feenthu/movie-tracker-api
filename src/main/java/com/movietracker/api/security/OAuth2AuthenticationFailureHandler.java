package com.movietracker.api.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Value("${app.oauth2.redirect-uri:http://localhost:3001/auth/callback}")
    private String redirectUri;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                       AuthenticationException exception) throws IOException {
        
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("success", "false")
                .queryParam("error", exception.getLocalizedMessage())
                .build().toUriString();
                
        response.sendRedirect(targetUrl);
    }
}