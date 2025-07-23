package com.fundquest.auth.controller;

import com.fundquest.auth.constants.AppConstants;
import com.fundquest.auth.dto.request.VerifyMicrosoftTokenRequest;
import com.fundquest.auth.dto.response.ApiResponse;
import com.fundquest.auth.dto.response.AuthResponse;
import com.fundquest.auth.entity.User;
import com.fundquest.auth.exception.InvalidTokenException;
import com.fundquest.auth.service.AuthService;
import com.fundquest.auth.service.JwtService;
import com.fundquest.auth.service.UserService;
import com.fundquest.auth.util.AuthResponseHelper;
import com.fundquest.auth.util.CookieHelper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(AppConstants.AUTH_BASE_PATH)
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final AuthResponseHelper authResponseHelper;
    private final CookieHelper cookieHelper;

    @PostMapping("/microsoft/verify")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyMicrosoftToken(
            @Valid @RequestBody VerifyMicrosoftTokenRequest request,
            HttpServletResponse response) {

        log.info("Received Microsoft token verification request");

        AuthResponse authResponse = authService.verifyMicrosoftToken(request);

        authResponseHelper.prepareAuthResponseWithCookie(authResponse, response);

        log.info("Successfully verified Microsoft token and set refresh token cookie");

        return ResponseEntity.ok(ApiResponse.success(authResponse));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(HttpServletRequest request) {

        log.info("Received token refresh request");

        String refreshToken = cookieHelper.extractRefreshTokenFromCookies(request)
                .orElseThrow(() -> new InvalidTokenException(
                        "Refresh token not found in cookies",
                        AppConstants.NO_REFRESH_TOKEN
                ));

        AuthResponse authResponse = authService.refreshAccessToken(refreshToken);

        log.info("Successfully refreshed access token");

        return ResponseEntity.ok(ApiResponse.success(authResponse));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletResponse response) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof String email) {
            authService.logout(email);
        }
        authResponseHelper.clearAuthSession(response);
        log.info("Successfully logged out user");
        return ResponseEntity.ok(ApiResponse.success(AppConstants.LOGOUT_SUCCESS));
    }
}