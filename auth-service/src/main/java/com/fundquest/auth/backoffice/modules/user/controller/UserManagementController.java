package com.fundquest.auth.backoffice.modules.user.controller;

import com.fundquest.auth.dto.response.ApiResponse;
import com.fundquest.auth.backoffice.modules.user.dto.response.UserPageResponse;
import com.fundquest.auth.backoffice.modules.user.service.management.UserManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/user")
@RequiredArgsConstructor
@Slf4j
@Tag(
        name = "User Management",
        description = "Endpoints for managing and retrieving user information with pagination, search, and filtering"
)
public class UserManagementController {

    private final UserManagementService userManagementService;

    @GetMapping("/list")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('VIEW_OTHER_ADMIN_USERS')")
    @Operation(
            summary = "Get All Users with Pagination",
            description = "Retrieve all users with pagination support",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<UserPageResponse>> getAllUsers(
            @Parameter(
                    description = "Page number (1-based). Default: 1",
                    example = "1",
                    schema = @Schema(type = "integer", minimum = "1", defaultValue = "1")
            )
            @RequestParam(defaultValue = "1") int page,

            @Parameter(
                    description = "Page size (maximum 8). Default: 8",
                    example = "8",
                    schema = @Schema(type = "integer", minimum = "1", maximum = "8", defaultValue = "8")
            )
            @RequestParam(defaultValue = "8") int size) {

        return ResponseEntity.ok(ApiResponse.success(userManagementService.getAllUsers(page, size)));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('VIEW_OTHER_ADMIN_USERS')")
    @Operation(
            summary = "Search Users by Name",
            description = "Search users by name with pagination",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<UserPageResponse>> searchUsers(
            @Parameter(
                    description = "Name to search for (case-insensitive partial match)",
                    required = true,
                    example = "John",
                    schema = @Schema(type = "string", minLength = 1)
            )
            @RequestParam String name,

            @Parameter(
                    description = "Page number (1-based). Default: 1",
                    example = "1",
                    schema = @Schema(type = "integer", minimum = "1", defaultValue = "1")
            )
            @RequestParam(defaultValue = "1") int page,

            @Parameter(
                    description = "Page size (maximum 8). Default: 8",
                    example = "8",
                    schema = @Schema(type = "integer", minimum = "1", maximum = "8", defaultValue = "8")
            )
            @RequestParam(defaultValue = "8") int size) {

        return ResponseEntity.ok(ApiResponse.success(userManagementService.searchUsersByName(name, page, size)));
    }

    @GetMapping("/filter")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('VIEW_OTHER_ADMIN_USERS')")
    @Operation(
            summary = "Filter Users by Status",
            description = "Filter users by active/inactive status with pagination",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<UserPageResponse>> filterUsers(
            @Parameter(
                    description = "Filter by user status. true = active users, false = inactive users",
                    required = true,
                    example = "true",
                    schema = @Schema(type = "boolean")
            )
            @RequestParam("status") boolean isActive,

            @Parameter(
                    description = "Page number (1-based). Default: 1",
                    example = "1",
                    schema = @Schema(type = "integer", minimum = "1", defaultValue = "1")
            )
            @RequestParam(defaultValue = "1") int page,

            @Parameter(
                    description = "Page size (maximum 8). Default: 8",
                    example = "8",
                    schema = @Schema(type = "integer", minimum = "1", maximum = "8", defaultValue = "8")
            )
            @RequestParam(defaultValue = "8") int size) {

        return ResponseEntity.ok(ApiResponse.success(userManagementService.filterUsersByStatus(isActive, page, size)));
    }
}
