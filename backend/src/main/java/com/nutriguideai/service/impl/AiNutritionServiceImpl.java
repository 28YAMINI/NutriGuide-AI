package com.nutriguideai.service.impl;

import com.nutriguideai.ai.AiProvider;
import com.nutriguideai.dto.request.AiChatRequest;
import com.nutriguideai.dto.request.MealPlanRequest;
import com.nutriguideai.dto.response.AiChatResponse;
import com.nutriguideai.dto.response.MealPlanResponse;
import com.nutriguideai.entity.User;
import com.nutriguideai.entity.UserGoal;
import com.nutriguideai.exception.ResourceNotFoundException;
import com.nutriguideai.exception.UnauthorizedException;
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
 * The plan is built from the user's profile + stored goal, with calorie and
 * macro targets computed server-side by {@link TargetCalculator}.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiNutritionServiceImpl implements AiNutritionService {

    private final UserRepository userRepository;
    private final UserGoalRepository userGoalRepository;
    private final AiProvider aiProvider;

    @Override
    public AiChatResponse chat(AiChatRequest request) {
        return null;
    }

    @Override
    public MealPlanResponse generateMealPlan(MealPlanRequest request) {
        User user = currentUser();
        UserGoal goal = userGoalRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "UserGoal", "userId", user.getId()));

        Targets targets = TargetCalculator.calculateTargets(
                user, goal.getPrimaryGoal(), goal.getActivityLevel());
        if (targets == null) {
            throw new IllegalStateException(
                    "Complete your profile (age, height, weight) before generating a meal plan.");
        }

        String systemPrompt = """
                You are NutriGuide AI, an evidence-informed nutrition assistant. \
                Give practical, budget-aware advice using locally available ingredients. \
                This is educational guidance, not medical advice. \
                Respond with a structured daily meal plan: breakfast, lunch, snacks, dinner.""";
        String userPrompt = String.format("""
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

        String reply = aiProvider.generateChat(systemPrompt, userPrompt);
        log.info("Meal plan generated for {}", user.getEmail());

        return MealPlanResponse.builder()
                .plan(reply)
                .targets(targets)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    /** Identity ALWAYS comes from the JWT principal, never from client input. */
    private User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new UnauthorizedException("User is not authenticated");
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "email", authentication.getName()));
    }
}