package com.nutriguideai.entity;

import com.nutriguideai.enums.MedicalCondition;
import com.nutriguideai.enums.Severity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * A medical condition recorded for a user. A user may have many
 * conditions; the (user_id, condition) pair is unique so the same
 * condition cannot be recorded twice.
 *
 * <p>The physical column is {@code medical_condition} because {@code
 * condition} is a reserved word in MySQL 8+; the Java property and the
 * API JSON field remain {@code condition}.</p>
 */
@Entity
@Table(
        name = "user_conditions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_condition",
                        columnNames = {"user_id", "medical_condition"}
                )
        },
        indexes = {
                @Index(name = "idx_conditions_user", columnList = "user_id"),
                @Index(name = "idx_conditions_condition", columnList = "medical_condition")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "A medical condition associated with a user.")
public class UserCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "condition_id")
    @Schema(
            description = "Unique condition record id",
            example = "1"
    )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @Schema(description = "User this condition belongs to")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "medical_condition", nullable = false, length = 30)
    @Schema(
            description = "The medical condition",
            example = "DIABETES"
    )
    private MedicalCondition condition;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    @Schema(
            description = "Condition severity level",
            example = "MODERATE",
            nullable = true
    )
    private Severity severity;

    @Column(name = "diagnosed_date")
    @Schema(
            description = "Date the condition was diagnosed",
            example = "2024-01-15",
            nullable = true
    )
    private LocalDate diagnosedDate;

    @Column(length = 500)
    @Schema(
            description = "Additional notes about the condition",
            example = "Type 2 diabetes, controlled with diet",
            nullable = true
    )
    private String notes;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    @Schema(
            description = "Timestamp when the condition was recorded",
            example = "2026-07-20T14:35:00"
    )
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    @Schema(
            description = "Timestamp when the condition was last updated",
            example = "2026-07-20T14:35:00"
    )
    private LocalDateTime updatedAt;
}