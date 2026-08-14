package com.nutriguideai.entity;

import com.nutriguideai.enums.BudgetLevel;
import com.nutriguideai.enums.DietType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Dietary preferences and restrictions for a user (1:1 with users).
 *
 * <p>{@code allergies_json} is stored as a native JSON column on MySQL and
 * exposed as {@code List<String>} on the API.</p>
 */
@Entity
@Table(
        name = "food_preferences",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_food_preferences_user", columnNames = "user_id")
        },
        indexes = {
                @Index(name = "idx_prefs_diet_type", columnList = "diet_type")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "A user's dietary preferences and restrictions.")
public class FoodPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(
            description = "Unique preference record id",
            example = "1"
    )
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @Schema(description = "User these preferences belong to")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "diet_type", nullable = false, length = 20)
    @Schema(
            description = "Dietary preference",
            example = "VEGETARIAN"
    )
    private DietType dietType;

    @Enumerated(EnumType.STRING)
    @Column(name = "budget_level", nullable = false, length = 10)
    @Schema(
            description = "Budget for meal cost",
            example = "MEDIUM"
    )
    private BudgetLevel budgetLevel;

    @Column(length = 100)
    @Schema(
            description = "Geographic region for local food suggestions",
            example = "North India",
            nullable = true
    )
    private String region;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "allergies_json", columnDefinition = "json")
    @Schema(
            description = "List of allergies",
            example = "[\"LACTOSE\"]",
            nullable = true
    )
    private List<String> allergies;

    @Column(name = "excluded_foods", columnDefinition = "text")
    @Schema(
            description = "Free-text list of foods to exclude",
            example = "Mushrooms, Okra",
            nullable = true
    )
    private String excludedFoods;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    @Schema(
            description = "Timestamp when the record was created",
            example = "2026-07-20T14:40:00"
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