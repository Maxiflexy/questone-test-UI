package com.fundquest.auth.util;

import com.fundquest.auth.dto.response.AuthUserData;
import com.fundquest.auth.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserMapper {

    /**
     * Converts User entity to AuthUserData DTO for authentication responses
     *
     * @param user User entity
     * @return AuthUserData DTO with user information
     */
    public AuthUserData toAuthUserData(User user) {
        if (user == null) {
            log.warn("Attempted to convert null User to AuthUserData");
            return null;
        }

        log.debug("Converting User entity to AuthUserData for user: {}", user.getEmail());

        return AuthUserData.builder()
                .email(user.getEmail())
                .name(user.getName())
                .microsoftId(user.getMicrosoftId())
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .build();
    }

    /**
     * Converts User entity to UserProfileResponse DTO for profile endpoints
     *
     * @param user User entity
     * @return UserProfileResponse DTO with user profile information
     */
    public AuthUserData toUserProfileResponse(User user) {
        if (user == null) {
            log.warn("Attempted to convert null User to UserProfileResponse");
            return null;
        }

        log.debug("Converting User entity to UserProfileResponse for user: {}", user.getEmail());

        return AuthUserData.builder()
                .email(user.getEmail())
                .name(user.getName())
                .microsoftId(user.getMicrosoftId())
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .build();
    }

    /**
     * Creates a minimal AuthUserData with just essential information
     * Useful when full user data is not needed
     *
     * @param email User email
     * @param name User name
     * @return Minimal AuthUserData DTO
     */
    public AuthUserData createMinimalAuthUserData(String email, String name) {
        log.debug("Creating minimal AuthUserData for user: {}", email);

        return AuthUserData.builder()
                .email(email)
                .name(name)
                .build();
    }
}