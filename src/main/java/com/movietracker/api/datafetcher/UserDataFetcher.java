package com.movietracker.api.datafetcher;

import com.movietracker.api.entity.User;
import com.movietracker.api.exception.AuthenticationException;
import com.movietracker.api.security.SecurityContextHelper;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.types.errors.ErrorType;
import org.springframework.beans.factory.annotation.Autowired;

@DgsComponent
public class UserDataFetcher {
    
    private final SecurityContextHelper securityContextHelper;
    
    @Autowired
    public UserDataFetcher(SecurityContextHelper securityContextHelper) {
        this.securityContextHelper = securityContextHelper;
    }
    
    @DgsQuery
    public User me() {
        return securityContextHelper.getCurrentUser()
            .orElseThrow(() -> new AuthenticationException(
                "Authentication required", 
                ErrorType.UNAUTHENTICATED
            ));
    }
}
