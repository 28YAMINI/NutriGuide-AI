package com.nutriguideai.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "A fresh token pair returned by POST /api/auth/refresh.")
public class RefreshTokenResponse {

    @Schema(
            description = "New access token (short-lived JWT)",
            example = "eyJhbGciOiJIUzI1NiJ9..."
    )
    private final String token;

    @Schema(
            description = "New refresh token (rotated — the presented one is revoked)",
            example = "kY2xv2L..."
    )
    private final String refreshToken;

    @Schema(
            description = "Always \"Bearer\"",
            example = "Bearer"
    )
    private final String tokenType;

    public static RefreshTokenResponse of(String token, String refreshToken) {
        return RefreshTokenResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .build();
    }
}