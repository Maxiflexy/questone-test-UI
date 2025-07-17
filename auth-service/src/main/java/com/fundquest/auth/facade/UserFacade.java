package com.fundquest.auth.facade;

import com.fundquest.auth.dto.response.UserResponse;
import com.fundquest.auth.entity.User;
import com.fundquest.auth.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Facade for user operations
 * Handles business logic and coordinates between services
 */
@Component
public class UserFacade {

    private final UserService userService;

    @Autowired
    public UserFacade(UserService userService) {
        this.userService = userService;
    }

    /**
     * Get user profile by user entity
     */
    public UserResponse getUserProfile(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        return userService.convertToUserResponse(user);
    }

    /**
     * Get user profile by email
     */
    public UserResponse getUserProfileByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        Optional<User> user = userService.findByEmail(email);
        if (user.isEmpty()) {
            throw new RuntimeException("User not found with email: " + email);
        }

        return userService.convertToUserResponse(user.get());
    }

    /**
     * Update user profile
     */
    public UserResponse updateUserProfile(User currentUser, Map<String, Object> updates) {
        if (currentUser == null) {
            throw new IllegalArgumentException("Current user cannot be null");
        }

        if (updates == null || updates.isEmpty()) {
            throw new IllegalArgumentException("Updates cannot be null or empty");
        }

        // Validate update fields
        validateUpdateFields(updates);

        User updatedUser = userService.updateUserProfile(currentUser.getId(), updates);
        return userService.convertToUserResponse(updatedUser);
    }

    /**
     * Get user by ID
     */
    public UserResponse getUserById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        Optional<User> user = userService.findById(id);
        if (user.isEmpty()) {
            throw new RuntimeException("User not found with ID: " + id);
        }

        return userService.convertToUserResponse(user.get());
    }

    /**
     * Deactivate user
     */
    public void deactivateUser(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        userService.deactivateUser(id);
    }

    /**
     * Activate user
     */
    public void activateUser(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        userService.activateUser(id);
    }

    /**
     * Get user statistics
     */
    public Map<String, Object> getUserStats() {
        long activeUsersCount = userService.getActiveUsersCount();

        return Map.of(
                "totalActiveUsers", activeUsersCount,
                "timestamp", java.time.LocalDateTime.now()
        );
    }

    /**
     * Check if user exists by email
     */
    public boolean checkUserExistsByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        return userService.existsByEmail(email);
    }

    /**
     * Check if user exists by Microsoft ID
     */
    public boolean checkUserExistsByMicrosoftId(String microsoftId) {
        if (microsoftId == null || microsoftId.trim().isEmpty()) {
            throw new IllegalArgumentException("Microsoft ID is required");
        }

        return userService.existsByMicrosoftId(microsoftId);
    }

    /**
     * Validate update fields
     */
    private void validateUpdateFields(Map<String, Object> updates) {
        // Define allowed fields for update
        String[] allowedFields = {
                "name", "jobTitle", "department", "officeLocation",
                "mobilePhone", "businessPhones"
        };

        // Check for invalid fields
        for (String key : updates.keySet()) {
            boolean isAllowed = false;
            for (String allowedField : allowedFields) {
                if (allowedField.equals(key)) {
                    isAllowed = true;
                    break;
                }
            }
            if (!isAllowed) {
                throw new IllegalArgumentException("Field '" + key + "' is not allowed for update");
            }
        }

        // Validate field values
        if (updates.containsKey("name")) {
            Object name = updates.get("name");
            if (name == null || name.toString().trim().isEmpty()) {
                throw new IllegalArgumentException("Name cannot be empty");
            }
        }
    }
}