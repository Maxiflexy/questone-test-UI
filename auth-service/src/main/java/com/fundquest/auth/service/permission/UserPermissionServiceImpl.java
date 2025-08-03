package com.fundquest.auth.service.permission;

import com.fundquest.auth.entity.Permission;
import com.fundquest.auth.entity.User;
import com.fundquest.auth.repository.UserRepository;
import com.fundquest.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserPermissionServiceImpl implements UserPermissionService {

    private final UserRepository userRepository;
    private final PermissionService permissionService;

    // Using @Lazy to avoid circular dependency
    @Lazy
    private final UserService userService;

    @Override
    public User assignPermissions(String userId, Set<String> permissionNames) {
        log.info("Assigning permissions {} to user {}", permissionNames, userId);

        User user = userService.findById(userId);
        Set<Permission> permissionsToAdd = permissionService.findByNames(permissionNames);

        for (Permission permission : permissionsToAdd) {
            user.addPermission(permission);
        }

        User savedUser = userRepository.save(user);
        log.info("Successfully assigned {} permissions to user {}", permissionsToAdd.size(), userId);
        return savedUser;
    }

    @Override
    public User removePermissions(String userId, Set<String> permissionNames) {
        log.info("Removing permissions {} from user {}", permissionNames, userId);

        User user = userService.findById(userId);
        Set<Permission> permissionsToRemove = permissionService.findByNames(permissionNames);

        for (Permission permission : permissionsToRemove) {
            user.removePermission(permission);
        }

        User savedUser = userRepository.save(user);
        log.info("Successfully removed {} permissions from user {}", permissionsToRemove.size(), userId);
        return savedUser;
    }

    @Override
    public User replacePermissions(String userId, Set<String> permissionNames) {
        log.info("Replacing all permissions for user {} with {}", userId, permissionNames);

        User user = userService.findById(userId);
        Set<Permission> newPermissions = permissionService.findByNames(permissionNames);

        user.setPermissions(newPermissions);

        User savedUser = userRepository.save(user);
        log.info("Successfully replaced permissions for user {} with {} permissions", userId, newPermissions.size());
        return savedUser;
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Permission> getUserPermissions(String userId) {
        log.debug("Getting permissions for user {}", userId);

        User user = userService.findById(userId);
        return user.getPermissions();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPermission(String userId, String permissionName) {
        log.debug("Checking if user {} has permission {}", userId, permissionName);

        User user = userService.findById(userId);
        return user.hasPermission(permissionName);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasAnyPermission(String userId, Set<String> permissionNames) {
        log.debug("Checking if user {} has any of permissions {}", userId, permissionNames);

        if (permissionNames == null || permissionNames.isEmpty()) {
            return false;
        }

        User user = userService.findById(userId);
        return permissionNames.stream()
                .anyMatch(user::hasPermission);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasAllPermissions(String userId, Set<String> permissionNames) {
        log.debug("Checking if user {} has all permissions {}", userId, permissionNames);

        if (permissionNames == null || permissionNames.isEmpty()) {
            return true;
        }

        User user = userService.findById(userId);
        return permissionNames.stream()
                .allMatch(user::hasPermission);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getUserPermissionNames(String userId) {
        log.debug("Getting permission names for user {}", userId);

        User user = userService.findById(userId);
        return user.getPermissions().stream()
                .map(Permission::getName)
                .collect(Collectors.toList());
    }
}