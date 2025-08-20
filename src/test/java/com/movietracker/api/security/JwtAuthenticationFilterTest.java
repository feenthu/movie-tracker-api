package com.movietracker.api.security;

import com.movietracker.api.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {
    
    @Mock
    private JwtService jwtService;
    
    @Mock
    private HttpServletRequest request;
    
    @Mock
    private HttpServletResponse response;
    
    @Mock
    private FilterChain filterChain;
    
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @BeforeEach
    void setUp() {
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtService);
        SecurityContextHolder.clearContext();
    }
    
    @Test
    void doFilterInternal_WithValidToken_ShouldSetAuthentication() throws Exception {
        // Given
        String token = "valid.jwt.token";
        String userEmail = "test@example.com";
        
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.extractUsername(token)).thenReturn(userEmail);
        when(jwtService.validateToken(token)).thenReturn(true);
        
        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals(userEmail, authentication.getName());
        assertTrue(authentication.isAuthenticated());
        
        verify(filterChain).doFilter(request, response);
        verify(jwtService).extractUsername(token);
        verify(jwtService).validateToken(token);
    }
    
    @Test
    void doFilterInternal_WithInvalidToken_ShouldNotSetAuthentication() throws Exception {
        // Given
        String token = "invalid.jwt.token";
        
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.extractUsername(token)).thenReturn("test@example.com");
        when(jwtService.validateToken(token)).thenReturn(false);
        
        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);
        
        verify(filterChain).doFilter(request, response);
        verify(jwtService).extractUsername(token);
        verify(jwtService).validateToken(token);
    }
    
    @Test
    void doFilterInternal_WithoutAuthHeader_ShouldNotSetAuthentication() throws Exception {
        // Given
        when(request.getHeader("Authorization")).thenReturn(null);
        
        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);
        
        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).extractUsername(anyString());
        verify(jwtService, never()).validateToken(anyString());
    }
    
    @Test
    void doFilterInternal_WithInvalidAuthHeader_ShouldNotSetAuthentication() throws Exception {
        // Given
        when(request.getHeader("Authorization")).thenReturn("InvalidFormat token");
        
        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);
        
        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).extractUsername(anyString());
        verify(jwtService, never()).validateToken(anyString());
    }
    
    @Test
    void doFilterInternal_WithExceptionInTokenProcessing_ShouldContinueFilterChain() throws Exception {
        // Given
        String token = "problematic.jwt.token";
        
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.extractUsername(token)).thenThrow(new RuntimeException("Token processing error"));
        
        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);
        
        verify(filterChain).doFilter(request, response);
        verify(jwtService).extractUsername(token);
    }
    
    @Test
    void doFilterInternal_WithExistingAuthentication_ShouldNotOverride() throws Exception {
        // Given
        String token = "valid.jwt.token";
        String userEmail = "test@example.com";
        
        // Set existing authentication
        Authentication existingAuth = mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(existingAuth);
        
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.extractUsername(token)).thenReturn(userEmail);
        
        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertEquals(existingAuth, authentication); // Should remain unchanged
        
        verify(filterChain).doFilter(request, response);
        verify(jwtService).extractUsername(token);
        verify(jwtService, never()).validateToken(anyString());
    }
}
