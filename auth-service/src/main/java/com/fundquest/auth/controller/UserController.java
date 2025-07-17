package com.fundquest.auth.controller;

import com.fundquest.auth.dto.response.ApiResponse;
import com.fundquest.auth.dto.response.UserResponse;
import com.fundquest.auth.entity.User;
import com.fundquest.auth.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Get User Profile
     * GET /api/user/profile
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getUserProfile(HttpServletRequest request) {
        try {
            // Get current user from request attributes (set by JwtAuthenticationFilter)
            User currentUser = (User) request.getAttribute("currentUser");

            if (currentUser == null) {
                // Fallback: get from Security Context
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String email = authentication.getName();
                currentUser = userService.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("User not found"));
            }

            // Convert to response DTO
            UserResponse userResponse = userService.convertToUserResponse(currentUser);

            // Return success response
            ApiResponse<UserResponse> apiResponse = ApiResponse.success(userResponse);
            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * Update User Profile (Protected Endpoint)
     * PUT /api/user/profile
     */
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserProfile(
            @RequestBody Map<String, Object> updates,
            HttpServletRequest request) {

        try {
            // Get current user from request attributes
            User currentUser = (User) request.getAttribute("currentUser");

            if (currentUser == null) {
                // Fallback: get from Security Context
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String email = authentication.getName();
                currentUser = userService.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("User not found"));
            }

            // Update user profile
            User updatedUser = userService.updateUserProfile(currentUser.getId(), updates);

            // Convert to response DTO
            UserResponse userResponse = userService.convertToUserResponse(updatedUser);

            // Return success response
            ApiResponse<UserResponse> apiResponse = ApiResponse.success(userResponse);
            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * Get User by ID (Admin endpoint - for future use)
     * GET /api/user/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable UUID id) {
        try {
            User user = userService.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

            UserResponse userResponse = userService.convertToUserResponse(user);
            ApiResponse<UserResponse> apiResponse = ApiResponse.success(userResponse);

            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * Deactivate User (Admin endpoint - for future use)
     * DELETE /api/user/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(@PathVariable UUID id) {
        try {
            userService.deactivateUser(id);

            ApiResponse<Void> apiResponse = ApiResponse.success("User deactivated successfully");
            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * Activate User (Admin endpoint - for future use)
     * POST /api/user/{id}/activate
     */
    @PostMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> activateUser(@PathVariable UUID id) {
        try {
            userService.activateUser(id);

            ApiResponse<Void> apiResponse = ApiResponse.success("User activated successfully");
            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * Get User Statistics (Admin endpoint - for future use)
     * GET /api/user/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserStats() {
        try {
            long activeUsersCount = userService.getActiveUsersCount();

            Map<String, Object> stats = Map.of(
                    "totalActiveUsers", activeUsersCount,
                    "timestamp", java.time.LocalDateTime.now()
            );

            ApiResponse<Map<String, Object>> apiResponse = ApiResponse.success(stats);
            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * Check if user exists by email (Admin endpoint - for future use)
     * GET /api/user/exists/email/{email}
     */
    @GetMapping("/exists/email/{email}")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkUserExistsByEmail(
            @PathVariable String email) {

        try {
            boolean exists = userService.existsByEmail(email);

            Map<String, Boolean> result = Map.of("exists", exists);
            ApiResponse<Map<String, Boolean>> apiResponse = ApiResponse.success(result);

            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * Check if user exists by Microsoft ID (Admin endpoint - for future use)
     * GET /api/user/exists/microsoft/{microsoftId}
     */
    @GetMapping("/exists/microsoft/{microsoftId}")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkUserExistsByMicrosoftId(
            @PathVariable String microsoftId) {

        try {
            boolean exists = userService.existsByMicrosoftId(microsoftId);

            Map<String, Boolean> result = Map.of("exists", exists);
            ApiResponse<Map<String, Boolean>> apiResponse = ApiResponse.success(result);

            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            throw e;
        }
    }
}