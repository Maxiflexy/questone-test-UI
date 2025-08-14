package com.fundquest.auth.service.permission;

import com.fundquest.auth.dto.response.PermissionResponse;
import com.fundquest.auth.entity.Permission;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PermissionService {

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
     * Find permissions by category
     * @param category permission category
     * @return list of permissions in category
     */
    List<Permission> findByCategory(String category);

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
     * Get all permissions
     * @return list of all permissions
     */
    List<PermissionResponse> findAll();

    /**
     * Find permission by ID
     * @param id permission ID
     * @return optional permission
     */
    PermissionResponse findById(Long id);

    /**
     * Check if all permission IDs exist
     * @param permissionIds list of permission IDs to validate
     * @return true if all exist, false otherwise
     */
    boolean existsAllByIds(List<Long> permissionIds);
}