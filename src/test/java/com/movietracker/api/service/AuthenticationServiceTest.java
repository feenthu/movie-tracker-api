package com.movietracker.api.service;

import com.movietracker.api.dto.AuthPayload;
import com.movietracker.api.dto.LoginInput;
import com.movietracker.api.dto.RegisterInput;
import com.movietracker.api.entity.User;
import com.movietracker.api.exception.AuthenticationException;
import com.movietracker.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private JwtService jwtService;
    
    private AuthenticationService authenticationService;
    
    @BeforeEach
    void setUp() {
        authenticationService = new AuthenticationService(
            userRepository,
            passwordEncoder,
            jwtService
        );
    }
    
    @Test
    void register_WithValidInput_ShouldReturnAuthPayload() {
        // Given
        RegisterInput input = new RegisterInput();
        input.setEmail("test@example.com");
        input.setUsername("testuser");
        input.setPassword("password123");
        input.setFirstName("Test");
        input.setLastName("User");
        
        User savedUser = createTestUser();
        
        when(userRepository.existsByEmail(input.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(input.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(input.getPassword())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(savedUser)).thenReturn("test.jwt.token");
        
        // When
        AuthPayload result = authenticationService.register(input);
        
        // Then
        assertNotNull(result);
        assertEquals("test.jwt.token", result.getToken());
        assertEquals(savedUser, result.getUser());
        
        verify(userRepository).existsByEmail(input.getEmail());
        verify(userRepository).existsByUsername(input.getUsername());
        verify(passwordEncoder).encode(input.getPassword());
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(savedUser);
    }
    
    @Test
    void register_WithDuplicateEmail_ShouldThrowException() {
        // Given
        RegisterInput input = new RegisterInput();
        input.setEmail("existing@example.com");
        input.setUsername("newuser");
        input.setPassword("password123");
        
        when(userRepository.existsByEmail(input.getEmail())).thenReturn(true);
        
        // When & Then
        AuthenticationException exception = assertThrows(
            AuthenticationException.class,
            () -> authenticationService.register(input)
        );
        
        assertEquals("Email already exists", exception.getMessage());
        verify(userRepository).existsByEmail(input.getEmail());
        verify(userRepository, never()).existsByUsername(anyString());
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    void register_WithDuplicateUsername_ShouldThrowException() {
        // Given
        RegisterInput input = new RegisterInput();
        input.setEmail("new@example.com");
        input.setUsername("existinguser");
        input.setPassword("password123");
        
        when(userRepository.existsByEmail(input.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(input.getUsername())).thenReturn(true);
        
        // When & Then
        AuthenticationException exception = assertThrows(
            AuthenticationException.class,
            () -> authenticationService.register(input)
        );
        
        assertEquals("Username already exists", exception.getMessage());
        verify(userRepository).existsByEmail(input.getEmail());
        verify(userRepository).existsByUsername(input.getUsername());
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    void login_WithValidCredentials_ShouldReturnAuthPayload() {
        // Given
        LoginInput input = new LoginInput();
        input.setEmail("test@example.com");
        input.setPassword("password123");
        
        User existingUser = createTestUser();
        
        when(userRepository.findByEmail(input.getEmail())).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(input.getPassword(), existingUser.getPasswordHash())).thenReturn(true);
        when(jwtService.generateToken(existingUser)).thenReturn("test.jwt.token");
        
        // When
        AuthPayload result = authenticationService.login(input);
        
        // Then
        assertNotNull(result);
        assertEquals("test.jwt.token", result.getToken());
        assertEquals(existingUser, result.getUser());
        
        verify(userRepository).findByEmail(input.getEmail());
        verify(passwordEncoder).matches(input.getPassword(), existingUser.getPasswordHash());
        verify(jwtService).generateToken(existingUser);
    }
    
    @Test
    void login_WithNonExistentEmail_ShouldThrowException() {
        // Given
        LoginInput input = new LoginInput();
        input.setEmail("nonexistent@example.com");
        input.setPassword("password123");
        
        when(userRepository.findByEmail(input.getEmail())).thenReturn(Optional.empty());
        
        // When & Then
        AuthenticationException exception = assertThrows(
            AuthenticationException.class,
            () -> authenticationService.login(input)
        );
        
        assertEquals("Invalid credentials", exception.getMessage());
        verify(userRepository).findByEmail(input.getEmail());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }
    
    @Test
    void login_WithInvalidPassword_ShouldThrowException() {
        // Given
        LoginInput input = new LoginInput();
        input.setEmail("test@example.com");
        input.setPassword("wrongpassword");
        
        User existingUser = createTestUser();
        
        when(userRepository.findByEmail(input.getEmail())).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(input.getPassword(), existingUser.getPasswordHash())).thenReturn(false);
        
        // When & Then
        AuthenticationException exception = assertThrows(
            AuthenticationException.class,
            () -> authenticationService.login(input)
        );
        
        assertEquals("Invalid credentials", exception.getMessage());
        verify(userRepository).findByEmail(input.getEmail());
        verify(passwordEncoder).matches(input.getPassword(), existingUser.getPasswordHash());
        verify(jwtService, never()).generateToken(any(User.class));
    }
    
    @Test
    void login_WithInactiveUser_ShouldThrowException() {
        // Given
        LoginInput input = new LoginInput();
        input.setEmail("test@example.com");
        input.setPassword("password123");
        
        User inactiveUser = createTestUser();
        inactiveUser.setIsActive(false);
        
        when(userRepository.findByEmail(input.getEmail())).thenReturn(Optional.of(inactiveUser));
        
        // When & Then
        AuthenticationException exception = assertThrows(
            AuthenticationException.class,
            () -> authenticationService.login(input)
        );
        
        assertEquals("Account is inactive", exception.getMessage());
        verify(userRepository).findByEmail(input.getEmail());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }
    
    private User createTestUser() {
        User user = new User();
        user.setId("test-user-id");
        user.setEmail("test@example.com");
        user.setUsername("testuser");
        user.setPasswordHash("hashedPassword");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setIsActive(true);
        return user;
    }
}
