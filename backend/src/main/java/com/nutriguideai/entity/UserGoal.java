package com.nutriguideai.entity;

import com.nutriguideai.enums.ActivityLevel;
import com.nutriguideai.enums.PrimaryGoal;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Health goals and lifestyle data for a user (1:1 with users).
 *
 * <p>Target values are calculated server-side (see TargetCalculator) and
 * are not settable by the client. {@code user_id} is UNIQUE so each user
 * has at most one record (upsert semantics in GoalServiceImpl).</p>
 */
@Entity
@Table(
        name = "user_goals",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_goals_user", columnNames = "user_id")
        },
        indexes = {
                @Index(name = "idx_goals_primary", columnList = "primary_goal")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "A user's health goals and lifestyle settings.")
public class UserGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(
            description = "Unique goal record id",
            example = "1"
    )
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @Schema(description = "User these goals belong to")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "primary_goal", nullable = false, length = 30)
    @Schema(
            description = "Primary health goal",
            example = "WEIGHT_LOSS"
    )
    private PrimaryGoal primaryGoal;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_level", nullable = false, length = 20)
    @Schema(
            description = "Physical activity level used for the TDEE calculation",
            example = "MODERATE"
    )
    private ActivityLevel activityLevel;

    @Column(name = "target_calories")
    @Schema(
            description = "Recommended daily calorie target (calculated server-side)",
            example = "2100",
            nullable = true
    )
    private Integer targetCalories;

    @Column(name = "target_protein_g")
    @Schema(
            description = "Recommended daily protein in grams (calculated server-side)",
            example = "105",
            nullable = true
    )
    private Integer targetProteinG;

    @Column(name = "target_carbs_g")
    @Schema(
            description = "Recommended daily carbs in grams (calculated server-side)",
            example = "210",
            nullable = true
    )
    private Integer targetCarbsG;

    @Column(name = "target_fat_g")
    @Schema(
            description = "Recommended daily fat in grams (calculated server-side)",
            example = "70",
            nullable = true
    )
    private Integer targetFatG;

    @Column(name = "sleep_hours")
    @Schema(
            description = "Average daily sleep in hours (2.0–16.0)",
            example = "7.5",
            nullable = true
    )
    private Double sleepHours;

    @Column(name = "water_intake_ml")
    @Schema(
            description = "Daily water intake target in mL (200–10000)",
            example = "2500",
            nullable = true
    )
    private Integer waterIntakeMl;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    @Schema(
            description = "Timestamp when the record was created",
            example = "2026-07-20T14:35:00"
    )
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    @Schema(
            description = "Timestamp when the record was last updated",
            example = "2026-07-27T09:20:00"
    )
    private LocalDateTime updatedAt;
}