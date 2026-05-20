package com.example.movie_ticket_be.core.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

        @Bean
        public org.springdoc.core.customizers.OperationCustomizer customizeFilterParameter() {
                return (operation, handlerMethod) -> {
                        if (operation.getParameters() != null) {
                                operation.getParameters().stream()
                                                .filter(p -> "filter".equals(p.getName()))
                                                .forEach(p -> p.setRequired(false));
                        }
                        return operation;
                };
        }

        @Bean
        public OpenAPI customOpenAPI() {
                return new OpenAPI()
                                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                                .components(new Components()
                                                .addSecuritySchemes("bearerAuth",
                                                                new SecurityScheme()
                                                                                .name("bearerAuth")
                                                                                .type(SecurityScheme.Type.HTTP)
                                                                                .scheme("bearer")
                                                                                .bearerFormat("JWT")
                                                                                .description("Dán token JWT vào đây")));
        }
}