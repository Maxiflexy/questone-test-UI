package com.fundquest.auth.backoffice.modules.user.service.invite;

import com.fundquest.auth.audit_trail.annotation.Auditable;
import com.fundquest.auth.entity.Permission;
import com.fundquest.auth.entity.Role;
import com.fundquest.auth.entity.User;
import com.fundquest.auth.backoffice.modules.user.dto.request.InviteUserRequest;
import com.fundquest.auth.backoffice.modules.user.service.email.EmailIntegrationService;
import com.fundquest.auth.repository.UserRepository;
import com.fundquest.auth.service.UserService;
import com.fundquest.auth.service.permission.PermissionService;
import com.fundquest.auth.service.role.RoleService;
import com.fundquest.auth.util.SecurityContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static com.fundquest.auth.audit_trail.entity.enums.ActionType.INVITE;
import static com.fundquest.auth.audit_trail.entity.enums.ResourceType.USER;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserInvitationServiceImpl implements UserInvitationService {

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PermissionService permissionService;
    private final UserService userService;
    private final SecurityContextService securityContextService;
    private final EmailIntegrationService emailIntegrationService;

    @Override
    @Transactional
    @Auditable(
            actionType = INVITE,
            description = "User with email {0} was invited to the system with role {1} and {2} permissions",
            resourceType = USER,
            resourceIdExpression = "#result != null ? #result.id : 'unknown'",
            resourceIdentifierExpression = "#request.email",
            includeParameters = true
    )
    public void inviteUser(InviteUserRequest request) {
        String invitedByEmail = securityContextService.getAuthenticatedUserEmail();
        inviteUser(request, invitedByEmail);
    }

    @Transactional
    @Auditable(
            actionType = INVITE,
            description = "User {0} was invited by {1} with role and permissions",
            resourceType = USER,
            resourceIdExpression = "#result != null ? #result.id : 'unknown'",
            resourceIdentifierExpression = "#request.email"
    )
    public void inviteUser(InviteUserRequest request, String invitedByEmail) {
        if (userService.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("User with email '" + request.getEmail() + "' already exists");
        }

        ValidationResult validationResult = validateAndGetEntities(request);

        User user = createInvitedUser(request, validationResult.role(), validationResult.permissions(), invitedByEmail);

        userRepository.save(user);

        try {
            emailIntegrationService.sendWelcomeEmail(request.getEmail());
            log.info("Successfully completed user invitation process for: {} by: {}",
                    request.getEmail(), invitedByEmail);
        } catch (Exception e) {
            throw e; // Re-throw to trigger rollback
        }
    }

    /**
     * Validates request and returns the actual entities to avoid duplicate lookups
     * @param request the invitation request
     * @return ValidationResult containing role and permissions
     * @throws IllegalArgumentException if validation fails
     */
    private ValidationResult validateAndGetEntities(InviteUserRequest request) {
        log.debug("Validating invitation request for email: {}", request.getEmail());

        // Check for duplicate permission IDs first (no DB call needed)
        if (request.getPermissionIds().size() != request.getPermissionIds().stream().distinct().count()) {
            throw new IllegalArgumentException("Duplicate permission IDs are not allowed");
        }

        // Find role by ID - single DB call
        Role role;
        try {
            role = roleService.findEntityById(request.getRoleId());
            log.debug("Found role: {} for ID: {}", role.getName(), request.getRoleId());
        } catch (RuntimeException e) {
            log.debug("Role not found with ID: {}", request.getRoleId());
            throw new IllegalArgumentException("Invalid role ID: " + request.getRoleId());
        }

        // Find permissions by IDs - single DB call
        Set<Permission> permissions = permissionService.findByIds(request.getPermissionIds());

        if (permissions.size() != request.getPermissionIds().size()) {
            log.debug("Expected {} permissions but found {}", request.getPermissionIds().size(), permissions.size());
            throw new IllegalArgumentException("One or more permission IDs are invalid");
        }

        // Additional validation: ensure all permissions are active
        boolean allActive = permissions.stream().allMatch(Permission::isActive);
        if (!allActive) {
            throw new IllegalArgumentException("One or more permissions are inactive");
        }

        log.debug("Invitation request validation passed for email: {} with role: {} and {} permissions",
                request.getEmail(), role.getName(), permissions.size());

        return new ValidationResult(role, permissions);
    }

    private User createInvitedUser(InviteUserRequest request, Role role, Set<Permission> permissions, String invitedByEmail) {
        String userId = UUID.randomUUID().toString();

        return User.builder()
                .id(userId)
                .email(request.getEmail())
                .role(role)
                .permissions(permissions)
                .isInvited(true)
                .isMicrosoftVerified(false)
                .invitedBy(invitedByEmail)
                .invitedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Record to hold validation results and avoid duplicate database calls
     */
    private record ValidationResult(Role role, Set<Permission> permissions) {}
}