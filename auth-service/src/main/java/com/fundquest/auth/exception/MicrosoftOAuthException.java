package com.fundquest.auth.exception;

import com.fundquest.auth.constants.AppConstants;

public class MicrosoftOAuthException extends FundQuestAuthException {

    public MicrosoftOAuthException(String message) {
        super(message, AppConstants.INVALID_AUTH_CODE);
    }

    public MicrosoftOAuthException(String message, String errorCode) {
        super(message, errorCode);
    }
}