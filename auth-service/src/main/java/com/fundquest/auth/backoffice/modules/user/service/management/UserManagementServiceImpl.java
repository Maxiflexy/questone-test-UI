package com.fundquest.auth.backoffice.modules.user.service.management;

import com.fundquest.auth.backoffice.modules.user.dto.response.UserDetailResponse;
import com.fundquest.auth.backoffice.modules.user.mapper.UserDetailMapper;
import com.fundquest.auth.entity.Permission;
import com.fundquest.auth.entity.User;
import com.fundquest.auth.backoffice.modules.user.dto.response.UserPageResponse;
import com.fundquest.auth.backoffice.modules.user.mapper.UserListMapper;
import com.fundquest.auth.repository.UserRepository;
import com.fundquest.auth.service.permission.PermissionService;
import com.fundquest.auth.util.SecurityContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserManagementServiceImpl implements UserManagementService {

    private final UserRepository userRepository;
    private final UserListMapper userListMapper;
    private final UserDetailMapper userDetailMapper;
    private final PermissionService permissionService;
    private final SecurityContextService securityContextService;

    private static final int MAX_PAGE_SIZE = 8;
    private static final int DEFAULT_PAGE_SIZE = 8;

    @Override
    public UserPageResponse getAllUsers(int page, int size) {
        Pageable pageable = createPageable(page, size);
        Page<User> userPage = userRepository.findAllUsersWithRole(pageable);
        return userListMapper.toUserPageResponse(userPage);
    }

    @Override
    public UserPageResponse searchUsersByName(String name, int page, int size) {
        if (name == null || name.trim().isEmpty()) {
            return getAllUsers(page, size);
        }

        Pageable pageable = createPageable(page, size);
        Page<User> userPage = userRepository.findByNameContainingIgnoreCase(name.trim(), pageable);

        return userListMapper.toUserPageResponse(userPage);
    }

    @Override
    public UserPageResponse filterUsersByStatus(boolean isActive, int page, int size) {
        Pageable pageable = createPageable(page, size);
        Page<User> userPage = userRepository.findByIsActive(isActive, pageable);

        return userListMapper.toUserPageResponse(userPage);
    }

    @Override
    public UserPageResponse filterUsers(String name, Boolean isActive, int page, int size) {
        if ((name == null || name.trim().isEmpty()) && isActive == null) {
            log.debug("No filters provided, returning all users");
            return getAllUsers(page, size);
        }

        Pageable pageable = createPageable(page, size);
        String searchName = (name != null && !name.trim().isEmpty()) ? name.trim() : null;

        Page<User> userPage = userRepository.findByNameAndStatus(searchName, isActive, pageable);

        return userListMapper.toUserPageResponse(userPage);
    }


    @Override
    public UserDetailResponse getUserByEmail(String email) {
        log.info("Fetching user details by email: {}", email);

        User user = userRepository.findByEmailWithRoleAndPermissions(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        UserDetailResponse response = userDetailMapper.toUserDetailResponse(user);

        log.info("Successfully retrieved user details for email: {} with {} permissions",
                email, response.getPermissions().size());

        return response;
    }

    @Override
    @Transactional
    public UserDetailResponse updateUserPermissions(String email, List<String> permissionNames) {
        // Get the authenticated user who is making this change
        String modifiedBy = securityContextService.getAuthenticatedUserEmail();

        // Find the user to update
        User user = userRepository.findByEmailWithRoleAndPermissions(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        // Validate permission names and get permission entities
        Set<Permission> newPermissions = validateAndGetPermissions(permissionNames);

        // Update user permissions
        user.setPermissions(newPermissions);
        user.setLastModifiedBy(modifiedBy);

        // Save the updated user
        User savedUser = userRepository.save(user);
        return userDetailMapper.toUserDetailResponse(savedUser);
    }


    /**
     * Create Pageable object with proper page conversion and size validation
     * Converts 1-based page number to 0-based for Spring Data
     * Enforces maximum page size limit
     *
     * @param page 1-based page number from frontend
     * @param size requested page size
     * @return Pageable object for Spring Data (0-based page number)
     */
    private Pageable createPageable(int page, int size) {
        // Convert 1-based page to 0-based page for Spring Data
        int springPage = Math.max(0, page - 1);

        // Enforce size limits
        int validSize = validatePageSize(size);

        log.debug("Creating Pageable - Frontend page: {}, Spring page: {}, Size: {}",
                page, springPage, validSize);

        return PageRequest.of(springPage, validSize);
    }

    /**
     * Validate and adjust page size
     * Ensures page size is within acceptable limits
     *
     * @param requestedSize requested page size
     * @return validated page size
     */
    private int validatePageSize(int requestedSize) {
        if (requestedSize <= 0) {
            log.debug("Invalid page size {} provided, using default size {}", requestedSize, DEFAULT_PAGE_SIZE);
            return DEFAULT_PAGE_SIZE;
        }

        if (requestedSize > MAX_PAGE_SIZE) {
            log.debug("Requested page size {} exceeds maximum {}, using maximum", requestedSize, MAX_PAGE_SIZE);
            return MAX_PAGE_SIZE;
        }

        return requestedSize;
    }

    /**
     * Validate permission names and return corresponding Permission entities
     *
     * @param permissionNames list of permission names
     * @return set of validated Permission entities
     * @throws RuntimeException if any permission name is invalid
     */
    private Set<Permission> validateAndGetPermissions(List<String> permissionNames) {
        Set<Permission> permissions = new HashSet<>();

        for (String permissionName : permissionNames) {
            Permission permission = permissionService.findByName(permissionName.trim())
                    .orElseThrow(() -> new RuntimeException("Invalid permission name: " + permissionName));

            if (!permission.isActive()) {
                throw new RuntimeException("Permission is inactive: " + permissionName);
            }

            permissions.add(permission);
        }

        log.debug("Successfully validated {} permissions", permissions.size());
        return permissions;
    }
}
