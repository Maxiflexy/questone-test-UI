package com.fundquest.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Detailed Permission Response DTO with description
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionDetailResponse {
    private Long id;
    private String name;
    private String description;
}
