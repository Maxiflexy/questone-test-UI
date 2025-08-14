package com.fundquest.auth.backoffice.modules.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPageResponse {
    private List<UserListResponse> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean isFirstPage;
    private boolean isLastPage;
    private boolean hasNext;
    private boolean hasPrevious;
    private int numberOfElements;
}