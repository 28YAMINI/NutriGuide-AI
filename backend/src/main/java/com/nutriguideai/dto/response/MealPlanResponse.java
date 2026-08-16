package com.nutriguideai.dto.response;

import com.nutriguideai.service.TargetCalculator.Targets;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Result of an AI meal-plan generation: the assistant's reply plus the
 * computed daily targets the plan was built around.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealPlanResponse {

    /** The AI-generated daily meal plan (markdown text). */
    private String plan;

    /** The computed calorie/macro targets the plan was built around. */
    private Targets targets;

    /** When the plan was generated. */
    private LocalDateTime generatedAt;
}