package com.nutriguideai.controller;

import com.nutriguideai.config.StandardErrorResponses;
import com.nutriguideai.dto.request.LoginRequest;
import com.nutriguideai.dto.request.LogoutRequest;
import com.nutriguideai.dto.request.RefreshTokenRequest;
import com.nutriguideai.dto.request.RegisterRequest;
import com.nutriguideai.dto.response.LoginResponse;
import com.nutriguideai.dto.response.RefreshTokenResponse;
import com.nutriguideai.dto.response.RegisterResponse;
import com.nutriguideai.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(
        name = "Authentication",
        description = "Public endpoints for registering, signing in, refreshing tokens, and logging out."
)
public class AuthController {

    private final AuthService authService;

    // POST /api/auth/register

    @PostMapping("/register")
    @StandardErrorResponses
    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account with the provided details "
                    + "and initial health profile. No JWT token is issued. "
                    + "The user must sign in afterwards via POST /api/auth/login.",
            security = {}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Account created successfully",
                    content = @Content(
                            schema = @Schema(implementation = RegisterResponse.class)
                    )
            )
    })
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        log.info("POST /api/auth/register — email: {}", request.getEmail());

        RegisterResponse response = authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // POST /api/auth/login

    @PostMapping("/login")
    @StandardErrorResponses
    @Operation(
            summary = "Sign in",
            description = "Authenticates the user with email and password "
                    + "and returns an access token plus a refresh token.",
            security = {}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Authenticated successfully",
                    content = @Content(
                            schema = @Schema(implementation = LoginResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "423",
                    description = "Account temporarily locked after repeated failed attempts"
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "Too many login attempts — rate limited"
            )
    })
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        log.info("POST /api/auth/login — email: {}", request.getEmail());

        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(response);
    }

    // POST /api/auth/refresh

    @PostMapping("/refresh")
    @StandardErrorResponses
    @Operation(
            summary = "Refresh token pair",
            description = "Exchanges a valid refresh token for a new access token "
                    + "and a rotated refresh token. The presented refresh token is revoked.",
            security = {}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "New token pair",
                    content = @Content(
                            schema = @Schema(implementation = RefreshTokenResponse.class)
                    )
            )
    })
    public ResponseEntity<RefreshTokenResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {

        log.debug("POST /api/auth/refresh");

        return ResponseEntity.ok(authService.refresh(request));
    }

    // POST /api/auth/logout

    @PostMapping("/logout")
    @StandardErrorResponses
    @Operation(
            summary = "Log out",
            description = "Revokes the presented refresh token server-side. "
                    + "Idempotent: unknown or already-revoked tokens are ignored.",
            security = {}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Logged out"
            )
    })
    public ResponseEntity<Void> logout(
            @Valid @RequestBody LogoutRequest request) {

        log.debug("POST /api/auth/logout");

        authService.logout(request);

        return ResponseEntity.noContent().build();
    }
}