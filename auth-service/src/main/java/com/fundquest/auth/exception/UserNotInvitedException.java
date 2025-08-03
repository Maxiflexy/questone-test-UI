package com.fundquest.auth.exception;

import com.fundquest.auth.constants.AppConstants;

public class UserNotInvitedException extends FundQuestAuthException {

    public UserNotInvitedException(String message) {
        super(message, "USER_NOT_INVITED");
    }

    public UserNotInvitedException(String message, String errorCode) {
        super(message, errorCode);
    }
}
