package com.nutriguideai.dto.request;

import com.nutriguideai.enums.BudgetLevel;
import com.nutriguideai.enums.DietType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Payload for creating or updating food preferences. An empty allergies
 * array clears the stored list; a null field keeps its existing value.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Food preference values to save. Omitted fields keep "
        + "their existing values on update; an empty allergies array clears it.")
public class PreferenceRequest {

    @Schema(
            description = "Dietary preference",
            example = "VEGETARIAN",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "dietType is required")
    private DietType dietType;

    @Schema(
            description = "Budget for meal cost",
            example = "MEDIUM",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "budgetLevel is required")
    private BudgetLevel budgetLevel;

    @Schema(
            description = "Geographic region for local food suggestions",
            example = "North India",
            nullable = true
    )
    @Size(max = 100, message = "region must not exceed 100 characters")
    private String region;

    @Schema(
            description = "List of allergies",
            example = "[\"LACTOSE\"]",
            nullable = true
    )
    @Size(max = 50, message = "allergies must not contain more than 50 entries")
    private List<String> allergies;

    @Schema(
            description = "Free-text list of foods to exclude",
            example = "Mushrooms, Okra",
            nullable = true
    )
    @Size(max = 2000, message = "excludedFoods must not exceed 2000 characters")
    private String excludedFoods;
}