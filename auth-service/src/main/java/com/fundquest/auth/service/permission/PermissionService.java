package com.fundquest.auth.service.permission;

import com.fundquest.auth.dto.response.DetailedPermissionResponse;
import com.fundquest.auth.dto.response.HierarchicalPermissionResponse;
import com.fundquest.auth.dto.response.PermissionResponse;
import com.fundquest.auth.entity.Category;
import com.fundquest.auth.entity.Permission;
import com.fundquest.auth.entity.PermissionGroup;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PermissionService {

    // ===== Permission Group Operations =====

    /**
     * Find all permission groups with hierarchical structure
     * @return hierarchical permission response with groups, categories, and permissions
     */
    HierarchicalPermissionResponse findAllHierarchical();

    /**
     * Find permission group by ID
     * @param id permission group ID
     * @return optional permission group
     */
    Optional<PermissionGroup> findPermissionGroupById(Long id);

    /**
     * Find permission group by name
     * @param name permission group name
     * @return optional permission group
     */
    Optional<PermissionGroup> findPermissionGroupByName(String name);

    /**
     * Find all active permission groups
     * @return list of active permission groups
     */
    List<PermissionGroup> findAllActivePermissionGroups();

    // ===== Category Operations =====

    /**
     * Find category by ID
     * @param id category ID
     * @return optional category
     */
    Optional<Category> findCategoryById(Long id);

    /**
     * Find category by name
     * @param name category name
     * @return optional category
     */
    Optional<Category> findCategoryByName(String name);

    /**
     * Find categories by permission group ID
     * @param permissionGroupId permission group ID
     * @return list of categories
     */
    List<Category> findCategoriesByPermissionGroupId(Long permissionGroupId);

    // ===== Permission Operations =====

    /**
     * Find permission by name
     * @param name permission name
     * @return optional permission
     */
    Optional<Permission> findByName(String name);

    /**
     * Find all active permissions
     * @return list of active permissions
     */
    List<Permission> findAllActive();

    /**
     * Find permissions by category ID
     * @param categoryId category ID
     * @return list of permissions in category
     */
    List<Permission> findByCategoryId(Long categoryId);

    /**
     * Check if permission exists by name
     * @param name permission name
     * @return true if exists, false otherwise
     */
    boolean existsByName(String name);

    /**
     * Create new permission
     * @param permission permission to create
     * @return created permission
     */
    Permission create(Permission permission);

    /**
     * Find permissions by names
     * @param permissionNames set of permission names
     * @return set of permissions
     */
    Set<Permission> findByNames(Set<String> permissionNames);

    /**
     * Find permissions by IDs
     * @param permissionIds list of permission IDs
     * @return set of permissions
     */
    Set<Permission> findByIds(List<Long> permissionIds);

    /**
     * Get all permissions (flat list for backwards compatibility)
     * @return list of all permissions
     */
    List<PermissionResponse> findAll();

    /**
     * Find permission by ID (flat response for backwards compatibility)
     * @param id permission ID
     * @return permission response
     */
    PermissionResponse findById(Long id);

    /**
     * Find permission by ID with detailed information including category and permission group
     * @param id permission ID
     * @return detailed permission response with category and permission group info
     */
    DetailedPermissionResponse findByIdDetailed(Long id);

    /**
     * Check if all permission IDs exist
     * @param permissionIds list of permission IDs to validate
     * @return true if all exist, false otherwise
     */
    boolean existsAllByIds(List<Long> permissionIds);
}