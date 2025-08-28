package com.fundquest.auth.dto.response;

import com.fundquest.auth.backoffice.modules.user.dto.response.UserDetailResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Detailed Permission Response DTO that includes permission group and category information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetailedPermissionResponse {

    private PermissionInfo permission;
    private CategoryInfo category;
    private PermissionGroupInfo permissionGroup;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PermissionInfo {
        private Long id;
        private String name;
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryInfo {
        private Long id;
        private String name;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PermissionGroupInfo {
        private Long id;
        private String name;
        private String description;
    }

}