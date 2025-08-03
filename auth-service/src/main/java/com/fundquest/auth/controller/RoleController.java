package com.fundquest.auth.controller;

import com.fundquest.auth.dto.response.ApiResponse;
import com.fundquest.auth.dto.response.RoleResponse;
import com.fundquest.auth.entity.Role;
import com.fundquest.auth.service.role.RoleService;
import com.fundquest.auth.util.RoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.fundquest.auth.constants.AppConstants.ROLE_ENDPOINT;

@RestController
@RequestMapping(ROLE_ENDPOINT)
@RequiredArgsConstructor
@Slf4j
public class RoleController {

    private final RoleService roleService;
    private final RoleMapper roleMapper;

    /**
     * Get all roles
     * Only SUPER_ADMIN role can access this endpoint
     */
    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRoles() {
        log.info("Received request to fetch all roles");

        List<Role> roles = roleService.findAll();
        List<RoleResponse> roleResponses = roleMapper.toRoleResponseList(roles);

        log.info("Successfully retrieved {} roles", roleResponses.size());

        return ResponseEntity.ok(ApiResponse.success(roleResponses));
    }

    /**
     * Get all active roles
     * Users with VIEW_ADMIN or SUPER_ADMIN role can access
     */
    @GetMapping("/active")
    @PreAuthorize("hasAuthority('VIEW_ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getActiveRoles() {
        log.info("Received request to fetch active roles");

        List<Role> roles = roleService.findAllActive();
        List<RoleResponse> roleResponses = roleMapper.toRoleResponseList(roles);

        log.info("Successfully retrieved {} active roles", roleResponses.size());

        return ResponseEntity.ok(ApiResponse.success(roleResponses));
    }

    /**
     * Get role by ID
     * Users with VIEW_ADMIN permission or SUPER_ADMIN role can access
     */
    @GetMapping("/{roleId}")
    @PreAuthorize("hasAuthority('VIEW_ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<RoleResponse>> getRoleById(@PathVariable Long roleId) {
        log.info("Received request to fetch role with ID: {}", roleId);

        Role role = roleService.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with ID: " + roleId));

        RoleResponse roleResponse = roleMapper.toRoleResponse(role);

        log.info("Successfully retrieved role: {}", role.getName());

        return ResponseEntity.ok(ApiResponse.success(roleResponse));
    }
}