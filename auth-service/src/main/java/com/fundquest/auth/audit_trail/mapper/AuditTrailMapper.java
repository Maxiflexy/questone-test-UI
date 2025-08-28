package com.fundquest.auth.audit_trail.mapper;

import com.fundquest.auth.audit_trail.dto.response.AuditTrailPageResponse;
import com.fundquest.auth.audit_trail.dto.response.AuditTrailResponse;
import com.fundquest.auth.audit_trail.dto.response.AuditTrailSummaryResponse;
import com.fundquest.auth.audit_trail.entity.AuditTrail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class AuditTrailMapper {

    /**
     * Convert AuditTrail entity to AuditTrailResponse DTO
     */
    public AuditTrailResponse toAuditTrailResponse(AuditTrail auditTrail) {
        if (auditTrail == null) {
            log.warn("Attempted to convert null AuditTrail to AuditTrailResponse");
            return null;
        }

        return AuditTrailResponse.builder()
                .id(auditTrail.getId())
                .userEmail(auditTrail.getUserEmail())
                .userName(auditTrail.getUserName())
                .userRole(auditTrail.getUserRole())
                .actionType(auditTrail.getActionType())
                .actionDescription(auditTrail.getActionDescription())
                .resourceType(auditTrail.getResourceType())
                .resourceId(auditTrail.getResourceId())
                .resourceIdentifier(auditTrail.getResourceIdentifier())
                .endpoint(auditTrail.getEndpoint())
                .httpMethod(auditTrail.getHttpMethod())
                .requestParameters(auditTrail.getRequestParameters())
                .initiatedDate(auditTrail.getInitiatedDate())
                .initiatedTime(auditTrail.getInitiatedTime())
                .initiatedTimestamp(auditTrail.getInitiatedTimestamp())
                .ipAddress(auditTrail.getIpAddress())
                .userAgent(auditTrail.getUserAgent())
                .sessionId(auditTrail.getSessionId())
                .status(auditTrail.getStatus())
                .errorMessage(auditTrail.getErrorMessage())
                .serviceName(auditTrail.getServiceName())
                .createdAt(auditTrail.getCreatedAt())
                .build();
    }

    /**
     * Convert AuditTrail entity to AuditTrailSummaryResponse DTO
     */
    public AuditTrailSummaryResponse toAuditTrailSummaryResponse(AuditTrail auditTrail) {
        if (auditTrail == null) {
            log.warn("Attempted to convert null AuditTrail to AuditTrailSummaryResponse");
            return null;
        }

        return AuditTrailSummaryResponse.builder()
                .id(auditTrail.getId())
                .userEmail(auditTrail.getUserEmail())
                .userName(auditTrail.getUserName())
                .userRole(auditTrail.getUserRole())
                .actionType(auditTrail.getActionType())
                .actionDescription(auditTrail.getActionDescription())
                .resourceType(auditTrail.getResourceType())
                .resourceIdentifier(auditTrail.getResourceIdentifier())
                .initiatedDate(auditTrail.getInitiatedDate())
                .initiatedTime(auditTrail.getInitiatedTime())
                .status(auditTrail.getStatus())
                .serviceName(auditTrail.getServiceName())
                .build();
    }

    /**
     * Convert list of AuditTrail entities to list of AuditTrailSummaryResponse DTOs
     */
    public List<AuditTrailSummaryResponse> toAuditTrailSummaryResponseList(List<AuditTrail> auditTrails) {
        if (auditTrails == null) {
            log.warn("Attempted to convert null audit trails list to AuditTrailSummaryResponse list");
            return List.of();
        }

        return auditTrails.stream()
                .map(this::toAuditTrailSummaryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convert Spring Data Page<AuditTrail> to AuditTrailPageResponse DTO
     */
    public AuditTrailPageResponse toAuditTrailPageResponse(Page<AuditTrail> auditPage) {
        if (auditPage == null) {
            log.warn("Attempted to convert null Page<AuditTrail> to AuditTrailPageResponse");
            return AuditTrailPageResponse.builder()
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

        List<AuditTrailSummaryResponse> content = toAuditTrailSummaryResponseList(auditPage.getContent());

        // Convert 0-based page number to 1-based for frontend
        int pageNumber = auditPage.getNumber() + 1;

        return AuditTrailPageResponse.builder()
                .content(content)
                .page(pageNumber)
                .size(auditPage.getSize())
                .totalElements(auditPage.getTotalElements())
                .totalPages(auditPage.getTotalPages())
                .isFirstPage(auditPage.isFirst())
                .isLastPage(auditPage.isLast())
                .hasNext(auditPage.hasNext())
                .hasPrevious(auditPage.hasPrevious())
                .numberOfElements(auditPage.getNumberOfElements())
                .build();
    }
}