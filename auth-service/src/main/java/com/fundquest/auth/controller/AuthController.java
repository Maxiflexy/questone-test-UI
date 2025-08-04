package com.fundquest.auth.controller;

import com.fundquest.auth.config.swagger.SwaggerConstants;
import com.fundquest.auth.constants.AppConstants;
import com.fundquest.auth.dto.request.VerifyMicrosoftTokenRequest;
import com.fundquest.auth.dto.response.ApiResponse;
import com.fundquest.auth.dto.response.AuthResponse;
import com.fundquest.auth.exception.InvalidTokenException;
import com.fundquest.auth.service.AuthService;
import com.fundquest.auth.util.AuthResponseHelper;
import com.fundquest.auth.util.CookieHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import static com.fundquest.auth.config.swagger.SwaggerConstants.AUTH_TAG;
import static com.fundquest.auth.constants.AppConstants.*;

@RestController
@RequestMapping(AUTH_BASE_PATH)
@RequiredArgsConstructor
@Slf4j
@Tag(
        name = AUTH_TAG,
        description = SwaggerConstants.AUTH_TAG_DESCRIPTION
)
public class AuthController {

    private final AuthService authService;
    private final AuthResponseHelper authResponseHelper;
    private final CookieHelper cookieHelper;

    @PostMapping(MICROSOFT_VERIFY_ENDPOINT)
    @Operation(
            summary = SwaggerConstants.Auth.VERIFY_MICROSOFT_SUMMARY,
            description = SwaggerConstants.Auth.VERIFY_MICROSOFT_DESCRIPTION,
            tags = {AUTH_TAG}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Authentication successful - User verified and JWT tokens generated",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "Successful Authentication",
                                    value = SwaggerConstants.Auth.AUTH_RESPONSE_EXAMPLE,
                                    description = "Successful authentication with access token and user information"
                            )
                    )
            )
    })
    public ResponseEntity<ApiResponse<AuthResponse>> verifyMicrosoftToken(
            @Parameter(
                    description = "Microsoft OAuth2 authorization code received from frontend authentication flow",
                    required = true,
                    schema = @Schema(implementation = VerifyMicrosoftTokenRequest.class),
                    example = SwaggerConstants.Auth.VERIFY_REQUEST_EXAMPLE
            )
            @Valid @RequestBody VerifyMicrosoftTokenRequest request,
            HttpServletResponse response) {

        log.info("Received Microsoft token verification request");
        System.out.println("Received Microsoft token verification request");

        AuthResponse authResponse = authService.verifyMicrosoftToken(request);

        authResponseHelper.prepareAuthResponseWithCookie(authResponse, response);

        log.info("Successfully verified Microsoft token and set refresh token cookie");

        return ResponseEntity.ok(ApiResponse.success(authResponse));
    }

    @PostMapping(REFRESH_TOKEN_ENDPOINT)
    @Operation(
            summary = SwaggerConstants.Auth.REFRESH_TOKEN_SUMMARY,
            description = SwaggerConstants.Auth.REFRESH_TOKEN_DESCRIPTION,
            tags = {AUTH_TAG}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Token refresh successful - New access token generated",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "Successful Token Refresh",
                                    value = SwaggerConstants.Auth.AUTH_RESPONSE_EXAMPLE,
                                    description = "New access token generated successfully"
                            )
                    )
            )
    })
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @Parameter(hidden = true) HttpServletRequest request) {

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


    @PostMapping(LOGOUT_ENDPOINT)
    @Operation(
            summary = SwaggerConstants.Auth.LOGOUT_SUMMARY,
            description = SwaggerConstants.Auth.LOGOUT_DESCRIPTION,
            tags = {AUTH_TAG},
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Logout successful - User session cleared",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "Successful Logout",
                                    value = SwaggerConstants.SUCCESS_LOGOUT_RESPONSE,
                                    description = "User logged out successfully"
                            )
                    )
            )
    })
    public ResponseEntity<ApiResponse<String>> logout(
            @Parameter(hidden = true) HttpServletResponse response) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof String email) {
            authService.logout(email);
        }
        authResponseHelper.clearAuthSession(response);
        log.info("Successfully logged out user");
        return ResponseEntity.ok(ApiResponse.success(LOGOUT_SUCCESS));
    }
}