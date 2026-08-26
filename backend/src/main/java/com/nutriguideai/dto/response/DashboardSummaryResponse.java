package com.nutriguideai.dto.response;

import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {

    private Double currentWeightKg;
    private Double bmi;
    private Double targetCalories;
    private Integer caloriesConsumedToday;
    private Integer waterIntakeMl;
    private Double sleepHours;
    private Integer activeStreakDays;
    private LocalDate lastTrackedDate;
}