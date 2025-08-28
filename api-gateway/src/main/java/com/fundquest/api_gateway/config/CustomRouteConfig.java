package com.fundquest.api_gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomRouteConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()

                // Auth Service Swagger UI Route
                .route("auth-swagger-ui", r -> r.path("/auth/swagger-ui/**")
                        .filters(f -> f
                                .stripPrefix(1) // Remove /auth prefix
                                .addRequestHeader("X-Gateway-Source", "api-gateway")
                                .addRequestHeader("X-Service-Name", "auth-service")
                        )
                        .uri("lb://auth-service"))

                // Auth Service OpenAPI Docs Route
                .route("auth-api-docs", r -> r.path("/auth/v3/api-docs/**")
                        .filters(f -> f
                                .stripPrefix(1) // Remove /auth prefix
                                .addRequestHeader("X-Gateway-Source", "api-gateway")
                                .addRequestHeader("X-Service-Name", "auth-service")
                        )
                        .uri("lb://auth-service"))

                // Auth Service Swagger Resources Route (for swagger-ui dependencies)
                .route("auth-swagger-resources", r -> r.path("/auth/swagger-resources/**")
                        .filters(f -> f
                                .stripPrefix(1) // Remove /auth prefix
                                .addRequestHeader("X-Gateway-Source", "api-gateway")
                                .addRequestHeader("X-Service-Name", "auth-service")
                        )
                        .uri("lb://auth-service"))

                // Auth Service WebJars Route (for swagger-ui CSS/JS files)
                .route("auth-webjars", r -> r.path("/auth/webjars/**")
                        .filters(f -> f
                                .stripPrefix(1) // Remove /auth prefix
                                .addRequestHeader("X-Gateway-Source", "api-gateway")
                                .addRequestHeader("X-Service-Name", "auth-service")
                        )
                        .uri("lb://auth-service"))

                // =================================================================
                // AUDIT TRAIL ROUTES - Must be defined BEFORE the general auth route
                // =================================================================

                // Audit Trail - Search audit trails with filters (most specific first)
                .route("audit-trails-search", r -> r.path("/api/v1/auth/audit/search")
                        .and().method("GET")
                        .filters(f -> f
                                .addRequestHeader("X-Gateway-Source", "api-gateway")
                                .addRequestHeader("X-Service-Name", "auth-service")
                                .addRequestHeader("X-Audit-Context", "audit-trail-search")
                        )
                        .uri("lb://auth-service"))

                // Audit Trail - Get user specific audit trails
                .route("audit-trails-user", r -> r.path("/api/v1/auth/audit/user/**")
                        .and().method("GET")
                        .filters(f -> f
                                .addRequestHeader("X-Gateway-Source", "api-gateway")
                                .addRequestHeader("X-Service-Name", "auth-service")
                                .addRequestHeader("X-Audit-Context", "audit-trail-user")
                        )
                        .uri("lb://auth-service"))

                // Audit Trail - Get action types
                .route("audit-trails-actions", r -> r.path("/api/v1/auth/audit/actions")
                        .and().method("GET")
                        .filters(f -> f
                                .addRequestHeader("X-Gateway-Source", "api-gateway")
                                .addRequestHeader("X-Service-Name", "auth-service")
                                .addRequestHeader("X-Audit-Context", "audit-trail-actions")
                        )
                        .uri("lb://auth-service"))

                // Audit Trail - Get resource types
                .route("audit-trails-resources", r -> r.path("/api/v1/auth/audit/resources")
                        .and().method("GET")
                        .filters(f -> f
                                .addRequestHeader("X-Gateway-Source", "api-gateway")
                                .addRequestHeader("X-Service-Name", "auth-service")
                                .addRequestHeader("X-Audit-Context", "audit-trail-resources")
                        )
                        .uri("lb://auth-service"))

                // Audit Trail - Get audit statuses
                .route("audit-trails-statuses", r -> r.path("/api/v1/auth/audit/statuses")
                        .and().method("GET")
                        .filters(f -> f
                                .addRequestHeader("X-Gateway-Source", "api-gateway")
                                .addRequestHeader("X-Service-Name", "auth-service")
                                .addRequestHeader("X-Audit-Context", "audit-trail-statuses")
                        )
                        .uri("lb://auth-service"))

                // Audit Trail - Get audit trail by ID (must be after other specific routes)
                .route("audit-trails-detail", r -> r.path("/api/v1/auth/audit/{auditId}")
                        .and().method("GET")
                        .filters(f -> f
                                .addRequestHeader("X-Gateway-Source", "api-gateway")
                                .addRequestHeader("X-Service-Name", "auth-service")
                                .addRequestHeader("X-Audit-Context", "audit-trail-detail")
                        )
                        .uri("lb://auth-service"))

                // Audit Trail - Get all audit trails with pagination (catch remaining audit GET requests)
                .route("audit-trails-list", r -> r.path("/api/v1/auth/audit")
                        .and().method("GET")
                        .filters(f -> f
                                .addRequestHeader("X-Gateway-Source", "api-gateway")
                                .addRequestHeader("X-Service-Name", "auth-service")
                                .addRequestHeader("X-Audit-Context", "audit-trail-list")
                        )
                        .uri("lb://auth-service"))

                // =================================================================
                // USER MANAGEMENT ROUTES - Specific routes for user operations
                // =================================================================

                // User Management - Invite User (POST operation - needs audit context)
                .route("user-invite", r -> r.path("/api/v1/auth/user/invite")
                        .and().method("POST")
                        .filters(f -> f
                                .addRequestHeader("X-Gateway-Source", "api-gateway")
                                .addRequestHeader("X-Service-Name", "auth-service")
                                .addRequestHeader("X-Audit-Context", "user-invite")
                        )
                        .uri("lb://auth-service"))

                // User Management - Update user permissions (PUT operation - needs audit context)
                .route("user-permissions", r -> r.path("/api/v1/auth/user/permissions")
                        .and().method("PUT")
                        .filters(f -> f
                                .addRequestHeader("X-Gateway-Source", "api-gateway")
                                .addRequestHeader("X-Service-Name", "auth-service")
                                .addRequestHeader("X-Audit-Context", "user-permissions-update")
                        )
                        .uri("lb://auth-service"))

                // User Management - Update user status (PUT operation - needs audit context)
                .route("user-status", r -> r.path("/api/v1/auth/user/status")
                        .and().method("PUT")
                        .filters(f -> f
                                .addRequestHeader("X-Gateway-Source", "api-gateway")
                                .addRequestHeader("X-Service-Name", "auth-service")
                                .addRequestHeader("X-Audit-Context", "user-status-update")
                        )
                        .uri("lb://auth-service"))

                // =================================================================
                // AUTHENTICATION ROUTES - Specific auth operations that need tracking
                // =================================================================

                // Auth Service - Microsoft verification (POST operation - needs audit context)
                .route("auth-microsoft-verify", r -> r.path("/api/v1/auth/microsoft/verify")
                        .and().method("POST")
                        .filters(f -> f
                                .addRequestHeader("X-Gateway-Source", "api-gateway")
                                .addRequestHeader("X-Service-Name", "auth-service")
                                .addRequestHeader("X-Audit-Context", "microsoft-auth")
                        )
                        .uri("lb://auth-service"))

                // Auth Service - Token refresh (POST operation - needs audit context)
                .route("auth-refresh", r -> r.path("/api/v1/auth/refresh")
                        .and().method("POST")
                        .filters(f -> f
                                .addRequestHeader("X-Gateway-Source", "api-gateway")
                                .addRequestHeader("X-Service-Name", "auth-service")
                                .addRequestHeader("X-Audit-Context", "token-refresh")
                        )
                        .uri("lb://auth-service"))

                // Auth Service - Logout (POST operation - needs audit context)
                .route("auth-logout", r -> r.path("/api/v1/auth/logout")
                        .and().method("POST")
                        .filters(f -> f
                                .addRequestHeader("X-Gateway-Source", "api-gateway")
                                .addRequestHeader("X-Service-Name", "auth-service")
                                .addRequestHeader("X-Audit-Context", "logout")
                        )
                        .uri("lb://auth-service"))

                // =================================================================
                // GENERAL AUTH SERVICE ROUTE - Catches all remaining auth requests
                // =================================================================

                // Auth Service - All other auth API endpoints (fallback for GET operations and other routes)
                .route("auth-service", r -> r.path("/api/v1/auth/**")
                        .filters(f -> f
                                .addRequestHeader("X-Gateway-Source", "api-gateway")
                                .addRequestHeader("X-Service-Name", "auth-service")
                        )
                        .uri("lb://auth-service"))

                // Root Swagger UI Redirect Route (Optional - redirects root swagger to auth service)
                .route("root-swagger-redirect", r -> r.path("/swagger-ui/**", "/v3/api-docs/**")
                        .filters(f -> f
                                .addRequestHeader("X-Gateway-Source", "api-gateway")
                                .addRequestHeader("X-Service-Name", "auth-service")
                        )
                        .uri("lb://auth-service"))

                // Eureka Server Route
                .route("eureka-server", r -> r.path("/eureka/**")
                        .uri("http://localhost:8761"))

                .build();
    }
}