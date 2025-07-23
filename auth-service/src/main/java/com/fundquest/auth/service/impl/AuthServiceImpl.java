package com.fundquest.auth.service.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fundquest.auth.constants.AppConstants;
import com.fundquest.auth.dto.request.VerifyMicrosoftTokenRequest;
import com.fundquest.auth.dto.response.AuthResponse;
import com.fundquest.auth.entity.User;
import com.fundquest.auth.exception.InvalidTokenException;
import com.fundquest.auth.exception.TokenExtractionException;
import com.fundquest.auth.service.AuthService;
import com.fundquest.auth.service.JwtService;
import com.fundquest.auth.service.MicrosoftOAuthService;
import com.fundquest.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {

    private final MicrosoftOAuthService microsoftOAuthService;
    private final UserService userService;
    private final JwtService jwtService;

    @Override
    public AuthResponse verifyMicrosoftToken(VerifyMicrosoftTokenRequest request) {
        try {
            // Exchange authorization code for Microsoft tokens
            JSONObject tokenResponse = microsoftOAuthService.exchangeCodeForToken(request.getAuthCode());

            // Extract ID token
            String idToken = tokenResponse.optString("id_token");
            if (idToken == null || idToken.trim().isEmpty()) {
                throw new TokenExtractionException("ID token not found in Microsoft response");
            }

            // Decode the ID token without verification (Microsoft already verified it)
            DecodedJWT decodedJWT = JWT.decode(idToken);

            // Extract user information
            String microsoftId = extractClaim(decodedJWT, "oid", "Microsoft user ID");
            String name = decodedJWT.getClaim("name").asString();
            String email = extractEmail(decodedJWT);
            String preferredUsername = decodedJWT.getClaim("preferred_username").asString();

            // Create or update user
            User user = userService.createOrUpdateUser(microsoftId, email, name, preferredUsername);

            // Generate application tokens
            String accessToken = jwtService.generateAccessToken(user);

            log.info("Successfully verified Microsoft token for user: {}", email);

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .expiresIn(AppConstants.ACCESS_TOKEN_EXPIRY / 1000) // Convert to seconds
                    .tokenType(AppConstants.BEARER_TOKEN_TYPE)
                    .build();

        } catch (Exception e) {
            log.error("Error verifying Microsoft token", e);
            throw e;
        }
    }

    @Override
    public AuthResponse refreshAccessToken(String refreshToken) {
        if (!jwtService.validateToken(refreshToken)) {
            throw new InvalidTokenException("Invalid refresh token", AppConstants.INVALID_REFRESH_TOKEN);
        }

        String tokenType = jwtService.extractTokenType(refreshToken);
        if (!AppConstants.TOKEN_TYPE_REFRESH.equals(tokenType)) {
            throw new InvalidTokenException("Token is not a refresh token", AppConstants.INVALID_REFRESH_TOKEN);
        }

        String email = jwtService.extractEmailFromToken(refreshToken);
        User user = userService.findByEmail(email);

        // Update last login
        userService.updateLastLogin(email);

        // Generate new access token
        String accessToken = jwtService.generateAccessToken(user);

        log.info("Successfully refreshed access token for user: {}", email);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .expiresIn(AppConstants.ACCESS_TOKEN_EXPIRY / 1000)
                .tokenType(AppConstants.BEARER_TOKEN_TYPE)
                .build();
    }

    @Override
    public void logout(String email) {

        //todo
        // blacklist the  bearer token
        // Update user's last activity or mark session as invalid if needed
        log.info("User {} logged out successfully", email);
        // Note: JWT tokens are stateless, so we rely on token expiration
        // In production, you might want to maintain a blacklist of tokens
    }

    private String extractClaim(DecodedJWT jwt, String claimName, String claimDescription) {
        String claimValue = jwt.getClaim(claimName).asString();
        if (claimValue == null || claimValue.trim().isEmpty()) {
            throw new TokenExtractionException(claimDescription + " not found in Microsoft ID token");
        }
        return claimValue;
    }

    private String extractEmail(DecodedJWT jwt) {
        String email = jwt.getClaim("email").asString();
        if (email == null || email.trim().isEmpty()) {
            email = jwt.getClaim("preferred_username").asString();
        }

        if (email == null || email.trim().isEmpty()) {
            throw new TokenExtractionException("Email not found in Microsoft ID token");
        }

        return email;
    }
}