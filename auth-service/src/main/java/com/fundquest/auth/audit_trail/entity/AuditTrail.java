package com.fundquest.auth.audit_trail.entity;

import com.fundquest.auth.audit_trail.entity.enums.ActionType;
import com.fundquest.auth.audit_trail.entity.enums.AuditStatus;
import com.fundquest.auth.audit_trail.entity.enums.ResourceType;
import jakarta.persistence.*;
import lombok.*;

import java.net.InetAddress;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static com.fundquest.auth.audit_trail.entity.enums.AuditStatus.SUCCESS;

@Entity
@Table(name = "audit_trail_tbl")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditTrail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_email", nullable = false)
    private String userEmail;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "user_role", length = 100)
    private String userRole;

    @Column(name = "action_type", nullable = false, length = 100)
    @Enumerated(EnumType.STRING)
    private ActionType actionType;

    @Column(name = "action_description", nullable = false, columnDefinition = "TEXT")
    private String actionDescription;

    @Column(name = "resource_type", length = 100)
    @Enumerated(EnumType.STRING)
    private ResourceType resourceType;

    @Column(name = "resource_id")
    private String resourceId;

    @Column(name = "resource_identifier")
    private String resourceIdentifier;

    @Column(name = "endpoint", length = 500)
    private String endpoint;

    @Column(name = "http_method", length = 10)
    private String httpMethod;

    @Column(name = "request_parameters", columnDefinition = "TEXT")
    private String requestParameters;

    @Column(name = "initiated_date", nullable = false)
    private LocalDate initiatedDate;

    @Column(name = "initiated_time", nullable = false)
    private LocalTime initiatedTime;

    @Column(name = "initiated_timestamp", nullable = false)
    private LocalDateTime initiatedTimestamp;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AuditStatus status = SUCCESS;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    // Metadata
    @Column(name = "service_name", length = 100)
    @Builder.Default
    private String serviceName = "auth-service";

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    // Helper methods for setting timestamps
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (initiatedTimestamp == null) {
            initiatedTimestamp = now;
        }
        if (initiatedDate == null) {
            initiatedDate = now.toLocalDate();
        }
        if (initiatedTime == null) {
            initiatedTime = now.toLocalTime();
        }
        if (createdAt == null) {
            createdAt = now;
        }
    }

    // Convenience methods for setting common values
    public void setTimestamps(LocalDateTime timestamp) {
        this.initiatedTimestamp = timestamp;
        this.initiatedDate = timestamp.toLocalDate();
        this.initiatedTime = timestamp.toLocalTime();
    }

    public void markAsFailed(String errorMessage) {
        this.status = AuditStatus.FAILED;
        this.errorMessage = errorMessage;
    }

    public void markAsSuccess() {
        this.status = SUCCESS;
        this.errorMessage = null;
    }
}