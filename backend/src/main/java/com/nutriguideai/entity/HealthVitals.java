package com.nutriguideai.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Medical readings and lab values for a user (1:1 with users).
 *
 * <p>All vitals are optional; a user without a vitals record simply has
 * none. {@code user_id} carries a UNIQUE constraint so each user can
 * have at most one record (upsert semantics in HealthService).</p>
 */
@Entity
@Table(
        name = "health_vitals",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_health_vitals_user",
                        columnNames = "user_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "A user's medical vitals and lab readings.")
public class HealthVitals {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(
            description = "Unique vitals record id",
            example = "1"
    )
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @Schema(description = "User these vitals belong to")
    private User user;

    @Column(name = "blood_pressure_sys")
    @Schema(
            description = "Systolic blood pressure in mmHg (60–250)",
            example = "120",
            nullable = true
    )
    private Integer bloodPressureSystolic;

    @Column(name = "blood_pressure_dia")
    @Schema(
            description = "Diastolic blood pressure in mmHg (30–150)",
            example = "80",
            nullable = true
    )
    private Integer bloodPressureDiastolic;

    @Column(name = "fasting_sugar")
    @Schema(
            description = "Fasting blood glucose in mg/dL (20.0–500.0)",
            example = "92.0",
            nullable = true
    )
    private Double fastingSugar;

    @Column(name = "post_meal_sugar")
    @Schema(
            description = "Postprandial glucose in mg/dL (20.0–600.0)",
            example = "135.0",
            nullable = true
    )
    private Double postMealSugar;

    @Column(name = "hba1c")
    @Schema(
            description = "Glycated hemoglobin in % (2.0–20.0)",
            example = "5.4",
            nullable = true
    )
    private Double hba1c;

    @Column(name = "cholesterol_ldl")
    @Schema(
            description = "LDL cholesterol in mg/dL (10.0–500.0)",
            example = "100.0",
            nullable = true
    )
    private Double cholesterolLdl;

    @Column(name = "cholesterol_hdl")
    @Schema(
            description = "HDL cholesterol in mg/dL (5.0–150.0)",
            example = "55.0",
            nullable = true
    )
    private Double cholesterolHdl;

    @Column(name = "triglycerides")
    @Schema(
            description = "Triglycerides in mg/dL (10.0–2000.0)",
            example = "120.0",
            nullable = true
    )
    private Double triglycerides;

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