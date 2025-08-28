package com.fundquest.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Root response DTO containing all permission groups with nested structure
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HierarchicalPermissionResponse {
    private List<PermissionGroupResponse> permissionGroups;
}
