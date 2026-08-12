package com.nutriguideai.dto.response;

import com.nutriguideai.entity.FoodItem;
import com.nutriguideai.enums.FoodCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "A food entry in the catalog.")
public class FoodItemResponse {

    @Schema(
            description = "Unique food id",
            example = "1"
    )
    private final Long id;

    @Schema(
            description = "Food name",
            example = "Apple"
    )
    private final String name;

    @Schema(
            description = "Short description of the food",
            example = "Fresh red apple"
    )
    private final String description;

    @Schema(
            description = "Food category",
            example = "FRUITS"
    )
    private final FoodCategory category;

    @Schema(
            description = "Energy per serving (kcal)",
            example = "52"
    )
    private final Double calories;

    @Schema(
            description = "Protein per serving (g)",
            example = "0.3"
    )
    private final Double protein;

    @Schema(
            description = "Carbohydrates per serving (g)",
            example = "13.8"
    )
    private final Double carbohydrates;

    @Schema(
            description = "Fat per serving (g)",
            example = "0.2"
    )
    private final Double fat;

    @Schema(
            description = "Fiber per serving (g)",
            example = "2.4"
    )
    private final Double fiber;

    @Schema(
            description = "Serving size this nutrition applies to",
            example = "100 g"
    )
    private final String servingSize;

    @Schema(
            description = "Image URL — null allowed; clients can fall back to a placeholder",
            nullable = true,
            example = "https://example.com/images/apple.jpg"
    )
    private final String imageUrl;

    @Schema(
            description = "Whether the food is vegetarian",
            example = "true"
    )
    private final Boolean vegetarian;

    /**
     * Converts a FoodItem entity into the API response DTO.
     */
    public static FoodItemResponse fromEntity(FoodItem foodItem) {
        return FoodItemResponse.builder()
                .id(foodItem.getId())
                .name(foodItem.getName())
                .description(foodItem.getDescription())
                .category(foodItem.getCategory())
                .calories(foodItem.getCalories())
                .protein(foodItem.getProtein())
                .carbohydrates(foodItem.getCarbohydrates())
                .fat(foodItem.getFat())
                .fiber(foodItem.getFiber())
                .servingSize(foodItem.getServingSize())
                .imageUrl(foodItem.getImageUrl())
                .vegetarian(foodItem.getVegetarian())
                .build();
    }
}