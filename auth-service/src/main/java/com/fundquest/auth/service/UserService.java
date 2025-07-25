package com.fundquest.auth.service;

import com.fundquest.auth.entity.User;

import java.util.Optional;

public interface UserService {
    User findByEmail(String email);
    User findByMicrosoftId(String microsoftId);
    User createOrUpdateUser(String microsoftId, String email, String name, String preferredUsername);
    void updateLastLogin(String email);

}