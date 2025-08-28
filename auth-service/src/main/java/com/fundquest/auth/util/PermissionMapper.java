package com.fundquest.auth.util;

import com.fundquest.auth.dto.response.*;
import com.fundquest.auth.entity.Category;
import com.fundquest.auth.entity.Permission;
import com.fundquest.auth.entity.PermissionGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class PermissionMapper {

    /**
     * Convert Permission entity to PermissionResponse DTO (flat structure for backwards compatibility)
     *
     * @param permission Permission entity
     * @return PermissionResponse DTO
     */
    public PermissionResponse toPermissionResponse(Permission permission) {
        if (permission == null) {
            log.warn("Attempted to convert null Permission to PermissionResponse");
            return null;
        }

        log.debug("Converting Permission entity to PermissionResponse for permission: {}", permission.getName());

        return PermissionResponse.builder()
                .id(permission.getId())
                .name(permission.getName())
                .build();
    }

    /**
     * Convert list of Permission entities to list of PermissionResponse DTOs (flat structure)
     *
     * @param permissions List of Permission entities
     * @return List of PermissionResponse DTOs
     */
    public List<PermissionResponse> toPermissionResponseList(List<Permission> permissions) {
        if (permissions == null) {
            log.warn("Attempted to convert null permissions list to PermissionResponse list");
            return List.of();
        }

        log.debug("Converting {} permissions to PermissionResponse list", permissions.size());

        return permissions.stream()
                .map(this::toPermissionResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convert list of PermissionGroups to hierarchical permission response
     *
     * @param permissionGroups List of PermissionGroup entities with loaded categories and permissions
     * @return HierarchicalPermissionResponse with nested structure
     */
    public HierarchicalPermissionResponse toHierarchicalPermissionResponse(List<PermissionGroup> permissionGroups) {
        if (permissionGroups == null) {
            log.warn("Attempted to convert null permission groups to hierarchical response");
            return HierarchicalPermissionResponse.builder()
                    .permissionGroups(List.of())
                    .build();
        }

        log.debug("Converting {} permission groups to hierarchical response", permissionGroups.size());

        List<PermissionGroupResponse> groupResponses = permissionGroups.stream()
                .map(this::toPermissionGroupResponse)
                .collect(Collectors.toList());

        return HierarchicalPermissionResponse.builder()
                .permissionGroups(groupResponses)
                .build();
    }

    /**
     * Convert PermissionGroup entity to PermissionGroupResponse DTO
     *
     * @param permissionGroup PermissionGroup entity
     * @return PermissionGroupResponse DTO
     */
    public PermissionGroupResponse toPermissionGroupResponse(PermissionGroup permissionGroup) {
        if (permissionGroup == null) {
            log.warn("Attempted to convert null PermissionGroup to PermissionGroupResponse");
            return null;
        }

        log.debug("Converting PermissionGroup entity: {}", permissionGroup.getName());

        List<CategoryResponse> categoryResponses = permissionGroup.getCategories().stream()
                .filter(Category::isActive)
                .map(this::toCategoryResponse)
                .collect(Collectors.toList());

        return PermissionGroupResponse.builder()
                .id(permissionGroup.getId())
                .name(permissionGroup.getName())
                .description(permissionGroup.getDescription())
                .categories(categoryResponses)
                .build();
    }

    /**
     * Convert Category entity to CategoryResponse DTO
     *
     * @param category Category entity
     * @return CategoryResponse DTO
     */
    public CategoryResponse toCategoryResponse(Category category) {
        if (category == null) {
            log.warn("Attempted to convert null Category to CategoryResponse");
            return null;
        }

        log.debug("Converting Category entity: {}", category.getName());

        List<PermissionDetailResponse> permissionResponses = category.getPermissions().stream()
                .filter(Permission::isActive)
                .map(this::toPermissionDetailResponse)
                .collect(Collectors.toList());

        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .permissions(permissionResponses)
                .build();
    }

    /**
     * Convert Permission entity to DetailedPermissionResponse DTO with category and permission group info
     *
     * @param permission Permission entity with loaded category and permission group
     * @return DetailedPermissionResponse DTO
     */
    public DetailedPermissionResponse toDetailedPermissionResponse(Permission permission) {
        if (permission == null) {
            log.warn("Attempted to convert null Permission to DetailedPermissionResponse");
            return null;
        }

        // Build permission info
        DetailedPermissionResponse.PermissionInfo permissionInfo = DetailedPermissionResponse.PermissionInfo.builder()
                .id(permission.getId())
                .name(permission.getName())
                .description(permission.getDescription())
                .build();

        // Build category info
        DetailedPermissionResponse.CategoryInfo categoryInfo = null;
        if (permission.getCategory() != null) {
            categoryInfo = DetailedPermissionResponse.CategoryInfo.builder()
                    .id(permission.getCategory().getId())
                    .name(permission.getCategory().getName())
                    .build();
        }

        // Build permission group info
        DetailedPermissionResponse.PermissionGroupInfo permissionGroupInfo = null;
        if (permission.getCategory() != null && permission.getCategory().getPermissionGroup() != null) {
            permissionGroupInfo = DetailedPermissionResponse.PermissionGroupInfo.builder()
                    .id(permission.getCategory().getPermissionGroup().getId())
                    .name(permission.getCategory().getPermissionGroup().getName())
                    .description(permission.getCategory().getPermissionGroup().getDescription())
                    .build();
        }

        return DetailedPermissionResponse.builder()
                .permission(permissionInfo)
                .category(categoryInfo)
                .permissionGroup(permissionGroupInfo)
                .build();
    }
     /*
             * @param permission Permission entity
     * @return PermissionDetailResponse DTO
     */
    public PermissionDetailResponse toPermissionDetailResponse(Permission permission) {
        if (permission == null) {
            log.warn("Attempted to convert null Permission to PermissionDetailResponse");
            return null;
        }

        log.debug("Converting Permission entity to detailed response: {}", permission.getName());

        return PermissionDetailResponse.builder()
                .id(permission.getId())
                .name(permission.getName())
                .description(permission.getDescription())
                .build();
    }
}