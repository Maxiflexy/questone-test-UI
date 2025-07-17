package com.fundquest.auth.service.impl;

import com.fundquest.auth.dto.response.UserResponse;
import com.fundquest.auth.entity.User;
import com.fundquest.auth.repository.UserRepository;
import com.fundquest.auth.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of UserService interface
 * Handles user management operations
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User createOrUpdateUser(Map<String, Object> microsoftUserInfo) {
        logger.debug("Creating or updating user with Microsoft info: {}", microsoftUserInfo.get("email"));

        String email = (String) microsoftUserInfo.get("email");
        String microsoftId = (String) microsoftUserInfo.get("microsoftId");
        String name = (String) microsoftUserInfo.get("name");

        // Validate required fields
        validateRequiredFields(email, microsoftId, name);

        // Check if user already exists by email or Microsoft ID
        Optional<User> existingUser = userRepository.findByEmailOrMicrosoftId(email, microsoftId);

        if (existingUser.isPresent()) {
            logger.info("Updating existing user: {}", email);
            return updateExistingUser(existingUser.get(), microsoftUserInfo);
        } else {
            logger.info("Creating new user: {}", email);
            return createNewUser(microsoftUserInfo);
        }
    }

    @Override
    public Optional<User> findById(UUID id) {
        logger.debug("Finding user by ID: {}", id);
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        logger.debug("Finding user by email: {}", email);
        return userRepository.findByEmail(email);
    }

    @Override
    public Optional<User> findByMicrosoftId(String microsoftId) {
        logger.debug("Finding user by Microsoft ID: {}", microsoftId);
        return userRepository.findByMicrosoftId(microsoftId);
    }

    @Override
    public Optional<User> findActiveByEmail(String email) {
        logger.debug("Finding active user by email: {}", email);
        return userRepository.findByEmailAndIsActiveTrue(email);
    }

    @Override
    public Optional<User> findActiveByMicrosoftId(String microsoftId) {
        logger.debug("Finding active user by Microsoft ID: {}", microsoftId);
        return userRepository.findByMicrosoftIdAndIsActiveTrue(microsoftId);
    }

    @Override
    public void updateLastLogin(UUID userId) {
        logger.debug("Updating last login for user: {}", userId);

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.updateLastLogin();
            userRepository.save(user);
            logger.info("Updated last login for user: {}", user.getEmail());
        } else {
            logger.warn("User not found for last login update: {}", userId);
        }
    }

    @Override
    public void deactivateUser(UUID userId) {
        logger.info("Deactivating user: {}", userId);

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setIsActive(false);
            user.setUpdatedBy("SYSTEM");
            userRepository.save(user);
            logger.info("Deactivated user: {}", user.getEmail());
        } else {
            logger.warn("User not found for deactivation: {}", userId);
        }
    }

    @Override
    public void activateUser(UUID userId) {
        logger.info("Activating user: {}", userId);

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setIsActive(true);
            user.setUpdatedBy("SYSTEM");
            userRepository.save(user);
            logger.info("Activated user: {}", user.getEmail());
        } else {
            logger.warn("User not found for activation: {}", userId);
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        logger.debug("Checking if user exists by email: {}", email);
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByMicrosoftId(String microsoftId) {
        logger.debug("Checking if user exists by Microsoft ID: {}", microsoftId);
        return userRepository.existsByMicrosoftId(microsoftId);
    }

    @Override
    public long getActiveUsersCount() {
        logger.debug("Getting active users count");
        return userRepository.countByIsActiveTrue();
    }

    @Override
    public UserResponse convertToUserResponse(User user) {
        logger.debug("Converting user to response DTO: {}", user.getEmail());

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

    @Override
    public User updateUserProfile(UUID userId, Map<String, Object> updates) {
        logger.info("Updating user profile: {}", userId);

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found with ID: " + userId);
        }

        User user = userOpt.get();
        applyProfileUpdates(user, updates);
        user.setUpdatedBy("SYSTEM");

        User savedUser = userRepository.save(user);
        logger.info("Updated profile for user: {}", savedUser.getEmail());

        return savedUser;
    }

    @Override
    public void deleteUser(UUID userId) {
        logger.info("Soft deleting user: {}", userId);
        deactivateUser(userId);
    }

    /**
     * Validate required fields for user creation
     */
    private void validateRequiredFields(String email, String microsoftId, String name) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (microsoftId == null || microsoftId.trim().isEmpty()) {
            throw new IllegalArgumentException("Microsoft ID is required");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }
    }

    /**
     * Create new user from Microsoft information
     */
    private User createNewUser(Map<String, Object> microsoftUserInfo) {
        User user = new User();

        // Set required fields
        user.setEmail((String) microsoftUserInfo.get("email"));
        user.setMicrosoftId((String) microsoftUserInfo.get("microsoftId"));
        user.setName((String) microsoftUserInfo.get("name"));

        // Set optional fields
        setOptionalFields(user, microsoftUserInfo);

        // Set default values
        user.setIsActive(true);
        user.setIsEmailVerified(true); // Assuming Microsoft verified email
        user.setLoginCount(0L);
        user.setCreatedBy("SYSTEM");
        user.setUpdatedBy("SYSTEM");

        // Increment login count for first login
        user.incrementLoginCount();

        return userRepository.save(user);
    }

    /**
     * Update existing user from Microsoft information
     */
    private User updateExistingUser(User user, Map<String, Object> microsoftUserInfo) {
        // Update fields that might have changed
        user.setName((String) microsoftUserInfo.get("name"));
        setOptionalFields(user, microsoftUserInfo);

        // Ensure user is active and email is verified
        user.setIsActive(true);
        user.setIsEmailVerified(true);
        user.setUpdatedBy("SYSTEM");

        // Increment login count
        user.incrementLoginCount();

        return userRepository.save(user);
    }

    /**
     * Set optional fields from Microsoft information
     */
    private void setOptionalFields(User user, Map<String, Object> microsoftUserInfo) {
        user.setGivenName((String) microsoftUserInfo.get("givenName"));
        user.setFamilyName((String) microsoftUserInfo.get("familyName"));
        user.setPreferredUsername((String) microsoftUserInfo.get("preferredUsername"));
        user.setJobTitle((String) microsoftUserInfo.get("jobTitle"));
        user.setDepartment((String) microsoftUserInfo.get("department"));
        user.setOfficeLocation((String) microsoftUserInfo.get("officeLocation"));
        user.setMobilePhone((String) microsoftUserInfo.get("mobilePhone"));
        user.setBusinessPhones((String) microsoftUserInfo.get("businessPhones"));
    }

    /**
     * Apply profile updates to user
     */
    private void applyProfileUpdates(User user, Map<String, Object> updates) {
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
    }
}