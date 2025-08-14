package com.fundquest.auth.backoffice.modules.user.service.management;

import com.fundquest.auth.entity.User;
import com.fundquest.auth.backoffice.modules.user.dto.response.UserPageResponse;
import com.fundquest.auth.backoffice.modules.user.mapper.UserListMapper;
import com.fundquest.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserManagementServiceImpl implements UserManagementService {

    private final UserRepository userRepository;
    private final UserListMapper userListMapper;

    private static final int MAX_PAGE_SIZE = 8;
    private static final int DEFAULT_PAGE_SIZE = 8;

    @Override
    public UserPageResponse getAllUsers(int page, int size) {
        Pageable pageable = createPageable(page, size);
        Page<User> userPage = userRepository.findAllUsersWithRole(pageable);
        return userListMapper.toUserPageResponse(userPage);
    }

    @Override
    public UserPageResponse searchUsersByName(String name, int page, int size) {
        if (name == null || name.trim().isEmpty()) {
            return getAllUsers(page, size);
        }

        Pageable pageable = createPageable(page, size);
        Page<User> userPage = userRepository.findByNameContainingIgnoreCase(name.trim(), pageable);

        return userListMapper.toUserPageResponse(userPage);
    }

    @Override
    public UserPageResponse filterUsersByStatus(boolean isActive, int page, int size) {
        Pageable pageable = createPageable(page, size);
        Page<User> userPage = userRepository.findByIsActive(isActive, pageable);

        return userListMapper.toUserPageResponse(userPage);
    }

    @Override
    public UserPageResponse filterUsers(String name, Boolean isActive, int page, int size) {
        if ((name == null || name.trim().isEmpty()) && isActive == null) {
            log.debug("No filters provided, returning all users");
            return getAllUsers(page, size);
        }

        Pageable pageable = createPageable(page, size);
        String searchName = (name != null && !name.trim().isEmpty()) ? name.trim() : null;

        Page<User> userPage = userRepository.findByNameAndStatus(searchName, isActive, pageable);

        return userListMapper.toUserPageResponse(userPage);
    }

    /**
     * Create Pageable object with proper page conversion and size validation
     * Converts 1-based page number to 0-based for Spring Data
     * Enforces maximum page size limit
     *
     * @param page 1-based page number from frontend
     * @param size requested page size
     * @return Pageable object for Spring Data (0-based page number)
     */
    private Pageable createPageable(int page, int size) {
        // Convert 1-based page to 0-based page for Spring Data
        int springPage = Math.max(0, page - 1);

        // Enforce size limits
        int validSize = validatePageSize(size);

        log.debug("Creating Pageable - Frontend page: {}, Spring page: {}, Size: {}",
                page, springPage, validSize);

        return PageRequest.of(springPage, validSize);
    }

    /**
     * Validate and adjust page size
     * Ensures page size is within acceptable limits
     *
     * @param requestedSize requested page size
     * @return validated page size
     */
    private int validatePageSize(int requestedSize) {
        if (requestedSize <= 0) {
            log.debug("Invalid page size {} provided, using default size {}", requestedSize, DEFAULT_PAGE_SIZE);
            return DEFAULT_PAGE_SIZE;
        }

        if (requestedSize > MAX_PAGE_SIZE) {
            log.debug("Requested page size {} exceeds maximum {}, using maximum", requestedSize, MAX_PAGE_SIZE);
            return MAX_PAGE_SIZE;
        }

        return requestedSize;
    }
}
