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

    private static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        // PRIMARY: API Gateway server (for frontend integration)
                        new Server()
                                .url("http://localhost:" + gatewayPort)
                                .description("API Gateway"),
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
                        
                        ## Key Features
                        
                        - **Microsoft OAuth2 Integration**: Secure authentication using Microsoft Azure AD
                        - **JWT Token Management**: Access and refresh token handling with secure HTTP-only cookies
                        - **Role-Based Access Control**: Flexible RBAC system with roles and granular permissions
                        - **User Management**: User invitation system and profile management
                        - **Permission Management**: Fine-grained permission control for different operations
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