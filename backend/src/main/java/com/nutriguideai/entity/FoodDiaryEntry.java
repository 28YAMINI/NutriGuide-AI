package com.nutriguideai.entity;

import com.nutriguideai.enums.MealType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "food_diary_entries",
        indexes = {
                @Index(name = "idx_diary_user_date", columnList = "user_id, logged_date")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "A user-logged food diary entry.")
public class FoodDiaryEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique entry id", example = "1")
    private Long id;

    @Column(name = "user_id", nullable = false)
    @Schema(description = "Owner of this entry", example = "1")
    private Long userId;

    @Column(name = "food_id")
    @Schema(description = "FK to food catalog (nullable for custom foods)", example = "5")
    private Long foodId;

    @Column(name = "food_name", nullable = false, length = 150)
    @Schema(description = "Food name (snapshot at log time)", example = "Grilled Chicken Breast")
    private String foodName;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", nullable = false)
    @Schema(description = "Meal category", example = "LUNCH")
    private MealType mealType;

    @Column(name = "serving_size", nullable = false, length = 50)
    @Schema(description = "Serving size description", example = "1 breast (86 g)")
    private String servingSize;

    @Column(name = "quantity", nullable = false)
    @Schema(description = "Number of servings", example = "1.5")
    private Double quantity;

    @Column(name = "calories", nullable = false)
    @Schema(description = "Total calories for this entry", example = "230")
    private Double calories;

    @Column(name = "protein_g")
    @Schema(description = "Total protein in grams", example = "43.0")
    private Double proteinG;

    @Column(name = "carbs_g")
    @Schema(description = "Total carbs in grams", example = "0.0")
    private Double carbsG;

    @Column(name = "fat_g")
    @Schema(description = "Total fat in grams", example = "5.0")
    private Double fatG;

    @Column(name = "logged_date", nullable = false)
    @Schema(description = "Date the food was logged", example = "2026-08-18")
    private LocalDate loggedDate;

    @Column(name = "notes", length = 500)
    @Schema(description = "Optional notes", example = "Post-workout meal")
    private String notes;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}