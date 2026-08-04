package com.nutriguideai.dto.response;



import com.nutriguideai.entity.User;
import com.nutriguideai.enums.ActivityLevel;
import com.nutriguideai.enums.Gender;
import com.nutriguideai.enums.Goal;
import com.nutriguideai.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UserResponse {

    private final Long id;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final Role role;
    private final Integer age;
    private final Gender gender;
    private final Double height;
    private final Double weight;
    private final ActivityLevel activityLevel;
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