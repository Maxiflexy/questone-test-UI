package com.fundquest.auth.service;

import com.fundquest.auth.dto.request.VerifyMicrosoftTokenRequest;
import com.fundquest.auth.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse verifyMicrosoftToken(VerifyMicrosoftTokenRequest request);
    AuthResponse refreshAccessToken(String refreshToken);
    void logout(String email);
}