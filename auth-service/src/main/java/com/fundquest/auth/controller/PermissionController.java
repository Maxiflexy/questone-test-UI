package com.fundquest.auth.controller;

import com.fundquest.auth.config.swagger.SwaggerConstants;
import com.fundquest.auth.dto.response.ApiResponse;
import com.fundquest.auth.dto.response.DetailedPermissionResponse;
import com.fundquest.auth.dto.response.HierarchicalPermissionResponse;
import com.fundquest.auth.dto.response.PermissionResponse;
import com.fundquest.auth.service.permission.PermissionService;
import io.swagger.v3.oas.annotations.Hidden;
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

    @GetMapping("/hierarchical")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('VIEW_ADMIN_PERMISSIONS')")
    @Operation(
            summary = "Get All Permissions with Hierarchical Structure",
            description = """
                **Retrieve All Permissions Organized by Groups and Categories**
                
                Returns permissions organized in a hierarchical structure:
                - Permission Groups (top level)
                - Categories (within groups) 
                - Permissions (within categories)
                
                This provides a structured view for UI components and permission management.
                """,
            tags = {SwaggerConstants.PERMISSION_TAG},
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Hierarchical permissions retrieved successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "Hierarchical Permissions",
                                    value = """
                        {
                          "success": true,
                          "data": {
                            "permissionGroups": [
                              {
                                "id": 1,
                                "name": "Admin Permissions",
                                "description": "Administrative permissions for system management",
                                "categories": [
                                  {
                                    "id": 1,
                                    "name": "Main Admin Permissions",
                                    "permissions": [
                                      {
                                        "id": 1,
                                        "name": "VIEW_OTHER_ADMIN_USERS",
                                        "description": "Can view other Admin Users"
                                      }
                                    ]
                                  }
                                ]
                              }
                            ]
                          }
                        }
                        """,
                                    description = "Complete hierarchical structure of permissions"
                            )
                    )
            )
    })
    public ResponseEntity<ApiResponse<HierarchicalPermissionResponse>> getAllPermissionsHierarchical() {
        log.info("Received request for hierarchical permissions");
        HierarchicalPermissionResponse response = permissionService.findAllHierarchical();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/get-roles")
    @Hidden
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(
            summary = SwaggerConstants.Permission.GET_ALL_SUMMARY,
            description = SwaggerConstants.Permission.GET_ALL_DESCRIPTION + """
                
                **Note:** This endpoint returns a flat list of permissions for backwards compatibility.
                For structured permissions, use the `/hierarchical` endpoint.
                """,
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
                                    description = "Complete list of all system permissions (flat structure)"
                            )
                    )
            )
    })
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getAllPermissions() {
        return ResponseEntity.ok(ApiResponse.success(permissionService.findAll()));
    }

    @GetMapping("/{permissionId}")
    @PreAuthorize("hasAuthority('VIEW_ADMIN_PERMISSIONS') or hasRole('SUPER_ADMIN')")
    @Operation(
            summary = SwaggerConstants.Permission.GET_BY_ID_SUMMARY,
            description = SwaggerConstants.Permission.GET_BY_ID_DESCRIPTION + """
                
                **Enhanced Response:** This endpoint now returns detailed permission information including:
                - Permission details (ID, name, description)
                - Category information (ID, name)  
                - Permission Group information (ID, name, description)
                """,
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
                                    name = "Detailed Permission Information",
                                    value = """
                                            {
                                               "success": true,
                                               "data": {
                                                 "permission": {
                                                   "id": 1,
                                                   "name": "VIEW_OTHER_ADMIN_USERS",
                                                   "description": "Can view other Admin Users"
                                                 },
                                                 "category": {
                                                   "id": 1,
                                                   "name": "Main Admin Permissions"
                                                 },
                                                 "permissionGroup": {
                                                   "id": 1,
                                                   "name": "Admin Permissions",
                                                   "description": "Administrative permissions for system management"
                                                 }
                                               }
                                             }
                        """,
                                    description = "Complete permission details with category and permission group information"
                            )
                    )
            )
    })
    public ResponseEntity<ApiResponse<DetailedPermissionResponse>> getPermissionById(
            @Parameter(
                    description = "Unique identifier of the permission",
                    required = true,
                    example = "1",
                    schema = @Schema(type = "integer", format = "int64", minimum = "1")
            )
            @PathVariable Long permissionId) {

        return ResponseEntity.ok(ApiResponse.success(permissionService.findByIdDetailed(permissionId)));
    }
}