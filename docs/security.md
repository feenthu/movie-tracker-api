# Security Guide

## Overview

This document outlines the security architecture, best practices, and security considerations for the Movie Tracker API authentication system.

## Security Architecture

### Authentication Flow
```
Client Request → JWT Filter → Spring Security → GraphQL Resolver
     ↓              ↓              ↓               ↓
   Headers    Extract Token   Validate User   Execute Query
                     ↓              ↓
                Parse Claims   Set Security Context
```

### Security Layers

1. **Transport Security**
   - HTTPS recommended for production
   - Secure token transmission
   - CORS configuration for web clients

2. **Authentication Layer**
   - JWT token-based authentication
   - BCrypt password hashing
   - Stateless session management

3. **Authorization Layer**
   - Method-level security annotations
   - Protected GraphQL operations
   - User context propagation

4. **Input Validation**
   - Bean validation annotations
   - GraphQL schema validation
   - SQL injection prevention

## Password Security

### BCrypt Configuration
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(); // Default: 10 rounds
}
```

### Security Features
- **Salt Generation**: Automatic per-password salt
- **Adaptive Hashing**: Configurable work factor
- **Timing Attack Resistance**: Constant-time comparison
- **Rainbow Table Protection**: Unique salts prevent precomputed attacks

### Password Requirements
Current implementation has minimal requirements:
- Minimum 6 characters
- No character composition requirements (configurable for future enhancement)

### Recommended Enhancements
```java
// Future password policy configuration
@ConfigurationProperties(prefix = "app.security.password")
public class PasswordPolicy {
    private int minLength = 8;
    private boolean requireUppercase = true;
    private boolean requireLowercase = true;
    private boolean requireNumbers = true;
    private boolean requireSpecialChars = true;
    private int maxRepeatingChars = 3;
}
```

## JWT Security

### Token Structure
```json
{
  "header": {
    "alg": "HS256",
    "typ": "JWT"
  },
  "payload": {
    "sub": "user@example.com",
    "user_id": "550e8400-e29b-41d4-a716-446655440000",
    "email": "user@example.com",
    "username": "moviefan",
    "iat": 1642608000,
    "exp": 1642611600
  }
}
```

### Security Considerations

#### Algorithm Selection
- **Current**: HS256 (HMAC SHA-256)
- **Rationale**: Symmetric key, simpler key management for single-service architecture
- **Future**: RS256 for microservices architecture

#### Secret Key Management
```yaml
# Current configuration
app:
  auth:
    jwt:
      secret: ${JWT_SECRET:fallback-secret-key}
```

**Production Requirements**:
- Minimum 256 bits (32 characters) for HS256
- Use environment variables or secure key management
- Regular key rotation policy
- Never commit secrets to version control

#### Token Expiration
- **Default**: 1 hour
- **Rationale**: Balance between security and user experience
- **Configurable**: Via application properties

### Token Validation
```java
public boolean validateToken(String token) {
    try {
        Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token);
        return true;
    } catch (JwtException | IllegalArgumentException e) {
        return false;
    }
}
```

## Authorization Matrix

### Public Endpoints
| Operation | Authentication Required | Notes |
|-----------|------------------------|-------|
| `health` | No | System health check |
| `register` | No | Account creation |
| `login` | No | Authentication |

### Protected Endpoints
| Operation | Authentication Required | Authorization Level |
|-----------|------------------------|-------------------|
| `me` | Yes | Own profile only |
| `myMovies` | Yes | Own movies only |
| `addMovie` | Yes | Creates for authenticated user |
| `updateUserMovie` | Yes | Own movies only |
| `deleteUserMovie` | Yes | Own movies only |

### Future Authorization Enhancements
```java
// Role-based access control
@PreAuthorize("hasRole('ADMIN')")
public List<User> getAllUsers() { ... }

@PreAuthorize("hasRole('USER') and #userId == authentication.principal.id")
public User getUserById(@PathVariable String userId) { ... }
```

## Security Headers

### CORS Configuration
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOriginPatterns(Arrays.asList("*"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    return source;
}
```

### Recommended Security Headers
```yaml
# Future enhancement - security headers
server:
  servlet:
    session:
      cookie:
        http-only: true
        secure: true
        same-site: strict
```

## Threat Model

### Identified Threats

1. **Password Attacks**
   - **Threat**: Brute force, dictionary attacks
   - **Mitigation**: BCrypt hashing, rate limiting (future)
   - **Status**: Partially mitigated

2. **Token Theft**
   - **Threat**: XSS, man-in-the-middle attacks
   - **Mitigation**: HTTPS, HttpOnly cookies (future), short expiration
   - **Status**: Basic mitigation

3. **Session Fixation**
   - **Threat**: Session hijacking
   - **Mitigation**: Stateless JWT tokens
   - **Status**: Mitigated

4. **SQL Injection**
   - **Threat**: Database compromise
   - **Mitigation**: JPA parameterized queries
   - **Status**: Mitigated

5. **CSRF Attacks**
   - **Threat**: Cross-site request forgery
   - **Mitigation**: Stateless tokens, CORS configuration
   - **Status**: Mitigated

### Risk Assessment
| Threat | Likelihood | Impact | Risk Level | Mitigation Status |
|--------|------------|--------|------------|-------------------|
| Password Attacks | Medium | High | Medium | Partial |
| Token Theft | Low | High | Medium | Basic |
| SQL Injection | Low | High | Low | Complete |
| CSRF | Low | Medium | Low | Complete |
| XSS | Medium | Medium | Medium | Minimal |

## Security Best Practices

### Development
1. **Never log sensitive data** (passwords, tokens, personal information)
2. **Use parameterized queries** for all database operations
3. **Validate all inputs** at API boundaries
4. **Follow principle of least privilege** for database access
5. **Regular dependency updates** for security patches

### Deployment
1. **Use HTTPS in production** for all communications
2. **Secure environment variables** for secrets management
3. **Regular security scans** of dependencies
4. **Monitor authentication failures** for attack detection
5. **Implement rate limiting** to prevent abuse

### Configuration
```yaml
# Production security configuration
app:
  auth:
    jwt:
      secret: ${JWT_SECRET}  # From secure key management
      expiration-hours: 1
  security:
    rate-limiting:
      enabled: true
      login-attempts: 5
      window-minutes: 15

spring:
  security:
    require-ssl: true
  datasource:
    url: ${DATABASE_URL}  # From environment
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

## Compliance Considerations

### GDPR Compliance
- **Data Minimization**: Only collect necessary user data
- **Right to Deletion**: Implement user account deletion
- **Data Portability**: Provide user data export functionality
- **Consent Management**: Clear privacy policy and consent mechanisms

### OWASP Top 10 Mitigation
1. **Injection**: JPA parameterized queries ✅
2. **Broken Authentication**: JWT implementation ✅
3. **Sensitive Data Exposure**: BCrypt hashing ✅
4. **XML External Entities**: Not applicable (GraphQL) ✅
5. **Broken Access Control**: Method-level security ⚠️ (Basic)
6. **Security Misconfiguration**: Configuration validation ⚠️ (Partial)
7. **Cross-Site Scripting**: Input validation ⚠️ (Basic)
8. **Insecure Deserialization**: Not applicable ✅
9. **Components with Known Vulnerabilities**: Dependency scanning needed ❌
10. **Insufficient Logging**: Security event logging needed ❌

## Security Testing

### Automated Security Tests
```java
@Test
void testPasswordHashing() {
    String password = "testPassword123";
    String hash = passwordEncoder.encode(password);
    
    assertNotEquals(password, hash);
    assertTrue(passwordEncoder.matches(password, hash));
    assertFalse(passwordEncoder.matches("wrongPassword", hash));
}

@Test
void testJwtTokenValidation() {
    String token = jwtService.generateToken(testUser);
    assertTrue(jwtService.validateToken(token));
    
    String invalidToken = "invalid.token.here";
    assertFalse(jwtService.validateToken(invalidToken));
}
```

### Manual Security Testing
1. **Authentication Bypass**: Attempt to access protected endpoints without tokens
2. **Token Manipulation**: Modify JWT tokens and verify rejection
3. **SQL Injection**: Test input fields with malicious SQL
4. **Cross-Site Scripting**: Test input validation with script tags
5. **Brute Force**: Test multiple login attempts with invalid credentials

## Incident Response

### Security Event Monitoring
```java
// Future enhancement - security event logging
@EventListener
public void handleAuthenticationFailure(AuthenticationFailureBadCredentialsEvent event) {
    log.warn("Authentication failure for user: {}", event.getAuthentication().getName());
    // Implement rate limiting, alerting
}
```

### Response Procedures
1. **Suspected Compromise**: Immediately rotate JWT secret
2. **Brute Force Attack**: Implement temporary IP blocking
3. **Data Breach**: Notify users, force password resets
4. **Vulnerability Discovery**: Apply patches, conduct security review

## Future Security Enhancements

### Short Term
- [ ] Rate limiting for authentication endpoints
- [ ] Security event logging and monitoring
- [ ] Enhanced password policies
- [ ] Account lockout after failed attempts

### Medium Term
- [ ] OAuth2 integration with major providers
- [ ] Two-factor authentication support
- [ ] Session management improvements
- [ ] Security headers implementation

### Long Term
- [ ] Advanced threat detection
- [ ] Behavioral analysis for anomaly detection
- [ ] Integration with security information and event management (SIEM)
- [ ] Regular security audits and penetration testing

## References

- [OWASP Authentication Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html)
- [JWT Security Best Practices](https://tools.ietf.org/html/rfc8725)
- [Spring Security Documentation](https://spring.io/projects/spring-security)
- [NIST Cybersecurity Framework](https://www.nist.gov/cyberframework)