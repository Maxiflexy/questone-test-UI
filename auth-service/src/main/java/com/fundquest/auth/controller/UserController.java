package com.fundquest.auth.controller;

import com.fundquest.auth.config.swagger.SwaggerConstants;
import com.fundquest.auth.dto.response.ApiResponse;
import com.fundquest.auth.dto.response.AuthUserData;
import com.fundquest.auth.entity.User;
import com.fundquest.auth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.fundquest.auth.constants.AppConstants.USER_PROFILE_ENDPOINT;

@RestController
@RequestMapping(USER_PROFILE_ENDPOINT)
@RequiredArgsConstructor
@Slf4j
@Tag(
        name = SwaggerConstants.USER_TAG,
        description = SwaggerConstants.USER_TAG_DESCRIPTION
)
public class UserController {

    private final UserService userService;

    @GetMapping()
    @Operation(
            summary = SwaggerConstants.User.GET_PROFILE_SUMMARY,
            description = SwaggerConstants.User.GET_PROFILE_DESCRIPTION,
            tags = {SwaggerConstants.USER_TAG},
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Profile retrieved successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "User Profile",
                                    value = SwaggerConstants.User.PROFILE_RESPONSE_EXAMPLE,
                                    description = "Complete user profile information"
                            )
                    )
            )
    })
    public ResponseEntity<ApiResponse<AuthUserData>> getUserProfile() {

        log.info("Received user profile request");

        // Get email from security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = (String) authentication.getPrincipal();

        log.debug("Fetching profile for user: {}", email);

        User user = userService.findByEmail(email);

        AuthUserData profileResponse = AuthUserData.builder()
                .email(user.getEmail())
                .name(user.getName())
                .microsoftId(user.getMicrosoftId())
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .build();

        log.info("Successfully retrieved user profile for: {}", email);

        return ResponseEntity.ok(ApiResponse.success(profileResponse));
    }

    @GetMapping("/test")
    //@PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(
            summary = SwaggerConstants.User.TEST_SUPER_ADMIN_SUMMARY,
            description = SwaggerConstants.User.TEST_SUPER_ADMIN_DESCRIPTION,
            tags = {SwaggerConstants.USER_TAG},
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Super Admin access confirmed",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "Success Response",
                                    value = "\"Message received!!!\"",
                                    description = "Confirmation that user has Super Admin access"
                            )
                    )
            )
    })
    public ResponseEntity<?> test() {
        log.info("Message received!!!");
        return ResponseEntity.ok("Message received!!!");
    }
}