package com.nutriguideai.dto.request;

import com.nutriguideai.enums.MedicalCondition;
import com.nutriguideai.enums.Severity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payload for adding a medical condition to the user's profile.")
public class AddConditionRequest {

    @Schema(
            description = "Medical condition",
            example = "DIABETES",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "condition is required")
    private MedicalCondition condition;

    @Schema(
            description = "Condition severity level",
            example = "MODERATE",
            nullable = true
    )
    private Severity severity;

    @Schema(
            description = "Date the condition was diagnosed (must not be in the future)",
            example = "2024-01-15",
            nullable = true
    )
    @PastOrPresent(message = "diagnosedDate cannot be in the future")
    private LocalDate diagnosedDate;

    @Schema(
            description = "Additional notes about the condition",
            example = "Type 2 diabetes, controlled with diet",
            nullable = true
    )
    @Size(max = 500, message = "notes must not exceed 500 characters")
    private String notes;
}