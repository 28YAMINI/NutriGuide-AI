package com.nutriguideai.dto.request;


import com.nutriguideai.enums.FoodCategory;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a food item.
 *
 * <p>This class is the validation boundary: any client input that violates
 * these rules is rejected with HTTP 400 (via MethodArgumentNotValidException
 * in the GlobalExceptionHandler) before it ever reaches the service layer.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateFoodItemRequest {

    @NotBlank(message = "Food name is required")
    @Size(max = 150, message = "Food name must not exceed 150 characters")
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
            message = "Image URL must be a valid http(s) URL")
    private String imageUrl;

    @NotNull(message = "Vegetarian flag is required")
    private Boolean vegetarian;
}
