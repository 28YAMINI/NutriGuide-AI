package com.nutriguideai.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "progress_tracking",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_progress_user_date", columnNames = {"user_id", "recorded_date"})
        },
        indexes = {
                @Index(name = "idx_progress_user_date", columnList = "user_id, recorded_date"),
                @Index(name = "idx_progress_user_date_desc", columnList = "user_id, recorded_date DESC")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Daily progress tracking record — one per user per day.")
public class ProgressTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique tracking id", example = "1")
    private Long id;

    @Column(name = "user_id", nullable = false)
    @Schema(description = "Owner of this record", example = "1")
    private Long userId;

    @Column(name = "recorded_date", nullable = false)
    @Schema(description = "Date of this record", example = "2026-08-18")
    private LocalDate recordedDate;

    @Column(name = "weight_kg")
    @Schema(description = "Weight in kg", example = "78.0")
    private Double weightKg;

    @Column(name = "bmi")
    @Schema(description = "BMI on this date", example = "25.5")
    private Double bmi;

    @Column(name = "calories_consumed")
    @Schema(description = "Total calories consumed", example = "1850")
    private Integer caloriesConsumed;

    @Column(name = "protein_consumed")
    @Schema(description = "Total protein consumed (g)", example = "85.0")
    private Double proteinConsumed;

    @Column(name = "carbs_consumed")
    @Schema(description = "Total carbs consumed (g)", example = "220.0")
    private Double carbsConsumed;

    @Column(name = "fat_consumed")
    @Schema(description = "Total fat consumed (g)", example = "55.0")
    private Double fatConsumed;

    @Column(name = "water_intake_ml")
    @Schema(description = "Water intake in ml", example = "2500")
    private Integer waterIntakeMl;

    @Column(name = "sleep_hours")
    @Schema(description = "Sleep hours", example = "7.5")
    private Double sleepHours;

    @Column(name = "notes", length = 500)
    @Schema(description = "User notes for this day")
    private String notes;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}