package com.nutriguideai.dto.request;

import com.nutriguideai.enums.MealType;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodDiaryEntryRequest {

    private Long foodId;  // nullable — custom food if null

    @NotBlank(message = "Food name is required")
    @Size(min = 1, max = 150)
    private String foodName;

    @NotNull(message = "Meal type is required")
    private MealType mealType;

    @NotBlank(message = "Serving size is required")
    private String servingSize;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Double quantity;

    // Nutrition can be auto-filled from FoodItem or provided manually
    @PositiveOrZero
    private Double calories;

    @PositiveOrZero
    private Double proteinG;

    @PositiveOrZero
    private Double carbsG;

    @PositiveOrZero
    private Double fatG;

    @PastOrPresent(message = "Cannot log food for a future date")
    private java.time.LocalDate loggedDate;

    @Size(max = 500)
    private String notes;
}