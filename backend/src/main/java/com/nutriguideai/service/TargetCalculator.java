package com.nutriguideai.service;

import com.nutriguideai.entity.User;
import com.nutriguideai.enums.ActivityLevel;
import com.nutriguideai.enums.PrimaryGoal;

/**
 * Static nutrition math: BMR (Mifflin-St Jeor), TDEE, and the calorie +
 * macro targets stored on {@link com.nutriguideai.entity.UserGoal}.
 *
 * <p>Returns {@code null} when the profile is incomplete (age, height or
 * weight missing) so callers keep previously stored targets.</p>
 */
public final class TargetCalculator {

    private TargetCalculator() {
    }

    /** Calorie + macro targets stored on UserGoal (integers, grams). */
    public record TargetMacros(Integer calories, Integer proteinG, Integer carbsG, Integer fatG) {
    }

    /** Full targets surfaced in the AI prompt and meal-plan response. */
    public record Targets(
            double bmi,
            double bmr,
            double tdee,
            double dailyCalories,
            double proteinGrams,
            double carbsGrams,
            double fatGrams
    ) {
    }

    public static TargetMacros calculate(User user, PrimaryGoal primaryGoal, ActivityLevel activityLevel) {
        if (user == null || primaryGoal == null || activityLevel == null
                || user.getAge() == null || user.getHeight() == null || user.getWeight() == null) {
            return null;
        }

        // Mifflin-St Jeor BMR; OTHER/unspecified uses the midpoint of M/F formulas
        double base = 10 * user.getWeight() + 6.25 * user.getHeight() - 5 * user.getAge();
        double genderAdjustment = user.getGender() == null ? -78 : switch (user.getGender().name()) {
            case "MALE" -> 5;
            case "FEMALE" -> -161;
            default -> -78;
        };
        double bmr = base + genderAdjustment;

        double tdee = bmr * activityMultiplier(activityLevel);
        int calories = (int) Math.round(tdee * goalAdjustment(primaryGoal));

        int proteinG = (int) Math.round(calories * proteinPercent(primaryGoal) / 100.0 / 4.0);
        int carbsG = (int) Math.round(calories * carbsPercent(primaryGoal) / 100.0 / 4.0);
        int fatG = (int) Math.round(calories * fatPercent(primaryGoal) / 100.0 / 9.0);

        return new TargetMacros(calories, proteinG, carbsG, fatG);
    }

    /** Convenience for the AI flow: same inputs, but with BMI/BMR/TDEE too. */
    public static Targets calculateTargets(User user, PrimaryGoal primaryGoal, ActivityLevel activityLevel) {
        TargetMacros macros = calculate(user, primaryGoal, activityLevel);
        if (macros == null) {
            return null;
        }
        double heightM = user.getHeight() / 100.0;
        double bmi = user.getWeight() / (heightM * heightM);

        double base = 10 * user.getWeight() + 6.25 * user.getHeight() - 5 * user.getAge();
        double genderAdjustment = user.getGender() == null ? -78 : switch (user.getGender().name()) {
            case "MALE" -> 5;
            case "FEMALE" -> -161;
            default -> -78;
        };
        double bmr = base + genderAdjustment;
        double tdee = bmr * activityMultiplier(activityLevel);

        return new Targets(
                round1(bmi), round1(bmr), round1(tdee),
                macros.calories().doubleValue(),
                macros.proteinG().doubleValue(),
                macros.carbsG().doubleValue(),
                macros.fatG().doubleValue());
    }

    private static double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private static double activityMultiplier(ActivityLevel level) {
        return switch (level.name()) {
            case "SEDENTARY" -> 1.2;
            case "LIGHT", "LIGHTLY_ACTIVE" -> 1.375;
            case "MODERATE", "MODERATELY_ACTIVE" -> 1.55;
            case "ACTIVE" -> 1.725;
            case "VERY_ACTIVE", "EXTRA_ACTIVE" -> 1.9;
            default -> 1.2;
        };
    }

    private static double goalAdjustment(PrimaryGoal goal) {
        return switch (goal.name()) {
            case "WEIGHT_LOSS", "FAT_LOSS" -> 0.8;                              // 20% deficit
            case "WEIGHT_GAIN", "MUSCLE_GAIN", "BUILD_MUSCLE" -> 1.1;           // 10% surplus
            default -> 1.0;                                                     // MAINTENANCE / HEALTHY_LIFESTYLE
        };
    }

    private static double proteinPercent(PrimaryGoal goal) {
        return switch (goal.name()) {
            case "WEIGHT_LOSS", "FAT_LOSS", "MUSCLE_GAIN", "BUILD_MUSCLE" -> 30.0;
            default -> 25.0;
        };
    }

    private static double carbsPercent(PrimaryGoal goal) {
        return switch (goal.name()) {
            case "WEIGHT_LOSS", "FAT_LOSS" -> 40.0;
            case "MUSCLE_GAIN", "BUILD_MUSCLE" -> 45.0;
            case "WEIGHT_GAIN" -> 50.0;
            default -> 45.0;
        };
    }

    private static double fatPercent(PrimaryGoal goal) {
        return switch (goal.name()) {
            case "WEIGHT_LOSS", "FAT_LOSS" -> 30.0;
            case "MUSCLE_GAIN", "BUILD_MUSCLE", "WEIGHT_GAIN" -> 25.0;
            default -> 30.0;
        };
    }
}