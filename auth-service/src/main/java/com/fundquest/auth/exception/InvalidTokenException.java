package com.fundquest.auth.exception;

import com.fundquest.auth.constants.AppConstants;

public class InvalidTokenException extends FundQuestAuthException {

    public InvalidTokenException(String message) {
        super(message, AppConstants.UNAUTHORIZED);
    }

    public InvalidTokenException(String message, String errorCode) {
        super(message, errorCode);
    }
}