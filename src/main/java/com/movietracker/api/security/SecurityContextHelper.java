package com.movietracker.api.security;

import com.movietracker.api.entity.User;
import com.movietracker.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SecurityContextHelper {
    
    private final UserRepository userRepository;
    
    @Autowired
    public SecurityContextHelper(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    /**
     * Get the currently authenticated user
     */
    public Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || 
            "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.empty();
        }
        
        // The principal should be the user's email (username)
        String email = authentication.getName();
        return userRepository.findByEmail(email);
    }
    
    /**
     * Get the currently authenticated user ID
     */
    public Optional<String> getCurrentUserId() {
        return getCurrentUser().map(User::getId);
    }
    
    /**
     * Check if user is authenticated
     */
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && 
               authentication.isAuthenticated() && 
               !"anonymousUser".equals(authentication.getPrincipal());
    }
}
