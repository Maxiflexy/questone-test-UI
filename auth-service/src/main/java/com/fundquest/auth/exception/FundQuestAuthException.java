package com.fundquest.auth.exception;

import lombok.Getter;

@Getter
public class FundQuestAuthException extends RuntimeException {
    private final String errorCode;

    public FundQuestAuthException(String message) {
        super(message);
        this.errorCode = null;
    }

    public FundQuestAuthException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public FundQuestAuthException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
    }

    public FundQuestAuthException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}