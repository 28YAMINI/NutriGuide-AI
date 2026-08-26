package com.nutriguideai.ai;

import java.util.List;

/** Immutable snapshot of everything the AI needs to know about a user. */
public record UserHealthContext(
        String name,
        int age,
        String gender,
        double heightCm,
        double weightKg,
        String activityLevel,
        String sleepInfo,
        String waterInfo,
        List<String> medicalConditions,
        String goal,
        double tdeeCalories,
        double targetCalories,
        double proteinG,
        double carbsG,
        double fatG,
        String dietType,
        String budgetLevel,
        String region,
        List<String> allergies,
        List<String> excludedFoods) {
}