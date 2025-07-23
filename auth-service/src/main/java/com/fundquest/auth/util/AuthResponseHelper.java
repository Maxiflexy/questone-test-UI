package com.fundquest.auth.util;

import com.fundquest.auth.dto.response.AuthResponse;
import com.fundquest.auth.entity.User;
import com.fundquest.auth.service.JwtService;
import com.fundquest.auth.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthResponseHelper {

    private final JwtService jwtService;
    private final UserService userService;
    private final CookieHelper cookieHelper;

    /**
     * Prepares complete authentication response with refresh token cookie
     */
    public void prepareAuthResponseWithCookie(AuthResponse authResponse, HttpServletResponse response) {
        try {
            // Extract email from access token to get user
            String email = jwtService.extractEmailFromToken(authResponse.getAccessToken());
            User user = userService.findByEmail(email);

            // Generate refresh token
            String refreshToken = jwtService.generateRefreshToken(user);

            // Set refresh token as HTTP-only cookie
            cookieHelper.setRefreshTokenCookie(response, refreshToken);

            log.info("Auth response prepared with refresh token cookie for user: {}", email);

        } catch (Exception e) {
            log.error("Error preparing auth response with cookie", e);
            throw new RuntimeException("Failed to prepare authentication response", e);
        }
    }

    /**
     * Clears authentication session by removing refresh token cookie
     */
    public void clearAuthSession(HttpServletResponse response) {
        cookieHelper.clearRefreshTokenCookie(response);
        log.debug("Authentication session cleared");
    }
}