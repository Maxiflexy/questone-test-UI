package com.fundquest.auth.audit_trail.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fundquest.auth.audit_trail.dto.request.AuditSearchRequest;
import com.fundquest.auth.audit_trail.dto.response.AuditTrailPageResponse;
import com.fundquest.auth.audit_trail.dto.response.AuditTrailResponse;
import com.fundquest.auth.audit_trail.entity.AuditTrail;
import com.fundquest.auth.audit_trail.entity.enums.ActionType;
import com.fundquest.auth.audit_trail.entity.enums.ResourceType;
import com.fundquest.auth.audit_trail.mapper.AuditTrailMapper;
import com.fundquest.auth.audit_trail.repository.AuditTrailRepository;
import com.fundquest.auth.util.SecurityContextService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.fundquest.auth.audit_trail.entity.enums.ActionType.LOGIN;
import static com.fundquest.auth.audit_trail.entity.enums.ActionType.VERIFY;
import static com.fundquest.auth.audit_trail.entity.enums.AuditStatus.SUCCESS;
import static com.fundquest.auth.audit_trail.entity.enums.ResourceType.AUTHENTICATION;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuditTrailServiceImpl implements AuditTrailService {

    private final AuditTrailRepository auditTrailRepository;
    private final AuditTrailMapper auditTrailMapper;
    private final SecurityContextService securityContextService;
    private final ObjectMapper objectMapper;

    private static final int MAX_PAGE_SIZE = 8;
    private static final int DEFAULT_PAGE_SIZE = 8;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW) // Independent transaction
    public void logAudit(AuditTrail auditTrail) {
        try {
            enrichAuditTrail(auditTrail);
            auditTrailRepository.save(auditTrail);
            log.debug("Audit trail logged: {} by {}", auditTrail.getActionDescription(), auditTrail.getUserEmail());
        } catch (Exception e) {
            log.error("Failed to log audit trail: {}", e.getMessage(), e);
            // Don't throw exception to avoid breaking the main business operation
        }
    }

    @Override
    @Async("auditExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW) // Independent transaction for async
    public void logAuditAsync(AuditTrail auditTrail) {
        try {
            enrichAuditTrail(auditTrail);
            auditTrailRepository.save(auditTrail);
            log.debug("Audit trail logged asynchronously: {} by {}", auditTrail.getActionDescription(), auditTrail.getUserEmail());
        } catch (Exception e) {
            log.error("Failed to log audit trail asynchronously: {}", e.getMessage(), e);
            // Could implement retry logic or dead letter queue here
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AuditTrailPageResponse getAllAuditTrails(int page, int size) {
        Pageable pageable = createPageable(page, size);
        Page<AuditTrail> auditPage = auditTrailRepository.findAllOrderByTimestampDesc(pageable);
        return auditTrailMapper.toAuditTrailPageResponse(auditPage);
    }

    @Override
    @Transactional(readOnly = true)
    public AuditTrailPageResponse searchAuditTrails(AuditSearchRequest searchRequest, int page, int size) {
        Pageable pageable = createPageable(page, size);

        Page<AuditTrail> auditPage = auditTrailRepository.findWithFilters(
                searchRequest.getUserEmail(),
                searchRequest.getUserName(),
                searchRequest.getActionType(),
                searchRequest.getResourceType(),
                searchRequest.getStatus(),
                searchRequest.getStartDate(),
                searchRequest.getEndDate(),
                searchRequest.getSearchTerm(),
                pageable
        );

        return auditTrailMapper.toAuditTrailPageResponse(auditPage);
    }

    @Override
    @Transactional(readOnly = true)
    public AuditTrailPageResponse getUserAuditTrails(String userEmail, int page, int size) {
        Pageable pageable = createPageable(page, size);
        Page<AuditTrail> auditPage = auditTrailRepository.findByUserEmail(userEmail, pageable);
        return auditTrailMapper.toAuditTrailPageResponse(auditPage);
    }

    @Override
    @Transactional(readOnly = true)
    public AuditTrailResponse getAuditTrailById(Long id) {
        AuditTrail auditTrail = auditTrailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Audit trail not found with ID: " + id));
        return auditTrailMapper.toAuditTrailResponse(auditTrail);
    }

    @Override
    public AuditTrail.AuditTrailBuilder createAuditBuilder() {
        LocalDateTime now = LocalDateTime.now();

        return AuditTrail.builder()
                .initiatedTimestamp(now)
                .initiatedDate(now.toLocalDate())
                .initiatedTime(now.toLocalTime())
                .serviceName("auth-service")
                .status(SUCCESS);
    }

    @Override
    public AuditTrail createMethodAudit(
            ActionType actionType,
            String description,
            ResourceType resourceType,
            String resourceId,
            String resourceIdentifier) {

        return createAuditBuilder()
                .actionType(actionType)
                .actionDescription(description)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .resourceIdentifier(resourceIdentifier)
                .build();
    }

    @Override
    public void logMethodFailure(AuditTrail originalAudit, Exception exception) {
        originalAudit.markAsFailed(exception.getMessage());
        logAuditAsync(originalAudit);
    }

    private void enrichAuditTrail(AuditTrail auditTrail) {
        // Set request information first
        setRequestInformation(auditTrail);

        // Only try to set user information from security context if not already set by aspect
        // and if it's not a login operation with ANONYMOUS user
        if (shouldSetUserInfoFromContext(auditTrail)) {
            setUserInformation(auditTrail);
        }

        if (auditTrail.getInitiatedTimestamp() == null) {
            auditTrail.setTimestamps(LocalDateTime.now());
        }
    }

    /**
     * IMPROVED: Check if we should set user info from security context
     */
    private boolean shouldSetUserInfoFromContext(AuditTrail auditTrail) {
        // Don't override user info that was already set by the aspect
        if (auditTrail.getUserEmail() != null &&
                !auditTrail.getUserEmail().trim().isEmpty() &&
                !"unknown".equals(auditTrail.getUserEmail())) {
            return false;
        }

        // Don't try to get user info from context for login operations
        if (isLoginOperation(auditTrail)) {
            // Ensure login operations have proper fallback values
            if (auditTrail.getUserEmail() == null ||
                    auditTrail.getUserEmail().trim().isEmpty() ||
                    "unknown".equals(auditTrail.getUserEmail())) {
                auditTrail.setUserEmail("ANONYMOUS");
                auditTrail.setUserName("Anonymous User");
                auditTrail.setUserRole("UNAUTHENTICATED");
            }
            return false;
        }

        return true;
    }

    /**
     * Check if this is a login operation
     */
    private boolean isLoginOperation(AuditTrail auditTrail) {
        return auditTrail.getActionType() == LOGIN ||
                auditTrail.getActionType() == VERIFY ||
                auditTrail.getResourceType() == AUTHENTICATION;
    }

    private void setUserInformation(AuditTrail auditTrail) {
        try {
            if (securityContextService.isAuthenticated()) {
                String userEmail = securityContextService.getAuthenticatedUserEmailSafely();
                if (userEmail != null) {
                    auditTrail.setUserEmail(userEmail);

                    Authentication auth = securityContextService.getCurrentAuthentication();
                    if (auth != null) {
                        Optional<String> role = auth.getAuthorities().stream()
                                .map(GrantedAuthority::getAuthority)
                                .filter(authority -> authority.startsWith("ROLE_"))
                                .map(authority -> authority.substring(5))
                                .findFirst();

                        role.ifPresent(auditTrail::setUserRole);
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Could not set user information for audit trail: {}", e.getMessage());
            // Set fallback values if no user info available
            if (auditTrail.getUserEmail() == null || auditTrail.getUserEmail().trim().isEmpty()) {
                auditTrail.setUserEmail("SYSTEM");
                auditTrail.setUserRole("SYSTEM");
            }
        }
    }

    private void setRequestInformation(AuditTrail auditTrail) {
        try {
            ServletRequestAttributes requestAttributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (requestAttributes != null) {
                HttpServletRequest request = requestAttributes.getRequest();

                auditTrail.setEndpoint(request.getRequestURI());
                auditTrail.setHttpMethod(request.getMethod());

                // FIXED: Better IP address handling
                String clientIp = getClientIpAddress(request);
                auditTrail.setIpAddress(clientIp); // Uses the custom setter with validation

                auditTrail.setUserAgent(request.getHeader("User-Agent"));
                auditTrail.setSessionId(request.getSession(false) != null ?
                        request.getSession().getId() : null);
            }
        } catch (Exception e) {
            log.debug("Could not set request information for audit trail: {}", e.getMessage());
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {
                "X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP",
                "WL-Proxy-Client-IP", "HTTP_X_FORWARDED_FOR",
                "HTTP_X_FORWARDED", "HTTP_X_CLUSTER_CLIENT_IP",
                "HTTP_CLIENT_IP", "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED", "HTTP_VIA", "REMOTE_ADDR"
        };

        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                // Validate IP address format and length
                if (isValidIpAddress(ip)) {
                    return ip;
                }
            }
        }

        String remoteAddr = request.getRemoteAddr();
        return isValidIpAddress(remoteAddr) ? remoteAddr : "127.0.0.1";
    }

    private boolean isValidIpAddress(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }

        // Basic validation - check if it's a reasonable IP address format
        return ip.length() <= 45 && // Max length for IPv6
                (ip.matches("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$") || // IPv4 pattern
                        ip.contains(":") && ip.length() >= 2); // Basic IPv6 pattern
    }

    private Pageable createPageable(int page, int size) {
        int springPage = Math.max(0, page - 1);
        int validSize = validatePageSize(size);
        return PageRequest.of(springPage, validSize);
    }

    private int validatePageSize(int requestedSize) {
        if (requestedSize <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(requestedSize, MAX_PAGE_SIZE);
    }
}