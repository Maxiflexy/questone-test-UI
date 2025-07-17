package com.fundquest.auth.exception;

import lombok.Getter;

/**
 * Custom exception for token validation errors
 */
@Getter
public class TokenValidationException extends RuntimeException {

    private final String errorCode;

    public TokenValidationException(String message) {
        super(message);
        this.errorCode = "TOKEN_VALIDATION_ERROR";
    }

    public TokenValidationException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "TOKEN_VALIDATION_ERROR";
    }

    public TokenValidationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public TokenValidationException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

}
