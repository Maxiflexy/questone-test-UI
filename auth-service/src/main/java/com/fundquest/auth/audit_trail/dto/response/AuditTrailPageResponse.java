package com.fundquest.auth.audit_trail.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditTrailPageResponse {
    private List<AuditTrailSummaryResponse> content;
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