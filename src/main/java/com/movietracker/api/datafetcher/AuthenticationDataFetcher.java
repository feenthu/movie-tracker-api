package com.movietracker.api.datafetcher;

import com.movietracker.api.config.AuthConfig;
import com.movietracker.api.dto.AuthPayload;
import com.movietracker.api.dto.LoginInput;
import com.movietracker.api.dto.RegisterInput;
import com.movietracker.api.exception.AuthenticationException;
import com.movietracker.api.service.AuthenticationService;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.InputArgument;
import com.netflix.graphql.types.errors.ErrorType;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;

@DgsComponent
public class AuthenticationDataFetcher {
    
    private final AuthenticationService authenticationService;
    private final AuthConfig authConfig;
    
    @Autowired
    public AuthenticationDataFetcher(AuthenticationService authenticationService,
                                   AuthConfig authConfig) {
        this.authenticationService = authenticationService;
        this.authConfig = authConfig;
    }
    
    @DgsMutation
    public AuthPayload register(@InputArgument @Valid RegisterInput input) {
        // Check if local authentication is enabled
        if (!authConfig.isLocalAuthEnabled()) {
            throw new AuthenticationException(
                "Local authentication is disabled", 
                ErrorType.PERMISSION_DENIED
            );
        }
        
        return authenticationService.register(input);
    }
    
    @DgsMutation
    public AuthPayload login(@InputArgument @Valid LoginInput input) {
        // Check if local authentication is enabled
        if (!authConfig.isLocalAuthEnabled()) {
            throw new AuthenticationException(
                "Local authentication is disabled", 
                ErrorType.PERMISSION_DENIED
            );
        }
        
        return authenticationService.login(input);
    }
}
