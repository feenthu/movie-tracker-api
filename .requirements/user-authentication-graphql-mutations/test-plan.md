# Test Plan: User Authentication GraphQL Mutations

## Testing Strategy

Focus primarily on integration tests using GraphQL operations to verify end-to-end authentication flows. Include targeted unit tests for complex business logic components like JWT service and authentication service. Use Spring Boot test framework with DGS test utilities for GraphQL integration testing.

## Integration Tests

Primary testing approach using GraphQL integration tests with realistic scenarios.

**Test Scenarios**:

### Authentication Mutation Tests
- **Register Success**: Valid user registration with all required fields
- **Register Duplicate Email**: Registration fails when email already exists  
- **Register Duplicate Username**: Registration fails when username already exists
- **Register Invalid Data**: Registration fails with invalid email format or missing required fields
- **Login Success**: Valid login with correct email/password returns token and user data
- **Login Invalid Credentials**: Login fails with incorrect password
- **Login Nonexistent User**: Login fails with non-existent email
- **Login Inactive User**: Login fails for inactive user accounts

### Authentication Context Tests  
- **Me Query Authenticated**: Authenticated user can query their profile information
- **Me Query Unauthenticated**: Unauthenticated request to me query fails appropriately
- **Protected Mutation Authenticated**: Authenticated user can perform movie CRUD operations
- **Protected Mutation Unauthenticated**: Unauthenticated movie operations fail appropriately
- **Invalid Token**: Requests with invalid JWT tokens are rejected
- **Expired Token**: Requests with expired JWT tokens are rejected

### Feature Toggle Tests
- **Local Auth Enabled**: Authentication works when local auth is enabled
- **Local Auth Disabled**: Authentication is properly disabled when feature toggle is off

**Test Case Files**:
```
test-cases/authentication/
├── register/
│   ├── register-success.case.json
│   ├── register-duplicate-email.case.json
│   ├── register-duplicate-username.case.json
│   └── register-invalid-data.case.json
├── login/
│   ├── login-success.case.json
│   ├── login-invalid-credentials.case.json
│   ├── login-nonexistent-user.case.json
│   └── login-inactive-user.case.json
├── me-query/
│   ├── me-authenticated.case.json
│   └── me-unauthenticated.case.json
└── protected-operations/
    ├── authenticated-movie-operations.case.json
    ├── unauthenticated-movie-operations.case.json
    ├── invalid-token.case.json
    └── expired-token.case.json
```

### Integration Test Implementation Files
- `src/test/java/com/movietracker/api/datafetcher/AuthenticationDataFetcherTest.java`
- `src/test/java/com/movietracker/api/datafetcher/UserDataFetcherTest.java`  
- `src/test/java/com/movietracker/api/security/JwtAuthenticationIntegrationTest.java`
- `src/test/java/com/movietracker/api/config/AuthConfigIntegrationTest.java`

## Unit Tests

Focused unit tests for complex business logic and security-critical components.

**Test Cases**:

### JWT Service Tests
- **Token Generation**: Verify JWT tokens are generated with correct claims and structure
- **Token Validation**: Verify valid tokens pass validation, invalid tokens fail
- **Token Parsing**: Verify user information is correctly extracted from valid tokens
- **Token Expiration**: Verify expired tokens are properly identified and rejected
- **Invalid Signatures**: Verify tokens with invalid signatures are rejected

### Authentication Service Tests  
- **Password Hashing**: Verify passwords are properly hashed using BCrypt
- **Password Verification**: Verify password verification works correctly
- **User Creation**: Verify user entities are created with correct data
- **Duplicate Handling**: Verify proper handling of duplicate email/username scenarios

### Security Filter Tests
- **Filter Chain Integration**: Verify JWT filter is properly integrated in security chain
- **Request Processing**: Verify authenticated requests set proper security context
- **Error Handling**: Verify authentication failures are handled gracefully

### Configuration Tests
- **Feature Toggle Loading**: Verify authentication feature toggles load correctly from configuration
- **JWT Configuration**: Verify JWT settings are loaded and applied correctly

**Unit Test Files**:
- `src/test/java/com/movietracker/api/service/JwtServiceTest.java`
- `src/test/java/com/movietracker/api/service/AuthenticationServiceTest.java`
- `src/test/java/com/movietracker/api/security/JwtAuthenticationFilterTest.java`
- `src/test/java/com/movietracker/api/config/AuthConfigTest.java`

## Test Data Setup

### Test Users
Create test users with known credentials for consistent testing:
```java
// Test user data
User testUser = new User("test@example.com", "testuser", hashedPassword);
User inactiveUser = new User("inactive@example.com", "inactive", hashedPassword);
inactiveUser.setIsActive(false);
```

### Test JWT Tokens
Generate test tokens for authentication scenarios:
- Valid tokens with different expiration times
- Invalid tokens with corrupted signatures  
- Expired tokens for expiration testing
- Tokens with missing or invalid claims

### Database State Management
- Use @Transactional and @Rollback for test isolation
- Create dedicated test data sets for each test scenario
- Use @Sql annotations for complex data setup scenarios

## Test Execution Strategy

### Test Environment Setup
- Use H2 in-memory database for integration tests
- Configure test-specific application properties
- Mock external dependencies where appropriate
- Use test containers for more complex integration scenarios (future enhancement)

### Test Categories
- **Unit Tests**: Fast, isolated tests for business logic
- **Integration Tests**: Full GraphQL request/response testing  
- **Security Tests**: Authentication and authorization verification
- **Configuration Tests**: Feature toggle and configuration validation

### Continuous Integration
- All tests must pass before code merge
- Integration tests run with real database interactions
- Security tests verify no authentication bypasses
- Performance tests ensure authentication doesn't degrade response times

### Test Coverage Goals
- Minimum 80% code coverage for authentication components
- 100% coverage for security-critical paths (token validation, password handling)
- All GraphQL mutations and queries covered by integration tests
- All error scenarios covered by appropriate tests