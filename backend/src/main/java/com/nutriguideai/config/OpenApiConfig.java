package com.nutriguideai.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI nutriguideOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("NutriGuide AI API")
                        .description(
                                "REST API for the NutriGuide AI nutrition platform. "
                                        + "All endpoints except registration and login require a JWT bearer token "
                                        + "issued by POST /api/auth/login.")
                        .version("1.0.0")
                        .contact(new Contact().name("NutriGuide AI Team").email("hello@nutriguideai.app")))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description(
                                                "JWT returned by POST /api/auth/login in the `token` field. "
                                                        + "Send it as: Authorization: Bearer <token>")))
                // Bearer is required by default on every operation; the two
                // public auth endpoints opt out with @Operation(security = {}).
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }
}
