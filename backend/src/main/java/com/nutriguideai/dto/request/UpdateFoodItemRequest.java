package com.nutriguideai.dto.request;

import com.nutriguideai.enums.FoodCategory;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Complete food details used to update an existing food item.")
public class UpdateFoodItemRequest {

    @Schema(
            description = "Food name",
            example = "Apple",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Food name is required")
    @Size(
            min = 2,
            max = 150,
            message = "Food name must be between 2 and 150 characters"
    )
    @Pattern(
            regexp = "^[A-Za-z0-9\\s()\\-]+$",
            message = "Food name contains invalid characters"
    )
    private String name;

    @Schema(
            description = "Short description of the food",
            example = "Fresh red apple"
    )
    @Size(
            max = 1000,
            message = "Description must not exceed 1000 characters"
    )
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
            description = "Image URL — null allowed; clients can fall back to a placeholder",
            example = "https://example.com/images/apple.jpg",
            nullable = true
    )
    @Size(
            max = 500,
            message = "Image URL must not exceed 500 characters"
    )
    @Pattern(
            regexp = "^(http|https)://.*$",
            message = "Image URL must start with http:// or https://"
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