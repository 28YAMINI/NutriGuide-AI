package com.nutriguideai.dto.request;


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
public class RegisterRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Size(max = 150, message = "Email must not exceed 150 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,}$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character"
    )
    private String password;

    @NotNull(message = "Age is required")
    @Min(value = 1, message = "Age must be at least 1")
    @Max(value = 120, message = "Age must not exceed 120")
    private Integer age;

    @NotNull(message = "Gender is required")
    private Gender gender;

    @NotNull(message = "Height is required")
    @DecimalMin(value = "50.0", message = "Height must be at least 50 cm")
    @DecimalMax(value = "250.0", message = "Height must not exceed 250 cm")
    private Double height;

    @NotNull(message = "Weight is required")
    @DecimalMin(value = "20.0", message = "Weight must be at least 20 kg")
    @DecimalMax(value = "300.0", message = "Weight must not exceed 300 kg")
    private Double weight;

    @NotNull(message = "Activity level is required")
    private ActivityLevel activityLevel;

    @NotNull(message = "Goal is required")
    private Goal goal;
}
