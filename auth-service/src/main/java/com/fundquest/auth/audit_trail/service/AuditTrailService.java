package com.fundquest.auth.audit_trail.service;


import com.fundquest.auth.audit_trail.dto.request.AuditSearchRequest;
import com.fundquest.auth.audit_trail.dto.response.AuditTrailPageResponse;
import com.fundquest.auth.audit_trail.dto.response.AuditTrailResponse;
import com.fundquest.auth.audit_trail.entity.AuditTrail;
import com.fundquest.auth.audit_trail.entity.enums.ActionType;
import com.fundquest.auth.audit_trail.entity.enums.ResourceType;

public interface AuditTrailService {

    /**
     * Log an audit trail entry
     * @param auditTrail the audit trail to log
     */
    void logAudit(AuditTrail auditTrail);

    /**
     * Log an audit trail entry asynchronously
     * @param auditTrail the audit trail to log
     */
    void logAuditAsync(AuditTrail auditTrail);

    /**
     * Get all audit trails with pagination
     * @param page 1-based page number
     * @param size page size (max 8)
     * @return paginated audit trails
     */
    AuditTrailPageResponse getAllAuditTrails(int page, int size);

    /**
     * Search audit trails with filters
     * @param searchRequest search criteria
     * @param page 1-based page number
     * @param size page size (max 8)
     * @return paginated filtered audit trails
     */
    AuditTrailPageResponse searchAuditTrails(AuditSearchRequest searchRequest, int page, int size);

    /**
     * Get audit trails for a specific user
     * @param userEmail user email
     * @param page 1-based page number
     * @param size page size (max 8)
     * @return paginated user audit trails
     */
    AuditTrailPageResponse getUserAuditTrails(String userEmail, int page, int size);

    /**
     * Get audit trail by ID
     * @param id audit trail ID
     * @return audit trail details
     */
    AuditTrailResponse getAuditTrailById(Long id);

    /**
     * Create audit trail builder with common defaults
     * @return AuditTrail builder with defaults set
     */
    AuditTrail.AuditTrailBuilder createAuditBuilder();

    /**
     * Create audit trail for method execution
     * @param actionType type of action
     * @param description action description
     * @param resourceType resource type
     * @param resourceId resource ID
     * @param resourceIdentifier resource identifier
     * @return audit trail ready for logging
     */
    AuditTrail createMethodAudit(
            ActionType actionType,
            String description,
            ResourceType resourceType,
            String resourceId,
            String resourceIdentifier
    );

    /**
     * Log method failure
     * @param originalAudit the original audit entry
     * @param exception the exception that occurred
     */
    void logMethodFailure(AuditTrail originalAudit, Exception exception);
}
