package com.fundquest.auth.service.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fundquest.auth.audit_trail.annotation.Auditable;
import com.fundquest.auth.constants.AppConstants;
import com.fundquest.auth.dto.request.VerifyMicrosoftTokenRequest;
import com.fundquest.auth.dto.response.AuthResponse;
import com.fundquest.auth.entity.User;
import com.fundquest.auth.exception.InvalidTokenException;
import com.fundquest.auth.exception.TokenExtractionException;
import com.fundquest.auth.exception.UserNotInvitedException;
import com.fundquest.auth.service.AuthService;
import com.fundquest.auth.service.JwtService;
import com.fundquest.auth.service.MicrosoftOAuthService;
import com.fundquest.auth.service.UserService;
import com.fundquest.auth.util.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.fundquest.auth.audit_trail.entity.enums.ActionType.LOGIN;
import static com.fundquest.auth.audit_trail.entity.enums.ActionType.LOGOUT;
import static com.fundquest.auth.audit_trail.entity.enums.ResourceType.AUTHENTICATION;
import static com.fundquest.auth.constants.AppConstants.USER_NOT_INVITED;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {

    private final MicrosoftOAuthService microsoftOAuthService;
    private final UserService userService;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    @Override
    @Auditable(
            actionType = LOGIN,
            description = "User successfully authenticated via Microsoft OAuth",
            resourceType = AUTHENTICATION,
            resourceIdExpression = "#result != null ? #result.user.email : 'unknown'",
            resourceIdentifierExpression = "#result != null ? #result.user.email : 'unknown'",
            includeResult = true
    )
    public AuthResponse verifyMicrosoftToken(VerifyMicrosoftTokenRequest request) {
        try {
            log.info("Starting Microsoft token verification");

            // 1. Exchange authorization code for Microsoft tokens
            JSONObject tokenResponse = microsoftOAuthService.exchangeCodeForToken(request.getAuthCode());

            // 2. Extract and validate ID token
            String idToken = extractIdToken(tokenResponse);
            DecodedJWT decodedJWT = JWT.decode(idToken);

            // 3. Extract user information from token
            UserInfo userInfo = extractUserInfo(decodedJWT);

            // 4. Verify user invitation status (KEY SECURITY CHECK)
            validateUserInvitation(userInfo.email());

            // 5. Complete Microsoft verification and update user
            User user = userService.completeMicrosoftVerification(
                    userInfo.microsoftId(),
                    userInfo.email(),
                    userInfo.name(),
                    userInfo.preferredUsername()
            );

            // 6. Generate JWT with user's specific permissions
            String accessToken = jwtService.generateAccessToken(user);

            log.info("Successfully verified Microsoft token for user: {}", userInfo.email());

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .expiresIn(AppConstants.ACCESS_TOKEN_EXPIRY / 1000)
                    .tokenType(AppConstants.BEARER_TOKEN_TYPE)
                    .user(userMapper.toAuthUserData(user))
                    .build();

        } catch (UserNotInvitedException e) {
            log.warn("Uninvited user attempted authentication: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error during Microsoft token verification", e);
            throw e;
        }
    }

    @Override
    @Auditable(
            actionType = LOGIN,
            description = "Access token refreshed for authenticated user",
            resourceType = AUTHENTICATION,
            resourceIdExpression = "#result != null ? #result.user.email : 'unknown'",
            resourceIdentifierExpression = "#result != null ? #result.user.email : 'unknown'",
            includeResult = true
    )
    public AuthResponse refreshAccessToken(String refreshToken) {
        log.info("Processing token refresh request");

        validateRefreshToken(refreshToken);

        String email = jwtService.extractEmailFromToken(refreshToken);
        User user = userService.findByEmail(email);

        userService.updateLastLogin(email);

        String accessToken = jwtService.generateAccessToken(user);

        log.info("Successfully refreshed access token for user: {}", email);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .expiresIn(AppConstants.ACCESS_TOKEN_EXPIRY / 1000)
                .tokenType(AppConstants.BEARER_TOKEN_TYPE)
                .user(userMapper.toAuthUserData(user))
                .build();
    }

    @Override
    @Auditable(
            actionType = LOGOUT,
            description = "User logged out from the system",
            resourceType = AUTHENTICATION,
            resourceIdExpression = "#email",
            resourceIdentifierExpression = "#email"
    )
    public void logout(String email) {
        log.info("Processing logout for user: {}", email);

        // TODO: Implement token blacklisting for enhanced security
        // For now, rely on token expiration since JWT is stateless

        log.info("User {} logged out successfully", email);
    }

    // Helper methods following SRP

    private String extractIdToken(JSONObject tokenResponse) {
        String idToken = tokenResponse.optString("id_token");
        if (idToken == null || idToken.trim().isEmpty()) {
            throw new TokenExtractionException("ID token not found in Microsoft response");
        }
        return idToken;
    }

    private UserInfo extractUserInfo(DecodedJWT decodedJWT) {
        String microsoftId = extractClaim(decodedJWT, "oid", "Microsoft user ID");
        String name = decodedJWT.getClaim("name").asString();
        String email = extractEmail(decodedJWT);
        String preferredUsername = decodedJWT.getClaim("preferred_username").asString();

        return new UserInfo(microsoftId, email, name, preferredUsername);
    }

    private void validateUserInvitation(String email) {
        if (!userService.isUserInvited(email)) {
            throw new UserNotInvitedException(
                    "User has not been invited to access this application. Please contact your administrator.",
                    USER_NOT_INVITED
            );
        }
    }

    private void validateRefreshToken(String refreshToken) {
        if (!jwtService.validateToken(refreshToken)) {
            throw new InvalidTokenException("Invalid refresh token", AppConstants.INVALID_REFRESH_TOKEN);
        }

        String tokenType = jwtService.extractTokenType(refreshToken);
        if (!AppConstants.TOKEN_TYPE_REFRESH.equals(tokenType)) {
            throw new InvalidTokenException("Token is not a refresh token", AppConstants.INVALID_REFRESH_TOKEN);
        }
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

    private record UserInfo(String microsoftId, String email, String name, String preferredUsername) {}
}