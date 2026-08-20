package com.nutriguideai.service.impl;

import com.nutriguideai.ai.AiProvider;
import com.nutriguideai.dto.request.AiChatRequest;
import com.nutriguideai.dto.request.MealPlanRequest;
import com.nutriguideai.dto.response.AiChatResponse;
import com.nutriguideai.dto.response.MealPlanResponse;
import com.nutriguideai.entity.FoodPreference;
import com.nutriguideai.entity.User;
import com.nutriguideai.entity.UserGoal;
import com.nutriguideai.exception.ResourceNotFoundException;
import com.nutriguideai.exception.UnauthorizedException;
import com.nutriguideai.repository.FoodPreferenceRepository;
import com.nutriguideai.repository.UserGoalRepository;
import com.nutriguideai.repository.UserRepository;
import com.nutriguideai.service.AiNutritionService;
import com.nutriguideai.service.TargetCalculator;
import com.nutriguideai.service.TargetCalculator.Targets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * AI meal-plan generation.
 *
 * <p>Identity always comes from the JWT principal, never from client input.
 * The plan is built from the user's profile, stored goal and food preferences,
 * with calorie and macro targets computed server-side by {@link TargetCalculator}.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiNutritionServiceImpl implements AiNutritionService {

    private final UserRepository userRepository;
    private final UserGoalRepository userGoalRepository;
    private final FoodPreferenceRepository foodPreferenceRepository;
    private final AiProvider aiProvider;

    @Override
    public AiChatResponse chat(AiChatRequest request) {
        return null;
    }

    @Override
    public MealPlanResponse generateMealPlan(MealPlanRequest request) {
        User user = currentUser();

        FoodPreference preference = foodPreferenceRepository
                .findByUserId(user.getId())
                .orElse(null);

        UserGoal goal = userGoalRepository
                .findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "UserGoal", "userId", user.getId()));

        Targets targets = TargetCalculator.calculateTargets(
                user,
                goal.getPrimaryGoal(),
                goal.getActivityLevel()
        );

        if (targets == null) {
            throw new IllegalStateException(
                    "Complete your profile (age, height, weight) before generating a meal plan."
            );
        }

        String focus = request.getFocus() == null
                ? "DAILY"
                : request.getFocus().name();

        String dietType = preference == null || preference.getDietType() == null
                ? "Not specified"
                : preference.getDietType().name();

        String budgetLevel = preference == null || preference.getBudgetLevel() == null
                ? "Not specified"
                : preference.getBudgetLevel().name();

        String region = preference == null
                || preference.getRegion() == null
                || preference.getRegion().isBlank()
                ? "Not specified"
                : preference.getRegion();

        String allergies = preference == null
                || preference.getAllergies() == null
                || preference.getAllergies().isBlank()
                ? "None"
                : preference.getAllergies();

        String excludedFoods = preference == null
                || preference.getExcludedFoods() == null
                || preference.getExcludedFoods().isBlank()
                ? "None"
                : preference.getExcludedFoods();

        String systemPrompt = """
                You are NutriGuide AI, an evidence-informed nutrition assistant.
                Create practical, realistic and budget-aware meal plans using locally
                available ingredients.

                This is educational nutrition guidance, not medical advice.

                Follow the user's dietary restrictions, allergies and excluded foods strictly.
                Never recommend an ingredient listed as an allergy or excluded food.

                Provide clear meal names, ingredients, serving sizes and estimated
                calories and macros for each meal.

                Keep the total daily intake as close as practical to the supplied
                calorie and macro targets.
                """;

        String userPrompt = String.format("""
                Create a personalized %s meal plan.

                USER PROFILE
                Age: %d years
                Gender: %s
                Height: %.1f cm
                Weight: %.1f kg
                BMI: %.1f
                Goal: %s
                Activity level: %s
                Sleep: %.1f hours/day
                Water intake: %d ml/day

                DAILY NUTRITION TARGETS
                Calories: %.0f kcal
                Protein: %.0f g
                Carbohydrates: %.0f g
                Fat: %.0f g

                FOOD PREFERENCES
                Diet type: %s
                Budget level: %s
                Region: %s
                Allergies: %s
                Excluded foods: %s

                PLAN REQUIREMENTS
                - Create exactly %d meals per day.
                - Respect the diet type.
                - Never use listed allergens.
                - Never use excluded foods.
                - Prefer affordable ingredients appropriate for the region.
                - Provide practical serving sizes.
                - Give estimated calories, protein, carbohydrates and fat for every meal.
                - Include a daily nutrition summary.
                - Keep meals simple enough for normal home preparation.
                """,
                focus,
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
                targets.fatGrams(),
                dietType,
                budgetLevel,
                region,
                allergies,
                excludedFoods,
                request.getMealsPerDay()
        );

        String reply = aiProvider.generateChat(systemPrompt, userPrompt);

        log.info(
                "Meal plan generated for {} with focus {} and {} meals/day",
                user.getEmail(),
                focus,
                request.getMealsPerDay()
        );

        return MealPlanResponse.builder()
                .plan(reply)
                .targets(targets)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    /** Identity ALWAYS comes from the JWT principal, never from client input. */
    private User currentUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            throw new UnauthorizedException("User is not authenticated");
        }

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "email", authentication.getName()));
    }
}