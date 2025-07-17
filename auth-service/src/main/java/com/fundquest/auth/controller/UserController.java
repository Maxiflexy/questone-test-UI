package com.fundquest.auth.controller;

import com.fundquest.auth.dto.response.ApiResponse;
import com.fundquest.auth.dto.response.UserResponse;
import com.fundquest.auth.entity.User;
import com.fundquest.auth.facade.UserFacade;
import com.fundquest.auth.util.CookieHelper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for user operations
 * Delegates business logic to UserFacade
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserFacade userFacade;
    private final CookieHelper cookieHelper;

    @Autowired
    public UserController(UserFacade userFacade, CookieHelper cookieHelper) {
        this.userFacade = userFacade;
        this.cookieHelper = cookieHelper;
    }


    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getUserProfile(HttpServletRequest request) {
        User currentUser = getCurrentUser(request);
        UserResponse userResponse = userFacade.getUserProfile(currentUser);
        return ResponseEntity.ok(ApiResponse.success(userResponse));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserProfile(
            @RequestBody Map<String, Object> updates,
            HttpServletRequest request) {

        User currentUser = getCurrentUser(request);
        UserResponse userResponse = userFacade.updateUserProfile(currentUser, updates);
        return ResponseEntity.ok(ApiResponse.success(userResponse));
    }


    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable UUID id) {
        UserResponse userResponse = userFacade.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(userResponse));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(@PathVariable UUID id) {
        userFacade.deactivateUser(id);
        return ResponseEntity.ok(ApiResponse.successVoid("User deactivated successfully"));
    }


    @PostMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> activateUser(@PathVariable UUID id) {
        userFacade.activateUser(id);
        return ResponseEntity.ok(ApiResponse.successVoid("User activated successfully"));
    }

    /**
     * Get User Statistics (Admin endpoint)
     * GET /api/user/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserStats() {
        Map<String, Object> stats = userFacade.getUserStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * Check if user exists by email (Admin endpoint)
     * GET /api/user/exists/email/{email}
     */
    @GetMapping("/exists/email/{email}")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkUserExistsByEmail(
            @PathVariable String email) {

        boolean exists = userFacade.checkUserExistsByEmail(email);
        Map<String, Boolean> result = Map.of("exists", exists);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * Check if user exists by Microsoft ID (Admin endpoint)
     * GET /api/user/exists/microsoft/{microsoftId}
     */
    @GetMapping("/exists/microsoft/{microsoftId}")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkUserExistsByMicrosoftId(
            @PathVariable String microsoftId) {

        boolean exists = userFacade.checkUserExistsByMicrosoftId(microsoftId);
        Map<String, Boolean> result = Map.of("exists", exists);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * Get current user from request attributes or security context
     */
    private User getCurrentUser(HttpServletRequest request) {
        // Try to get from request attributes first (set by JwtAuthenticationFilter)
        User currentUser = (User) request.getAttribute("currentUser");

        if (currentUser == null) {
            // Fallback: get from Security Context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.getName() != null) {
                String email = authentication.getName();
                UserResponse userResponse = userFacade.getUserProfileByEmail(email);
                // Note: This approach requires converting back to User entity
                // In a real application, you might want to modify the facade to return User entity
                throw new RuntimeException("User not found in request attributes");
            }
            throw new RuntimeException("No authenticated user found");
        }

        return currentUser;
    }
}