package com.nutriguideai.dto.request;
import io.swagger.v3.oas.annotations.media.Schema;
import com.nutriguideai.enums.ActivityLevel;
import com.nutriguideai.enums.Gender;
import com.nutriguideai.enums.Goal;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Account details plus the initial health profile.")
public class RegisterRequest {

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
            description = "Account email",
            example = "amaya@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Size(
            max = 150,
            message = "Email must not exceed 150 characters"
    )
    private String email;

    @Schema(
            description = "Password, at least 8 characters",
            example = "Password@123",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Password is required")
    @Size(
            min = 8,
            max = 72,
            message = "Password must be between 8 and 72 characters"
    )
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,}$",
            message = "Password must contain at least one uppercase letter, "
                    + "one lowercase letter, one digit, and one special character"
    )
    private String password;

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
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Gender is required")
    private Gender gender;

    @Schema(
            description = "Height in centimeters (50–250)",
            example = "163",
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
            example = "58",
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
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Activity level is required")
    private ActivityLevel activityLevel;

    @Schema(
            description = "Health goal",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Health goal is required")
    private Goal goal;
}