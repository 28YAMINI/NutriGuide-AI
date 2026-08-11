package com.nutriguideai.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Credentials used to authenticate an existing user.")
public class LoginRequest {

    @Schema(
            description = "Account email",
            example = "amaya@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Size(
            max = 150,
            message = "Email must not exceed 150 characters"
    )
    private String email;

    @Schema(
            description = "Account password",
            example = "Password@123",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Password is required")
    private String password;
}