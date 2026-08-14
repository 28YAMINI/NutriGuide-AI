package com.nutriguideai.dto.request;

import com.nutriguideai.enums.ActivityLevel;
import com.nutriguideai.enums.PrimaryGoal;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload for creating or updating goals. Calorie and macro targets are
 * calculated server-side and cannot be set by the client.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Goal values to save. Calorie and macro targets are "
        + "calculated server-side and are not accepted from the client.")
public class GoalRequest {

    @Schema(
            description = "Primary health goal",
            example = "WEIGHT_LOSS",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "primaryGoal is required")
    private PrimaryGoal primaryGoal;

    @Schema(
            description = "Physical activity level",
            example = "MODERATE",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "activityLevel is required")
    private ActivityLevel activityLevel;

    @Schema(
            description = "Average daily sleep in hours (2.0–16.0)",
            example = "7.5",
            nullable = true
    )
    @DecimalMin(value = "2.0", message = "sleepHours must be between 2.0 and 16.0")
    @DecimalMax(value = "16.0", message = "sleepHours must be between 2.0 and 16.0")
    private Double sleepHours;

    @Schema(
            description = "Daily water intake target in mL (200–10000)",
            example = "2500",
            nullable = true
    )
    @Min(value = 200, message = "waterIntakeMl must be between 200 and 10000")
    @Max(value = 10000, message = "waterIntakeMl must be between 200 and 10000")
    private Integer waterIntakeMl;
}