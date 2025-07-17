package com.fundquest.auth.service.impl;

import com.fundquest.auth.dto.response.UserResponse;
import com.fundquest.auth.entity.User;
import com.fundquest.auth.repository.UserRepository;
import com.fundquest.auth.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Create or update user from Microsoft token information
     */
    public User createOrUpdateUser(Map<String, Object> microsoftUserInfo) {
        String email = (String) microsoftUserInfo.get("email");
        String microsoftId = (String) microsoftUserInfo.get("microsoftId");
        String name = (String) microsoftUserInfo.get("name");

        // Validate required fields
        if (email == null || microsoftId == null || name == null) {
            throw new IllegalArgumentException("Email, Microsoft ID, and name are required");
        }

        // Check if user already exists by email or Microsoft ID
        Optional<User> existingUser = userRepository.findByEmailOrMicrosoftId(email, microsoftId);

        if (existingUser.isPresent()) {
            // Update existing user
            User user = existingUser.get();
            updateUserFromMicrosoftInfo(user, microsoftUserInfo);
            user.incrementLoginCount();
            user.setUpdatedBy("SYSTEM");
            return userRepository.save(user);
        } else {
            // Create new user
            User user = createUserFromMicrosoftInfo(microsoftUserInfo);
            user.setCreatedBy("SYSTEM");
            user.setUpdatedBy("SYSTEM");
            user.incrementLoginCount();
            return userRepository.save(user);
        }
    }

    /**
     * Create new user from Microsoft information
     */
    private User createUserFromMicrosoftInfo(Map<String, Object> microsoftUserInfo) {
        User user = new User();

        // Required fields
        user.setEmail((String) microsoftUserInfo.get("email"));
        user.setMicrosoftId((String) microsoftUserInfo.get("microsoftId"));
        user.setName((String) microsoftUserInfo.get("name"));

        // Optional fields
        user.setGivenName((String) microsoftUserInfo.get("givenName"));
        user.setFamilyName((String) microsoftUserInfo.get("familyName"));
        user.setPreferredUsername((String) microsoftUserInfo.get("preferredUsername"));
        user.setJobTitle((String) microsoftUserInfo.get("jobTitle"));
        user.setDepartment((String) microsoftUserInfo.get("department"));
        user.setOfficeLocation((String) microsoftUserInfo.get("officeLocation"));
        user.setMobilePhone((String) microsoftUserInfo.get("mobilePhone"));
        user.setBusinessPhones((String) microsoftUserInfo.get("businessPhones"));

        // Default values
        user.setIsActive(true);
        user.setIsEmailVerified(true); // Assuming Microsoft verified email
        user.setLoginCount(0L);

        return user;
    }

    /**
     * Update existing user from Microsoft information
     */
    private void updateUserFromMicrosoftInfo(User user, Map<String, Object> microsoftUserInfo) {
        // Update fields that might have changed
        user.setName((String) microsoftUserInfo.get("name"));
        user.setGivenName((String) microsoftUserInfo.get("givenName"));
        user.setFamilyName((String) microsoftUserInfo.get("familyName"));
        user.setPreferredUsername((String) microsoftUserInfo.get("preferredUsername"));
        user.setJobTitle((String) microsoftUserInfo.get("jobTitle"));
        user.setDepartment((String) microsoftUserInfo.get("department"));
        user.setOfficeLocation((String) microsoftUserInfo.get("officeLocation"));
        user.setMobilePhone((String) microsoftUserInfo.get("mobilePhone"));
        user.setBusinessPhones((String) microsoftUserInfo.get("businessPhones"));

        // Ensure user is active and email is verified
        user.setIsActive(true);
        user.setIsEmailVerified(true);
    }

    /**
     * Find user by ID
     */
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    /**
     * Find user by email
     */
    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found")));
    }

    /**
     * Find user by Microsoft ID
     */
    public Optional<User> findByMicrosoftId(String microsoftId) {
        return userRepository.findByMicrosoftId(microsoftId);
    }

    /**
     * Find active user by email
     */
    public Optional<User> findActiveByEmail(String email) {
        return userRepository.findByEmailAndIsActiveTrue(email);
    }

    /**
     * Find active user by Microsoft ID
     */
    public Optional<User> findActiveByMicrosoftId(String microsoftId) {
        return userRepository.findByMicrosoftIdAndIsActiveTrue(microsoftId);
    }

    /**
     * Update user's last login
     */
    public void updateLastLogin(UUID userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setLastLogin(LocalDateTime.now());
            user.incrementLoginCount();
            userRepository.save(user);
        }
    }

    /**
     * Deactivate user
     */
    public void deactivateUser(UUID userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setIsActive(false);
            user.setUpdatedBy("SYSTEM");
            userRepository.save(user);
        }
    }

    /**
     * Activate user
     */
    public void activateUser(UUID userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setIsActive(true);
            user.setUpdatedBy("SYSTEM");
            userRepository.save(user);
        }
    }

    /**
     * Check if user exists by email
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Check if user exists by Microsoft ID
     */
    public boolean existsByMicrosoftId(String microsoftId) {
        return userRepository.existsByMicrosoftId(microsoftId);
    }

    /**
     * Get total active users count
     */
    public long getActiveUsersCount() {
        return userRepository.countByIsActiveTrue();
    }

    /**
     * Convert User entity to UserResponse DTO
     */
    public UserResponse convertToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setName(user.getName());
        response.setMicrosoftId(user.getMicrosoftId());
        response.setGivenName(user.getGivenName());
        response.setFamilyName(user.getFamilyName());
        response.setJobTitle(user.getJobTitle());
        response.setDepartment(user.getDepartment());
        response.setCreatedAt(user.getCreatedAt());
        response.setLastLogin(user.getLastLogin());
        return response;
    }

    /**
     * Update user profile
     */
    public User updateUserProfile(UUID userId, Map<String, Object> updates) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found with ID: " + userId);
        }

        User user = userOpt.get();

        // Update allowed fields
        if (updates.containsKey("name")) {
            user.setName((String) updates.get("name"));
        }
        if (updates.containsKey("jobTitle")) {
            user.setJobTitle((String) updates.get("jobTitle"));
        }
        if (updates.containsKey("department")) {
            user.setDepartment((String) updates.get("department"));
        }
        if (updates.containsKey("officeLocation")) {
            user.setOfficeLocation((String) updates.get("officeLocation"));
        }
        if (updates.containsKey("mobilePhone")) {
            user.setMobilePhone((String) updates.get("mobilePhone"));
        }
        if (updates.containsKey("businessPhones")) {
            user.setBusinessPhones((String) updates.get("businessPhones"));
        }

        user.setUpdatedBy("SYSTEM");

        return userRepository.save(user);
    }

    /**
     * Delete user (soft delete by deactivating)
     */
    public void deleteUser(UUID userId) {
        deactivateUser(userId);
    }
}