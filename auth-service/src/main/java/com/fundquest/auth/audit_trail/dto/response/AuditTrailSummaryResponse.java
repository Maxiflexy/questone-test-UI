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
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditTrailSummaryResponse {

    private Long id;
    private String userEmail;
    private String userName;
    private String userRole;
    private ActionType actionType;
    private String actionDescription;
    private ResourceType resourceType;
    private String resourceIdentifier;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate initiatedDate;

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime initiatedTime;

    private AuditStatus status;
    private String serviceName;
}
