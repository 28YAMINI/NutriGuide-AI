package com.nutriguideai.service;

import com.nutriguideai.entity.User;
import com.nutriguideai.enums.ActivityLevel;
import com.nutriguideai.enums.Gender;
import com.nutriguideai.enums.PrimaryGoal;

/**
 * Calculates daily calorie and macro targets from a user's profile,
 * primary goal, and activity level.
 *
 * <p>Pipeline (docs/04_DATABASE.md §4.5, docs/07_TASKS.md TASK-010):
 * Mifflin-St Jeor BMR → TDEE (activity multiplier) → goal adjustment →
 * 30/40/30 macro split, clamped to the documented ranges. Returns
 * {@code null} when the profile is missing any value needed for BMR
 * (age, gender, height, weight).</p>
 */
public final class TargetCalculator {

    private TargetCalculator() {
    }

    /** Daily calorie and macro targets for a single user. */
    public record TargetMacros(int calories, int proteinG, int carbsG, int fatG) {
    }

    /** Mifflin-St Jeor TDEE multipliers by activity level. */
    private static final double SEDENTARY_MULTIPLIER = 1.2;
    private static final double LIGHT_MULTIPLIER = 1.375;
    private static final double MODERATE_MULTIPLIER = 1.55;
    private static final double ACTIVE_MULTIPLIER = 1.725;
    private static final double VERY_ACTIVE_MULTIPLIER = 1.9;

    /** Goal calorie adjustments (fraction of TDEE). */
    private static final double WEIGHT_LOSS_ADJUSTMENT = 0.80;
    private static final double WEIGHT_GAIN_ADJUSTMENT = 1.20;
    private static final double MUSCLE_GAIN_ADJUSTMENT = 1.15;
    private static final double MAINTENANCE_ADJUSTMENT = 1.00;
    private static final double HEALTHY_LIFESTYLE_ADJUSTMENT = 1.00;

    /** Clamp ranges from docs/04_DATABASE.md §4.5. */
    private static final int MIN_CALORIES = 800;
    private static final int MAX_CALORIES = 5000;
    private static final int MIN_PROTEIN_G = 10;
    private static final int MAX_PROTEIN_G = 400;
    private static final int MIN_CARBS_G = 10;
    private static final int MAX_CARBS_G = 600;
    private static final int MIN_FAT_G = 5;
    private static final int MAX_FAT_G = 300;

    /** Macro split: 30% protein, 40% carbs, 30% fat. */
    private static final double PROTEIN_RATIO = 0.30;
    private static final double CARBS_RATIO = 0.40;
    private static final double FAT_RATIO = 0.30;
    private static final double CALORIES_PER_G_PROTEIN = 4.0;
    private static final double CALORIES_PER_G_CARBS = 4.0;
    private static final double CALORIES_PER_G_FAT = 9.0;

    /**
     * Computes the daily targets for a user.
     *
     * @param user          the user (age, gender, height, weight must be set)
     * @param goal          the primary health goal
     * @param activityLevel the activity level for the TDEE multiplier
     * @return the calculated targets, or {@code null} when profile data
     *         needed for BMR is missing
     */
    public static TargetMacros calculate(
            User user, PrimaryGoal goal, ActivityLevel activityLevel) {

        if (user.getAge() == null || user.getGender() == null
                || user.getHeight() == null || user.getWeight() == null) {
            return null;
        }

        double bmr = bmr(user.getWeight(), user.getHeight(), user.getAge(), user.getGender());
        double tdee = bmr * activityMultiplier(activityLevel);
        double adjusted = tdee * goalAdjustment(goal);

        int calories = clamp((int) Math.round(adjusted), MIN_CALORIES, MAX_CALORIES);
        int proteinG = clamp((int) Math.round(calories * PROTEIN_RATIO / CALORIES_PER_G_PROTEIN),
                MIN_PROTEIN_G, MAX_PROTEIN_G);
        int carbsG = clamp((int) Math.round(calories * CARBS_RATIO / CALORIES_PER_G_CARBS),
                MIN_CARBS_G, MAX_CARBS_G);
        int fatG = clamp((int) Math.round(calories * FAT_RATIO / CALORIES_PER_G_FAT),
                MIN_FAT_G, MAX_FAT_G);

        return new TargetMacros(calories, proteinG, carbsG, fatG);
    }

    private static double activityMultiplier(ActivityLevel level) {
        return switch (level) {
            case SEDENTARY -> SEDENTARY_MULTIPLIER;
            case LIGHT -> LIGHT_MULTIPLIER;
            case MODERATE -> MODERATE_MULTIPLIER;
            case ACTIVE -> ACTIVE_MULTIPLIER;
            case VERY_ACTIVE -> VERY_ACTIVE_MULTIPLIER;
        };
    }

    private static double goalAdjustment(PrimaryGoal goal) {
        return switch (goal) {
            case WEIGHT_LOSS -> WEIGHT_LOSS_ADJUSTMENT;
            case WEIGHT_GAIN -> WEIGHT_GAIN_ADJUSTMENT;
            case MUSCLE_GAIN -> MUSCLE_GAIN_ADJUSTMENT;
            case MAINTENANCE -> MAINTENANCE_ADJUSTMENT;
            case HEALTHY_LIFESTYLE -> HEALTHY_LIFESTYLE_ADJUSTMENT;
        };
    }

    /** Mifflin-St Jeor BMR. OTHER is treated as the sex-neutral midpoint. */
    private static double bmr(double weightKg, double heightCm, int age, Gender gender) {
        double base = 10.0 * weightKg + 6.25 * heightCm - 5.0 * age;
        return switch (gender) {
            case MALE -> base + 5.0;
            case FEMALE -> base - 161.0;
            case OTHER -> base - 78.0;
        };
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}