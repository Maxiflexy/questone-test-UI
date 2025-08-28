package com.fundquest.auth.audit_trail.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fundquest.auth.audit_trail.entity.enums.ActionType;
import com.fundquest.auth.audit_trail.entity.enums.AuditStatus;
import com.fundquest.auth.audit_trail.entity.enums.ResourceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditTrailResponse {

    private Long id;

    // User Information
    private String userEmail;
    private String userName;
    private String userRole;

    // Action Information
    private ActionType actionType;
    private String actionDescription;

    // Resource Information
    private ResourceType resourceType;
    private String resourceId;
    private String resourceIdentifier;

    // Request Information
    private String endpoint;
    private String httpMethod;
    private String requestParameters;

    // Timestamp Information
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate initiatedDate;

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime initiatedTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime initiatedTimestamp;

    // Additional Context
    private String ipAddress;
    private String userAgent;
    private String sessionId;

    // Status Information
    private AuditStatus status;
    private String errorMessage;

    // Metadata
    private String serviceName;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
}