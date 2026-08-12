package com.nutriguideai.controller;

import com.nutriguideai.config.StandardErrorResponses;
import com.nutriguideai.dto.request.UpdateUserRequest;
import com.nutriguideai.dto.response.UserResponse;
import com.nutriguideai.service.UserService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(
        name = "Users",
        description = "The authenticated user's own profile."
)
public class UserController {

    private final UserService userService;

    // ──────────────────────────────────────────────
    // GET /api/users/me
    // ──────────────────────────────────────────────

    @GetMapping("/me")
    @StandardErrorResponses
    @Operation(
            summary = "Get my profile",
            description = "Returns the profile of the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Profile",
                    content = @Content(
                            schema = @Schema(implementation = UserResponse.class)
                    )
            )
    })
    public ResponseEntity<UserResponse> getCurrentUser() {

        log.info("Fetching profile of current user");

        return ResponseEntity.ok(userService.getCurrentUser());
    }

    // ──────────────────────────────────────────────
    // PUT /api/users/me
    // ──────────────────────────────────────────────

    @PutMapping("/me")
    @StandardErrorResponses
    @Operation(
            summary = "Update my profile",
            description = "Updates the authenticated user's profile. Email is not changeable "
                    + "and is not accepted by this endpoint."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Updated profile",
                    content = @Content(
                            schema = @Schema(implementation = UserResponse.class)
                    )
            )
    })
    public ResponseEntity<UserResponse> updateProfile(
            @Valid @RequestBody UpdateUserRequest request) {

        log.info("Updating profile of current user");

        return ResponseEntity.ok(userService.updateProfile(request));
    }

    // ──────────────────────────────────────────────
    // GET /api/users/{id}
    // Admin only
    // ──────────────────────────────────────────────

    @GetMapping("/{id}")
    @StandardErrorResponses
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get user by id (Admin only)",
            description = "Fetches any user by primary key. Requires the ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User found",
                    content = @Content(
                            schema = @Schema(implementation = UserResponse.class)
                    )
            )
    })
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable Long id) {

        log.info("Admin fetching user by id: {}", id);

        return ResponseEntity.ok(userService.getUserById(id));
    }

    // ──────────────────────────────────────────────
    // DELETE /api/users/{id}
    // Admin only
    // ──────────────────────────────────────────────

    @DeleteMapping("/{id}")
    @StandardErrorResponses
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete user (Admin only)",
            description = "Deletes a user account by id. Requires the ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "User deleted"
            )
    })
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id) {

        log.info("Admin deleting user by id: {}", id);

        userService.deleteUser(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}

