package com.movietracker.api.service;

import com.movietracker.api.dto.AuthPayload;
import com.movietracker.api.dto.LoginInput;
import com.movietracker.api.dto.RegisterInput;
import com.movietracker.api.entity.User;
import com.movietracker.api.exception.AuthenticationException;
import com.movietracker.api.repository.UserRepository;
import com.netflix.graphql.types.errors.ErrorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service responsible for user authentication operations including registration and login.
 * 
 * <p>This service handles:
 * <ul>
 *   <li>User registration with validation of unique email/username</li>
 *   <li>User authentication with credential verification</li>
 *   <li>JWT token generation for authenticated users</li>
 *   <li>Password security using BCrypt hashing</li>
 * </ul>
 * 
 * <p>All methods are transactional to ensure data consistency.
 * 
 * @author Movie Tracker API Team
 * @since 1.0.0
 */
@Service
@Transactional
public class AuthenticationService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    
    @Autowired
    public AuthenticationService(UserRepository userRepository, 
                               PasswordEncoder passwordEncoder,
                               JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }
    
    /**
     * Register a new user account.
     * 
     * <p>This method:
     * <ul>
     *   <li>Validates email and username uniqueness</li>
     *   <li>Securely hashes the password using BCrypt</li>
     *   <li>Creates a new active user account</li>
     *   <li>Generates a JWT token for immediate authentication</li>
     * </ul>
     * 
     * @param input the registration input containing user details
     * @return an AuthPayload containing the JWT token and user information
     * @throws AuthenticationException if email or username already exists
     */
    public AuthPayload register(RegisterInput input) {
        // Validate email uniqueness
        if (userRepository.existsByEmail(input.getEmail())) {
            throw new AuthenticationException(
                "Email already exists", 
                ErrorType.BAD_REQUEST
            );
        }
        
        // Validate username uniqueness
        if (userRepository.existsByUsername(input.getUsername())) {
            throw new AuthenticationException(
                "Username already exists", 
                ErrorType.BAD_REQUEST
            );
        }
        
        // Create new user
        User user = new User();
        user.setEmail(input.getEmail());
        user.setUsername(input.getUsername());
        user.setPasswordHash(passwordEncoder.encode(input.getPassword()));
        user.setFirstName(input.getFirstName());
        user.setLastName(input.getLastName());
        user.setIsActive(true);
        
        // Save user
        User savedUser = userRepository.save(user);
        
        // Generate token
        String token = jwtService.generateToken(savedUser);
        
        return new AuthPayload(token, savedUser);
    }
    
    /**
     * Authenticate user login
     */
    public AuthPayload login(LoginInput input) {
        // Find user by email
        User user = userRepository.findByEmail(input.getEmail())
            .orElseThrow(() -> new AuthenticationException("Invalid credentials"));
        
        // Check if user is active
        if (!user.getIsActive()) {
            throw new AuthenticationException("Account is inactive");
        }
        
        // Verify password
        if (!passwordEncoder.matches(input.getPassword(), user.getPasswordHash())) {
            throw new AuthenticationException("Invalid credentials");
        }
        
        // Update last login (optional for now)
        // user.setLastLogin(LocalDateTime.now());
        // userRepository.save(user);
        
        // Generate token
        String token = jwtService.generateToken(user);
        
        return new AuthPayload(token, user);
    }
}
