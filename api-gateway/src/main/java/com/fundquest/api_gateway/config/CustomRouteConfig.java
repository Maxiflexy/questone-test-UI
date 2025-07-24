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

                // Auth Service - All auth endpoints (no prefix stripping needed)
                .route("auth-service", r -> r.path("/api/v1/auth/**")
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
