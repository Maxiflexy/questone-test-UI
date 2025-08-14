package com.fundquest.auth.backoffice.modules.user.service.invite;

import com.fundquest.auth.backoffice.modules.user.dto.request.InviteUserRequest;

public interface UserInvitationService {

    /**
     * Invite a new user with specified role and permissions
     * Uses security context to determine who is sending the invitation
     * @param request invitation request containing email, role ID, and permission IDs
     * @throws IllegalArgumentException if user already exists or invalid role/permissions
     * @throws RuntimeException if role or permissions not found
     */
    void inviteUser(InviteUserRequest request);

}