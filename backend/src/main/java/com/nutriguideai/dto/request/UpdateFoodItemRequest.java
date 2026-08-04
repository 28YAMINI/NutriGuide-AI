package com.nutriguideai.dto.request;


import com.nutriguideai.enums.FoodCategory;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating a food item (PUT /api/foods/{id}).
 *
 * <p>Deliberately identical to CreateFoodItemRequest: this is a PUT
 * (full replacement) — the client must send the complete food item,
 * and every required field is validated with the same rules as creation.
 * Missing fields are rejected with HTTP 400; no partial updates allowed.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFoodItemRequest {

    @NotBlank(message = "Food name is required")
    @Size(min = 2, max = 150,
            message = "Food name must be between 2 and 150 characters")
    @Pattern(regexp = "^[A-Za-z0-9\\s()\\-]+$",
            message = "Food name contains invalid characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotNull(message = "Category is required")
    private FoodCategory category;

    @NotNull(message = "Calories are required")
    @DecimalMin(value = "0.0", message = "Calories must be zero or positive")
    @DecimalMax(value = "9999.99", message = "Calories must not exceed 9999.99")
    private Double calories;

    @DecimalMin(value = "0.0", message = "Protein must be zero or positive")
    @DecimalMax(value = "9999.99", message = "Protein must not exceed 9999.99")
    private Double protein;

    @DecimalMin(value = "0.0", message = "Carbohydrates must be zero or positive")
    @DecimalMax(value = "9999.99", message = "Carbohydrates must not exceed 9999.99")
    private Double carbohydrates;

    @DecimalMin(value = "0.0", message = "Fat must be zero or positive")
    @DecimalMax(value = "9999.99", message = "Fat must not exceed 9999.99")
    private Double fat;

    @DecimalMin(value = "0.0", message = "Fiber must be zero or positive")
    @DecimalMax(value = "9999.99", message = "Fiber must not exceed 9999.99")
    private Double fiber;

    @NotBlank(message = "Serving size is required")
    @Size(max = 50, message = "Serving size must not exceed 50 characters")
    private String servingSize;

    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    @Pattern(regexp = "^(http|https)://.*$",
            message = "Image URL must start with http:// or https://")
    private String imageUrl;

    @NotNull(message = "Vegetarian flag is required")
    private Boolean vegetarian;
}
