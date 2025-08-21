package com.movietracker.api.exception;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.exceptions.DgsDataFetchingException;
import com.netflix.graphql.types.errors.TypedGraphQLError;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.dao.DataIntegrityViolationException;
import graphql.schema.DataFetchingEnvironment;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;

@DgsComponent
public class GraphQLExceptionHandler {

    public TypedGraphQLError handle(AuthenticationException ex) {
        return TypedGraphQLError.newBuilder()
                .message(ex.getMessage())
                .errorType(ex.getErrorType())
                .build();
    }

    public TypedGraphQLError handle(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        
        return TypedGraphQLError.newBuilder()
                .message("Validation failed: " + message)
                .errorType(com.netflix.graphql.types.errors.ErrorType.BAD_REQUEST)
                .build();
    }

    public TypedGraphQLError handle(BindException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        
        return TypedGraphQLError.newBuilder()
                .message("Validation failed: " + message)
                .errorType(com.netflix.graphql.types.errors.ErrorType.BAD_REQUEST)
                .build();
    }

    public TypedGraphQLError handle(DataIntegrityViolationException ex) {
        String message = "Data integrity violation";
        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("email")) {
                message = "Email already exists";
            } else if (ex.getMessage().contains("username")) {
                message = "Username already exists";
            }
        }
        
        return TypedGraphQLError.newBuilder()
                .message(message)
                .errorType(com.netflix.graphql.types.errors.ErrorType.BAD_REQUEST)
                .build();
    }

    public TypedGraphQLError handle(Exception ex, DataFetchingEnvironment dfe) {
        return TypedGraphQLError.newBuilder()
                .message("An unexpected error occurred")
                .errorType(com.netflix.graphql.types.errors.ErrorType.INTERNAL)
                .path(dfe.getExecutionStepInfo().getPath())
                .build();
    }
}
