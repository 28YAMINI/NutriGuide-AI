package com.nutriguideai.dto.response;

import com.nutriguideai.enums.MealType;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodDiaryEntryResponse {

    private Long id;
    private Long foodId;
    private String foodName;
    private MealType mealType;
    private String servingSize;
    private Double quantity;
    private Double calories;
    private Double proteinG;
    private Double carbsG;
    private Double fatG;
    private LocalDate loggedDate;
    private String notes;
    private LocalDateTime createdAt;
}