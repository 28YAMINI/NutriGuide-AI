package com.nutriguideai.entity;

import com.nutriguideai.enums.FoodCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "food_items",
        indexes = {
                @Index(name = "idx_food_category", columnList = "category")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "A food entry in the catalog.")
public class FoodItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(
            description = "Unique food id",
            example = "1"
    )
    private Long id;

    @Column(nullable = false, length = 150)
    @Schema(
            description = "Food name",
            example = "Apple"
    )
    private String name;

    @Column(length = 1000)
    @Schema(
            description = "Short description of the food",
            example = "Fresh red apple"
    )
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(
            description = "Food category",
            example = "FRUITS"
    )
    private FoodCategory category;

    @Column(nullable = false)
    @Schema(
            description = "Energy per serving (kcal)",
            example = "52"
    )
    private Double calories;

    @Column
    @Schema(
            description = "Protein per serving (g)",
            example = "0.3"
    )
    private Double protein;

    @Column
    @Schema(
            description = "Carbohydrates per serving (g)",
            example = "13.8"
    )
    private Double carbohydrates;

    @Column
    @Schema(
            description = "Fat per serving (g)",
            example = "0.2"
    )
    private Double fat;

    @Column
    @Schema(
            description = "Fiber per serving (g)",
            example = "2.4"
    )
    private Double fiber;

    @Column(nullable = false, length = 50)
    @Schema(
            description = "Serving size this nutrition applies to",
            example = "100 g"
    )
    private String servingSize;

    @Column(length = 500)
    @Schema(
            description = "Image URL — null allowed; clients can fall back to a placeholder",
            nullable = true,
            example = "https://example.com/images/apple.jpg"
    )
    private String imageUrl;

    @Column(nullable = false)
    @Schema(
            description = "Whether the food is vegetarian",
            example = "true"
    )
    private Boolean vegetarian;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    @Schema(
            description = "Timestamp when the food was created",
            example = "2026-08-11T10:30:00"
    )
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    @Schema(
            description = "Timestamp when the food was last updated",
            example = "2026-08-11T11:30:00"
    )
    private LocalDateTime updatedAt;
}