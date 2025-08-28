package com.fundquest.auth.audit_trail.annotation;

import com.fundquest.auth.audit_trail.entity.enums.ActionType;
import com.fundquest.auth.audit_trail.entity.enums.ResourceType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods that should be audited.
 * When applied to a method, the AuditAspect will automatically log
 * the method execution details to the audit trail.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {

    /**
     * The type of action being performed
     */
    ActionType actionType();

    /**
     * Description template for the action. Can use placeholders like {0}, {1} etc.
     * for method parameters. Use {result} for method return value.
     * Example: "User {0} was activated by admin"
     */
    String description();

    /**
     * The type of resource being affected
     */
    ResourceType resourceType() default ResourceType.USER;

    /**
     * Expression to extract resource ID from method parameters.
     * Use SpEL expressions like "#p0" for first parameter, "#email" for named parameter, etc.
     * Example: "#p0" (first parameter), "#request.email" (email field from request object)
     */
    String resourceIdExpression() default "";

    /**
     * Expression to extract resource identifier (human-readable) from method parameters.
     * Example: "#email", "#request.email", "#result.email"
     */
    String resourceIdentifierExpression() default "";

    /**
     * Whether to log even if the method throws an exception
     */
    boolean logOnFailure() default true;

    /**
     * Whether to include method parameters in the audit log
     */
    boolean includeParameters() default false;

    /**
     * Whether to include the return value in the audit log
     */
    boolean includeResult() default false;
}