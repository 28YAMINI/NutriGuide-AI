package com.nutriguideai.ai;

import com.nutriguideai.dto.request.MealPlanRequest;
import com.nutriguideai.entity.User;
import com.nutriguideai.entity.UserGoal;
import com.nutriguideai.service.TargetCalculator.Targets;
import org.springframework.stereotype.Component;

/**
 * Builds the system and user prompts sent to the AI provider.
 */
@Component
public class AiPromptBuilder {

    public String buildSystemPrompt() {
        return """
                You are NutriGuide AI, an evidence-informed nutrition assistant. \
                Give practical, budget-aware advice using locally available ingredients. \
                This is educational guidance, not medical advice.""";
    }

    public String buildMealPlanUserPrompt(User user, UserGoal goal, Targets targets,
                                          MealPlanRequest request) {
        return String.format("""
                Profile: %d years old, %s, %.1f cm, %.1f kg (BMI %.1f).
                Goal: %s. Activity: %s. Sleep: %.1f h/day. Water: %d ml/day.
                Daily target: %.0f kcal — protein %.0f g, carbs %.0f g, fat %.0f g.
                Give a complete daily meal plan matching these targets.""",
                user.getAge(),
                user.getGender(),
                user.getHeight(),
                user.getWeight(),
                targets.bmi(),
                goal.getPrimaryGoal(),
                goal.getActivityLevel(),
                goal.getSleepHours() == null ? 0.0 : goal.getSleepHours(),
                goal.getWaterIntakeMl() == null ? 0 : goal.getWaterIntakeMl(),
                targets.dailyCalories(),
                targets.proteinGrams(),
                targets.carbsGrams(),
                targets.fatGrams());
    }
}