package com.fundquest.auth.util;

import com.fundquest.auth.dto.response.RoleResponse;
import com.fundquest.auth.entity.Role;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;


@Component
@Slf4j
public class RoleMapper {

    /**
     * Convert Role entity to RoleResponse DTO
     *
     * @param role Role entity
     * @return RoleResponse DTO
     */
    public RoleResponse toRoleResponse(Role role) {
        if (role == null) {
            log.warn("Attempted to convert null Role to RoleResponse");
            return null;
        }

        log.debug("Converting Role entity to RoleResponse for role: {}", role.getName());

        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .build();
    }

    /**
     * Convert list of Role entities to list of RoleResponse DTOs
     *
     * @param roles List of Role entities
     * @return List of RoleResponse DTOs
     */
    public List<RoleResponse> toRoleResponseList(List<Role> roles) {
        if (roles == null) {
            log.warn("Attempted to convert null roles list to RoleResponse list");
            return List.of();
        }

        log.debug("Converting {} roles to RoleResponse list", roles.size());

        return roles.stream()
                .map(this::toRoleResponse)
                .collect(Collectors.toList());
    }
}