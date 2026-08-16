package com.nutriguideai.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * A user's health goal (1:N with User — a user can keep a history,
 * but at most one goal should be {@code active} at a time).
 */
@Entity
@Table(name = "goals", indexes = {
        @Index(name = "idx_goals_user_id", columnList = "user_id"),
        @Index(name = "idx_goals_user_active", columnList = "user_id, is_active")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Goal {

    public enum GoalType {
        WEIGHT_LOSS, WEIGHT_GAIN, MUSCLE_GAIN,
        HEALTHY_LIFESTYLE, MAINTENANCE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "goal_type", nullable = false, length = 40)
    private GoalType goalType;

    /** Target weight in kg — used for loss/gain goals. */
    @Column(name = "target_weight_kg", precision = 5, scale = 2)
    private BigDecimal targetWeightKg;

    /** Optional target date. */
    @Column(name = "target_date")
    private LocalDate targetDate;

    /** Safe weekly rate (kg/week) the plan should aim for. */
    @Column(name = "weekly_target_kg", precision = 4, scale = 2)
    private BigDecimal weeklyTargetKg;

    @Column(name = "notes", length = 500)
    private String notes;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}