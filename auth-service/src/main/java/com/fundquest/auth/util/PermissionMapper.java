package com.fundquest.auth.util;

import com.fundquest.auth.dto.response.PermissionResponse;
import com.fundquest.auth.entity.Permission;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;


@Component
@Slf4j
public class PermissionMapper {

    /**
     * Convert Permission entity to PermissionResponse DTO
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
     * Convert list of Permission entities to list of PermissionResponse DTOs
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
}