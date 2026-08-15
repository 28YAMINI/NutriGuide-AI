package com.nutriguideai.dto.request;

import com.nutriguideai.ai.MealPlanFocus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealPlanRequest {

    @NotNull(message = "days is required")
    @Min(1)
    @Max(7)
    private Integer days;

    @NotNull(message = "mealsPerDay is required")
    @Min(2)
    @Max(6)
    private Integer mealsPerDay;

    /** Optional; defaults to DAILY in the service. */
    private MealPlanFocus focus;
}