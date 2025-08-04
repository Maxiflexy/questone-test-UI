package com.fundquest.auth.controller;

import com.fundquest.auth.config.swagger.SwaggerConstants;
import com.fundquest.auth.dto.response.ApiResponse;
import com.fundquest.auth.dto.response.PermissionResponse;
import com.fundquest.auth.entity.Permission;
import com.fundquest.auth.service.permission.PermissionService;
import com.fundquest.auth.util.PermissionMapper;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.fundquest.auth.constants.AppConstants.PERMISSIONS_ENDPOINT;

@RestController
@RequestMapping(PERMISSIONS_ENDPOINT)
@RequiredArgsConstructor
@Slf4j
@Tag(
        name = SwaggerConstants.PERMISSION_TAG,
        description = SwaggerConstants.PERMISSION_TAG_DESCRIPTION
)
public class PermissionController {

    private final PermissionService permissionService;
    private final PermissionMapper permissionMapper;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(
            summary = SwaggerConstants.Permission.GET_ALL_SUMMARY,
            description = SwaggerConstants.Permission.GET_ALL_DESCRIPTION,
            tags = {SwaggerConstants.PERMISSION_TAG},
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "All permissions retrieved successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "All Permissions",
                                    value = SwaggerConstants.Permission.PERMISSIONS_RESPONSE_EXAMPLE,
                                    description = "Complete list of all system permissions"
                            )
                    )
            )
    })
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getAllPermissions() {
        log.info("Received request to fetch all permissions");

        List<Permission> permissions = permissionService.findAll();
        List<PermissionResponse> permissionResponses = permissionMapper.toPermissionResponseList(permissions);

        log.info("Successfully retrieved {} permissions", permissionResponses.size());

        return ResponseEntity.ok(ApiResponse.success(permissionResponses));
    }


//    @GetMapping("/active")
//    @PreAuthorize("hasAuthority('ASSIGN_AND_UNASSIGN') or hasRole('SUPER_ADMIN')")
//    @Operation(
//            summary = SwaggerConstants.Permission.GET_ACTIVE_SUMMARY,
//            description = SwaggerConstants.Permission.GET_ACTIVE_DESCRIPTION,
//            tags = {SwaggerConstants.PERMISSION_TAG},
//            security = @SecurityRequirement(name = "bearerAuth")
//    )
//    @ApiResponses(value = {
//            @io.swagger.v3.oas.annotations.responses.ApiResponse(
//                    responseCode = "200",
//                    description = "Active permissions retrieved successfully",
//                    content = @Content(
//                            mediaType = MediaType.APPLICATION_JSON_VALUE,
//                            schema = @Schema(implementation = ApiResponse.class),
//                            examples = @ExampleObject(
//                                    name = "Active Permissions",
//                                    value = SwaggerConstants.Permission.PERMISSIONS_RESPONSE_EXAMPLE,
//                                    description = "List of currently active permissions available for assignment"
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
//                    description = "Forbidden - ASSIGN_AND_UNASSIGN permission or SUPER_ADMIN role required",
//                    content = @Content(
//                            mediaType = MediaType.APPLICATION_JSON_VALUE,
//                            schema = @Schema(implementation = ApiResponse.class),
//                            examples = @ExampleObject(
//                                    name = "Insufficient Permissions",
//                                    value = SwaggerConstants.FORBIDDEN_RESPONSE,
//                                    description = "User needs ASSIGN_AND_UNASSIGN permission or SUPER_ADMIN role"
//                            )
//                    )
//            )
//    })
//    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getActivePermissions() {
//        log.info("Received request to fetch active permissions");
//
//        List<Permission> permissions = permissionService.findAllActive();
//        List<PermissionResponse> permissionResponses = permissionMapper.toPermissionResponseList(permissions);
//
//        log.info("Successfully retrieved {} active permissions", permissionResponses.size());
//
//        return ResponseEntity.ok(ApiResponse.success(permissionResponses));
//    }
//    @GetMapping("/category/{category}")
//    @PreAuthorize("hasAuthority('ASSIGN_AND_UNASSIGN') or hasRole('SUPER_ADMIN')")
//    @Operation(
//            summary = SwaggerConstants.Permission.GET_BY_CATEGORY_SUMMARY,
//            description = SwaggerConstants.Permission.GET_BY_CATEGORY_DESCRIPTION,
//            tags = {SwaggerConstants.PERMISSION_TAG},
//            security = @SecurityRequirement(name = "bearerAuth")
//    )
//    @ApiResponses(value = {
//            @io.swagger.v3.oas.annotations.responses.ApiResponse(
//                    responseCode = "200",
//                    description = "Permissions for category retrieved successfully",
//                    content = @Content(
//                            mediaType = MediaType.APPLICATION_JSON_VALUE,
//                            schema = @Schema(implementation = ApiResponse.class),
//                            examples = @ExampleObject(
//                                    name = "Category Permissions",
//                                    value = """
//                        {
//                          "success": true,
//                          "data": [
//                            {
//                              "id": 1,
//                              "name": "INVITE_ADMIN"
//                            },
//                            {
//                              "id": 2,
//                              "name": "VIEW_OTHER_ADMIN_USERS"
//                            },
//                            {
//                              "id": 3,
//                              "name": "EDIT_ADMIN_PROFILE"
//                            }
//                          ]
//                        }
//                        """,
//                                    description = "Permissions filtered by ADMIN_MANAGEMENT category"
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
//                    description = "Forbidden - ASSIGN_AND_UNASSIGN permission or SUPER_ADMIN role required",
//                    content = @Content(
//                            mediaType = MediaType.APPLICATION_JSON_VALUE,
//                            schema = @Schema(implementation = ApiResponse.class),
//                            examples = @ExampleObject(
//                                    name = "Insufficient Permissions",
//                                    value = SwaggerConstants.FORBIDDEN_RESPONSE
//                            )
//                    )
//            )
//    })
//    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getPermissionsByCategory(
//            @Parameter(
//                    description = "Permission category to filter by",
//                    required = true,
//                    example = "ADMIN_MANAGEMENT",
//                    schema = @Schema(
//                            type = "string",
//                            allowableValues = {"ADMIN_MANAGEMENT", "CUSTOMER_MANAGEMENT", "GENERAL"}
//                    )
//            )
//            @PathVariable String category) {
//        log.info("Received request to fetch permissions by category: {}", category);
//
//        List<Permission> permissions = permissionService.findByCategory(category);
//        List<PermissionResponse> permissionResponses = permissionMapper.toPermissionResponseList(permissions);
//
//        log.info("Successfully retrieved {} permissions for category: {}", permissionResponses.size(), category);
//
//        return ResponseEntity.ok(ApiResponse.success(permissionResponses));
//    }

    @GetMapping("/{permissionId}")
    @PreAuthorize("hasAuthority('VIEW_ADMIN') or hasRole('SUPER_ADMIN')")
    @Operation(
            summary = SwaggerConstants.Permission.GET_BY_ID_SUMMARY,
            description = SwaggerConstants.Permission.GET_BY_ID_DESCRIPTION,
            tags = {SwaggerConstants.PERMISSION_TAG},
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Permission details retrieved successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "Permission Details",
                                    value = """
                        {
                          "success": true,
                          "data": {
                            "id": 1,
                            "name": "VIEW_ADMIN"
                          }
                        }
                        """,
                                    description = "Detailed information about specific permission"
                            )
                    )
            )
    })
    public ResponseEntity<ApiResponse<PermissionResponse>> getPermissionById(
            @Parameter(
                    description = "Unique identifier of the permission",
                    required = true,
                    example = "1",
                    schema = @Schema(type = "integer", format = "int64", minimum = "1")
            )
            @PathVariable Long permissionId) {
        log.info("Received request to fetch permission with ID: {}", permissionId);

        Permission permission = permissionService.findById(permissionId)
                .orElseThrow(() -> new RuntimeException("Permission not found with ID: " + permissionId));

        PermissionResponse permissionResponse = permissionMapper.toPermissionResponse(permission);

        log.info("Successfully retrieved permission: {}", permission.getName());

        return ResponseEntity.ok(ApiResponse.success(permissionResponse));
    }
}