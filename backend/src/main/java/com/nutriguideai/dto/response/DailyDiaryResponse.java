package com.nutriguideai.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyDiaryResponse {

    private LocalDate date;
    private List<FoodDiaryEntryResponse> entries;
    private int totalEntries;
    private Double totalCalories;
    private Double totalProteinG;
    private Double totalCarbsG;
    private Double totalFatG;
}