//package com.fundquest.auth.backoffice.modules.user.controller;
//
//import com.fundquest.auth.dto.response.ApiResponse;
//import com.fundquest.auth.backoffice.modules.user.dto.request.InviteUserRequest;
//import com.fundquest.auth.backoffice.modules.user.service.invite.UserInvitationService;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.Parameter;
//import io.swagger.v3.oas.annotations.media.Schema;
//import io.swagger.v3.oas.annotations.security.SecurityRequirement;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/api/v1/auth/user/invite")
//@RequiredArgsConstructor
//@Slf4j
//@Tag(
//        name = "User Invitation Management",
//        description = "Endpoints for inviting new users with specific roles and permissions"
//)
//public class UserInvitationController {
//
//    private final UserInvitationService userInvitationService;
//
//    @PostMapping()
//    @PreAuthorize("hasRole('SUPER_ADMIN')")
//    @Operation(
//            summary = "Invite New User",
//            description = "Invite a new user to the system with specified role and permissions",
//            security = @SecurityRequirement(name = "bearerAuth")
//    )
//    public ResponseEntity<ApiResponse<String>> inviteUser(
//            @Parameter(
//                    description = "User invitation request with email, role ID, and permission IDs",
//                    required = true,
//                    schema = @Schema(implementation = InviteUserRequest.class),
//                    example = """
//                    {
//                      "email": "newuser@fundquestnigeria.com",
//                      "roleId": 2,
//                      "permissionIds": [1, 8]
//                    }
//                    """
//            )
//            @Valid @RequestBody InviteUserRequest request) {
//
//        userInvitationService.inviteUser(request);
//
//        return ResponseEntity.status(HttpStatus.CREATED)
//                .body(ApiResponse.success("User invitation sent successfully"));
//    }
//}