package com.fundquest.auth.audit_trail.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fundquest.auth.audit_trail.annotation.Auditable;
import com.fundquest.auth.audit_trail.entity.AuditTrail;
import com.fundquest.auth.audit_trail.entity.enums.ActionType;
import com.fundquest.auth.audit_trail.entity.enums.ResourceType;
import com.fundquest.auth.audit_trail.service.AuditTrailService;
import com.fundquest.auth.dto.response.AuthResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

/**
 * AOP Aspect for automatic audit logging
 * Intercepts methods annotated with @Auditable and logs their execution
 * IMPROVED: Handles login scenarios where no user is authenticated yet
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
@Order(1) // Execute before other aspects
public class AuditAspect {

    private final AuditTrailService auditTrailService;
    private final ObjectMapper objectMapper;
    private final ExpressionParser expressionParser = new SpelExpressionParser();

    // Thread-local storage for audit context during method execution
    private final ThreadLocal<AuditContext> auditContext = new ThreadLocal<>();

    /**
     * Before advice - prepare audit context before method execution
     */
    @Before("@annotation(auditable)")
    public void beforeAuditableMethod(JoinPoint joinPoint, Auditable auditable) {
        try {
            log.debug("Preparing audit for method: {}", joinPoint.getSignature().getName());

            AuditTrail auditTrail = createAuditTrail(joinPoint, auditable, null);
            AuditContext context = new AuditContext(auditTrail, joinPoint, auditable);
            auditContext.set(context);

        } catch (Exception e) {
            log.error("Error preparing audit context for method {}: {}",
                    joinPoint.getSignature().getName(), e.getMessage(), e);
        }
    }

    /**
     * After returning advice - log successful method execution
     * IMPROVED: Updates audit with result information for login scenarios
     */
    @AfterReturning(pointcut = "@annotation(auditable)", returning = "result")
    public void afterAuditableMethodSuccess(JoinPoint joinPoint, Auditable auditable, Object result) {
        try {
            AuditContext context = auditContext.get();
            if (context != null) {
                // Update audit trail with result information if needed
                updateAuditWithResult(context.auditTrail, joinPoint, auditable, result);

                // SPECIAL HANDLING for login operations: extract user email from result
                if (isLoginOperation(auditable) && result instanceof AuthResponse) {
                    updateAuditWithLoginInfo(context.auditTrail, (AuthResponse) result);
                }

                // Ensure we have a user email (set default if still null)
                ensureUserEmail(context.auditTrail, auditable);

                // Log the successful audit
                auditTrailService.logAuditAsync(context.auditTrail);

                log.debug("Audit logged successfully for method: {}", joinPoint.getSignature().getName());
            }
        } catch (Exception e) {
            log.error("Error logging successful audit for method {}: {}",
                    joinPoint.getSignature().getName(), e.getMessage(), e);
        } finally {
            auditContext.remove();
        }
    }

    /**
     * After throwing advice - log failed method execution
     */
    @AfterThrowing(pointcut = "@annotation(auditable)", throwing = "exception")
    public void afterAuditableMethodFailure(JoinPoint joinPoint, Auditable auditable, Exception exception) {
        try {
            AuditContext context = auditContext.get();
            if (context != null && auditable.logOnFailure()) {
                // Mark audit as failed and include error information
                context.auditTrail.markAsFailed(exception.getMessage());

                // Update description to indicate failure
                String originalDescription = context.auditTrail.getActionDescription();
                context.auditTrail.setActionDescription("FAILED: " + originalDescription);

                // Ensure we have a user email for failure cases too
                ensureUserEmail(context.auditTrail, auditable);

                // Log the failed audit
                auditTrailService.logAuditAsync(context.auditTrail);

                log.debug("Failure audit logged for method: {} - Error: {}",
                        joinPoint.getSignature().getName(), exception.getMessage());
            }
        } catch (Exception e) {
            log.error("Error logging failure audit for method {}: {}",
                    joinPoint.getSignature().getName(), e.getMessage(), e);
        } finally {
            auditContext.remove();
        }
    }

    /**
     * Create initial audit trail from method signature and annotation
     */
    private AuditTrail createAuditTrail(JoinPoint joinPoint, Auditable auditable, Object result) {
        StandardEvaluationContext context = createEvaluationContext(joinPoint, result);

        // Extract resource information using SpEL expressions
        String resourceId = extractValue(auditable.resourceIdExpression(), context);
        String resourceIdentifier = extractValue(auditable.resourceIdentifierExpression(), context);

        // Format description with method parameters
        String description = formatDescription(auditable.description(), joinPoint, result);

        // Create audit trail
        AuditTrail auditTrail = auditTrailService.createMethodAudit(
                auditable.actionType(),
                description,
                auditable.resourceType(),
                resourceId,
                resourceIdentifier
        );

        // Add request parameters if requested
        if (auditable.includeParameters()) {
            auditTrail.setRequestParameters(serializeParameters(joinPoint));
        }

        return auditTrail;
    }

    /**
     * Update audit trail with result information
     */
    private void updateAuditWithResult(AuditTrail auditTrail, JoinPoint joinPoint, Auditable auditable, Object result) {
        if (auditable.includeResult() && result != null) {
            // Re-evaluate expressions that might use result
            StandardEvaluationContext context = createEvaluationContext(joinPoint, result);

            // Re-extract resource information with result context
            String resourceId = extractValue(auditable.resourceIdExpression(), context);
            String resourceIdentifier = extractValue(auditable.resourceIdentifierExpression(), context);

            if (resourceId != null && !resourceId.equals(auditTrail.getResourceId())) {
                auditTrail.setResourceId(resourceId);
            }

            if (resourceIdentifier != null && !resourceIdentifier.equals(auditTrail.getResourceIdentifier())) {
                auditTrail.setResourceIdentifier(resourceIdentifier);
            }

            // Re-format description with result
            String updatedDescription = formatDescription(auditable.description(), joinPoint, result);
            auditTrail.setActionDescription(updatedDescription);
        }
    }

    /**
     * IMPROVED: Special handling for login operations
     * Extracts user information from AuthResponse result
     */
    private void updateAuditWithLoginInfo(AuditTrail auditTrail, AuthResponse authResponse) {
        if (authResponse != null && authResponse.getUser() != null) {
            String userEmail = authResponse.getUser().getEmail();
            String userName = authResponse.getUser().getName();

            log.debug("Updating audit with login info - Email: {}, Name: {}", userEmail, userName);

            // Set user information from the auth response
            if (userEmail != null) {
                auditTrail.setUserEmail(userEmail);
            }
            if (userName != null) {
                auditTrail.setUserName(userName);
            }

            // Update description with actual user email
            String currentDescription = auditTrail.getActionDescription();
            if (currentDescription != null && userEmail != null) {
                // Replace placeholder with actual email
                String updatedDescription = currentDescription.replace("{0}", userEmail)
                        .replace("VerifyMicrosoftTokenRequest", "user " + userEmail);
                auditTrail.setActionDescription(updatedDescription);
            }

            // Update resource identifier
            if (userEmail != null && "unknown".equals(auditTrail.getResourceIdentifier())) {
                auditTrail.setResourceIdentifier(userEmail);
            }
        }
    }

    /**
     * IMPROVED: Ensures audit trail has a user email, setting defaults for special cases
     */
    private void ensureUserEmail(AuditTrail auditTrail, Auditable auditable) {
        if (auditTrail.getUserEmail() == null || auditTrail.getUserEmail().trim().isEmpty()) {
            if (isLoginOperation(auditable)) {
                // For login operations, if we still don't have a user email, set as ANONYMOUS
                auditTrail.setUserEmail("ANONYMOUS");
                auditTrail.setUserName("Anonymous User");
                auditTrail.setUserRole("UNAUTHENTICATED");
                log.debug("Set audit user to ANONYMOUS for login operation");
            } else {
                // For other operations, this shouldn't happen, but set fallback
                auditTrail.setUserEmail("SYSTEM");
                auditTrail.setUserName("System");
                auditTrail.setUserRole("SYSTEM");
                log.warn("Set audit user to SYSTEM as fallback for operation: {}", auditable.actionType());
            }
        }
    }

    /**
     * Check if this is a login-related operation
     */
    private boolean isLoginOperation(Auditable auditable) {
        ActionType actionType = auditable.actionType();
        return actionType == ActionType.LOGIN ||
                actionType == ActionType.VERIFY ||
                auditable.resourceType() == ResourceType.AUTHENTICATION;
    }

    /**
     * Create SpEL evaluation context with method parameters and result
     */
    private StandardEvaluationContext createEvaluationContext(JoinPoint joinPoint, Object result) {
        StandardEvaluationContext context = new StandardEvaluationContext();

        // Add method parameters
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Parameter[] parameters = signature.getMethod().getParameters();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameters.length && i < args.length; i++) {
            // Add by parameter name
            context.setVariable(parameters[i].getName(), args[i]);
            // Add by position (p0, p1, etc.)
            context.setVariable("p" + i, args[i]);
        }

        // Add result if available
        if (result != null) {
            context.setVariable("result", result);
        }

        return context;
    }

    /**
     * Extract value using SpEL expression
     */
    private String extractValue(String expression, StandardEvaluationContext context) {
        if (expression == null || expression.trim().isEmpty()) {
            return null;
        }

        try {
            Expression expr = expressionParser.parseExpression(expression);
            Object value = expr.getValue(context);
            return value != null ? value.toString() : null;
        } catch (Exception e) {
            log.debug("Failed to evaluate expression '{}': {}", expression, e.getMessage());
            return null;
        }
    }

    /**
     * Format description with method parameters and result
     * IMPROVED: Better handling of complex objects in descriptions
     */
    private String formatDescription(String template, JoinPoint joinPoint, Object result) {
        try {
            Object[] args = joinPoint.getArgs();

            // Simple parameter substitution
            String formatted = template;
            for (int i = 0; i < args.length; i++) {
                String placeholder = "{" + i + "}";
                if (formatted.contains(placeholder)) {
                    String value = formatParameterValue(args[i]);
                    formatted = formatted.replace(placeholder, value);
                }
            }

            // Result substitution
            if (result != null && formatted.contains("{result}")) {
                String resultValue = formatParameterValue(result);
                formatted = formatted.replace("{result}", resultValue);
            }

            return formatted;
        } catch (Exception e) {
            log.debug("Failed to format description '{}': {}", template, e.getMessage());
            return template; // Return original template if formatting fails
        }
    }

    /**
     * IMPROVED: Better formatting of parameter values for descriptions
     */
    private String formatParameterValue(Object value) {
        if (value == null) {
            return "null";
        }

        // Handle AuthResponse specially to get user email
        if (value instanceof AuthResponse) {
            AuthResponse authResponse = (AuthResponse) value;
            if (authResponse.getUser() != null && authResponse.getUser().getEmail() != null) {
                return authResponse.getUser().getEmail();
            }
            return "AuthResponse";
        }

        // Handle other common types
        String stringValue = value.toString();

        // Limit length to avoid overly long descriptions
        if (stringValue.length() > 100) {
            return stringValue.substring(0, 100) + "...";
        }

        return stringValue;
    }

    /**
     * Serialize method parameters to JSON
     */
    private String serializeParameters(JoinPoint joinPoint) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Parameter[] parameters = signature.getMethod().getParameters();
            Object[] args = joinPoint.getArgs();

            Map<String, Object> paramMap = new HashMap<>();
            for (int i = 0; i < parameters.length && i < args.length; i++) {
                // Avoid serializing sensitive data
                String paramName = parameters[i].getName();
                if (isSensitiveParameter(paramName)) {
                    paramMap.put(paramName, "[REDACTED]");
                } else {
                    paramMap.put(paramName, args[i]);
                }
            }

            return objectMapper.writeValueAsString(paramMap);
        } catch (JsonProcessingException e) {
            log.debug("Failed to serialize parameters: {}", e.getMessage());
            return "Failed to serialize parameters";
        }
    }

    /**
     * Check if parameter contains sensitive data that should be redacted
     */
    private boolean isSensitiveParameter(String paramName) {
        if (paramName == null) return false;

        String lowerName = paramName.toLowerCase();
        return lowerName.contains("password") ||
                lowerName.contains("token") ||
                lowerName.contains("secret") ||
                lowerName.contains("authcode");
    }

    /**
     * Inner class to hold audit context during method execution
     */
    private static class AuditContext {
        final AuditTrail auditTrail;
        final JoinPoint joinPoint;
        final Auditable auditable;

        AuditContext(AuditTrail auditTrail, JoinPoint joinPoint, Auditable auditable) {
            this.auditTrail = auditTrail;
            this.joinPoint = joinPoint;
            this.auditable = auditable;
        }
    }
}