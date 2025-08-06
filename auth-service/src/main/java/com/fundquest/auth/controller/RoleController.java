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

import static com.fundquest.auth.config.swagger.SwaggerConstants.*;
import static com.fundquest.auth.constants.AppConstants.ROLE_ENDPOINT;

@RestController
@RequestMapping(ROLE_ENDPOINT)
@RequiredArgsConstructor
@Slf4j
@Tag(
        name = ROLE_TAG,
        description = ROLE_TAG_DESCRIPTION
)
public class RoleController {

    private final RoleService roleService;


    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(
            summary = SwaggerConstants.Role.GET_ALL_SUMMARY,
            description = SwaggerConstants.Role.GET_ALL_DESCRIPTION,
            tags = {ROLE_TAG},
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
        return ResponseEntity.ok(ApiResponse.success(roleService.findAll()));
    }

    @GetMapping("/{roleId}")
    @PreAuthorize("hasAuthority('VIEW_ADMIN') or hasRole('SUPER_ADMIN')")
    @Operation(
            summary = SwaggerConstants.Role.GET_BY_ID_SUMMARY,
            description = SwaggerConstants.Role.GET_BY_ID_DESCRIPTION,
            tags = {ROLE_TAG},
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


        return ResponseEntity.ok(ApiResponse.success(roleService.findById(roleId)));
    }
}