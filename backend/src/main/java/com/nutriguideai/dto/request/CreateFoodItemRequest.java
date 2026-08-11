package com.nutriguideai.dto.request;

import com.nutriguideai.enums.FoodCategory;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Food item details used when creating a new food.")
public class CreateFoodItemRequest {

    @Schema(
            description = "Food name",
            example = "Apple",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Food name is required")
    @Size(max = 150, message = "Food name must not exceed 150 characters")
    private String name;

    @Schema(
            description = "Short description of the food",
            example = "Fresh red apple"
    )
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @Schema(
            description = "Food category",
            example = "FRUITS",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Category is required")
    private FoodCategory category;

    @Schema(
            description = "Energy per serving (kcal)",
            example = "52",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Calories are required")
    @DecimalMin(
            value = "0.0",
            message = "Calories must be zero or positive"
    )
    @DecimalMax(
            value = "9999.99",
            message = "Calories must not exceed 9999.99"
    )
    private Double calories;

    @Schema(
            description = "Protein per serving (g)",
            example = "0.3"
    )
    @DecimalMin(
            value = "0.0",
            message = "Protein must be zero or positive"
    )
    @DecimalMax(
            value = "9999.99",
            message = "Protein must not exceed 9999.99"
    )
    private Double protein;

    @Schema(
            description = "Carbohydrates per serving (g)",
            example = "13.8"
    )
    @DecimalMin(
            value = "0.0",
            message = "Carbohydrates must be zero or positive"
    )
    @DecimalMax(
            value = "9999.99",
            message = "Carbohydrates must not exceed 9999.99"
    )
    private Double carbohydrates;

    @Schema(
            description = "Fat per serving (g)",
            example = "0.2"
    )
    @DecimalMin(
            value = "0.0",
            message = "Fat must be zero or positive"
    )
    @DecimalMax(
            value = "9999.99",
            message = "Fat must not exceed 9999.99"
    )
    private Double fat;

    @Schema(
            description = "Fiber per serving (g)",
            example = "2.4"
    )
    @DecimalMin(
            value = "0.0",
            message = "Fiber must be zero or positive"
    )
    @DecimalMax(
            value = "9999.99",
            message = "Fiber must not exceed 9999.99"
    )
    private Double fiber;

    @Schema(
            description = "Serving size this nutrition applies to",
            example = "100 g",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Serving size is required")
    @Size(
            max = 50,
            message = "Serving size must not exceed 50 characters"
    )
    private String servingSize;

    @Schema(
            description = "Image URL — optional; clients can fall back to a placeholder",
            example = "https://example.com/images/apple.jpg"
    )
    @Size(
            max = 500,
            message = "Image URL must not exceed 500 characters"
    )
    @Pattern(
            regexp = "^(http|https)://.*$",
            message = "Image URL must be a valid http(s) URL"
    )
    private String imageUrl;

    @Schema(
            description = "Whether the food is vegetarian",
            example = "true",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Vegetarian flag is required")
    private Boolean vegetarian;
}