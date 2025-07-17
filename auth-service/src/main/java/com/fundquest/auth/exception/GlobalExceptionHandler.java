package com.fundquest.auth.exception;

import com.fundquest.auth.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle authentication exceptions
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(
            AuthenticationException ex, WebRequest request) {

        HttpStatus status = getHttpStatusForAuthError(ex.getErrorCode());
        ApiResponse<Void> response = ApiResponse.error(ex.getErrorCode(), ex.getMessage());

        return new ResponseEntity<>(response, status);
    }

    /**
     * Handle token validation exceptions
     */
    @ExceptionHandler(TokenValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleTokenValidationException(
            TokenValidationException ex, WebRequest request) {

        HttpStatus status = getHttpStatusForTokenError(ex.getErrorCode());
        ApiResponse<Void> response = ApiResponse.error(ex.getErrorCode(), ex.getMessage());

        return new ResponseEntity<>(response, status);
    }

    /**
     * Handle user exceptions
     */
    @ExceptionHandler(UserException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserException(
            UserException ex, WebRequest request) {

        HttpStatus status = getHttpStatusForUserError(ex.getErrorCode());
        ApiResponse<Void> response = ApiResponse.error(ex.getErrorCode(), ex.getMessage());

        return new ResponseEntity<>(response, status);
    }

    /**
     * Handle validation exceptions
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ApiResponse<Map<String, String>> response = ApiResponse.error("VALIDATION_ERROR", "Validation failed");
        response.setData(errors);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle illegal argument exceptions
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {

        ApiResponse<Void> response = ApiResponse.error("INVALID_ARGUMENT", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle generic runtime exceptions
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(
            RuntimeException ex, WebRequest request) {

        ApiResponse<Void> response = ApiResponse.error("INTERNAL_ERROR", "An unexpected error occurred");
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGlobalException(
            Exception ex, WebRequest request) {

        ApiResponse<Void> response = ApiResponse.error("INTERNAL_ERROR", "An unexpected error occurred");
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Get HTTP status for authentication errors
     */
    private HttpStatus getHttpStatusForAuthError(String errorCode) {
        return switch (errorCode) {
            case "INVALID_TOKEN", "TOKEN_EXPIRED" -> HttpStatus.BAD_REQUEST;
            case "INVALID_TENANT" -> HttpStatus.FORBIDDEN;
            case "UNAUTHORIZED", "INVALID_ACCESS_TOKEN", "INVALID_REFRESH_TOKEN", "NO_REFRESH_TOKEN" -> HttpStatus.UNAUTHORIZED;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    /**
     * Get HTTP status for token errors
     */
    private HttpStatus getHttpStatusForTokenError(String errorCode) {
        return switch (errorCode) {
            case "TOKEN_VALIDATION_ERROR" -> HttpStatus.BAD_REQUEST;
            case "INVALID_TOKEN", "TOKEN_EXPIRED" -> HttpStatus.UNAUTHORIZED;
            case "INVALID_TENANT" -> HttpStatus.FORBIDDEN;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    /**
     * Get HTTP status for user errors
     */
    private HttpStatus getHttpStatusForUserError(String errorCode) {
        return switch (errorCode) {
            case "USER_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "USER_INACTIVE" -> HttpStatus.FORBIDDEN;
            case "INVALID_USER_DATA" -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}