package com.fundquest.auth.service;

import com.fundquest.auth.dto.response.UserResponse;
import com.fundquest.auth.entity.User;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface UserService {

    User createOrUpdateUser(Map<String, Object> microsoftUserInfo);

    UserResponse convertToUserResponse(User user);

    Optional<User> findById(UUID userId);

    void updateLastLogin(UUID userId);

    Optional<User> findByEmail(String email);

    Optional<User> findByMicrosoftId(String microsoftId);

    boolean existsByEmail(String email);

    boolean existsByMicrosoftId(String microsoftId);

    void deactivateUser(UUID userId);

    void activateUser(UUID userId);

    User updateUserProfile(UUID id, Map<String, Object> updates);

    long getActiveUsersCount();
}
