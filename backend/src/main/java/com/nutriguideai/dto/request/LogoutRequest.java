package com.nutriguideai.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Body of POST /api/auth/logout — the refresh token to revoke.")
public class LogoutRequest {

    @Schema(
            description = "The refresh token to revoke server-side",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}