package com.fundquest.auth.util;

import com.fundquest.auth.entity.User;
import com.fundquest.auth.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Helper class for user-related operations
 * Provides utility methods for user management
 */
@Component
public class UserHelper {

    private final UserService userService;

    @Autowired
    public UserHelper(UserService userService) {
        this.userService = userService;
    }

    /**
     * Get current user from request
     * First tries to get from request attributes, then falls back to Security Context
     */
    public User getCurrentUser(HttpServletRequest request) {
        // Try to get from request attributes first (set by JwtAuthenticationFilter)
        User currentUser = (User) request.getAttribute("currentUser");

        if (currentUser == null) {
            // Fallback: get from Security Context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getName() != null) {
                String email = authentication.getName();
                currentUser = userService.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("User not found"));
            } else {
                throw new RuntimeException("No authenticated user found");
            }
        }

        return currentUser;
    }

    /**
     * Get current user ID from request
     */
    public String getCurrentUserId(HttpServletRequest request) {
        User currentUser = getCurrentUser(request);
        return currentUser.getId().toString();
    }

    /**
     * Get current user email from request
     */
    public String getCurrentUserEmail(HttpServletRequest request) {
        User currentUser = getCurrentUser(request);
        return currentUser.getEmail();
    }

    /**
     * Check if current user is active
     */
    public boolean isCurrentUserActive(HttpServletRequest request) {
        User currentUser = getCurrentUser(request);
        return currentUser.getIsActive();
    }

    /**
     * Validate user permissions for a specific operation
     * This can be extended for role-based access control
     */
    public boolean canUserAccessResource(HttpServletRequest request, String resourceId) {
        User currentUser = getCurrentUser(request);

        // Basic validation - user must be active
        if (!currentUser.getIsActive()) {
            return false;
        }

        // Add more complex permission logic here
        // For now, all active users can access their own resources
        return true;
    }
}