package com.movietracker.api.exception;

import com.netflix.graphql.types.errors.ErrorType;

public class AuthenticationException extends RuntimeException {
    
    private final ErrorType errorType;
    
    public AuthenticationException(String message) {
        super(message);
        this.errorType = ErrorType.UNAUTHENTICATED;
    }
    
    public AuthenticationException(String message, ErrorType errorType) {
        super(message);
        this.errorType = errorType;
    }
    
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
        this.errorType = ErrorType.UNAUTHENTICATED;
    }
    
    public ErrorType getErrorType() {
        return errorType;
    }
}
