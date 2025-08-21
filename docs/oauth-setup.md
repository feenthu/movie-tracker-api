# OAuth2 Integration Setup Guide

This guide will help you configure OAuth2 authentication with Google, Facebook, and Apple for the Movie Tracker API.

## Overview

The Movie Tracker API now supports OAuth2 authentication alongside traditional email/password login. Users can authenticate using:

- Google OAuth2
- Facebook OAuth2  
- Apple Sign In

## Environment Variables Required

### Google OAuth2 Setup

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing one
3. Enable the Google+ API
4. Go to "Credentials" → "Create Credentials" → "OAuth 2.0 Client IDs"
5. Configure OAuth consent screen
6. Set authorized redirect URIs:
   - Local: `http://localhost:8081/login/oauth2/code/google`
   - Production: `https://your-api-domain/login/oauth2/code/google`

Set these environment variables:
```bash
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
```

### Facebook OAuth2 Setup

1. Go to [Facebook Developers](https://developers.facebook.com/)
2. Create a new app
3. Add "Facebook Login" product
4. Configure OAuth redirect URIs:
   - Local: `http://localhost:8081/login/oauth2/code/facebook`
   - Production: `https://your-api-domain/login/oauth2/code/facebook`

Set these environment variables:
```bash
FACEBOOK_CLIENT_ID=your_facebook_app_id
FACEBOOK_CLIENT_SECRET=your_facebook_app_secret
```

### Apple Sign In Setup

1. Go to [Apple Developer Portal](https://developer.apple.com/)
2. Create a new App ID with Sign In with Apple capability
3. Create a Services ID for web authentication
4. Configure domains and redirect URLs:
   - Local: `http://localhost:8081/login/oauth2/code/apple`
   - Production: `https://your-api-domain/login/oauth2/code/apple`
5. Generate a private key for server-to-server authentication

Set these environment variables:
```bash
APPLE_CLIENT_ID=your_apple_services_id
APPLE_CLIENT_SECRET=your_generated_jwt_token
```

Note: Apple requires a JWT token as the client secret. You'll need to generate this using your private key.

## Railway Environment Configuration

In your Railway dashboard, set these environment variables:

### Production Environment Variables
```bash
# OAuth2 Configuration
OAUTH2_ENABLED=true
OAUTH2_REDIRECT_URI=https://your-frontend-domain/auth/callback
API_BASE_URL=https://your-api-domain

# Google OAuth2
GOOGLE_CLIENT_ID=your_production_google_client_id
GOOGLE_CLIENT_SECRET=your_production_google_client_secret

# Facebook OAuth2
FACEBOOK_CLIENT_ID=your_production_facebook_app_id
FACEBOOK_CLIENT_SECRET=your_production_facebook_app_secret

# Apple Sign In
APPLE_CLIENT_ID=your_production_apple_services_id
APPLE_CLIENT_SECRET=your_production_apple_jwt_token
```

## GraphQL Usage

### Get OAuth2 Login URL
```graphql
mutation GetGoogleLoginUrl {
  getOAuth2LoginUrl(provider: GOOGLE) {
    provider
    loginUrl
  }
}
```

### Available Providers
- `GOOGLE`
- `FACEBOOK`
- `APPLE`

## Authentication Flow

1. Frontend calls `getOAuth2LoginUrl` mutation to get the OAuth provider URL
2. User is redirected to the OAuth provider (Google/Facebook/Apple)
3. After successful authentication, provider redirects to our callback URL
4. Our backend processes the OAuth response and creates/updates user
5. Backend redirects to frontend with JWT token
6. Frontend stores JWT token for subsequent API calls

## Security Considerations

1. **HTTPS Required**: OAuth2 providers require HTTPS in production
2. **Secure Redirect URIs**: Only register trusted redirect URIs
3. **Token Expiration**: JWT tokens have configurable expiration
4. **Cross-Origin**: CORS is configured to allow frontend domain

## Testing Locally

1. Set up local environment variables in `.env` file
2. Start the application: `./gradlew bootRun`
3. Test OAuth URLs via GraphiQL: `http://localhost:8081/graphiql`
4. Configure your frontend to use OAuth login URLs

## Troubleshooting

### Common Issues

1. **Invalid Redirect URI**: Ensure redirect URIs match exactly in OAuth provider settings
2. **CORS Errors**: Check CORS configuration allows your frontend domain
3. **Apple JWT Token**: Apple client secret must be a properly signed JWT token
4. **Facebook App Review**: Facebook may require app review for production use

### Debug Logs

Enable debug logging by setting:
```bash
LOGGING_LEVEL_COM_MOVIETRACKER_API=DEBUG
```

This will log OAuth2 authentication steps for debugging.