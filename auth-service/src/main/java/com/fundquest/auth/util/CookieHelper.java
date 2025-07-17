package com.fundquest.auth.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Utility class for cookie operations
 */
@Component
public class CookieHelper {

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${app.cookie.same-site:Lax}")
    private String cookieSameSite;

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    private static final String COOKIE_PATH = "/";

    /**
     * Create refresh token cookie
     */
    public Cookie createRefreshTokenCookie(String refreshToken, long maxAgeInSeconds) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath(COOKIE_PATH);
        cookie.setMaxAge((int) maxAgeInSeconds);

        // Note: SameSite attribute is not directly supported in Cookie class
        // In production, you might want to set it via response headers
        return cookie;
    }

    /**
     * Create cookie to clear refresh token
     */
    public Cookie createClearRefreshTokenCookie() {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath(COOKIE_PATH);
        cookie.setMaxAge(0);
        return cookie;
    }

    /**
     * Get refresh token from request cookies
     */
    public String getRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }

    /**
     * Add refresh token cookie to response
     */
    public void addRefreshTokenCookie(HttpServletResponse response, String refreshToken, long maxAgeInSeconds) {
        Cookie cookie = createRefreshTokenCookie(refreshToken, maxAgeInSeconds);
        response.addCookie(cookie);
    }

    /**
     * Clear refresh token cookie from response
     */
    public void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = createClearRefreshTokenCookie();
        response.addCookie(cookie);
    }

    /**
     * Extract bearer token from authorization header
     */
    public String extractBearerToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        return null;
    }
}