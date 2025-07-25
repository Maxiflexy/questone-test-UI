package com.fundquest.auth.service.impl;

import com.fundquest.auth.entity.User;
import com.fundquest.auth.exception.UserNotFoundException;
import com.fundquest.auth.repository.UserRepository;
import com.fundquest.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmailAndIsActiveTrue(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public User findByMicrosoftId(String microsoftId) {
        return userRepository.findByMicrosoftId(microsoftId)
                .orElseThrow(() -> new UserNotFoundException("User not found with Microsoft ID: " + microsoftId));
    }

    @Override
    public User createOrUpdateUser(String microsoftId, String email, String name, String preferredUsername) {
        Optional<User> existingUser = userRepository.findByMicrosoftId(microsoftId);

        if (existingUser.isPresent()) {
            // User exists, just update lastLogin without full persistence
            User user = existingUser.get();
            userRepository.updateLastLoginByEmail(user.getEmail(), LocalDateTime.now());
            log.info("User found, lastLogin updated for: {}", user.getEmail());
            return user;
        }

        User newUser = User.builder()
                .microsoftId(microsoftId)
                .email(email)
                .name(name != null && !name.trim().isEmpty() ? name : "Unknown User")
                .preferredUsername(preferredUsername)
                .isActive(true)
                .build();

        newUser.updateLastLogin();

        User savedUser = userRepository.save(newUser);
        log.info("New user created successfully for: {}", email);

        return savedUser;
    }

    @Override
    public void updateLastLogin(String email) {
        log.debug("Updated last login for user with email: {}", email);
        userRepository.updateLastLoginByEmail(email, LocalDateTime.now());
    }
}