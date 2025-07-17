package com.fundquest.auth.service;

import com.fundquest.auth.dto.response.AuthResponse;
import com.fundquest.auth.dto.response.TokenResponse;
import com.fundquest.auth.entity.User;
import java.util.Optional;

public interface AuthService {

    AuthResponse authenticateWithMicrosoft(String idToken);

    TokenResponse refreshAccessToken(String refreshToken);

    User validateAccessToken(String accessToken);

    Optional<User> getUserByEmail(String email);

    void logout(String accessToken);

    Object parseToken(String token);
}
