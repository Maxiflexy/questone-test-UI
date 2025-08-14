package com.fundquest.auth.backoffice.modules.user.mapper;

import com.fundquest.auth.entity.User;
import com.fundquest.auth.backoffice.modules.user.dto.response.UserListResponse;
import com.fundquest.auth.backoffice.modules.user.dto.response.UserPageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class UserListMapper {

    /**
     * Convert User entity to UserListResponse DTO
     *
     * @param user User entity
     * @return UserListResponse DTO
     */
    public UserListResponse toUserListResponse(User user) {
        if (user == null) {
            log.warn("Attempted to convert null User to UserListResponse");
            return null;
        }

        return UserListResponse.builder()
                .name(user.getName())
                .email(user.getEmail())
                .roleName(user.getRole() != null ? user.getRole().getName() : null)
                .phoneNumber(user.getPhoneNumber())
                .lastModifiedBy(user.getLastModifiedBy())
                .status(user.isActive()) // is_active field mapped to status
                .createdDate(user.getCreatedAt())
                .lastModifiedDate(user.getUpdatedAt())
                .build();
    }

    /**
     * Convert list of User entities to list of UserListResponse DTOs
     *
     * @param users List of User entities
     * @return List of UserListResponse DTOs
     */
    public List<UserListResponse> toUserListResponseList(List<User> users) {
        if (users == null) {
            log.warn("Attempted to convert null users list to UserListResponse list");
            return List.of();
        }

        log.debug("Converting {} users to UserListResponse list", users.size());

        return users.stream()
                .map(this::toUserListResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convert Spring Data Page<User> to UserPageResponse DTO
     * Handles the page number conversion (0-based to 1-based)
     *
     * @param userPage Spring Data Page of Users (0-based)
     * @return UserPageResponse DTO (1-based page numbers)
     */
    public UserPageResponse toUserPageResponse(Page<User> userPage) {
        if (userPage == null) {
            log.warn("Attempted to convert null Page<User> to UserPageResponse");
            return UserPageResponse.builder()
                    .content(List.of())
                    .page(1)
                    .size(0)
                    .totalElements(0)
                    .totalPages(0)
                    .isFirstPage(true)
                    .isLastPage(true)
                    .hasNext(false)
                    .hasPrevious(false)
                    .numberOfElements(0)
                    .build();
        }

        List<UserListResponse> content = toUserListResponseList(userPage.getContent());

        // Convert 0-based page number to 1-based for frontend
        int pageNumber = userPage.getNumber() + 1;

        return UserPageResponse.builder()
                .content(content)
                .page(pageNumber) // Convert 0-based to 1-based
                .size(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .isFirstPage(userPage.isFirst())
                .isLastPage(userPage.isLast())
                .hasNext(userPage.hasNext())
                .hasPrevious(userPage.hasPrevious())
                .numberOfElements(userPage.getNumberOfElements())
                .build();
    }
}
