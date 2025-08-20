package com.movietracker.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.auth")
public class AuthConfig {
    
    private boolean localAuthEnabled = true;
    private boolean oauth2Enabled = false;
    
    // JWT Configuration
    private Jwt jwt = new Jwt();
    
    public boolean isLocalAuthEnabled() {
        return localAuthEnabled;
    }
    
    public void setLocalAuthEnabled(boolean localAuthEnabled) {
        this.localAuthEnabled = localAuthEnabled;
    }
    
    public boolean isOauth2Enabled() {
        return oauth2Enabled;
    }
    
    public void setOauth2Enabled(boolean oauth2Enabled) {
        this.oauth2Enabled = oauth2Enabled;
    }
    
    public Jwt getJwt() {
        return jwt;
    }
    
    public void setJwt(Jwt jwt) {
        this.jwt = jwt;
    }
    
    public static class Jwt {
        private String secret = "movieTracker2024SecretKeyThatIsLongEnoughForHS256Algorithm";
        private int expirationHours = 1;
        
        public String getSecret() {
            return secret;
        }
        
        public void setSecret(String secret) {
            this.secret = secret;
        }
        
        public int getExpirationHours() {
            return expirationHours;
        }
        
        public void setExpirationHours(int expirationHours) {
            this.expirationHours = expirationHours;
        }
    }
}
