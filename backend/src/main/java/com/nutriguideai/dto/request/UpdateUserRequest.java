package com.nutriguideai.dto.request;

import com.nutriguideai.enums.ActivityLevel;
import com.nutriguideai.enums.Gender;
import com.nutriguideai.enums.Goal;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Profile details used to update the authenticated user's information.")
public class UpdateUserRequest {

    @Schema(
            description = "First name",
            example = "Amaya",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "First name is required")
    @Size(
            max = 100,
            message = "First name must not exceed 100 characters"
    )
    private String firstName;

    @Schema(
            description = "Last name",
            example = "Perera",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Last name is required")
    @Size(
            max = 100,
            message = "Last name must not exceed 100 characters"
    )
    private String lastName;

    @Schema(
            description = "Age in years (13–120)",
            example = "24",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Age is required")
    @Min(
            value = 13,
            message = "Age must be at least 13"
    )
    @Max(
            value = 120,
            message = "Age must not exceed 120"
    )
    private Integer age;

    @Schema(
            description = "Biological gender",
            example = "FEMALE",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Gender is required")
    private Gender gender;

    @Schema(
            description = "Height in centimeters (50–250)",
            example = "163.0",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Height is required")
    @DecimalMin(
            value = "50.0",
            message = "Height must be at least 50 cm"
    )
    @DecimalMax(
            value = "250.0",
            message = "Height must not exceed 250 cm"
    )
    private Double height;

    @Schema(
            description = "Weight in kilograms (20–300)",
            example = "58.0",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Weight is required")
    @DecimalMin(
            value = "20.0",
            message = "Weight must be at least 20 kg"
    )
    @DecimalMax(
            value = "300.0",
            message = "Weight must not exceed 300 kg"
    )
    private Double weight;

    @Schema(
            description = "Activity level",
            example = "MODERATELY_ACTIVE",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Activity level is required")
    private ActivityLevel activityLevel;

    @Schema(
            description = "Health goal",
            example = "WEIGHT_LOSS",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Goal is required")
    private Goal goal;
}