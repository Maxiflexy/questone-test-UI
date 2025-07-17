package com.fundquest.auth.controller;

import com.fundquest.auth.dto.request.MicrosoftTokenRequest;
import com.fundquest.auth.dto.response.ApiResponse;
import com.fundquest.auth.dto.response.AuthResponse;
import com.fundquest.auth.dto.response.TokenResponse;
import com.fundquest.auth.entity.User;
import com.fundquest.auth.facade.AuthFacade;
import com.fundquest.auth.util.CookieHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * REST Controller for authentication operations
 * Delegates business logic to AuthFacade
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthFacade authFacade;
    private final CookieHelper cookieHelper;

    @Autowired
    public AuthController(AuthFacade authFacade, CookieHelper cookieHelper) {
        this.authFacade = authFacade;
        this.cookieHelper = cookieHelper;
    }

    /**
     * Verify Microsoft ID Token
     */
    @PostMapping("/microsoft/verify")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyMicrosoftToken(
            @Valid @RequestBody MicrosoftTokenRequest request,
            HttpServletResponse response) {

        // Authenticate user with Microsoft ID token
        AuthResponse authResponse = authFacade.authenticateWithMicrosoft(request);

        // Generate refresh token
        Optional<User> user = authFacade.getUserByEmail(authResponse.getUser().getEmail());
        if (user.isPresent()) {
            String refreshToken = authFacade.generateRefreshToken(user.get());

            // Set refresh token as HttpOnly cookie
            cookieHelper.addRefreshTokenCookie(
                    response,
                    refreshToken,
                    authFacade.getRefreshTokenExpirationInSeconds()
            );
        }

        return ResponseEntity.ok(ApiResponse.success(authResponse));
    }

    /**
     * Refresh Access Token
     * POST /auth/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response) {

        // Get refresh token from cookie
        String refreshToken = cookieHelper.getRefreshTokenFromCookie(request);

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("NO_REFRESH_TOKEN", "Refresh token not found in cookies"));
        }

        try {
            // Refresh access token
            TokenResponse tokenResponse = authFacade.refreshAccessToken(refreshToken);
            return ResponseEntity.ok(ApiResponse.success(tokenResponse));

        } catch (Exception e) {
            // Clear the refresh token cookie on error
            cookieHelper.clearRefreshTokenCookie(response);
            throw e;
        }
    }

    /**
     * Logout
     * POST /auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletRequest request,
            HttpServletResponse response) {

        try {
            // Get access token from Authorization header
            String accessToken = cookieHelper.extractBearerToken(request);

            // Logout user
            authFacade.logout(accessToken);

            return ResponseEntity.ok(ApiResponse.successVoid("Successfully logged out"));

        } finally {
            // Always clear the refresh token cookie
            cookieHelper.clearRefreshTokenCookie(response);
        }
    }

    /**
     * Health Check Endpoint
     * GET /auth/health
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("FundQuest Auth Service is healthy"));
    }

    /**
     * Get token info (for debugging - remove in production)
     * POST /auth/token/info
     */
    @PostMapping("/token/info")
    public ResponseEntity<ApiResponse<Object>> getTokenInfo(@RequestBody String token) {
        Object tokenInfo = authFacade.parseToken(token);
        return ResponseEntity.ok(ApiResponse.success(tokenInfo));
    }
}