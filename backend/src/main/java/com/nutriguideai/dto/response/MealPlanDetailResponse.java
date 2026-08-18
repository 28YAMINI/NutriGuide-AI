package com.nutriguideai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealPlanDetailResponse {

    private Long id;
    private LocalDate planDate;
    private Integer totalCalories;
    private Double totalProteinG;
    private Double totalCarbsG;
    private Double totalFatG;
    private String plan;
    private LocalDateTime generatedAt;
}