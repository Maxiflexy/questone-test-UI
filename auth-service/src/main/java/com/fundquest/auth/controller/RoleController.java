package com.fundquest.auth.controller;

import com.fundquest.auth.config.swagger.SwaggerConstants;
import com.fundquest.auth.dto.response.ApiResponse;
import com.fundquest.auth.dto.response.RoleResponse;
import com.fundquest.auth.entity.Role;
import com.fundquest.auth.service.role.RoleService;
import com.fundquest.auth.util.RoleMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.fundquest.auth.constants.AppConstants.ROLE_ENDPOINT;

@RestController
@RequestMapping(ROLE_ENDPOINT)
@RequiredArgsConstructor
@Slf4j
@Tag(
        name = SwaggerConstants.ROLE_TAG,
        description = SwaggerConstants.ROLE_TAG_DESCRIPTION
)
public class RoleController {

    private final RoleService roleService;
    private final RoleMapper roleMapper;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(
            summary = SwaggerConstants.Role.GET_ALL_SUMMARY,
            description = SwaggerConstants.Role.GET_ALL_DESCRIPTION,
            tags = {SwaggerConstants.ROLE_TAG},
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "All roles retrieved successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "All Roles",
                                    value = SwaggerConstants.Role.ROLES_RESPONSE_EXAMPLE,
                                    description = "Complete list of all system roles including inactive ones"
                            )
                    )
            )
    })
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRoles() {
        log.info("Received request to fetch all roles");

        List<Role> roles = roleService.findAll();
        List<RoleResponse> roleResponses = roleMapper.toRoleResponseList(roles);

        log.info("Successfully retrieved {} roles", roleResponses.size());

        return ResponseEntity.ok(ApiResponse.success(roleResponses));
    }

//    @GetMapping("/active")
//    @PreAuthorize("hasAuthority('VIEW_ADMIN') or hasRole('SUPER_ADMIN')")
//    @Operation(
//            summary = SwaggerConstants.Role.GET_ACTIVE_SUMMARY,
//            description = SwaggerConstants.Role.GET_ACTIVE_DESCRIPTION,
//            tags = {SwaggerConstants.ROLE_TAG},
//            security = @SecurityRequirement(name = "bearerAuth")
//    )
//    @ApiResponses(value = {
//            @io.swagger.v3.oas.annotations.responses.ApiResponse(
//                    responseCode = "200",
//                    description = "Active roles retrieved successfully",
//                    content = @Content(
//                            mediaType = MediaType.APPLICATION_JSON_VALUE,
//                            schema = @Schema(implementation = ApiResponse.class),
//                            examples = @ExampleObject(
//                                    name = "Active Roles",
//                                    value = SwaggerConstants.Role.ROLES_RESPONSE_EXAMPLE,
//                                    description = "List of currently active roles available for user assignment"
//                            )
//                    )
//            ),
//            @io.swagger.v3.oas.annotations.responses.ApiResponse(
//                    responseCode = "401",
//                    description = "Unauthorized - Invalid or missing authentication token",
//                    content = @Content(
//                            mediaType = MediaType.APPLICATION_JSON_VALUE,
//                            schema = @Schema(implementation = ApiResponse.class),
//                            examples = @ExampleObject(
//                                    name = "Authentication Required",
//                                    value = SwaggerConstants.UNAUTHORIZED_RESPONSE
//                            )
//                    )
//            ),
//            @io.swagger.v3.oas.annotations.responses.ApiResponse(
//                    responseCode = "403",
//                    description = "Forbidden - VIEW_ADMIN permission or SUPER_ADMIN role required",
//                    content = @Content(
//                            mediaType = MediaType.APPLICATION_JSON_VALUE,
//                            schema = @Schema(implementation = ApiResponse.class),
//                            examples = @ExampleObject(
//                                    name = "Insufficient Permissions",
//                                    value = SwaggerConstants.FORBIDDEN_RESPONSE,
//                                    description = "User needs VIEW_ADMIN permission or SUPER_ADMIN role"
//                            )
//                    )
//            )
//    })
//    public ResponseEntity<ApiResponse<List<RoleResponse>>> getActiveRoles() {
//        log.info("Received request to fetch active roles");
//
//        List<Role> roles = roleService.findAllActive();
//        List<RoleResponse> roleResponses = roleMapper.toRoleResponseList(roles);
//
//        log.info("Successfully retrieved {} active roles", roleResponses.size());
//
//        return ResponseEntity.ok(ApiResponse.success(roleResponses));
//    }

    @GetMapping("/{roleId}")
    @PreAuthorize("hasAuthority('VIEW_ADMIN') or hasRole('SUPER_ADMIN')")
    @Operation(
            summary = SwaggerConstants.Role.GET_BY_ID_SUMMARY,
            description = SwaggerConstants.Role.GET_BY_ID_DESCRIPTION,
            tags = {SwaggerConstants.ROLE_TAG},
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Role details retrieved successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "Role Details",
                                    value = """
                        {
                          "success": true,
                          "data": {
                            "id": 1,
                            "name": "SUPER_ADMIN"
                          }
                        }
                        """,
                                    description = "Detailed information about specific role"
                            )
                    )
            )
    })
    public ResponseEntity<ApiResponse<RoleResponse>> getRoleById(
            @Parameter(
                    description = "Unique identifier of the role",
                    required = true,
                    example = "1",
                    schema = @Schema(type = "integer", format = "int64", minimum = "1")
            )
            @PathVariable Long roleId) {
        log.info("Received request to fetch role with ID: {}", roleId);

        Role role = roleService.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with ID: " + roleId));

        RoleResponse roleResponse = roleMapper.toRoleResponse(role);

        log.info("Successfully retrieved role: {}", role.getName());

        return ResponseEntity.ok(ApiResponse.success(roleResponse));
    }
}