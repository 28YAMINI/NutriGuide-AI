package com.nutriguideai.dto.request;

import com.nutriguideai.enums.ActivityLevel;
import com.nutriguideai.enums.PrimaryGoal;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalRequest {

    @NotNull(message = "primaryGoal is required")
    private PrimaryGoal primaryGoal;

    @NotNull(message = "activityLevel is required")
    private ActivityLevel activityLevel;

    @DecimalMin(
            value = "2.0",
            message = "sleepHours must be between 2.0 and 16.0"
    )
    @DecimalMax(
            value = "16.0",
            message = "sleepHours must be between 2.0 and 16.0"
    )
    private Double sleepHours;

    @Min(
            value = 200,
            message = "waterIntakeMl must be between 200 and 10000"
    )
    @Max(
            value = 10000,
            message = "waterIntakeMl must be between 200 and 10000"
    )
    private Integer waterIntakeMl;
}