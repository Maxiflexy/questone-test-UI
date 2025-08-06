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
        return ResponseEntity.ok(ApiResponse.success(permissionService.findAll()));
    }


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

        return ResponseEntity.ok(ApiResponse.success(permissionService.findById(permissionId)));
    }
}