package com.fundquest.auth.audit_trail.service;

import com.fundquest.auth.audit_trail.entity.AuditTrail;
import com.fundquest.auth.audit_trail.repository.AuditTrailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Async audit logger to handle audit trail persistence without blocking main operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncAuditLogger {

    private final AuditTrailRepository auditTrailRepository;

    /**
     * Asynchronously log audit trail with independent transaction
     * Uses REQUIRES_NEW to ensure audit logging doesn't affect main transaction
     */
    @Async("auditExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAsync(AuditTrail auditTrail) {
        try {
            auditTrailRepository.save(auditTrail);
            log.debug("Audit trail logged asynchronously: {} by {}",
                    auditTrail.getActionDescription(), auditTrail.getUserEmail());
        } catch (Exception e) {
            log.error("Failed to log audit trail asynchronously: {}", e.getMessage(), e);
            // Could implement retry logic or dead letter queue here
        }
    }

    /**
     * Batch log multiple audit trails
     */
    @Async("auditExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logBatchAsync(Iterable<AuditTrail> auditTrails) {
        try {
            auditTrailRepository.saveAll(auditTrails);
            log.debug("Batch audit trails logged asynchronously");
        } catch (Exception e) {
            log.error("Failed to log batch audit trails asynchronously: {}", e.getMessage(), e);
        }
    }
}
