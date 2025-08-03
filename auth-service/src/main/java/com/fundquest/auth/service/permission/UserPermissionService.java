package com.fundquest.auth.service.permission;

import com.fundquest.auth.entity.Permission;
import com.fundquest.auth.entity.User;

import java.util.List;
import java.util.Set;

public interface UserPermissionService {

    /**
     * Assign permissions to user
     * @param userId user ID
     * @param permissionNames set of permission names to assign
     * @return updated user
     */
    User assignPermissions(String userId, Set<String> permissionNames);

    /**
     * Remove permissions from user
     * @param userId user ID
     * @param permissionNames set of permission names to remove
     * @return updated user
     */
    User removePermissions(String userId, Set<String> permissionNames);

    /**
     * Replace all user permissions
     * @param userId user ID
     * @param permissionNames set of permission names to set
     * @return updated user
     */
    User replacePermissions(String userId, Set<String> permissionNames);

    /**
     * Get user permissions
     * @param userId user ID
     * @return set of user permissions
     */
    Set<Permission> getUserPermissions(String userId);

    /**
     * Check if user has permission
     * @param userId user ID
     * @param permissionName permission name
     * @return true if user has permission, false otherwise
     */
    boolean hasPermission(String userId, String permissionName);

    /**
     * Check if user has any of the specified permissions
     * @param userId user ID
     * @param permissionNames set of permission names to check
     * @return true if user has any of the permissions, false otherwise
     */
    boolean hasAnyPermission(String userId, Set<String> permissionNames);

    /**
     * Check if user has all of the specified permissions
     * @param userId user ID
     * @param permissionNames set of permission names to check
     * @return true if user has all permissions, false otherwise
     */
    boolean hasAllPermissions(String userId, Set<String> permissionNames);

    /**
     * Get permission names for user
     * @param userId user ID
     * @return list of permission names
     */
    List<String> getUserPermissionNames(String userId);
}
