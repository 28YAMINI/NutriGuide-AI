package com.nutriguideai.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalorieTrendResponse {

    private List<CaloriePoint> dataPoints;
    private Double averageConsumed;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CaloriePoint {
        private LocalDate date;
        private Integer consumed;
        private Integer target;
    }
}