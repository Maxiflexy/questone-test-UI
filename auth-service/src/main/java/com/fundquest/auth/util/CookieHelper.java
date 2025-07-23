package com.fundquest.auth.util;

import com.fundquest.auth.constants.AppConstants;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class CookieHelper {

    /**
     * Sets refresh token as HTTP-only secure cookie
     */
    public void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie refreshTokenCookie = new Cookie(AppConstants.REFRESH_TOKEN_COOKIE, refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(AppConstants.COOKIE_MAX_AGE);
        refreshTokenCookie.setAttribute("SameSite", "Lax");

        response.addCookie(refreshTokenCookie);
        log.debug("Refresh token cookie set successfully");
    }

    /**
     * Clears refresh token cookie by setting it to empty with zero max age
     */
    public void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie refreshTokenCookie = new Cookie(AppConstants.REFRESH_TOKEN_COOKIE, "");
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0);
        refreshTokenCookie.setAttribute("SameSite", "Lax");

        response.addCookie(refreshTokenCookie);
        log.debug("Refresh token cookie cleared successfully");
    }

    /**
     * Extracts refresh token from request cookies
     */
    public Optional<String> extractRefreshTokenFromCookies(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (AppConstants.REFRESH_TOKEN_COOKIE.equals(cookie.getName())) {
                    String value = cookie.getValue();
                    if (value != null && !value.trim().isEmpty()) {
                        log.debug("Refresh token extracted from cookies");
                        return Optional.of(value);
                    }
                }
            }
        }
        log.debug("No refresh token found in cookies");
        return Optional.empty();
    }

    /**
     * Validates if refresh token exists in cookies
     */
    public boolean hasRefreshToken(HttpServletRequest request) {
        return extractRefreshTokenFromCookies(request).isPresent();
    }
}