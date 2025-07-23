package com.fundquest.auth.service;

import org.json.JSONObject;

public interface MicrosoftOAuthService {
    JSONObject exchangeCodeForToken(String authCode);
}