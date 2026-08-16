package com.nutriguideai.dto.response;

import com.nutriguideai.entity.FoodPreference;
import com.nutriguideai.enums.BudgetLevel;
import com.nutriguideai.enums.DietType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreferenceResponse {

    private Long userId;
    private DietType dietType;
    private BudgetLevel budgetLevel;
    private String region;
    private List<String> allergies;
    private String excludedFoods;

    public static PreferenceResponse fromEntity(FoodPreference entity) {
        return PreferenceResponse.builder()
                .userId(entity.getUser().getId())
                .dietType(entity.getDietType())
                .budgetLevel(entity.getBudgetLevel())
                .region(entity.getRegion())
                .allergies(splitValues(entity.getAllergies()))
                .excludedFoods(entity.getExcludedFoods())
                .build();
    }

    private static List<String> splitValues(String value) {
        if (value == null || value.isBlank()) {
            return Collections.emptyList();
        }

        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(valuePart -> !valuePart.isEmpty())
                .toList();
    }
}