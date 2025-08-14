package com.fundquest.auth.exception;


public class BusinessException extends FundQuestAuthException {

    public BusinessException(String message) {
        super(message, "BUSINESS_LOGIC_ERROR");
    }

    public BusinessException(String message, String errorCode) {
        super(message, errorCode);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, "BUSINESS_LOGIC_ERROR", cause);
    }

    public BusinessException(String message, String errorCode, Throwable cause) {
        super(message, errorCode, cause);
    }
}