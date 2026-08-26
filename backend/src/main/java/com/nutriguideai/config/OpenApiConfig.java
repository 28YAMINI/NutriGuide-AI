package com.nutriguideai.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI nutriguideOpenAPI() {

        Schema<?> errorResponse = new Schema<>()
                .name("ErrorResponse")
                .description("Consistent error body returned for every failed request")
                .type("object")
                .addProperty(
                        "timestamp",
                        new Schema<>()
                                .type("string")
                                .format("date-time")
                                .description("When the error occurred (UTC)")
                )
                .addProperty(
                        "status",
                        new Schema<>()
                                .type("integer")
                                .format("int32")
                                .description("HTTP status code")
                )
                .addProperty(
                        "error",
                        new Schema<>()
                                .type("string")
                                .description("Short HTTP reason phrase, e.g. Not Found")
                )
                .addProperty(
                        "message",
                        new Schema<>()
                                .type("string")
                                .description("Human-readable detail")
                )
                .addProperty(
                        "path",
                        new Schema<>()
                                .type("string")
                                .description("Request path that produced the error")
                );

        return new OpenAPI()
                .info(new Info()
                        .title("NutriGuide AI API")
                        .version("1.0.0")
                        .description(
                                "REST API for the NutriGuide AI nutrition assistant. "
                                        + "Register, sign in with a JWT, manage your health profile, "
                                        + "and browse the food catalog. All error responses share "
                                        + "the ErrorResponse schema."
                        )
                        .contact(new Contact()
                                .name("NutriGuide AI")
                                .email("hello@nutriguideai.app")
                        )
                )
                .components(new Components()
                        .addSecuritySchemes(
                                SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )
                        .addSchemas("ErrorResponse", errorResponse)
                )
                .addSecurityItem(
                        new SecurityRequirement()
                                .addList(SECURITY_SCHEME_NAME)
                );
    }
}

