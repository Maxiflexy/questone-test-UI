package com.fundquest.auth.config.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    // API Gateway port for routing requests
    @Value("${api.gateway.port:8080}")
    private String gatewayPort;

    // Server configuration for dynamic deployment
    @Value("${server.external.host:91.134.107.175}")
    private String externalHost;

    @Value("${server.external.port:8080}")
    private String externalPort;

    @Value("${server.context.path:/auth}")
    private String contextPath;

    private static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        // CURRENT DEPLOYMENT: Your server IP with context path
                        new Server()
                                .url("http://" + externalHost + ":" + externalPort)
                                .description("Current Deployment Server"),

                        // LOCAL GATEWAY: API Gateway server (for local development)
                        new Server()
                                .url("http://localhost:" + gatewayPort)
                                .description("Local API Gateway"),

                        // LOCAL DIRECT: Direct auth service (for local development)
                        new Server()
                                .url("http://localhost:8010")
                                .description("Local Auth Service Direct"),

                        // PRODUCTION: Production server
                        new Server()
                                .url("https://api.fundquest.com")
                                .description("Production Server")
                ))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, createAPIKeyScheme()));
    }

    private Info apiInfo() {
        return new Info()
                .title("FundQuest Authentication Service API")
                .description("""
                        ## Overview
                        
                        The FundQuest Authentication Service provides secure Microsoft OAuth2-based authentication 
                        and authorization for the FundQuest platform. This service manages user authentication, 
                        role-based access control (RBAC), and permission management.
                        """)
                .version("1.0.1")
                .contact(new Contact()
                        .name("FundQuest Engineering Team")
                        .email("engineering@fundquestnigeria.com")
                        .url("https://fundquestnigeria.com"))
                .license(new License()
                        .name("Proprietary")
                        .url("https://fundquestnigeria.com/terms"));
    }

    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer")
                .description("""
                        **JWT Bearer Token Authentication**
                        """);
    }
}