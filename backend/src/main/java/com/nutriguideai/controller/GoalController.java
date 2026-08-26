package com.nutriguideai.controller;

import com.nutriguideai.config.StandardErrorResponses;
import com.nutriguideai.dto.request.GoalRequest;
import com.nutriguideai.dto.response.GoalResponse;
import com.nutriguideai.service.GoalService;
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
 * REST controller for health goals and lifestyle data.
 *
 * <p>Every operation is scoped to the authenticated user. Calorie and
 * macro targets are calculated server-side and returned in the response;
 * they cannot be set by the client.</p>
 */
@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
@Slf4j
@Tag(
        name = "Goals",
        description = "Health goals and lifestyle settings. Every operation "
                + "is scoped to the authenticated user's own data."
)
public class GoalController {

    private final GoalService goalService;

    // ──────────────────────────────────────────────
    // GET /api/goals
    // ──────────────────────────────────────────────

    @GetMapping
    @StandardErrorResponses
    @Operation(
            summary = "Get my goals",
            description = "Returns the authenticated user's health goals and "
                    + "calculated calorie/macro targets. Returns 404 when no "
                    + "goals have been saved yet."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Goals",
                    content = @Content(
                            schema = @Schema(implementation = GoalResponse.class)
                    )
            )
    })
    public ResponseEntity<GoalResponse> getGoals() {

        log.info("Fetching goals of current user");

        return ResponseEntity.ok(goalService.getGoals());
    }

    // ──────────────────────────────────────────────
    // PUT /api/goals
    // ──────────────────────────────────────────────

    @PutMapping
    @StandardErrorResponses
    @Operation(
            summary = "Create or update my goals",
            description = "Creates or updates the authenticated user's goals. "
                    + "Calorie and macro targets are recalculated server-side "
                    + "from the profile, goal, and activity level."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Goals updated",
                    content = @Content(
                            schema = @Schema(implementation = GoalResponse.class)
                    )
            )
    })
    public ResponseEntity<GoalResponse> upsertGoals(@Valid @RequestBody GoalRequest request) {

        log.info("Upserting goals for current user");

        return ResponseEntity.ok(goalService.upsertGoals(request));
    }
}