package com.fundquest.auth.backoffice.modules.user.service.management;

import com.fundquest.auth.backoffice.modules.user.dto.response.UserDetailResponse;
import com.fundquest.auth.backoffice.modules.user.dto.response.UserPageResponse;

import java.util.List;

public interface UserManagementService {

    /**
     * Get all users with pagination
     *
     * @param page 1-based page number (will be converted to 0-based internally)
     * @param size page size (max 8)
     * @return UserPageResponse with user data and pagination info
     */
    UserPageResponse getAllUsers(int page, int size);

    /**
     * Search users by name with pagination
     *
     * @param name name to search for (case-insensitive partial match)
     * @param page 1-based page number
     * @param size page size (max 8)
     * @return UserPageResponse with matching users and pagination info
     */
    UserPageResponse searchUsersByName(String name, int page, int size);

    /**
     * Filter users by active status with pagination
     *
     * @param isActive filter by active status (true/false)
     * @param page 1-based page number
     * @param size page size (max 8)
     * @return UserPageResponse with filtered users and pagination info
     */
    UserPageResponse filterUsersByStatus(boolean isActive, int page, int size);

    /**
     * Advanced filter users by multiple criteria
     *
     * @param name name to search for (optional)
     * @param isActive status filter (optional)
     * @param page 1-based page number
     * @param size page size (max 8)
     * @return UserPageResponse with filtered users and pagination info
     */
    UserPageResponse filterUsers(String name, Boolean isActive, int page, int size);

    /**
     * Get single user by email with detailed information including permissions and role
     *
     * @param email user email
     * @return UserDetailResponse with user details, permissions, and role info
     */
    UserDetailResponse getUserByEmail(String email);

    /**
     * Update user permissions
     *
     * @param email user email
     * @param permissionNames list of permission names to assign to the user
     * @return UserDetailResponse with updated user information
     */
    UserDetailResponse updateUserPermissions(String email, List<String> permissionNames);

    /**
     * Update user status (activate/deactivate)
     *
     * @param email user email
     * @param isActive true to activate user, false to deactivate
     * @return UserDetailResponse with updated user information
     */
    UserDetailResponse updateUserStatus(String email, boolean isActive);

}
