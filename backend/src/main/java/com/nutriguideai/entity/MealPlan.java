package com.nutriguideai.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "meal_plans",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_mealplans_user_date", columnNames = {"user_id", "plan_date"})
        },
        indexes = {
                @Index(name = "idx_mealplans_user", columnList = "user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "A generated daily meal plan.")
public class MealPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique plan id", example = "1")
    private Long id;

    @Column(name = "user_id", nullable = false)
    @Schema(description = "Owner of this plan", example = "1")
    private Long userId;

    @Column(name = "plan_date", nullable = false)
    @Schema(description = "Date this plan is for", example = "2026-08-18")
    private LocalDate planDate;

    @Column(name = "total_calories", nullable = false)
    @Schema(description = "Total daily calories", example = "1850")
    private Integer totalCalories;

    @Column(name = "total_protein_g", nullable = false)
    @Schema(description = "Total daily protein in grams", example = "85.0")
    private Double totalProteinG;

    @Column(name = "total_carbs_g", nullable = false)
    @Schema(description = "Total daily carbs in grams", example = "220.0")
    private Double totalCarbsG;

    @Column(name = "total_fat_g", nullable = false)
    @Schema(description = "Total daily fat in grams", example = "55.0")
    private Double totalFatG;

    @Column(name = "plan_text", columnDefinition = "TEXT")
    @Schema(description = "Full generated plan (AI markdown text)", example = "**Breakfast:** ...")
    private String planText;

    @Column(name = "is_generated", nullable = false)
    @Schema(description = "True if AI-generated", example = "true")
    private Boolean isGenerated;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    @Schema(description = "When the plan was created", example = "2026-08-18T08:00:00")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    @Schema(description = "When the plan was last updated", example = "2026-08-18T08:00:00")
    private LocalDateTime updatedAt;
}