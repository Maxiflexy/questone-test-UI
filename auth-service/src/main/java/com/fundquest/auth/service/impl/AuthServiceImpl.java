package com.fundquest.auth.service.impl;

import com.fundquest.auth.dto.response.AuthResponse;
import com.fundquest.auth.dto.response.TokenResponse;
import com.fundquest.auth.dto.response.UserResponse;
import com.fundquest.auth.entity.User;
import com.fundquest.auth.exception.AuthenticationException;
import com.fundquest.auth.exception.TokenValidationException;
import com.fundquest.auth.service.AuthService;
import com.fundquest.auth.service.JwtService;
import com.fundquest.auth.service.MicrosoftTokenService;
import com.fundquest.auth.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final MicrosoftTokenService microsoftTokenService;
    private final UserService userService;
    private final JwtService jwtService;

    @Autowired
    public AuthServiceImpl(MicrosoftTokenService microsoftTokenService,
                       UserService userService,
                       JwtService jwtService) {

        this.microsoftTokenService = microsoftTokenService;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    /**
     * Verify Microsoft ID token and authenticate user
     */
    public AuthResponse authenticateWithMicrosoft(String idToken) {
        try {
            // Step 1: Verify Microsoft ID token
            Map<String, Object> microsoftUserInfo = microsoftTokenService.verifyIdToken(idToken);

            // Step 2: Create or update user
            User user = userService.createOrUpdateUser(microsoftUserInfo);

            // Step 3: Generate FundQuest tokens
            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            // Step 4: Convert user to response DTO
            UserResponse userResponse = userService.convertToUserResponse(user);

            // Step 5: Create authentication response
            AuthResponse authResponse = new AuthResponse();
            authResponse.setAccessToken(accessToken);
            authResponse.setExpiresIn(jwtService.getAccessTokenExpirationInSeconds());
            authResponse.setTokenType("Bearer");
            authResponse.setUser(userResponse);

            return authResponse;

        } catch (TokenValidationException e) {
            throw new AuthenticationException("INVALID_TOKEN", "Invalid or expired Microsoft ID token: " + e.getMessage());
        } catch (Exception e) {
            throw new AuthenticationException("AUTHENTICATION_FAILED", "Authentication failed: " + e.getMessage());
        }
    }

    /**
     * Refresh access token using refresh token
     */
    public TokenResponse refreshAccessToken(String refreshToken) {
        try {
            // Validate refresh token
            if (!jwtService.isValidToken(refreshToken)) {
                throw new AuthenticationException("INVALID_REFRESH_TOKEN", "Invalid refresh token");
            }

            // Check if it's actually a refresh token
            if (!jwtService.isRefreshToken(refreshToken)) {
                throw new AuthenticationException("INVALID_REFRESH_TOKEN", "Token is not a refresh token");
            }

            // Check if token is expired
            if (jwtService.isTokenExpired(refreshToken)) {
                throw new AuthenticationException("INVALID_REFRESH_TOKEN", "Refresh token has expired");
            }

            // Get user information from refresh token
            UUID userId = jwtService.getUserIdFromToken(refreshToken);
            String email = jwtService.getEmailFromToken(refreshToken);

            // Validate user still exists and is active
            Optional<User> userOpt = userService.findById(userId);
            if (userOpt.isEmpty()) {
                throw new AuthenticationException("INVALID_REFRESH_TOKEN", "User not found");
            }

            User user = userOpt.get();
            if (!user.getIsActive()) {
                throw new AuthenticationException("INVALID_REFRESH_TOKEN", "User account is deactivated");
            }

            // Verify email matches
            if (!user.getEmail().equals(email)) {
                throw new AuthenticationException("INVALID_REFRESH_TOKEN", "Token email mismatch");
            }

            // Generate new access token
            String newAccessToken = jwtService.generateAccessToken(user);

            // Update user's last login
            userService.updateLastLogin(userId);

            return new TokenResponse(newAccessToken, jwtService.getAccessTokenExpirationInSeconds());

        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthenticationException("REFRESH_TOKEN_FAILED", "Failed to refresh token: " + e.getMessage());
        }
    }

    /**
     * Validate access token and get user information
     */
    public User validateAccessToken(String accessToken) {
        try {
            // Validate token structure and signature
            if (!jwtService.isValidToken(accessToken)) {
                throw new AuthenticationException("INVALID_ACCESS_TOKEN", "Invalid access token");
            }

            // Check if it's actually an access token
            if (!jwtService.isAccessToken(accessToken)) {
                throw new AuthenticationException("INVALID_ACCESS_TOKEN", "Token is not an access token");
            }

            // Check if token is expired
            if (jwtService.isTokenExpired(accessToken)) {
                throw new AuthenticationException("INVALID_ACCESS_TOKEN", "Access token has expired");
            }

            // Get user information from token
            UUID userId = jwtService.getUserIdFromToken(accessToken);
            String email = jwtService.getEmailFromToken(accessToken);

            // Validate user still exists and is active
            Optional<User> userOpt = userService.findById(userId);
            if (userOpt.isEmpty()) {
                throw new AuthenticationException("INVALID_ACCESS_TOKEN", "User not found");
            }

            User user = userOpt.get();
            if (!user.getIsActive()) {
                throw new AuthenticationException("INVALID_ACCESS_TOKEN", "User account is deactivated");
            }

            // Verify email matches
            if (!user.getEmail().equals(email)) {
                throw new AuthenticationException("INVALID_ACCESS_TOKEN", "Token email mismatch");
            }

            return user;

        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthenticationException("ACCESS_TOKEN_VALIDATION_FAILED", "Failed to validate access token: " + e.getMessage());
        }
    }

    /**
     * Get user profile by access token
     */
    public UserResponse getUserProfile(String accessToken) {
        User user = validateAccessToken(accessToken);
        return userService.convertToUserResponse(user);
    }

    /**
     * Logout user (invalidate tokens)
     * Note: With JWT, we can't truly invalidate tokens without a blacklist
     * This method can be extended to add token blacklisting if needed
     */
    public void logout(String accessToken) {
        // Validate the token to ensure it's valid before "logout"
        validateAccessToken(accessToken);

        // In a production system, you might want to:
        // 1. Add token to blacklist/revocation list
        // 2. Store revoked tokens in cache/database
        // 3. Log security event

        // For now, we'll just validate the token exists
        // The frontend will handle removing the token from cookies
    }

    /**
     * Get user by email
     */
    public Optional<User> getUserByEmail(String email) {
        return Optional.ofNullable(userService.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found")));
    }

    /**
     * Get user by Microsoft ID
     */
    public Optional<User> getUserByMicrosoftId(String microsoftId) {
        return userService.findByMicrosoftId(microsoftId);
    }

    /**
     * Check if user exists by email
     */
    public boolean userExistsByEmail(String email) {
        return userService.existsByEmail(email);
    }

    /**
     * Check if user exists by Microsoft ID
     */
    public boolean userExistsByMicrosoftId(String microsoftId) {
        return userService.existsByMicrosoftId(microsoftId);
    }

    /**
     * Deactivate user account
     */
    public void deactivateUser(UUID userId) {
        userService.deactivateUser(userId);
    }

    /**
     * Activate user account
     */
    public void activateUser(UUID userId) {
        userService.activateUser(userId);
    }

    /**
     * Parse JWT token and get user details (for debugging/admin purposes)
     */
    public Map<String, Object> parseToken(String token) {
        if (!jwtService.isValidToken(token)) {
            throw new AuthenticationException("INVALID_TOKEN", "Invalid token format");
        }

        return jwtService.parseUserFromToken(token);
    }
}