package com.movietracker.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(unique = true, nullable = false)
    @Email
    @NotBlank
    private String email;
    
    @Column(unique = true, nullable = false)
    @NotBlank
    @Size(min = 3, max = 50)
    private String username;
    
    @Column(nullable = true)  // OAuth users may not have password
    private String passwordHash;
    
    private String firstName;
    private String lastName;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    private LocalDateTime lastLogin;
    
    @Column(nullable = false)
    private Boolean isActive = true;
    
    // OAuth2 fields
    private String provider; // google, github, etc.
    private String providerId; // OAuth2 provider user ID
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserMovie> userMovies = new ArrayList<>();
    
    // Constructors
    public User() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public User(String email, String username, String passwordHash) {
        this();
        this.email = email;
        this.username = username;
        this.passwordHash = passwordHash;
    }
    
    // Constructor for OAuth2 users
    public User(String email, String username, String provider, String providerId) {
        this();
        this.email = email;
        this.username = username;
        this.provider = provider;
        this.providerId = providerId;
        this.passwordHash = null; // OAuth users don't have passwords
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    
    public String getProviderId() { return providerId; }
    public void setProviderId(String providerId) { this.providerId = providerId; }
    
    public List<UserMovie> getUserMovies() { return userMovies; }
    public void setUserMovies(List<UserMovie> userMovies) { this.userMovies = userMovies; }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
