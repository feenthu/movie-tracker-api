# Deployment Guide

## Overview

This guide covers deployment configurations for the Movie Tracker API across different environments, with special attention to authentication security settings.

## Environment Configurations

### Development Environment

**Configuration**: `application-dev.yml`
```yaml
spring:
  profiles:
    active: dev
  datasource:
    url: jdbc:h2:mem:devdb
    username: sa
    password: 
  h2:
    console:
      enabled: true

app:
  auth:
    local-auth-enabled: true
    oauth2-enabled: false
    jwt:
      secret: dev-secret-key-change-in-production
      expiration-hours: 24  # Longer for development convenience

logging:
  level:
    com.movietracker.api: DEBUG
```

### Staging Environment

**Configuration**: `application-staging.yml`
```yaml
spring:
  profiles:
    active: staging
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:5432/${DB_NAME:movietracker_staging}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false

app:
  auth:
    local-auth-enabled: ${LOCAL_AUTH_ENABLED:true}
    oauth2-enabled: ${OAUTH2_ENABLED:false}
    jwt:
      secret: ${JWT_SECRET}
      expiration-hours: ${JWT_EXPIRATION_HOURS:2}

server:
  port: ${PORT:8080}
  ssl:
    enabled: ${SSL_ENABLED:false}

logging:
  level:
    com.movietracker.api: INFO
    org.springframework.security: WARN
```

### Production Environment

**Configuration**: `application-prod.yml`
```yaml
spring:
  profiles:
    active: prod
  datasource:
    url: jdbc:postgresql://${DB_HOST}:5432/${DB_NAME}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 20000
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false

app:
  auth:
    local-auth-enabled: ${LOCAL_AUTH_ENABLED:true}
    oauth2-enabled: ${OAUTH2_ENABLED:false}
    jwt:
      secret: ${JWT_SECRET}
      expiration-hours: ${JWT_EXPIRATION_HOURS:1}

server:
  port: ${PORT:8080}
  ssl:
    enabled: true
    key-store: ${SSL_KEY_STORE}
    key-store-password: ${SSL_KEY_STORE_PASSWORD}

management:
  endpoints:
    web:
      exposure:
        include: health,metrics
  endpoint:
    health:
      show-details: when-authorized

logging:
  level:
    com.movietracker.api: INFO
    org.springframework.security: WARN
    org.springframework.web: WARN
```

## Environment Variables

### Required Variables (Production)
```bash
# Database Configuration
DB_HOST=your-database-host
DB_NAME=movietracker_prod
DB_USERNAME=app_user
DB_PASSWORD=secure_database_password

# JWT Configuration
JWT_SECRET=your-256-bit-secret-key-for-jwt-signing-minimum-32-characters
JWT_EXPIRATION_HOURS=1

# Authentication Features
LOCAL_AUTH_ENABLED=true
OAUTH2_ENABLED=false

# OAuth2 Configuration (if enabled)
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
GITHUB_CLIENT_ID=your-github-client-id
GITHUB_CLIENT_SECRET=your-github-client-secret

# SSL Configuration
SSL_ENABLED=true
SSL_KEY_STORE=/path/to/keystore.p12
SSL_KEY_STORE_PASSWORD=keystore_password

# Server Configuration
PORT=8080
```

### Optional Variables
```bash
# Logging Configuration
LOG_LEVEL=INFO
SPRING_PROFILES_ACTIVE=prod

# Database Pool Configuration
DB_POOL_SIZE=20
DB_MIN_IDLE=5
```

## Security Checklist

### Pre-Deployment Security Verification

- [ ] **JWT Secret**: Generate cryptographically secure 256-bit secret
- [ ] **Database Credentials**: Use dedicated application user with minimal permissions
- [ ] **Environment Variables**: All secrets stored in secure environment variable system
- [ ] **HTTPS**: SSL/TLS enabled for all production communications
- [ ] **Database Encryption**: Database connections use TLS
- [ ] **Password Policy**: BCrypt rounds configured appropriately (10-12)
- [ ] **CORS Configuration**: Restricted to allowed origins only
- [ ] **Dependency Scan**: No known vulnerabilities in dependencies

### JWT Secret Generation
```bash
# Generate secure JWT secret (Linux/macOS)
openssl rand -base64 32

# Generate secure JWT secret (Windows PowerShell)
[System.Web.Security.Membership]::GeneratePassword(32, 0)

# Verify secret length (minimum 32 characters for HS256)
echo -n "your-secret-key" | wc -c
```

## Database Setup

### PostgreSQL Setup
```sql
-- Create database
CREATE DATABASE movietracker_prod;

-- Create application user
CREATE USER app_user WITH PASSWORD 'secure_password';

-- Grant permissions
GRANT CONNECT ON DATABASE movietracker_prod TO app_user;
GRANT USAGE ON SCHEMA public TO app_user;
GRANT CREATE ON SCHEMA public TO app_user;

-- For production (more restrictive)
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO app_user;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO app_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO app_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT USAGE, SELECT ON SEQUENCES TO app_user;
```

### Database Migration
```bash
# First deployment (creates tables)
java -jar movie-tracker-api.jar --spring.jpa.hibernate.ddl-auto=create

# Subsequent deployments (validates schema)
java -jar movie-tracker-api.jar --spring.jpa.hibernate.ddl-auto=validate
```

## Container Deployment

### Docker Configuration

**Dockerfile**:
```dockerfile
FROM openjdk:17-jre-slim

# Create application user
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Set working directory
WORKDIR /app

# Copy application jar
COPY build/libs/movie-tracker-api-*.jar app.jar

# Change ownership
RUN chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Expose port
EXPOSE 8080

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**docker-compose.yml**:
```yaml
version: '3.8'

services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_HOST=db
      - DB_NAME=movietracker
      - DB_USERNAME=app_user
      - DB_PASSWORD=${DB_PASSWORD}
      - JWT_SECRET=${JWT_SECRET}
    depends_on:
      - db
    restart: unless-stopped

  db:
    image: postgres:15
    environment:
      - POSTGRES_DB=movietracker
      - POSTGRES_USER=app_user
      - POSTGRES_PASSWORD=${DB_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql
    ports:
      - "5432:5432"
    restart: unless-stopped

volumes:
  postgres_data:
```

### Kubernetes Deployment

**deployment.yaml**:
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: movie-tracker-api
spec:
  replicas: 3
  selector:
    matchLabels:
      app: movie-tracker-api
  template:
    metadata:
      labels:
        app: movie-tracker-api
    spec:
      containers:
      - name: movie-tracker-api
        image: movie-tracker-api:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: DB_HOST
          valueFrom:
            secretKeyRef:
              name: db-config
              key: host
        - name: DB_USERNAME
          valueFrom:
            secretKeyRef:
              name: db-config
              key: username
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: db-config
              key: password
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: jwt-config
              key: secret
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 10
          periodSeconds: 5
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
```

## Monitoring and Health Checks

### Health Check Endpoints
- **Application Health**: `GET /actuator/health`
- **Database Connectivity**: Included in health check
- **Custom Health Indicators**: Authentication service status

### Application Metrics
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

### Log Configuration
```yaml
logging:
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/movie-tracker-api.log
  level:
    com.movietracker.api: INFO
    org.springframework.security: WARN
```

## Performance Optimization

### JVM Configuration
```bash
# Production JVM settings
java -Xms512m -Xmx1g \
     -XX:+UseG1GC \
     -XX:G1HeapRegionSize=16m \
     -XX:+UseStringDeduplication \
     -XX:+OptimizeStringConcat \
     -jar movie-tracker-api.jar
```

### Database Connection Pool
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 20000
      idle-timeout: 300000
      max-lifetime: 1200000
      leak-detection-threshold: 60000
```

## Troubleshooting

### Common Issues

1. **Application Won't Start**
   ```bash
   # Check logs
   docker logs container-name
   
   # Verify environment variables
   printenv | grep -E "(DB_|JWT_|AUTH_)"
   ```

2. **Database Connection Issues**
   ```bash
   # Test database connectivity
   psql -h $DB_HOST -U $DB_USERNAME -d $DB_NAME
   
   # Check network connectivity
   nc -zv $DB_HOST 5432
   ```

3. **Authentication Issues**
   ```bash
   # Verify JWT secret length
   echo -n "$JWT_SECRET" | wc -c  # Should be >= 32
   
   # Check authentication logs
   kubectl logs deployment/movie-tracker-api | grep -i auth
   ```

### Debug Mode
```yaml
# Enable debug logging temporarily
logging:
  level:
    com.movietracker.api: DEBUG
    org.springframework.security: DEBUG
    org.springframework.web: DEBUG
```

## Rollback Procedures

### Application Rollback
```bash
# Docker Compose
docker-compose down
docker-compose up -d

# Kubernetes
kubectl rollout undo deployment/movie-tracker-api
kubectl rollout status deployment/movie-tracker-api
```

### Database Rollback
- Maintain database migration scripts
- Test rollback procedures in staging
- Consider data migration implications

## Security Monitoring

### Security Events to Monitor
- Failed authentication attempts
- Invalid JWT tokens
- Unusual access patterns
- Database connection failures
- SSL/TLS handshake failures

### Log Analysis Queries
```bash
# Failed login attempts
grep "Authentication failure" /var/log/movie-tracker-api.log

# Invalid tokens
grep "JWT token validation failed" /var/log/movie-tracker-api.log

# Monitor authentication patterns
grep -E "(register|login)" /var/log/movie-tracker-api.log | awk '{print $1, $2, $NF}'
```