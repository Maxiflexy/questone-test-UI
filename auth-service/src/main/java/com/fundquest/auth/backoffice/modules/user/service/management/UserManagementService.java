package com.fundquest.auth.backoffice.modules.user.service.management;

import com.fundquest.auth.backoffice.modules.user.dto.response.UserPageResponse;

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
}
