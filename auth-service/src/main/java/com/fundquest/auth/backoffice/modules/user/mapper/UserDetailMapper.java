package com.fundquest.auth.backoffice.modules.user.mapper;

import com.fundquest.auth.entity.User;
import com.fundquest.auth.backoffice.modules.user.dto.response.UserDetailResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class UserDetailMapper {

    /**
     * Convert User entity to UserDetailResponse DTO
     *
     * @param user User entity
     * @return UserDetailResponse DTO
     */
    public UserDetailResponse toUserDetailResponse(User user) {
        if (user == null) {
            log.warn("Attempted to convert null User to UserDetailResponse");
            return null;
        }

        // Convert role information
        UserDetailResponse.RoleInfo roleInfo = null;
        if (user.getRole() != null) {
            roleInfo = UserDetailResponse.RoleInfo.builder()
                    .id(user.getRole().getId())
                    .name(user.getRole().getName())
                    .build();
        }

        // Convert permissions information
        List<UserDetailResponse.PermissionInfo> permissionInfos = user.getPermissions().stream()
                .map(permission -> UserDetailResponse.PermissionInfo.builder()
                        .id(permission.getId())
                        .name(permission.getName())
                        .build())
                .collect(Collectors.toList());

        return UserDetailResponse.builder()
                .email(user.getEmail())
                .name(user.getName())
                .phoneNumber(user.getPhoneNumber())
                .lastModifiedBy(user.getLastModifiedBy())
                .status(user.isActive())
                .createdDate(user.getCreatedAt())
                .lastModifiedDate(user.getUpdatedAt())
                .role(roleInfo)
                .permissions(permissionInfos)
                .build();
    }
}