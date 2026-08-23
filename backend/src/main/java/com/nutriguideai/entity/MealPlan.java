// backend/src/main/java/com/nutriguideai/entity/MealPlan.java
package com.nutriguideai.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "meal_plans")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "plan_date", nullable = false)
    private LocalDate planDate;

    @Column(name = "total_calories")
    private Integer totalCalories;

    @Column(name = "total_protein_g")
    private Double totalProteinG;

    @Column(name = "total_carbs_g")
    private Double totalCarbsG;

    @Column(name = "total_fat_g")
    private Double totalFatG;


    @Column(name = "plan_text", columnDefinition = "LONGTEXT")
    private String planText;

    @Builder.Default
    @Column(name = "is_generated", nullable = false)
    private Boolean isGenerated = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}