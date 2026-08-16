package com.nutriguideai.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A user's health profile — strict 1:1 with User (unique user_id).
 * Identity/ownership is enforced in the service layer, never from client input.
 */
@Entity
@Table(name = "health_profiles", uniqueConstraints = {
        @UniqueConstraint(name = "uk_health_profiles_user", columnNames = "user_id")
}, indexes = {
        @Index(name = "idx_health_profiles_gender", columnList = "gender"),

})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthProfile {

    public enum Gender { MALE, FEMALE, OTHER }

    public enum ActivityLevel {
        SEDENTARY, LIGHTLY_ACTIVE, MODERATELY_ACTIVE, VERY_ACTIVE, EXTREMELY_ACTIVE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonIgnore
    private User user;

    @Column(nullable = false)
    private Integer age;

    @Column(name = "height_cm", nullable = false, precision = 5, scale = 2)
    private BigDecimal heightCm;

    @Column(name = "weight_kg", nullable = false, precision = 5, scale = 2)
    private BigDecimal weightKg;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_level", nullable = false, length = 30)
    private ActivityLevel activityLevel;

    @Column(name = "sleep_hours", nullable = false, precision = 3, scale = 1)
    private BigDecimal sleepHours;

    @Column(name = "water_intake_ml", nullable = false)
    private Integer waterIntakeMl;

    @Column(name = "exercise_minutes_per_week", nullable = false)
    private Integer exerciseMinutesPerWeek;

    /** Stored as a native JSON array — e.g. ["DIABETES","HYPERTENSION"]. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "medical_conditions", columnDefinition = "json")
    @Builder.Default
    private List<String> medicalConditions = new ArrayList<>();

    @Column(name = "blood_group", length = 5)
    private String bloodGroup;

    /** BMI snapshot (kg/m²), recalculated in the service whenever weight/height change. */
    @Column(precision = 4, scale = 1)
    private BigDecimal bmi;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}