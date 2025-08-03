package com.fundquest.auth.controller;

import com.fundquest.auth.constants.AppConstants;
import com.fundquest.auth.dto.response.ApiResponse;
import com.fundquest.auth.dto.response.PermissionResponse;
import com.fundquest.auth.entity.Permission;
import com.fundquest.auth.service.permission.PermissionService;
import com.fundquest.auth.util.PermissionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.fundquest.auth.constants.AppConstants.PERMISSIONS_ENDPOINT;

@RestController
@RequestMapping(PERMISSIONS_ENDPOINT)
@RequiredArgsConstructor
@Slf4j
public class PermissionController {

    private final PermissionService permissionService;
    private final PermissionMapper permissionMapper;

    /**
     * Get all permissions
     * Only SUPER_ADMIN role can access all permissions
     */
    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getAllPermissions() {
        log.info("Received request to fetch all permissions");

        List<Permission> permissions = permissionService.findAll();
        List<PermissionResponse> permissionResponses = permissionMapper.toPermissionResponseList(permissions);

        log.info("Successfully retrieved {} permissions", permissionResponses.size());

        return ResponseEntity.ok(ApiResponse.success(permissionResponses));
    }

    /**
     * Get all active permissions
     * Users with ASSIGN_AND_UNASSIGN permission or SUPER_ADMIN role can access
     */
    @GetMapping("/active")
    @PreAuthorize("hasAuthority('ASSIGN_AND_UNASSIGN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getActivePermissions() {
        log.info("Received request to fetch active permissions");

        List<Permission> permissions = permissionService.findAllActive();
        List<PermissionResponse> permissionResponses = permissionMapper.toPermissionResponseList(permissions);

        log.info("Successfully retrieved {} active permissions", permissionResponses.size());

        return ResponseEntity.ok(ApiResponse.success(permissionResponses));
    }

    /**
     * Get permissions by category
     * Users with ASSIGN_AND_UNASSIGN permission or SUPER_ADMIN role can access
     */
    @GetMapping("/category/{category}")
    @PreAuthorize("hasAuthority('ASSIGN_AND_UNASSIGN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getPermissionsByCategory(@PathVariable String category) {
        log.info("Received request to fetch permissions by category: {}", category);

        List<Permission> permissions = permissionService.findByCategory(category);
        List<PermissionResponse> permissionResponses = permissionMapper.toPermissionResponseList(permissions);

        log.info("Successfully retrieved {} permissions for category: {}", permissionResponses.size(), category);

        return ResponseEntity.ok(ApiResponse.success(permissionResponses));
    }

    /**
     * Get permission by ID
     * Users with VIEW_ADMIN permission or SUPER_ADMIN role can access
     */
    @GetMapping("/{permissionId}")
    @PreAuthorize("hasAuthority('VIEW_ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PermissionResponse>> getPermissionById(@PathVariable Long permissionId) {
        log.info("Received request to fetch permission with ID: {}", permissionId);

        Permission permission = permissionService.findById(permissionId)
                .orElseThrow(() -> new RuntimeException("Permission not found with ID: " + permissionId));

        PermissionResponse permissionResponse = permissionMapper.toPermissionResponse(permission);

        log.info("Successfully retrieved permission: {}", permission.getName());

        return ResponseEntity.ok(ApiResponse.success(permissionResponse));
    }
}