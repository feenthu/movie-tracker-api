package com.movietracker.api.datafetcher;

import com.movietracker.api.dto.OAuth2LoginUrl;
import com.movietracker.api.dto.OAuth2Provider;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.InputArgument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@DgsComponent
@ConditionalOnProperty(name = "app.auth.oauth2-enabled", havingValue = "true")
public class OAuth2DataFetcher {
    
    @Value("${app.api-base-url:http://localhost:8081}")
    private String apiBaseUrl;

    @DgsMutation
    public OAuth2LoginUrl getOAuth2LoginUrl(@InputArgument OAuth2Provider provider) {
        String providerName = provider.name().toLowerCase();
        
        // IMMEDIATE FIX: Use new secure OAuth2 flow endpoint with PKCE support
        // This bypasses Spring's problematic OAuth2 session management
        String loginUrl = apiBaseUrl + "/oauth2/authorize/" + providerName;
        
        System.out.println("OAuth2DataFetcher: Returning new PKCE endpoint: " + loginUrl);
        
        return new OAuth2LoginUrl(provider, loginUrl);
    }
}