package com.school.management.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger configuration.
 * Adds Bearer JWT authentication to the Swagger UI — clicking "Authorize"
 * lets you paste a JWT token and test protected endpoints directly.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("School Management System API")
                        .description(
                                "A comprehensive School ERP with JWT security, Redis caching, and role-based access control.\n\n"
                                        +
                                        "**Roles:** ADMIN | TEACHER | STUDENT\n\n" +
                                        "**Authentication:** Use `POST /api/auth/login` to get a JWT token, then click **Authorize** and enter: `Bearer <your-token>`")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("School Management System")
                                .email("admin@school.com")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter JWT token obtained from /api/auth/login")));
    }
}
