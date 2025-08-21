package com.fundquest.auth.backoffice.modules.user.controller;

import com.fundquest.auth.backoffice.modules.user.dto.request.InviteUserRequest;
import com.fundquest.auth.backoffice.modules.user.dto.request.UpdateUserPermissionsRequest;
import com.fundquest.auth.backoffice.modules.user.dto.request.UpdateUserStatusRequest;
import com.fundquest.auth.backoffice.modules.user.dto.response.UserDetailResponse;
import com.fundquest.auth.backoffice.modules.user.service.invite.UserInvitationService;
import com.fundquest.auth.dto.response.ApiResponse;
import com.fundquest.auth.backoffice.modules.user.dto.response.UserPageResponse;
import com.fundquest.auth.backoffice.modules.user.service.management.UserManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.fundquest.auth.constants.AppConstants.AUTH_BASE_PATH;

@RestController
@RequestMapping(AUTH_BASE_PATH + "/user")
@RequiredArgsConstructor
@Slf4j
@Tag(
        name = "User Management",
        description = "Endpoints for inviting, managing and retrieving user information with pagination, search, and filtering"
)
public class UserManagementController {

    private final UserManagementService userManagementService;
    private final UserInvitationService userInvitationService;

    @PostMapping("/invite")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(
            summary = "Invite New User",
            description = "Invite a new user to the system with specified role and permissions",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<String>> inviteUser(
            @Parameter(
                    description = "User invitation request with email, role ID, and permission IDs",
                    required = true,
                    schema = @Schema(implementation = InviteUserRequest.class),
                    example = """
                    {
                      "email": "newuser@fundquestnigeria.com",
                      "roleId": 2,
                      "permissionIds": [1, 8]
                    }
                    """
            )
            @Valid @RequestBody InviteUserRequest request) {

        userInvitationService.inviteUser(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User invitation sent successfully"));
    }

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

    @GetMapping("/details")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('VIEW_OTHER_ADMIN_USERS')")
    @Operation(
            summary = "Get User Details by Email",
            description = "Retrieve detailed information for a specific user including their permissions and role",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<UserDetailResponse>> getUserByEmail(
            @Parameter(
                    description = "Email address of the user to retrieve",
                    required = true,
                    example = "john.doe@fundquestnigeria.com",
                    schema = @Schema(type = "string", format = "email")
            )
            @RequestParam String email) {

        return ResponseEntity.ok(ApiResponse.success(userManagementService.getUserByEmail(email)));
    }

    @PutMapping("/permissions")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('INITIATE_PERMISSION_ASSIGNMENT')")
    @Operation(
            summary = "Update User Permissions",
            description = "Update the permissions assigned to a specific user. This will replace all existing permissions with the new ones provided.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<UserDetailResponse>> updateUserPermissions(
            @Parameter(
                    description = "Email address of the user whose permissions are to be updated",
                    required = true,
                    example = "john.doe@fundquestnigeria.com",
                    schema = @Schema(type = "string", format = "email")
            )
            @RequestParam String email,

            @Parameter(
                    description = "List of permission names to assign to the user",
                    required = true,
                    schema = @Schema(implementation = UpdateUserPermissionsRequest.class)
            )
            @Valid @RequestBody UpdateUserPermissionsRequest request) {

        UserDetailResponse response = userManagementService.updateUserPermissions(email, request.getPermissionNames());
        return ResponseEntity.ok(ApiResponse.success(response, "User permissions updated successfully"));
    }

    @PutMapping("/status")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('INITIATE_ADMIN_STATUS_CHANGE')")
    @Operation(
            summary = "Update User Status (Activate/Deactivate)",
            description = "Activate or deactivate a user account. Deactivated users cannot access the system. Users cannot deactivate their own account.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<UserDetailResponse>> updateUserStatus(
            @Parameter(
                    description = "Email address of the user whose status is to be updated",
                    required = true,
                    example = "john.doe@fundquestnigeria.com",
                    schema = @Schema(type = "string", format = "email")
            )
            @RequestParam String email,

            @Parameter(
                    description = "User status update request. Set isActive to true to activate user, false to deactivate.",
                    required = true,
                    schema = @Schema(implementation = UpdateUserStatusRequest.class),
                    example = """
                    {
                      "isActive": true
                    }
                    """
            )
            @Valid @RequestBody UpdateUserStatusRequest request) {

        return ResponseEntity.ok(ApiResponse.success(
                userManagementService.updateUserStatus(email, request.getIsActive()),
                request.getIsActive() ? "User activated successfully" : "User deactivated successfully")
        );
    }
}
