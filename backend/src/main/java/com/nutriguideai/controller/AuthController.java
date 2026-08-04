package com.nutriguideai.controller;

import com.nutriguideai.dto.request.LoginRequest;
import com.nutriguideai.dto.request.RegisterRequest;
import com.nutriguideai.dto.response.LoginResponse;
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
@Tag(name = "Authentication", description = "Endpoints for user registration and login")
public class AuthController {

    private final AuthService authService;

    // ──────────────────────────────────────────────
    // POST /api/auth/register
    // ──────────────────────────────────────────────

    @PostMapping("/register")
    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account with the provided details. " +
                    "Returns the created user information without a JWT token. " +
                    "The user must login after registration to receive a token."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = RegisterResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed — invalid input"),
            @ApiResponse(responseCode = "409", description = "Email already registered")
    })
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {

        log.info("POST /api/auth/register — email: {}", request.getEmail());

        RegisterResponse response = authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ──────────────────────────────────────────────
    // POST /api/auth/login
    // ──────────────────────────────────────────────

    @PostMapping("/login")
    @Operation(
            summary = "Authenticate a user",
            description = "Authenticates the user with email and password. " +
                    "Returns a signed JWT token on successful authentication."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful — JWT token returned",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed — invalid input"),
            @ApiResponse(responseCode = "401", description = "Invalid email or password")
    })
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {

        log.info("POST /api/auth/login — email: {}", request.getEmail());

        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(response);
    }
}