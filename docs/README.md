# Movie Tracker API Documentation

Welcome to the Movie Tracker API documentation. This collection of documents provides comprehensive information about the authentication system, security model, and deployment procedures.

## 📚 Documentation Index

### Getting Started
- **[Main README](../README.md)** - Project overview, quick start guide, and basic usage examples
- **[Authentication API](authentication.md)** - Complete authentication API reference with examples
- **[Security Guide](security.md)** - Security architecture, best practices, and threat model

### Development & Deployment
- **[Deployment Guide](deployment.md)** - Environment configuration, container deployment, and monitoring
- **[AI Workflow](../.ai/workflow.md)** - AI-assisted development workflow and collaboration guidelines

## 🔐 Authentication System

The Movie Tracker API uses **JWT-based authentication** with the following key features:

- **Secure Registration & Login** - BCrypt password hashing with email/username validation
- **Stateless Authentication** - JWT tokens with configurable expiration
- **Feature Toggles** - Configurable authentication methods for different environments
- **Spring Security Integration** - Industry-standard security framework implementation

### Quick Authentication Example

```graphql
# Register a new user
mutation {
  register(input: {
    email: "user@example.com"
    username: "moviefan"
    password: "securePassword123"
  }) {
    token
    user { id email username }
  }
}

# Use token for authenticated requests
# Header: Authorization: Bearer <your-jwt-token>
query {
  me { id email username }
}
```

## 🎬 Core Features

### Movie Tracking
- Personal movie library management
- Theater experience recording (seat, showtime, price)
- Personal ratings and notes
- Movie search and discovery

### Security Features
- **Password Security**: BCrypt hashing with configurable rounds
- **Token Security**: HMAC SHA-256 signed JWT tokens
- **Input Validation**: Comprehensive validation at API boundaries
- **Feature Toggles**: Runtime configuration for authentication methods

### Technical Stack
- **Backend**: Spring Boot 3.4 with Spring Security
- **GraphQL**: Netflix DGS framework with GraphiQL interface
- **Database**: JPA/Hibernate with PostgreSQL/H2 support
- **Authentication**: JWT with configurable providers
- **Testing**: JUnit 5 with comprehensive unit and integration tests

## 📖 Document Summaries

### [Authentication API](authentication.md)
Complete reference for authentication endpoints including:
- Registration and login mutations
- JWT token structure and configuration
- Error handling and validation
- Frontend integration examples
- Troubleshooting guide

### [Security Guide](security.md)
Comprehensive security documentation covering:
- Security architecture and threat model
- Password and JWT security implementation
- Authorization matrix and access controls
- OWASP Top 10 mitigation strategies
- Security testing and incident response procedures

### [Deployment Guide](deployment.md)
Production deployment instructions including:
- Environment-specific configurations
- Security checklist and best practices
- Container and Kubernetes deployment
- Monitoring, health checks, and troubleshooting
- Performance optimization guidelines

## 🔧 Configuration Reference

### Authentication Configuration
```yaml
app:
  auth:
    local-auth-enabled: true    # Enable email/password authentication
    oauth2-enabled: false       # Enable OAuth2 providers (future)
    jwt:
      secret: your-secret-key   # JWT signing secret (256-bit minimum)
      expiration-hours: 1       # Token expiration time
```

### Security Headers
```yaml
server:
  ssl:
    enabled: true              # Enable HTTPS in production
spring:
  security:
    require-ssl: true          # Require SSL for all requests
```

## 🧪 Testing

The authentication system includes comprehensive tests:

- **26 Unit Tests** - Core authentication logic
- **JWT Service Tests** - Token generation and validation
- **Authentication Service Tests** - Registration and login flows
- **Security Filter Tests** - Request processing and token validation
- **Configuration Tests** - Feature toggle and JWT settings

Run tests with:
```bash
./gradlew test
```

## 🚀 Development Workflow

This project follows an **AI-assisted development workflow** documented in [`.ai/workflow.md`](../.ai/workflow.md). The workflow includes:

1. **Requirement Refinement** - Collaborative specification creation
2. **Implementation Planning** - Detailed technical planning
3. **Code Implementation** - Systematic feature development
4. **Testing Strategy** - Comprehensive test implementation
5. **Documentation** - Complete documentation updates

## 🔒 Security Considerations

### Production Checklist
- [ ] Generate cryptographically secure JWT secret (>= 256 bits)
- [ ] Enable HTTPS/TLS for all communications
- [ ] Configure secure database credentials
- [ ] Implement rate limiting for authentication endpoints
- [ ] Set up security monitoring and alerting
- [ ] Regular dependency vulnerability scanning

### Security Monitoring
Monitor these security events:
- Failed authentication attempts
- Invalid JWT token usage
- Unusual access patterns
- Account creation spikes
- Database connection anomalies

## 📞 Support

### Common Issues
- **Authentication failures**: Check token format and expiration
- **CORS errors**: Verify allowed origins configuration
- **Database connectivity**: Validate connection strings and credentials
- **JWT errors**: Ensure secret key meets minimum requirements

### Debug Mode
Enable detailed logging for troubleshooting:
```yaml
logging:
  level:
    com.movietracker.api: DEBUG
    org.springframework.security: DEBUG
```

## 📈 Future Enhancements

### Planned Features
- **OAuth2 Integration** - Google, GitHub, Facebook authentication
- **Two-Factor Authentication** - SMS/email verification
- **Advanced Security** - Rate limiting, account lockout, audit logging
- **Role-Based Access** - Admin and user permission management

### Architecture Evolution
- **Microservices Ready** - Modular authentication service
- **External Feature Flags** - Integration with feature flag services
- **Advanced Monitoring** - Security analytics and threat detection

---

For additional support or questions, please refer to the specific documentation sections or review the codebase implementation details.