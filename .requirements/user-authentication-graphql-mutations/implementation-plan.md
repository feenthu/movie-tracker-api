# Implementation Plan: User Authentication GraphQL Mutations

## Overview

Implement user authentication for the movie tracker API by creating JWT-based authentication services, GraphQL data fetchers for register/login mutations, and security integration. The implementation follows Spring Security best practices with Netflix DGS GraphQL framework.

## Prerequisites

- Existing User entity and UserRepository
- Spring Security and JWT dependencies (already in build.gradle)
- Netflix DGS GraphQL framework setup
- BCrypt password encoder configuration (already exists)

## Technical Feasibility Analysis

### Existing Patterns

- **Entity/Repository Pattern**: User entity and UserRepository already exist with required fields
- **GraphQL Data Fetchers**: HealthDataFetcher shows the pattern for implementing GraphQL resolvers
- **Spring Security Configuration**: SecurityConfig exists and can be extended for JWT integration
- **Password Encoding**: BCryptPasswordEncoder is already configured

### Technical Challenges

- **JWT Integration**: Need to integrate JWT token validation with Spring Security filter chain
- **GraphQL Security Context**: Need to make authenticated user available to GraphQL resolvers
- **Error Handling**: Need consistent error responses for authentication failures
- **Feature Toggle Implementation**: Need configuration-based authentication method toggles

## Component Changes

### New Components

- `JwtService`: JWT token generation, validation, and parsing
- `AuthConfig`: Configuration properties for authentication features and JWT settings
- `AuthenticationDataFetcher`: GraphQL data fetcher for register/login mutations
- `UserDataFetcher`: GraphQL data fetcher for me query and user operations
- `AuthenticationService`: Business logic for user registration and login
- `JwtAuthenticationFilter`: Custom filter for JWT token validation
- `AuthenticationException`: Custom exceptions for authentication errors

### Modified Components

- `SecurityConfig`: Add JWT authentication filter and configure security rules
- `application.yml`: Add JWT configuration properties and feature toggles

## Database Changes

No database schema changes required. All necessary fields exist in the User entity.

## Implementation Steps

### Step 1: Create Authentication Configuration

Create configuration properties for feature toggles and JWT settings.

**Files to create/modify**:
- `src/main/java/com/movietracker/api/config/AuthConfig.java`: Feature toggle configuration
- `src/main/resources/application.yml`: Add authentication and JWT configuration

**Implementation details**:
```java
@ConfigurationProperties(prefix = "app.auth")
public class AuthConfig {
    private boolean localAuthEnabled = true;
    private boolean oauth2Enabled = false;
    // JWT configuration properties
}
```

### Step 2: Implement JWT Service

Create service for JWT token operations including generation, validation, and parsing.

**Files to create/modify**:
- `src/main/java/com/movietracker/api/service/JwtService.java`: JWT token operations

**Implementation details**:
```java
@Service
public class JwtService {
    public String generateToken(User user) { /* implementation */ }
    public boolean validateToken(String token) { /* implementation */ }
    public String extractUsername(String token) { /* implementation */ }
}
```

### Step 3: Create Authentication Service

Implement business logic for user registration and authentication.

**Files to create/modify**:
- `src/main/java/com/movietracker/api/service/AuthenticationService.java`: Authentication business logic
- `src/main/java/com/movietracker/api/exception/AuthenticationException.java`: Custom authentication exceptions

**Implementation details**:
```java
@Service
public class AuthenticationService {
    public AuthPayload register(RegisterInput input) { /* implementation */ }
    public AuthPayload login(LoginInput input) { /* implementation */ }
}
```

### Step 4: Create GraphQL Data Transfer Objects

Create DTOs for GraphQL input and output types.

**Files to create/modify**:
- `src/main/java/com/movietracker/api/dto/RegisterInput.java`: Registration input DTO
- `src/main/java/com/movietracker/api/dto/LoginInput.java`: Login input DTO  
- `src/main/java/com/movietracker/api/dto/AuthPayload.java`: Authentication response DTO

**Implementation details**:
```java
public class RegisterInput {
    private String email;
    private String username;
    private String password;
    // other fields and validation annotations
}
```

### Step 5: Implement Authentication Data Fetcher

Create GraphQL data fetcher for authentication mutations.

**Files to create/modify**:
- `src/main/java/com/movietracker/api/datafetcher/AuthenticationDataFetcher.java`: GraphQL mutations for auth

**Implementation details**:
```java
@DgsComponent
public class AuthenticationDataFetcher {
    @DgsMutation
    public AuthPayload register(@InputArgument RegisterInput input) { /* implementation */ }
    
    @DgsMutation  
    public AuthPayload login(@InputArgument LoginInput input) { /* implementation */ }
}
```

### Step 6: Implement User Data Fetcher

Create GraphQL data fetcher for user queries including the 'me' query.

**Files to create/modify**:
- `src/main/java/com/movietracker/api/datafetcher/UserDataFetcher.java`: User-related GraphQL operations

**Implementation details**:
```java
@DgsComponent
public class UserDataFetcher {
    @DgsQuery
    public User me(DgsRequestData requestData) { /* implementation */ }
}
```

### Step 7: Create JWT Authentication Filter

Implement Spring Security filter for JWT token validation.

**Files to create/modify**:
- `src/main/java/com/movietracker/api/security/JwtAuthenticationFilter.java`: JWT validation filter

**Implementation details**:
```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) { /* implementation */ }
}
```

### Step 8: Update Security Configuration

Integrate JWT filter with Spring Security configuration and update security rules.

**Files to create/modify**:
- `src/main/java/com/movietracker/api/config/SecurityConfig.java`: Add JWT filter and configure rules

**Implementation details**:
```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        // Configure authentication rules for GraphQL operations
}
```

### Step 9: Add Security Context Integration

Ensure authenticated user is available throughout the application context.

**Files to create/modify**:
- `src/main/java/com/movietracker/api/security/SecurityContextHelper.java`: Helper for accessing authenticated user

**Implementation details**:
```java
@Component
public class SecurityContextHelper {
    public Optional<User> getCurrentUser() { /* implementation */ }
}
```

### Step 10: Implement Error Handling

Create comprehensive error handling for authentication scenarios.

**Files to create/modify**:
- `src/main/java/com/movietracker/api/exception/GraphQLExceptionHandler.java`: GraphQL error handling
- Update existing exception classes for authentication errors

## Integration Points

### Spring Security Integration
- JWT filter integrates with Spring Security filter chain
- Authentication context propagated to GraphQL resolvers
- Security rules applied to GraphQL endpoints

### GraphQL Integration  
- Data fetchers use DGS annotations for GraphQL binding
- Input validation integrated with GraphQL schema
- Error responses follow GraphQL error format

### Database Integration
- Uses existing UserRepository for database operations
- Leverages existing User entity without schema changes
- Maintains existing database constraints and indexes

### Configuration Integration
- Feature toggles integrate with Spring Boot configuration
- JWT settings configurable via application.yml
- Ready for external feature flag integration (future)

### Existing Security Components
- Builds upon existing BCryptPasswordEncoder
- Extends current SecurityConfig without breaking existing setup
- Maintains existing CORS configuration