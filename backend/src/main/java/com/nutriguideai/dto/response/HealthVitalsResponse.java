package com.nutriguideai.dto.response;

import com.nutriguideai.entity.HealthVitals;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "Health vitals as returned by the API.")
public class HealthVitalsResponse {

    @Schema(
            description = "User these vitals belong to",
            example = "1"
    )
    private final Long userId;

    @Schema(
            description = "Systolic blood pressure in mmHg",
            example = "120",
            nullable = true
    )
    private final Integer bloodPressureSystolic;

    @Schema(
            description = "Diastolic blood pressure in mmHg",
            example = "80",
            nullable = true
    )
    private final Integer bloodPressureDiastolic;

    @Schema(
            description = "Fasting blood glucose in mg/dL",
            example = "92.0",
            nullable = true
    )
    private final Double fastingSugar;

    @Schema(
            description = "Postprandial glucose in mg/dL",
            example = "135.0",
            nullable = true
    )
    private final Double postMealSugar;

    @Schema(
            description = "Glycated hemoglobin in %",
            example = "5.4",
            nullable = true
    )
    private final Double hba1c;

    @Schema(
            description = "LDL cholesterol in mg/dL",
            example = "100.0",
            nullable = true
    )
    private final Double cholesterolLdl;

    @Schema(
            description = "HDL cholesterol in mg/dL",
            example = "55.0",
            nullable = true
    )
    private final Double cholesterolHdl;

    @Schema(
            description = "Triglycerides in mg/dL",
            example = "120.0",
            nullable = true
    )
    private final Double triglycerides;

    public static HealthVitalsResponse fromEntity(HealthVitals vitals) {
        return HealthVitalsResponse.builder()
                .userId(vitals.getUser().getId())
                .bloodPressureSystolic(vitals.getBloodPressureSystolic())
                .bloodPressureDiastolic(vitals.getBloodPressureDiastolic())
                .fastingSugar(vitals.getFastingSugar())
                .postMealSugar(vitals.getPostMealSugar())
                .hba1c(vitals.getHba1c())
                .cholesterolLdl(vitals.getCholesterolLdl())
                .cholesterolHdl(vitals.getCholesterolHdl())
                .triglycerides(vitals.getTriglycerides())
                .build();
    }
}