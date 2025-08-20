# Authentication API Documentation

## Overview

The Movie Tracker API provides JWT-based authentication for secure user registration and login. This document details the authentication endpoints, security model, and usage patterns.

## Authentication Flow

### 1. User Registration

**Mutation**: `register`

```graphql
mutation Register($input: RegisterInput!) {
  register(input: $input) {
    token
    user {
      id
      email
      username
      firstName
      lastName
      createdAt
    }
  }
}
```

**Input Variables**:
```json
{
  "input": {
    "email": "user@example.com",
    "username": "moviefan",
    "password": "securePassword123",
    "firstName": "John",
    "lastName": "Doe"
  }
}
```

**Response**:
```json
{
  "data": {
    "register": {
      "token": "eyJhbGciOiJIUzI1NiJ9...",
      "user": {
        "id": "550e8400-e29b-41d4-a716-446655440000",
        "email": "user@example.com",
        "username": "moviefan",
        "firstName": "John",
        "lastName": "Doe",
        "createdAt": "2024-01-15T10:30:00Z"
      }
    }
  }
}
```

**Validation Rules**:
- `email`: Valid email format, must be unique
- `username`: 3-50 characters, must be unique
- `password`: Minimum 6 characters
- `firstName` and `lastName`: Optional

### 2. User Login

**Mutation**: `login`

```graphql
mutation Login($input: LoginInput!) {
  login(input: $input) {
    token
    user {
      id
      email
      username
    }
  }
}
```

**Input Variables**:
```json
{
  "input": {
    "email": "user@example.com",
    "password": "securePassword123"
  }
}
```

**Response**:
```json
{
  "data": {
    "login": {
      "token": "eyJhbGciOiJIUzI1NiJ9...",
      "user": {
        "id": "550e8400-e29b-41d4-a716-446655440000",
        "email": "user@example.com",
        "username": "moviefan"
      }
    }
  }
}
```

### 3. Current User Query

**Query**: `me`

```graphql
query Me {
  me {
    id
    email
    username
    firstName
    lastName
    createdAt
  }
}
```

**Required Headers**:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Response**:
```json
{
  "data": {
    "me": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "email": "user@example.com",
      "username": "moviefan",
      "firstName": "John",
      "lastName": "Doe",
      "createdAt": "2024-01-15T10:30:00Z"
    }
  }
}
```

## JWT Token Structure

### Token Claims
- `sub`: User email (subject)
- `user_id`: Unique user identifier
- `email`: User email address
- `username`: User username
- `iat`: Issued at timestamp
- `exp`: Expiration timestamp

### Token Configuration
- **Algorithm**: HS256
- **Default Expiration**: 1 hour
- **Configurable**: Via `app.auth.jwt.expiration-hours`

## Error Responses

### Validation Errors
```json
{
  "errors": [
    {
      "message": "Validation failed: Email already exists",
      "extensions": {
        "classification": "BAD_REQUEST"
      }
    }
  ]
}
```

### Authentication Errors
```json
{
  "errors": [
    {
      "message": "Invalid credentials",
      "extensions": {
        "classification": "UNAUTHENTICATED"
      }
    }
  ]
}
```

### Authorization Errors
```json
{
  "errors": [
    {
      "message": "Authentication required",
      "extensions": {
        "classification": "UNAUTHENTICATED"
      }
    }
  ]
}
```

## Security Features

### Password Security
- **Hashing Algorithm**: BCrypt with configurable rounds
- **Salt**: Automatically generated per password
- **Storage**: Only hashed passwords stored, never plain text

### Token Security
- **Signing**: HMAC SHA-256 with configurable secret
- **Validation**: Signature and expiration checked on each request
- **Stateless**: No server-side session storage required

### Input Validation
- **Email Format**: RFC 5322 compliant email validation
- **Username Length**: 3-50 character limit
- **Password Strength**: Minimum 6 characters (configurable)
- **SQL Injection**: Protected by JPA parameterized queries

## Feature Toggles

### Configuration Options
```yaml
app:
  auth:
    local-auth-enabled: true    # Enable/disable email/password auth
    oauth2-enabled: false       # Enable/disable OAuth2 providers
```

### Feature Toggle Behavior
- **Local Auth Disabled**: Registration and login mutations return permission denied
- **OAuth2 Enabled**: Additional OAuth2 endpoints become available (future feature)

## Rate Limiting (Future)

### Planned Protections
- **Login Attempts**: Configurable maximum attempts per IP/user
- **Registration**: Rate limiting for account creation
- **Token Refresh**: Limitations on token generation frequency

## Testing Authentication

### Using GraphiQL
1. Navigate to `http://localhost:8080/graphiql`
2. Execute registration or login mutation
3. Copy the returned token
4. Add to request headers: `{"Authorization": "Bearer <token>"}`
5. Execute protected queries/mutations

### Using cURL
```bash
# Register a user
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "mutation Register($input: RegisterInput!) { register(input: $input) { token user { id email } } }",
    "variables": {
      "input": {
        "email": "test@example.com",
        "username": "testuser",
        "password": "password123"
      }
    }
  }'

# Use the token for authenticated requests
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your-token>" \
  -d '{
    "query": "query Me { me { id email username } }"
  }'
```

## Integration Examples

### Frontend Integration (JavaScript)
```javascript
// Authentication service
class AuthService {
  async register(email, username, password, firstName, lastName) {
    const response = await fetch('/graphql', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        query: `
          mutation Register($input: RegisterInput!) {
            register(input: $input) {
              token
              user { id email username firstName lastName }
            }
          }
        `,
        variables: {
          input: { email, username, password, firstName, lastName }
        }
      })
    });
    
    const data = await response.json();
    if (data.data?.register?.token) {
      localStorage.setItem('auth_token', data.data.register.token);
      return data.data.register;
    }
    throw new Error(data.errors?.[0]?.message || 'Registration failed');
  }

  async login(email, password) {
    const response = await fetch('/graphql', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        query: `
          mutation Login($input: LoginInput!) {
            login(input: $input) {
              token
              user { id email username }
            }
          }
        `,
        variables: { input: { email, password } }
      })
    });
    
    const data = await response.json();
    if (data.data?.login?.token) {
      localStorage.setItem('auth_token', data.data.login.token);
      return data.data.login;
    }
    throw new Error(data.errors?.[0]?.message || 'Login failed');
  }

  getAuthHeader() {
    const token = localStorage.getItem('auth_token');
    return token ? { 'Authorization': `Bearer ${token}` } : {};
  }

  logout() {
    localStorage.removeItem('auth_token');
  }
}
```

### Mobile Integration (React Native)
```javascript
import AsyncStorage from '@react-native-async-storage/async-storage';

class MobileAuthService {
  async authenticatedRequest(query, variables = {}) {
    const token = await AsyncStorage.getItem('auth_token');
    
    const response = await fetch('http://your-api.com/graphql', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(token && { 'Authorization': `Bearer ${token}` })
      },
      body: JSON.stringify({ query, variables })
    });
    
    return response.json();
  }
}
```

## Troubleshooting

### Common Issues

1. **"Authentication required" error**
   - Ensure token is included in Authorization header
   - Check token hasn't expired
   - Verify token format: `Bearer <token>`

2. **"Invalid credentials" error**
   - Verify email and password are correct
   - Check if account is active
   - Ensure local authentication is enabled

3. **"Email already exists" error**
   - Email must be unique across all users
   - Try login instead of registration
   - Check for typos in email address

4. **Token expiration**
   - Default expiration is 1 hour
   - Re-authenticate to get new token
   - Consider implementing token refresh

### Debug Mode
Enable debug logging in `application.yml`:
```yaml
logging:
  level:
    com.movietracker.api.security: DEBUG
    org.springframework.security: DEBUG
```

This will log authentication attempts, token validation, and security events.