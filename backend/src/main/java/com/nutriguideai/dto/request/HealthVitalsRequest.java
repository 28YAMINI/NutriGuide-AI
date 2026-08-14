package com.nutriguideai.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload for creating or updating health vitals (upsert pattern).
 *
 * <p>Every field is optional. Fields that are omitted (null) are left
 * unchanged when a vitals record already exists, so a partial update
 * never wipes values the client did not send.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Health vitals values to save. All fields are optional; "
        + "omitted fields keep their existing values on update.")
public class HealthVitalsRequest {

    @Schema(
            description = "Systolic blood pressure in mmHg (60–250)",
            example = "120",
            nullable = true
    )
    @Min(value = 60, message = "bloodPressureSystolic must be between 60 and 250")
    @Max(value = 250, message = "bloodPressureSystolic must be between 60 and 250")
    private Integer bloodPressureSystolic;

    @Schema(
            description = "Diastolic blood pressure in mmHg (30–150)",
            example = "80",
            nullable = true
    )
    @Min(value = 30, message = "bloodPressureDiastolic must be between 30 and 150")
    @Max(value = 150, message = "bloodPressureDiastolic must be between 30 and 150")
    private Integer bloodPressureDiastolic;

    @Schema(
            description = "Fasting blood glucose in mg/dL (20.0–500.0)",
            example = "92.0",
            nullable = true
    )
    @DecimalMin(value = "20.0", message = "fastingSugar must be between 20.0 and 500.0")
    @DecimalMax(value = "500.0", message = "fastingSugar must be between 20.0 and 500.0")
    private Double fastingSugar;

    @Schema(
            description = "Postprandial glucose in mg/dL (20.0–600.0)",
            example = "135.0",
            nullable = true
    )
    @DecimalMin(value = "20.0", message = "postMealSugar must be between 20.0 and 600.0")
    @DecimalMax(value = "600.0", message = "postMealSugar must be between 20.0 and 600.0")
    private Double postMealSugar;

    @Schema(
            description = "Glycated hemoglobin in % (2.0–20.0)",
            example = "5.4",
            nullable = true
    )
    @DecimalMin(value = "2.0", message = "hba1c must be between 2.0 and 20.0")
    @DecimalMax(value = "20.0", message = "hba1c must be between 2.0 and 20.0")
    private Double hba1c;

    @Schema(
            description = "LDL cholesterol in mg/dL (10.0–500.0)",
            example = "100.0",
            nullable = true
    )
    @DecimalMin(value = "10.0", message = "cholesterolLdl must be between 10.0 and 500.0")
    @DecimalMax(value = "500.0", message = "cholesterolLdl must be between 10.0 and 500.0")
    private Double cholesterolLdl;

    @Schema(
            description = "HDL cholesterol in mg/dL (5.0–150.0)",
            example = "55.0",
            nullable = true
    )
    @DecimalMin(value = "5.0", message = "cholesterolHdl must be between 5.0 and 150.0")
    @DecimalMax(value = "150.0", message = "cholesterolHdl must be between 5.0 and 150.0")
    private Double cholesterolHdl;

    @Schema(
            description = "Triglycerides in mg/dL (10.0–2000.0)",
            example = "120.0",
            nullable = true
    )
    @DecimalMin(value = "10.0", message = "triglycerides must be between 10.0 and 2000.0")
    @DecimalMax(value = "2000.0", message = "triglycerides must be between 10.0 and 2000.0")
    private Double triglycerides;
}