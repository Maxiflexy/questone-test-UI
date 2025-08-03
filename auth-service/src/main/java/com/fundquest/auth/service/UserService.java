package com.fundquest.auth.service;

import com.fundquest.auth.entity.Role;
import com.fundquest.auth.entity.User;
import java.util.Optional;
import java.util.Set;

public interface UserService {

    /**
     * Find user by email
     * @param email user email
     * @return user entity
     * @throws com.fundquest.auth.exception.UserNotFoundException if user not found
     */
    User findByEmail(String email);

    /**
     * Find user by email (optional)
     * @param email user email
     * @return optional user
     */
    Optional<User> findOptionalByEmail(String email);

    /**
     * Find user by Microsoft ID
     * @param microsoftId Microsoft ID
     * @return user entity
     * @throws com.fundquest.auth.exception.UserNotFoundException if user not found
     */
    User findByMicrosoftId(String microsoftId);

    /**
     * Check if user is invited
     * @param email user email
     * @return true if user is invited and active, false otherwise
     */
    boolean isUserInvited(String email);

    /**
     * Complete Microsoft verification for user
     * @param microsoftId Microsoft ID
     * @param email user email
     * @param name user name
     * @param preferredUsername preferred username
     * @return updated user
     */
    User completeMicrosoftVerification(String microsoftId, String email, String name, String preferredUsername);

    /**
     * Update last login timestamp
     * @param email user email
     */
    void updateLastLogin(String email);

    /**
     * Create/invite a new user
     * @param email user email
     * @param role user role
     * @param permissionNames set of permission names to assign
     * @param invitedByEmail email of who is inviting
     * @return created user
     */
    User inviteUser(String email, Role role, Set<String> permissionNames, String invitedByEmail);

    /**
     * Find user by ID
     * @param userId user ID
     * @return user entity
     * @throws com.fundquest.auth.exception.UserNotFoundException if user not found
     */
    User findById(String userId);

    /**
     * Check if user exists by email
     * @param email user email
     * @return true if exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Update user role
     * @param userId user ID
     * @param role new role
     * @return updated user
     */
    User updateRole(String userId, Role role);

    /**
     * Activate user
     * @param userId user ID
     * @return updated user
     */
    User activateUser(String userId);

    /**
     * Deactivate user
     * @param userId user ID
     * @return updated user
     */
    User deactivateUser(String userId);
}