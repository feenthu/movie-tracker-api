package com.movietracker.api.service;

import com.movietracker.api.dto.AuthPayload;
import com.movietracker.api.dto.LoginInput;
import com.movietracker.api.dto.RegisterInput;
import com.movietracker.api.entity.User;
import com.movietracker.api.exception.AuthenticationException;
import com.movietracker.api.repository.UserRepository;
import com.movietracker.api.security.OAuth2UserInfo;
import com.movietracker.api.security.OAuth2UserInfoFactory;
import com.netflix.graphql.types.errors.ErrorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

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
    
    /**
     * Process OAuth2 user authentication and registration.
     * 
     * <p>This method:
     * <ul>
     *   <li>Extracts user information from OAuth2 provider</li>
     *   <li>Finds existing user or creates new account</li>
     *   <li>Updates user information from OAuth2 provider</li>
     *   <li>Returns the user for JWT token generation</li>
     * </ul>
     * 
     * @param oAuth2User the OAuth2 user from provider
     * @param registrationId the OAuth2 provider ID (google, facebook, apple)
     * @return the User entity (existing or newly created)
     * @throws AuthenticationException if OAuth2 processing fails
     */
    public User processOAuth2User(OAuth2User oAuth2User, String registrationId) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        
        // Extract common OAuth2 attributes based on provider
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, attributes);
        
        if (userInfo.getEmail() == null || userInfo.getEmail().isEmpty()) {
            throw new AuthenticationException("Email not found from OAuth2 provider");
        }
        
        // Find existing user by email
        Optional<User> existingUser = userRepository.findByEmail(userInfo.getEmail());
        
        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
            // Update user with OAuth2 info if needed
            user = updateExistingUser(user, userInfo, registrationId);
        } else {
            // Create new user from OAuth2 info
            user = createUserFromOAuth2(userInfo, registrationId);
        }
        
        // Update last login
        user.setLastLogin(LocalDateTime.now());
        
        return userRepository.save(user);
    }
    
    /**
     * Create a new user from OAuth2 information.
     */
    private User createUserFromOAuth2(OAuth2UserInfo userInfo, String provider) {
        User user = new User();
        user.setEmail(userInfo.getEmail());
        user.setFirstName(userInfo.getFirstName());
        user.setLastName(userInfo.getLastName());
        user.setProvider(provider);
        user.setProviderId(userInfo.getId());
        user.setIsActive(true);
        
        // Generate a unique username from email if not provided
        String username = generateUsernameFromEmail(userInfo.getEmail());
        user.setUsername(username);
        
        // OAuth2 users don't have a password, set a random one
        user.setPasswordHash(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
        
        return user;
    }
    
    /**
     * Update existing user with OAuth2 information.
     */
    private User updateExistingUser(User user, OAuth2UserInfo userInfo, String provider) {
        // Update provider information if not set
        if (user.getProvider() == null) {
            user.setProvider(provider);
            user.setProviderId(userInfo.getId());
        }
        
        // Update user info if OAuth2 has more recent information
        if (userInfo.getFirstName() != null && !userInfo.getFirstName().isEmpty()) {
            user.setFirstName(userInfo.getFirstName());
        }
        if (userInfo.getLastName() != null && !userInfo.getLastName().isEmpty()) {
            user.setLastName(userInfo.getLastName());
        }
        
        return user;
    }
    
    /**
     * Generate a unique username from email.
     */
    private String generateUsernameFromEmail(String email) {
        String baseUsername = email.substring(0, email.indexOf("@"));
        String username = baseUsername;
        int counter = 1;
        
        while (userRepository.existsByUsername(username)) {
            username = baseUsername + counter;
            counter++;
        }
        
        return username;
    }
}
