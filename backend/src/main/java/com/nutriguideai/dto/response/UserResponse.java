package com.nutriguideai.dto.response;

import com.nutriguideai.entity.User;
import com.nutriguideai.enums.ActivityLevel;
import com.nutriguideai.enums.Gender;
import com.nutriguideai.enums.Goal;
import com.nutriguideai.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "User profile as returned by the API.")
public class UserResponse {

    @Schema(
            description = "Unique user id",
            example = "1"
    )
    private final Long id;

    @Schema(
            description = "User's first name",
            example = "Amaya"
    )
    private final String firstName;

    @Schema(
            description = "User's last name",
            example = "Perera"
    )
    private final String lastName;

    @Schema(
            description = "User's account email",
            example = "amaya@example.com"
    )
    private final String email;

    @Schema(
            description = "User's assigned role",
            example = "USER"
    )
    private final Role role;

    @Schema(
            description = "Age in years",
            example = "24"
    )
    private final Integer age;

    @Schema(
            description = "Biological gender",
            example = "FEMALE"
    )
    private final Gender gender;

    @Schema(
            description = "Height in centimeters",
            example = "163.0"
    )
    private final Double height;

    @Schema(
            description = "Weight in kilograms",
            example = "58.0"
    )
    private final Double weight;

    @Schema(
            description = "User's activity level",
            example = "MODERATELY_ACTIVE"
    )
    private final ActivityLevel activityLevel;

    @Schema(
            description = "User's health goal",
            example = "WEIGHT_LOSS"
    )
    private final Goal goal;

    /**
     * Centralizes entity → DTO conversion.
     * The password field exists on User but is deliberately absent here —
     * it is the one field that must never cross the API boundary.
     */
    public static UserResponse fromEntity(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .age(user.getAge())
                .gender(user.getGender())
                .height(user.getHeight())
                .weight(user.getWeight())
                .activityLevel(user.getActivityLevel())
                .goal(user.getGoal())
                .build();
    }
}