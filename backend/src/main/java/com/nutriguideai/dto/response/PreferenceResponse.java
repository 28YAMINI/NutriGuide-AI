package com.nutriguideai.dto.response;

import com.nutriguideai.entity.FoodPreference;
import com.nutriguideai.enums.BudgetLevel;
import com.nutriguideai.enums.DietType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "Food preferences as returned by the API.")
public class PreferenceResponse {

    @Schema(
            description = "User these preferences belong to",
            example = "1"
    )
    private final Long userId;

    @Schema(
            description = "Dietary preference",
            example = "VEGETARIAN"
    )
    private final DietType dietType;

    @Schema(
            description = "Budget for meal cost",
            example = "MEDIUM"
    )
    private final BudgetLevel budgetLevel;

    @Schema(
            description = "Geographic region for local food suggestions",
            example = "North India",
            nullable = true
    )
    private final String region;

    @Schema(
            description = "List of allergies",
            example = "[\"LACTOSE\"]",
            nullable = true
    )
    private final List<String> allergies;

    @Schema(
            description = "Free-text list of foods to exclude",
            example = "Mushrooms, Okra",
            nullable = true
    )
    private final String excludedFoods;

    public static PreferenceResponse fromEntity(FoodPreference prefs) {
        return PreferenceResponse.builder()
                .userId(prefs.getUser().getId())
                .dietType(prefs.getDietType())
                .budgetLevel(prefs.getBudgetLevel())
                .region(prefs.getRegion())
                .allergies(prefs.getAllergies())
                .excludedFoods(prefs.getExcludedFoods())
                .build();
    }
}