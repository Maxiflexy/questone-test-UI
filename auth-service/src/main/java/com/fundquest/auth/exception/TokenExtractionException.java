package com.fundquest.auth.exception;

import com.fundquest.auth.constants.AppConstants;

public class TokenExtractionException extends FundQuestAuthException {

    public TokenExtractionException(String message) {
        super(message, AppConstants.INVALID_AUTH_CODE);
    }
}