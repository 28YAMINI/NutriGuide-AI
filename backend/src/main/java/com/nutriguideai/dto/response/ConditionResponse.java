package com.nutriguideai.dto.response;

import com.nutriguideai.entity.UserCondition;
import com.nutriguideai.enums.MedicalCondition;
import com.nutriguideai.enums.Severity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "A medical condition as returned by the API.")
public class ConditionResponse {

    @Schema(
            description = "Unique condition record id",
            example = "1"
    )
    private final Long conditionId;

    @Schema(
            description = "The medical condition",
            example = "DIABETES"
    )
    private final MedicalCondition condition;

    @Schema(
            description = "Condition severity level",
            example = "MODERATE",
            nullable = true
    )
    private final Severity severity;

    @Schema(
            description = "Date the condition was diagnosed",
            example = "2024-01-15",
            nullable = true
    )
    private final LocalDate diagnosedDate;

    @Schema(
            description = "Additional notes about the condition",
            example = "Type 2 diabetes, controlled with diet",
            nullable = true
    )
    private final String notes;

    public static ConditionResponse fromEntity(UserCondition condition) {
        return ConditionResponse.builder()
                .conditionId(condition.getId())
                .condition(condition.getCondition())
                .severity(condition.getSeverity())
                .diagnosedDate(condition.getDiagnosedDate())
                .notes(condition.getNotes())
                .build();
    }
}