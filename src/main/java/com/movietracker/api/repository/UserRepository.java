package com.movietracker.api.repository;

import com.movietracker.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    
    // OAuth2 integration
    Optional<User> findByProviderAndProviderId(String provider, String providerId);
    
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}
