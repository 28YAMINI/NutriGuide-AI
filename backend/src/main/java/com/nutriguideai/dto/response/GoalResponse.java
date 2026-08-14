package com.nutriguideai.dto.response;

import com.nutriguideai.entity.UserGoal;
import com.nutriguideai.enums.ActivityLevel;
import com.nutriguideai.enums.PrimaryGoal;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "User goals as returned by the API.")
public class GoalResponse {

    @Schema(
            description = "User these goals belong to",
            example = "1"
    )
    private final Long userId;

    @Schema(
            description = "Primary health goal",
            example = "WEIGHT_LOSS"
    )
    private final PrimaryGoal primaryGoal;

    @Schema(
            description = "Physical activity level",
            example = "MODERATE"
    )
    private final ActivityLevel activityLevel;

    @Schema(
            description = "Recommended daily calorie target (calculated server-side)",
            example = "2100",
            nullable = true
    )
    private final Integer targetCalories;

    @Schema(
            description = "Recommended daily protein in grams (calculated server-side)",
            example = "105",
            nullable = true
    )
    private final Integer targetProteinG;

    @Schema(
            description = "Recommended daily carbs in grams (calculated server-side)",
            example = "210",
            nullable = true
    )
    private final Integer targetCarbsG;

    @Schema(
            description = "Recommended daily fat in grams (calculated server-side)",
            example = "70",
            nullable = true
    )
    private final Integer targetFatG;

    @Schema(
            description = "Average daily sleep in hours",
            example = "7.5",
            nullable = true
    )
    private final Double sleepHours;

    @Schema(
            description = "Daily water intake target in mL",
            example = "2500",
            nullable = true
    )
    private final Integer waterIntakeMl;

    public static GoalResponse fromEntity(UserGoal goal) {
        return GoalResponse.builder()
                .userId(goal.getUser().getId())
                .primaryGoal(goal.getPrimaryGoal())
                .activityLevel(goal.getActivityLevel())
                .targetCalories(goal.getTargetCalories())
                .targetProteinG(goal.getTargetProteinG())
                .targetCarbsG(goal.getTargetCarbsG())
                .targetFatG(goal.getTargetFatG())
                .sleepHours(goal.getSleepHours())
                .waterIntakeMl(goal.getWaterIntakeMl())
                .build();
    }
}