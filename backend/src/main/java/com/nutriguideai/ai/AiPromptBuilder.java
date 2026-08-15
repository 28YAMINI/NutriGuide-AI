package com.nutriguideai.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Builds the system + user prompts from the user's context.
 * Kept separate from the service so it is trivially unit-testable.
 */
@Component
@RequiredArgsConstructor
public class AiPromptBuilder {

    public String buildSystemPrompt(UserHealthContext ctx) {
        return """
                You are NutriGuide AI, the nutrition assistant of the NutriGuide application.
                You give personalized, practical, affordable, evidence-informed nutrition guidance.

                CONTEXT — facts about the user. Use them, never contradict them:
                - Name: %s
                - Age: %s | Gender: %s | Height: %.1f cm | Weight: %.1f kg
                - Activity level: %s | Sleep: %s | Water intake: %s
                - Medical conditions: %s (empty means none reported)
                - Primary goal: %s
                - Calculated targets: TDEE %.0f kcal | target intake %.0f kcal | protein %.0f g | carbs %.0f g | fat %.0f g
                - Diet preference: %s | Budget: %s | Region: %s
                - Allergies: %s | Excluded foods: %s

                RULES:
                1. Answer in plain, friendly language with short paragraphs and bullet lists.
                2. NEVER recommend any allergy or excluded food.
                3. Prefer locally available, budget-friendly ingredients from the user's region.
                4. Keep portions realistic for the user's calorie and macro targets.
                5. If the user has a medical condition, keep advice general and safe. Do not diagnose or prescribe.
                6. Politely redirect anything outside nutrition and healthy living.
                7. Meal plans must be Markdown: one section per day, each meal with approximate calories and protein.
                8. If information needed for a good answer is missing, ask one clarifying question first.
                9. End every response with: "This is general nutrition guidance, not medical advice. Consult a doctor or dietitian for clinical questions."
                """.formatted(
                ctx.name(), ctx.age(), ctx.gender(), ctx.heightCm(), ctx.weightKg(),
                ctx.activityLevel(), ctx.sleepInfo(), ctx.waterInfo(),
                displayList(ctx.medicalConditions()), ctx.goal(),
                ctx.tdeeCalories(), ctx.targetCalories(), ctx.proteinG(), ctx.carbsG(), ctx.fatG(),
                ctx.dietType(), ctx.budgetLevel(), ctx.region(),
                displayList(ctx.allergies()), displayList(ctx.excludedFoods()));
    }

    public String buildMealPlanUserPrompt(UserHealthContext ctx, int days, int mealsPerDay, MealPlanFocus focus) {
        return switch (focus) {
            case GROCERY -> """
                    Generate a %d-day plan with %d meals per day AND a complete grocery list
                    organized by category, with estimated quantities, favoring the user's region and budget.
                    """.formatted(days, mealsPerDay);
            case WEEKLY -> """
                    Generate a %d-day weekly meal plan with %d meals per day.
                    Vary ingredients across days so the week is not repetitive, and end with a short grocery list.
                    """.formatted(days, mealsPerDay);
            default -> """
                    Generate a 1-day meal plan with %d meals per day for today, using mostly local,
                    budget-friendly ingredients, and add a short grocery list for it.
                    """.formatted(mealsPerDay);
        };
    }

    private String displayList(List<String> items) {
        return (items == null || items.isEmpty()) ? "none" : String.join(", ", items);
    }
}