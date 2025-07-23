package com.fundquest.auth.controller;

import com.fundquest.auth.constants.AppConstants;
import com.fundquest.auth.dto.response.ApiResponse;
import com.fundquest.auth.dto.response.UserProfileResponse;
import com.fundquest.auth.entity.User;
import com.fundquest.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserProfile() {

        log.info("Received user profile request");

        // Get email from security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = (String) authentication.getPrincipal();

        log.debug("Fetching profile for user: {}", email);

        User user = userService.findByEmail(email);

        UserProfileResponse profileResponse = UserProfileResponse.builder()
                .email(user.getEmail())
                .name(user.getName())
                .microsoftId(user.getMicrosoftId())
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .build();

        log.info("Successfully retrieved user profile for: {}", email);

        return ResponseEntity.ok(ApiResponse.success(profileResponse));
    }
}