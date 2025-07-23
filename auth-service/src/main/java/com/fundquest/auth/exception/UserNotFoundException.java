package com.fundquest.auth.exception;

import com.fundquest.auth.constants.AppConstants;

public class UserNotFoundException extends FundQuestAuthException {

    public UserNotFoundException(String message) {
        super(message, AppConstants.UNAUTHORIZED);
    }
}