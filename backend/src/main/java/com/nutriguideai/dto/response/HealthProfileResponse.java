package com.nutriguideai.dto.response;

import com.nutriguideai.entity.HealthProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthProfileResponse {

    private Long id;
    private Integer age;
    private BigDecimal heightCm;
    private BigDecimal weightKg;
    private HealthProfile.Gender gender;
    private HealthProfile.ActivityLevel activityLevel;
    private BigDecimal sleepHours;
    private Integer waterIntakeMl;
    private Integer exerciseMinutesPerWeek;
    private List<String> medicalConditions;
    private String bloodGroup;
    private BigDecimal bmi;

    public static HealthProfileResponse fromEntity(HealthProfile profile) {
        return HealthProfileResponse.builder()
                .id(profile.getId())
                .age(profile.getAge())
                .heightCm(profile.getHeightCm())
                .weightKg(profile.getWeightKg())
                .gender(profile.getGender())
                .activityLevel(profile.getActivityLevel())
                .sleepHours(profile.getSleepHours())
                .waterIntakeMl(profile.getWaterIntakeMl())
                .exerciseMinutesPerWeek(profile.getExerciseMinutesPerWeek())
                .medicalConditions(profile.getMedicalConditions())
                .bloodGroup(profile.getBloodGroup())
                .bmi(profile.getBmi())
                .build();
    }
}