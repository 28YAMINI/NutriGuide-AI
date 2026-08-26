package com.nutriguideai.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeightTrendResponse {

    private List<WeightPoint> dataPoints;
    private String trendDirection; // "UP", "DOWN", "STABLE"

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeightPoint {
        private LocalDate date;
        private Double weightKg;
    }
}