package com.fundquest.auth.controller;

import com.fundquest.auth.dto.request.MicrosoftTokenRequest;
import com.fundquest.auth.dto.response.ApiResponse;
import com.fundquest.auth.dto.response.AuthResponse;
import com.fundquest.auth.dto.response.TokenResponse;
import com.fundquest.auth.entity.User;
import com.fundquest.auth.service.AuthService;
import com.fundquest.auth.service.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${app.cookie.same-site:Lax}")
    private String cookieSameSite;

    @Autowired
    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    /**
     * Verify Microsoft ID Token
     * POST /auth/microsoft/verify
     */
    @PostMapping("/microsoft/verify")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyMicrosoftToken(
            @Valid @RequestBody MicrosoftTokenRequest request,
            HttpServletResponse response) {

        try {
            // Authenticate user with Microsoft ID token
            AuthResponse authResponse = authService.authenticateWithMicrosoft(request.getIdToken());

            // Generate refresh token - we need to get the actual User entity
            // Since AuthResponse contains UserResponse, we need to fetch the User entity
            Optional<User> user = authService.getUserByEmail(authResponse.getUser().getEmail());
            String refreshToken = jwtService.generateRefreshToken(user.get());

            // Set refresh token as HttpOnly cookie
            Cookie refreshTokenCookie = createRefreshTokenCookie(refreshToken);
            response.addCookie(refreshTokenCookie);

            // Return success response
            ApiResponse<AuthResponse> apiResponse = ApiResponse.success(authResponse);
            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            // Error handling is done by GlobalExceptionHandler
            throw e;
        }
    }

    /**
     * Refresh Access Token
     * POST /auth/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response) {

        try {
            // Get refresh token from cookie
            String refreshToken = getRefreshTokenFromCookie(request);

            if (refreshToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("NO_REFRESH_TOKEN", "Refresh token not found in cookies"));
            }

            // Refresh access token
            TokenResponse tokenResponse = authService.refreshAccessToken(refreshToken);

            // Optionally, you can generate a new refresh token and update the cookie
            // For now, we'll keep the existing refresh token

            // Return success response
            ApiResponse<TokenResponse> apiResponse = ApiResponse.success(tokenResponse);
            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            // Clear the refresh token cookie on error
            Cookie clearCookie = createRefreshTokenCookie("");
            clearCookie.setMaxAge(0);
            response.addCookie(clearCookie);

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
            String authHeader = request.getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String accessToken = authHeader.substring(7);

                // Validate token and log out user
                authService.logout(accessToken);
            }

            // Clear refresh token cookie
            Cookie clearCookie = createRefreshTokenCookie("");
            clearCookie.setMaxAge(0);
            response.addCookie(clearCookie);

            // Return success response
            ApiResponse<Void> apiResponse = ApiResponse.success("Successfully logged out");
            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            // Even if logout fails, still clear the cookie
            Cookie clearCookie = createRefreshTokenCookie("");
            clearCookie.setMaxAge(0);
            response.addCookie(clearCookie);

            throw e;
        }
    }

    /**
     * Health Check Endpoint
     * GET /auth/health
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        ApiResponse<String> response = ApiResponse.success("FundQuest Auth Service is healthy");
        return ResponseEntity.ok(response);
    }

    /**
     * Get token info (for debugging - remove in production)
     * POST /auth/token/info
     */
    @PostMapping("/token/info")
    public ResponseEntity<ApiResponse<?>> getTokenInfo(@RequestBody String token) {
        try {
            var tokenInfo = authService.parseToken(token);
            return ResponseEntity.ok(ApiResponse.success(tokenInfo));
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * Create refresh token cookie
     */
    private Cookie createRefreshTokenCookie(String refreshToken) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath("/");
        cookie.setMaxAge((int) jwtService.getRefreshTokenExpirationInSeconds());

        // Note: SameSite attribute is not directly supported in Cookie class
        // You might need to set it via response headers or use a custom solution
        return cookie;
    }

    /**
     * Get refresh token from request cookies
     */
    private String getRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}