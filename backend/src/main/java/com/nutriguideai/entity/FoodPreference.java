package com.nutriguideai.entity;

import com.nutriguideai.enums.BudgetLevel;
import com.nutriguideai.enums.DietType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "food_preferences",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_food_preferences_user",
                        columnNames = "user_id"
                )
        },
        indexes = {
                @Index(
                        name = "idx_prefs_diet_type",
                        columnList = "diet_type"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "diet_type", nullable = false, length = 20)
    private DietType dietType;

    @Enumerated(EnumType.STRING)
    @Column(name = "budget_level", nullable = false, length = 10)
    private BudgetLevel budgetLevel;

    @Column(length = 100)
    private String region;

    @Column(name = "allergies", columnDefinition = "TEXT")
    private String allergies;

    @Column(name = "excluded_foods", columnDefinition = "TEXT")
    private String excludedFoods;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}