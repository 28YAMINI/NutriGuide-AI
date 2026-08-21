package com.nutriguideai.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealPlanDetailResponse {

    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate planDate;

    private double totalCalories;
    private double totalProtein;
    private double totalCarbs;
    private double totalFat;
    private double waterIntake;
    private String dietaryTips;
    private String plan;                    // ← ADD THIS
    private LocalDateTime generatedAt;      // ← ADD THIS

    @Builder.Default
    private List<MealItem> items = List.of();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MealItem {
        private Long id;
        private String mealType;
        private String foodName;
        private double servingSize;
        private String servingUnit;
        private double calories;
        private double protein;
        private double carbs;
        private double fat;
    }
}