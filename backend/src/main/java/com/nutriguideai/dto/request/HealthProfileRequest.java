package com.nutriguideai.dto.request;

import com.nutriguideai.entity.HealthProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthProfileRequest {

    private Integer age;
    private BigDecimal heightCm;
    private BigDecimal weightKg;
    private HealthProfile.Gender gender;
    private HealthProfile.ActivityLevel activityLevel;
    private BigDecimal sleepHours;
    private Integer waterIntakeMl;
    private Integer exerciseMinutesPerWeek;
    private List<String> medicalConditions = new ArrayList<>();
    private String bloodGroup;
}