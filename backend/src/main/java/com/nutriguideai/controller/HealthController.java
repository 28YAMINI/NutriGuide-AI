package com.nutriguideai.controller;

import com.nutriguideai.config.StandardErrorResponses;
import com.nutriguideai.dto.request.AddConditionRequest;
import com.nutriguideai.dto.request.HealthVitalsRequest;
import com.nutriguideai.dto.response.ConditionResponse;
import com.nutriguideai.dto.response.HealthVitalsResponse;
import com.nutriguideai.service.HealthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for health vitals and medical conditions.
 *
 * <p>Every operation is scoped to the authenticated user: users manage
 * their own vitals and conditions, and deleting a condition owned by
 * another user returns 403 for any role.</p>
 */
@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
@Slf4j
@Tag(
        name = "Health",
        description = "Health vitals and medical conditions. Every operation "
                + "is scoped to the authenticated user's own data."
)
public class HealthController {

    private final HealthService healthService;

    // ──────────────────────────────────────────────
    // GET /api/health/vitals
    // ──────────────────────────────────────────────

    @GetMapping("/vitals")
    @StandardErrorResponses
    @Operation(
            summary = "Get my health vitals",
            description = "Returns the authenticated user's health vitals. "
                    + "Returns 404 when no vitals have been saved yet."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Health vitals",
                    content = @Content(
                            schema = @Schema(implementation = HealthVitalsResponse.class)
                    )
            )
    })
    public ResponseEntity<HealthVitalsResponse> getMyVitals() {

        log.info("Fetching health vitals of current user");

        return ResponseEntity.ok(healthService.getMyVitals());
    }

    // ──────────────────────────────────────────────
    // PUT /api/health/vitals
    // ──────────────────────────────────────────────

    @PutMapping("/vitals")
    @StandardErrorResponses
    @Operation(
            summary = "Create or update my health vitals",
            description = "Upserts the authenticated user's health vitals. "
                    + "Every field is optional — fields omitted from the request "
                    + "keep their existing values on update."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Saved health vitals",
                    content = @Content(
                            schema = @Schema(implementation = HealthVitalsResponse.class)
                    )
            )
    })
    public ResponseEntity<HealthVitalsResponse> upsertVitals(
            @Valid @RequestBody HealthVitalsRequest request) {

        log.info("Upserting health vitals for current user");

        return ResponseEntity.ok(healthService.upsertVitals(request));
    }

    // ──────────────────────────────────────────────
    // GET /api/health/conditions
    // ──────────────────────────────────────────────

    @GetMapping("/conditions")
    @StandardErrorResponses
    @Operation(
            summary = "Get my medical conditions",
            description = "Returns the authenticated user's medical conditions, "
                    + "ordered by when they were recorded."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "List of medical conditions"
            )
    })
    public ResponseEntity<List<ConditionResponse>> getMyConditions() {

        log.info("Fetching medical conditions of current user");

        return ResponseEntity.ok(healthService.getMyConditions());
    }

    // ──────────────────────────────────────────────
    // POST /api/health/conditions
    // ──────────────────────────────────────────────

    @PostMapping("/conditions")
    @StandardErrorResponses
    @Operation(
            summary = "Add a medical condition",
            description = "Records a medical condition for the authenticated user. "
                    + "Adding the same condition twice returns 409."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Condition added",
                    content = @Content(
                            schema = @Schema(implementation = ConditionResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Duplicate condition",
                    content = @Content(
                            schema = @Schema(implementation = com.nutriguideai.exception.ErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<ConditionResponse> addCondition(
            @Valid @RequestBody AddConditionRequest request) {

        log.info("Adding condition {} for current user", request.getCondition());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(healthService.addCondition(request));
    }

    // ──────────────────────────────────────────────
    // DELETE /api/health/conditions/{conditionId}
    // ──────────────────────────────────────────────

    @DeleteMapping("/conditions/{conditionId}")
    @StandardErrorResponses
    @Operation(
            summary = "Delete a medical condition",
            description = "Deletes a medical condition record owned by the "
                    + "authenticated user. Deleting a condition that belongs "
                    + "to another user returns 403 for any role."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Condition deleted"
            )
    })
    public ResponseEntity<Void> deleteCondition(
            @PathVariable
            @Parameter(description = "Condition record id")
            Long conditionId) {

        healthService.deleteCondition(conditionId);
        return ResponseEntity.noContent().build();
    }
}