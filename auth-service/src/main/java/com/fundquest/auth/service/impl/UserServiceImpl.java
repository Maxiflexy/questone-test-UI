package com.fundquest.auth.service.impl;

import com.fundquest.auth.entity.Role;
import com.fundquest.auth.entity.User;
import com.fundquest.auth.exception.UserNotFoundException;
import com.fundquest.auth.repository.UserRepository;
import com.fundquest.auth.service.UserService;
import com.fundquest.auth.service.permission.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Implementation of UserService
 * Follows Single Responsibility Principle - only handles user operations
 * Follows Dependency Inversion Principle - depends on abstractions
 * Follows Open/Closed Principle - open for extension, closed for modification
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PermissionService permissionService;

    @Override
    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        log.debug("Finding user by email: {}", email);
        return userRepository.findByEmailAndIsActiveTrue(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findOptionalByEmail(String email) {
        log.debug("Finding optional user by email: {}", email);
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public User findByMicrosoftId(String microsoftId) {
        log.debug("Finding user by Microsoft ID: {}", microsoftId);
        return userRepository.findByMicrosoftId(microsoftId)
                .orElseThrow(() -> new UserNotFoundException("User not found with Microsoft ID: " + microsoftId));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isUserInvited(String email) {
        log.debug("Checking if user is invited: {}", email);
        Optional<User> user = userRepository.findByEmail(email);
        return user.isPresent() && user.get().isInvited() && user.get().isActive();
    }

    @Override
    public User completeMicrosoftVerification(String microsoftId, String email, String name, String preferredUsername) {
        log.info("Completing Microsoft verification for user: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        user.completeMicrosoftVerification(microsoftId, name, preferredUsername);

        User savedUser = userRepository.save(user);
        log.info("Successfully completed Microsoft verification for user: {}", email);
        return savedUser;
    }

    @Override
    public void updateLastLogin(String email) {
        log.debug("Updating last login for user: {}", email);
        userRepository.updateLastLoginByEmail(email, LocalDateTime.now());
    }

    @Override
    public User inviteUser(String email, Role role, Set<String> permissionNames, String invitedByEmail) {
        log.info("Inviting user: {} with role: {} and permissions: {}", email, role.getName(), permissionNames);

        if (existsByEmail(email)) {
            throw new IllegalArgumentException("User with email '" + email + "' already exists");
        }

        // Generate UUID string for ID (compatible with database VARCHAR field)
        String userId = UUID.randomUUID().toString();

        // Create user
        User user = User.builder()
                .id(userId) // Set the generated UUID string
                .email(email)
                .role(role)
                .isInvited(true)
                .isMicrosoftVerified(false)
                .invitedBy(invitedByEmail)
                .invitedAt(LocalDateTime.now())
                .build();

        // Assign permissions
        if (permissionNames != null && !permissionNames.isEmpty()) {
            user.setPermissions(permissionService.findByNames(permissionNames));
        }

        User savedUser = userRepository.save(user);
        log.info("Successfully invited user: {} with {} permissions", email,
                savedUser.getPermissions().size());
        return savedUser;
    }

    @Override
    @Transactional(readOnly = true)
    public User findById(String userId) {
        log.debug("Finding user by ID: {}", userId);
        return userRepository.findById(userId) // Direct String lookup, no UUID conversion needed
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        log.debug("Checking if user exists by email: {}", email);
        return userRepository.existsByEmail(email);
    }

    @Override
    public User updateRole(String userId, Role role) {
        log.info("Updating role for user: {} to role: {}", userId, role.getName());

        User user = findById(userId);
        user.setRole(role);

        User savedUser = userRepository.save(user);
        log.info("Successfully updated role for user: {}", userId);
        return savedUser;
    }

    @Override
    public User activateUser(String userId) {
        log.info("Activating user: {}", userId);

        User user = findById(userId);
        user.activate();

        User savedUser = userRepository.save(user);
        log.info("Successfully activated user: {}", userId);
        return savedUser;
    }

    @Override
    public User deactivateUser(String userId) {
        log.info("Deactivating user: {}", userId);

        User user = findById(userId);
        user.deactivate();

        User savedUser = userRepository.save(user);
        log.info("Successfully deactivated user: {}", userId);
        return savedUser;
    }
}