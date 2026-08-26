package com.nutriguideai.controller;

import com.nutriguideai.config.StandardErrorResponses;
import com.nutriguideai.dto.request.PreferenceRequest;
import com.nutriguideai.dto.response.PreferenceResponse;
import com.nutriguideai.service.PreferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for food preferences.
 *
 * <p>Every operation is scoped to the authenticated user. Preferences are
 * a 1:1 record per user with upsert semantics.</p>
 */
@RestController
@RequestMapping("/api/preferences")
@RequiredArgsConstructor
@Slf4j
@Tag(
        name = "Food Preferences",
        description = "Dietary preferences, budget, region, and allergies. "
                + "Every operation is scoped to the authenticated user's own data."
)
public class PreferenceController {

    private final PreferenceService preferenceService;

    // ──────────────────────────────────────────────
    // GET /api/preferences
    // ──────────────────────────────────────────────

    @GetMapping
    @StandardErrorResponses
    @Operation(
            summary = "Get my food preferences",
            description = "Returns the authenticated user's food preferences. "
                    + "Returns 404 when no preferences have been saved yet."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Food preferences",
                    content = @Content(
                            schema = @Schema(implementation = PreferenceResponse.class)
                    )
            )
    })
    public ResponseEntity<PreferenceResponse> getPreferences() {

        log.info("Fetching food preferences of current user");

        return ResponseEntity.ok(preferenceService.getPreferences());
    }

    // ──────────────────────────────────────────────
    // PUT /api/preferences
    // ──────────────────────────────────────────────

    @PutMapping
    @StandardErrorResponses
    @Operation(
            summary = "Create or update my food preferences",
            description = "Creates or updates the authenticated user's food "
                    + "preferences (upsert). Fields omitted from a partial "
                    + "update keep their existing values."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Food preferences updated",
                    content = @Content(
                            schema = @Schema(implementation = PreferenceResponse.class)
                    )
            )
    })
    public ResponseEntity<PreferenceResponse> upsertPreferences(
            @Valid @RequestBody PreferenceRequest request) {

        log.info("Upserting food preferences for current user");

        return ResponseEntity.ok(preferenceService.upsertPreferences(request));
    }
}