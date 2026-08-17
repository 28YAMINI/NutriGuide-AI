package com.nutriguideai.dto.request;

import com.nutriguideai.enums.FoodCategory;
import com.nutriguideai.enums.Region;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FoodRequest {

    @NotBlank(message = "Food name is required")
    @Size(max = 150, message = "Food name must be at most 150 characters")
    private String name;

    @NotNull(message = "Category is required")
    private FoodCategory category;

    @NotNull(message = "Region is required")
    private Region region;

    @NotNull(message = "Calories are required")
    @DecimalMin(value = "0.0", message = "Calories cannot be negative")
    private Double calories;

    @NotNull(message = "Protein is required")
    @DecimalMin(value = "0.0", message = "Protein cannot be negative")
    private Double protein;

    @NotNull(message = "Carbs are required")
    @DecimalMin(value = "0.0", message = "Carbs cannot be negative")
    private Double carbs;

    @NotNull(message = "Fat is required")
    @DecimalMin(value = "0.0", message = "Fat cannot be negative")
    private Double fat;

    @NotNull(message = "Fiber is required")
    @DecimalMin(value = "0.0", message = "Fiber cannot be negative")
    private Double fiber;

    @NotNull(message = "Vegetarian flag is required")
    private Boolean vegetarian;

    @NotNull(message = "Vegan flag is required")
    private Boolean vegan;

    @Size(max = 255, message = "Allergens must be at most 255 characters")
    private String allergens;

    /** Optional; defaults to true (active). */
    private Boolean active;
}