# Movie Tracker API

A GraphQL-based backend service for tracking personal movie experiences, built with Spring Boot and Netflix DGS.

## Features

### 🔐 Authentication & Authorization
- **User Registration & Login** - JWT-based authentication with secure password hashing
- **Feature Toggles** - Configurable authentication methods (local auth, OAuth2)
- **Protected Operations** - Secure movie CRUD operations for authenticated users
- **Token Management** - Industry-standard JWT tokens with configurable expiration

### 🎬 Movie Tracking
- **Personal Movie Library** - Track movies you've watched with detailed information
- **Theater Experience** - Record theater, seat assignment, showtime, and ticket price
- **Personal Ratings** - Rate movies and add personal notes
- **Movie Search** - Search and discover movies to add to your collection

### 🔧 Technical Features
- **GraphQL API** - Modern API with GraphiQL interface for development
- **Spring Security** - Robust security implementation with JWT authentication
- **JPA/Hibernate** - Database persistence with PostgreSQL/H2 support
- **Comprehensive Testing** - Unit and integration tests for reliable functionality

## Quick Start

> **🚀 Live Demo:** [Coming Soon - Railway Deployment]



### Prerequisites
- Java 17 or higher
- Gradle 8.x

### Running the Application

1. **Clone and navigate to the project:**
   ```bash
   git clone <repository-url>
   cd movie-tracker-api
   ```

2. **Start the application:**
   ```bash
   ./gradlew bootRun
   ```

3. **Access the GraphiQL interface:**
   ```
   http://localhost:8080/graphiql
   ```

## Authentication

### Registration
Register a new user account:

```graphql
mutation Register {
  register(input: {
    email: "user@example.com"
    username: "moviefan"
    password: "securePassword123"
    firstName: "John"
    lastName: "Doe"
  }) {
    token
    user {
      id
      email
      username
      firstName
      lastName
    }
  }
}
```

### Login
Authenticate with existing credentials:

```graphql
mutation Login {
  login(input: {
    email: "user@example.com"
    password: "securePassword123"
  }) {
    token
    user {
      id
      email
      username
    }
  }
}
```

### Using Authentication Token
Include the JWT token in your request headers:
```
Authorization: Bearer <your-jwt-token>
```

### Current User
Get information about the authenticated user:

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

## Movie Operations

### Add a Movie Experience
```graphql
mutation AddMovie {
  addMovie(input: {
    movieTitle: "The Matrix"
    releaseYear: 1999
    genre: "Sci-Fi"
    director: "The Wachowskis"
    theater: "AMC Theater"
    dateWatched: "2024-01-15"
    personalRating: 9
    notes: "Mind-blowing experience!"
  }) {
    id
    movie {
      title
      releaseYear
    }
    theater
    personalRating
    notes
  }
}
```

### Get Your Movies
```graphql
query MyMovies {
  myMovies {
    id
    movie {
      title
      releaseYear
      genre
      director
    }
    theater
    dateWatched
    personalRating
    notes
  }
}
```

## Configuration

### Authentication Configuration
Configure authentication options in `application.yml`:

```yaml
app:
  auth:
    local-auth-enabled: true    # Enable/disable email/password auth
    oauth2-enabled: false       # Enable/disable OAuth2 providers
    jwt:
      secret: your-secret-key   # JWT signing secret
      expiration-hours: 1       # Token expiration time
```

### Database Configuration
The application supports both H2 (development) and PostgreSQL (production):

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/movietracker
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

## API Reference

### GraphQL Schema
The full GraphQL schema is available at `/graphql` and includes:

- **Queries**: `me`, `myMovies`, `movie`, `searchMovies`, `health`
- **Mutations**: `register`, `login`, `addMovie`, `updateUserMovie`, `deleteUserMovie`
- **Types**: `User`, `Movie`, `UserMovie`, `AuthPayload`

### Error Handling
The API returns structured GraphQL errors with appropriate error types:
- `UNAUTHENTICATED` - Authentication required
- `BAD_REQUEST` - Validation errors
- `PERMISSION_DENIED` - Authorization failures

## Development

### Running Tests
```bash
# Run all tests
./gradlew test

# Run specific test classes
./gradlew test --tests "*AuthenticationService*"
```

### Database Access
In development mode, access the H2 console at:
```
http://localhost:8080/h2-console
```

### Code Structure
```
src/main/java/com/movietracker/api/
├── config/          # Configuration classes
├── datafetcher/     # GraphQL resolvers
├── dto/            # Data transfer objects
├── entity/         # JPA entities
├── exception/      # Exception handling
├── repository/     # Data repositories
├── security/       # Security components
└── service/        # Business logic
```

## Security Features

### Password Security
- **BCrypt Hashing** - Industry-standard password encryption
- **Salt Rounds** - Configurable BCrypt complexity
- **No Plain Text Storage** - Passwords never stored in plain text

### JWT Security
- **HS256 Signing** - Secure token signing algorithm
- **Configurable Expiration** - Token lifetime management
- **Stateless Authentication** - No server-side session storage

### Feature Toggles
- **Local Authentication** - Can be enabled/disabled via configuration
- **OAuth2 Ready** - Infrastructure prepared for OAuth2 integration
- **Future Extensibility** - Ready for external feature flag services

## Contributing

1. **Follow the AI-Assisted Workflow** - See `.ai/workflow.md` for development guidelines
2. **Write Tests** - Maintain test coverage for new features
3. **Update Documentation** - Keep this README and code documentation current
4. **Security First** - Follow security best practices for authentication features

## Technology Stack

- **Spring Boot 3.4** - Application framework
- **Netflix DGS** - GraphQL framework
- **Spring Security** - Authentication and authorization
- **JWT (jjwt)** - JSON Web Token implementation
- **JPA/Hibernate** - Database ORM
- **H2/PostgreSQL** - Database support
- **JUnit 5** - Testing framework
- **Mockito** - Mocking framework

## Future Enhancements

- **OAuth2 Integration** - Google, GitHub, Facebook authentication
- **Email Verification** - Account activation workflow
- **Password Reset** - Self-service password recovery
- **Role-Based Access** - Admin and user role management
- **Rate Limiting** - API usage protection
- **Ticket Scanning** - OCR-based ticket information extraction

## License

This project is licensed under the MIT License.