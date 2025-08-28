package com.fundquest.auth.audit_trail.controller;

import com.fundquest.auth.audit_trail.dto.request.AuditSearchRequest;
import com.fundquest.auth.audit_trail.dto.response.AuditTrailPageResponse;
import com.fundquest.auth.audit_trail.dto.response.AuditTrailResponse;
import com.fundquest.auth.audit_trail.entity.enums.ActionType;
import com.fundquest.auth.audit_trail.entity.enums.AuditStatus;
import com.fundquest.auth.audit_trail.entity.enums.ResourceType;
import com.fundquest.auth.audit_trail.service.AuditTrailService;
import com.fundquest.auth.dto.response.ApiResponse;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import static com.fundquest.auth.constants.AppConstants.AUDIT_ENDPOINT;
import static com.fundquest.auth.constants.AppConstants.AUTH_BASE_PATH;

@RestController
@RequestMapping(AUTH_BASE_PATH +AUDIT_ENDPOINT)
@RequiredArgsConstructor
@Slf4j
@Tag(
        name = "Audit Trail Management",
        description = "Endpoints for viewing and searching system audit logs. All operations require Super Admin privileges."
)
public class AuditTrailController {

    private final AuditTrailService auditTrailService;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(
            summary = "Get All Audit Trails",
            description = "Retrieve all audit trails with pagination. Returns audit logs ordered by most recent first.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Audit trails retrieved successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "Audit Trails Response",
                                    value = """
                                    {
                                      "success": true,
                                      "data": {
                                        "content": [
                                          {
                                            "id": 1,
                                            "userEmail": "admin@fundquestnigeria.com",
                                            "userName": "John Doe",
                                            "userRole": "SUPER_ADMIN",
                                            "actionType": "ACTIVATE",
                                            "actionDescription": "User user@example.com was activated",
                                            "resourceType": "USER",
                                            "resourceIdentifier": "user@example.com",
                                            "initiatedDate": "2024-03-15",
                                            "initiatedTime": "14:30:45",
                                            "status": "SUCCESS",
                                            "serviceName": "auth-service"
                                          }
                                        ],
                                        "page": 1,
                                        "size": 8,
                                        "totalElements": 25,
                                        "totalPages": 4,
                                        "isFirstPage": true,
                                        "isLastPage": false,
                                        "hasNext": true,
                                        "hasPrevious": false,
                                        "numberOfElements": 8
                                      }
                                    }
                                    """,
                                    description = "Paginated list of audit trail records"
                            )
                    )
            )
    })
    public ResponseEntity<ApiResponse<AuditTrailPageResponse>> getAllAuditTrails(
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

        AuditTrailPageResponse response = auditTrailService.getAllAuditTrails(page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(
            summary = "Search Audit Trails",
            description = "Search audit trails with multiple filter criteria including user, action type, resource type, date range, and general search term.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Filtered audit trails retrieved successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            )
    })
    public ResponseEntity<ApiResponse<AuditTrailPageResponse>> searchAuditTrails(
            @Parameter(description = "Filter by user email (partial match, case-insensitive)")
            @RequestParam(required = false) String userEmail,

            @Parameter(description = "Filter by user name (partial match, case-insensitive)")
            @RequestParam(required = false) String userName,

            @Parameter(description = "Filter by action type")
            @RequestParam(required = false) ActionType actionType,

            @Parameter(description = "Filter by resource type")
            @RequestParam(required = false) ResourceType resourceType,

            @Parameter(description = "Filter by status")
            @RequestParam(required = false) AuditStatus status,

            @Parameter(description = "Start date for filtering (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @Parameter(description = "End date for filtering (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,

            @Parameter(description = "General search term for action description (partial match, case-insensitive)")
            @RequestParam(required = false) String searchTerm,

            @Parameter(description = "Page number (1-based). Default: 1")
            @RequestParam(defaultValue = "1") int page,

            @Parameter(description = "Page size (maximum 8). Default: 8")
            @RequestParam(defaultValue = "8") int size) {

        AuditSearchRequest searchRequest = AuditSearchRequest.builder()
                .userEmail(userEmail)
                .userName(userName)
                .actionType(actionType)
                .resourceType(resourceType)
                .status(status)
                .startDate(startDate)
                .endDate(endDate)
                .searchTerm(searchTerm)
                .build();

        AuditTrailPageResponse response = auditTrailService.searchAuditTrails(searchRequest, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/user/{userEmail:.+}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(
            summary = "Get User Audit Trails",
            description = "Retrieve all audit trails for a specific user with pagination.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "User audit trails retrieved successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            )
    })
    public ResponseEntity<ApiResponse<AuditTrailPageResponse>> getUserAuditTrails(
            @Parameter(
                    description = "User email to get audit trails for",
                    required = true,
                    example = "user@fundquestnigeria.com"
            )
            @PathVariable String userEmail,

            @Parameter(description = "Page number (1-based). Default: 1")
            @RequestParam(defaultValue = "1") int page,

            @Parameter(description = "Page size (maximum 8). Default: 8")
            @RequestParam(defaultValue = "8") int size) {

        AuditTrailPageResponse response = auditTrailService.getUserAuditTrails(userEmail, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{auditId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(
            summary = "Get Audit Trail Details",
            description = "Retrieve detailed information for a specific audit trail entry including full request parameters and context.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Audit trail details retrieved successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "Detailed Audit Trail",
                                    value = """
                                    {
                                      "success": true,
                                      "data": {
                                        "id": 1,
                                        "userEmail": "admin@fundquestnigeria.com",
                                        "userName": "John Doe",
                                        "userRole": "SUPER_ADMIN",
                                        "actionType": "ACTIVATE",
                                        "actionDescription": "User user@example.com was activated",
                                        "resourceType": "USER",
                                        "resourceId": "user-123",
                                        "resourceIdentifier": "user@example.com",
                                        "endpoint": "/api/v1/auth/user/status",
                                        "httpMethod": "PUT",
                                        "requestParameters": "{\\"email\\":\\"user@example.com\\",\\"isActive\\":true}",
                                        "initiatedDate": "2024-03-15",
                                        "initiatedTime": "14:30:45",
                                        "initiatedTimestamp": "2024-03-15T14:30:45",
                                        "ipAddress": "192.168.1.100",
                                        "userAgent": "Mozilla/5.0...",
                                        "sessionId": "SESSION123",
                                        "status": "SUCCESS",
                                        "serviceName": "auth-service",
                                        "createdAt": "2024-03-15T14:30:45"
                                      }
                                    }
                                    """,
                                    description = "Complete audit trail record with all details"
                            )
                    )
            )
    })
    public ResponseEntity<ApiResponse<AuditTrailResponse>> getAuditTrailById(
            @Parameter(
                    description = "Audit trail ID",
                    required = true,
                    example = "1"
            )
            @PathVariable Long auditId) {

        AuditTrailResponse response = auditTrailService.getAuditTrailById(auditId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/actions")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(
            summary = "Get Available Action Types",
            description = "Retrieve all available action types for filtering audit trails.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Action types retrieved successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "Action Types",
                                    value = """
                                    {
                                      "success": true,
                                      "data": [
                                        "CREATE", "UPDATE", "DELETE", "ACTIVATE", "DEACTIVATE", 
                                        "INVITE", "LOGIN", "LOGOUT", "ASSIGN_PERMISSION", 
                                        "REMOVE_PERMISSION", "CHANGE_ROLE", "RESET_PASSWORD", "VERIFY"
                                      ]
                                    }
                                    """,
                                    description = "List of all available action types"
                            )
                    )
            )
    })
    public ResponseEntity<ApiResponse<ActionType[]>> getActionTypes() {
        return ResponseEntity.ok(ApiResponse.success(ActionType.values()));
    }

    @GetMapping("/resources")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(
            summary = "Get Available Resource Types",
            description = "Retrieve all available resource types for filtering audit trails.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Resource types retrieved successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "Resource Types",
                                    value = """
                                    {
                                      "success": true,
                                      "data": [
                                        "USER", "ROLE", "PERMISSION", "SESSION", "AUTHENTICATION", "PROFILE"
                                      ]
                                    }
                                    """,
                                    description = "List of all available resource types"
                            )
                    )
            )
    })
    public ResponseEntity<ApiResponse<ResourceType[]>> getResourceTypes() {
        return ResponseEntity.ok(ApiResponse.success(ResourceType.values()));
    }

    @GetMapping("/statuses")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(
            summary = "Get Available Audit Statuses",
            description = "Retrieve all available audit statuses for filtering audit trails.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Audit statuses retrieved successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    name = "Audit Statuses",
                                    value = """
                                    {
                                      "success": true,
                                      "data": [
                                        "SUCCESS", "FAILED", "PARTIAL"
                                      ]
                                    }
                                    """,
                                    description = "List of all available audit statuses"
                            )
                    )
            )
    })
    public ResponseEntity<ApiResponse<AuditStatus[]>> getAuditStatuses() {
        return ResponseEntity.ok(ApiResponse.success(AuditStatus.values()));
    }
}
