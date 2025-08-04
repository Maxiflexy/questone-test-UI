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

                // Auth Service - All auth API endpoints (no prefix stripping needed)
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