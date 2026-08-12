package com.nutriguideai.config;

import com.nutriguideai.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Documents the standard error responses shared by every endpoint.
 * Springdoc merges these with any method-specific @ApiResponses.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses({
        @ApiResponse(responseCode = "400",
                description = "Validation failed, malformed body, or invalid enum/parameter value",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401",
                description = "Missing or invalid JWT, or invalid credentials",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403",
                description = "Authenticated user lacks permission for this endpoint",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404",
                description = "Resource not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409",
                description = "Conflict — duplicate email or food name",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500",
                description = "Unexpected server error",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
})
public @interface StandardErrorResponses {
}