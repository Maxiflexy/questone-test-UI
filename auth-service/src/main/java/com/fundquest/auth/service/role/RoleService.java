package com.fundquest.auth.service.role;

import com.fundquest.auth.dto.response.RoleResponse;
import com.fundquest.auth.entity.Role;

import java.util.List;
import java.util.Optional;


public interface RoleService {

    /**
     * Find role by name
     * @param name role name
     * @return optional role
     */
    Optional<Role> findByName(String name);

    /**
     * Find all active roles
     * @return list of active roles
     */
    List<Role> findAllActive();

    /**
     * Check if role exists by name
     * @param name role name
     * @return true if exists, false otherwise
     */
    boolean existsByName(String name);

    /**
     * Create new role
     * @param role role to create
     * @return created role
     */
    Role create(Role role);

    /**
     * Get all roles
     * @return list of all roles
     */
    List<RoleResponse> findAll();

    /**
     * Find role by ID
     * @param id role ID
     * @return optional role
     */
    RoleResponse findById(Long id);
}