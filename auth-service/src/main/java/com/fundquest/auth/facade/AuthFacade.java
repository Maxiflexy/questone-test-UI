package com.fundquest.auth.facade;

import com.fundquest.auth.dto.request.MicrosoftTokenRequest;
import com.fundquest.auth.dto.response.AuthResponse;
import com.fundquest.auth.dto.response.TokenResponse;
import com.fundquest.auth.entity.User;
import com.fundquest.auth.service.AuthService;
import com.fundquest.auth.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Facade for authentication operations
 * Handles business logic and coordinates between services
 */
@Component
public class AuthFacade {

    private final AuthService authService;
    private final JwtService jwtService;

    @Autowired
    public AuthFacade(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    /**
     * Handle Microsoft token verification and authentication
     */
    public AuthResponse authenticateWithMicrosoft(MicrosoftTokenRequest request) {
        return authService.authenticateWithMicrosoft(request.getIdToken());
    }

    /**
     * Handle access token refresh
     */
    public TokenResponse refreshAccessToken(String refreshToken) {
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            throw new IllegalArgumentException("Refresh token is required");
        }
        return authService.refreshAccessToken(refreshToken);
    }

    /**
     * Handle user logout
     */
    public void logout(String accessToken) {
        if (accessToken != null && !accessToken.trim().isEmpty()) {
            authService.logout(accessToken);
        }
    }

    /**
     * Generate refresh token for user
     */
    public String generateRefreshToken(User user) {
        return jwtService.generateRefreshToken(user);
    }

    /**
     * Get refresh token expiration time
     */
    public long getRefreshTokenExpirationInSeconds() {
        return jwtService.getRefreshTokenExpirationInSeconds();
    }

    /**
     * Parse token for debugging purposes
     */
    public Object parseToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token is required");
        }
        return authService.parseToken(token);
    }

    /**
     * Get user by email
     */
    public Optional<User> getUserByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return Optional.empty();
        }
        return authService.getUserByEmail(email);
    }

    /**
     * Validate access token and get user
     */
    public User validateAccessToken(String accessToken) {
        return authService.validateAccessToken(accessToken);
    }
}