package com.nutriguideai.dto.response;

import com.nutriguideai.entity.User;
import com.nutriguideai.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The authenticated user's health vitals (stored on {@link User}).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthVitalsResponse {

    private Integer age;
    private Gender gender;
    private Double height;
    private Double weight;

    public static HealthVitalsResponse fromEntity(User user) {
        return HealthVitalsResponse.builder()
                .age(user.getAge())
                .gender(user.getGender())
                .height(user.getHeight())
                .weight(user.getWeight())
                .build();
    }
}