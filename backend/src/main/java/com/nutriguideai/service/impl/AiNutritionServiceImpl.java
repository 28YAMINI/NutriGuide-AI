package com.nutriguideai.service.impl;

import com.nutriguideai.ai.AiPromptBuilder;
import com.nutriguideai.ai.AiProvider;
import com.nutriguideai.ai.MealPlanFocus;
import com.nutriguideai.ai.UserHealthContext;
import com.nutriguideai.dto.request.AiChatRequest;
import com.nutriguideai.dto.request.MealPlanRequest;
import com.nutriguideai.dto.response.AiChatResponse;
import com.nutriguideai.dto.response.MealPlanResponse;
import com.nutriguideai.entity.FoodPreference;
import com.nutriguideai.entity.Goal;
import com.nutriguideai.entity.HealthProfile;
import com.nutriguideai.entity.User;
import com.nutriguideai.exception.ResourceNotFoundException;
import com.nutriguideai.exception.UnauthorizedException;
import com.nutriguideai.repository.FoodPreferenceRepository;
import com.nutriguideai.repository.GoalRepository;
import com.nutriguideai.repository.HealthProfileRepository;
import com.nutriguideai.repository.UserRepository;
import com.nutriguideai.service.AiNutritionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiNutritionServiceImpl implements AiNutritionService {

    private final UserRepository userRepository;
    private final HealthProfileRepository healthProfileRepository;
    private final GoalRepository goalRepository;
    private final FoodPreferenceRepository foodPreferenceRepository;
    private final AiPromptBuilder promptBuilder;
    private final AiProvider aiProvider;
    // ⚠️ ADAPT: swap the type/signature below to match your TargetCalculator.
    private final TargetCalculator targetCalculator;

    @Override
    public AiChatResponse chat(AiChatRequest request) {
        User user = currentUser();
        UserHealthContext ctx = buildContext(user);
        String reply = aiProvider.generateChat(
                promptBuilder.buildSystemPrompt(ctx), request.getMessage());
        log.info("AI chat reply generated for {}", user.getEmail());
        return new AiChatResponse(reply, modelName(), LocalDateTime.now());
    }

    @Override
    public MealPlanResponse generateMealPlan(MealPlanRequest request) {
        User user = currentUser();
        UserHealthContext ctx = buildContext(user);
        MealPlanFocus focus = request.getFocus() != null ? request.getFocus() : MealPlanFocus.DAILY;

        String userPrompt = promptBuilder.buildMealPlanUserPrompt(
                ctx, request.getDays(), request.getMealsPerDay(), focus);
        String plan = aiProvider.generateChat(promptBuilder.buildSystemPrompt(ctx), userPrompt);

        log.info("Meal plan generated for {} ({} days, focus {})",
                user.getEmail(), request.getDays(), focus);
        return new MealPlanResponse(plan, modelName(), LocalDateTime.now());
    }

    /**
     * Assembles the AI context from the user's stored data.
     * ⚠️ ADAPT: getter names below must match your entities (HealthProfile, Goal,
     * FoodPreference) and your TargetCalculator's return type.
     */
    private UserHealthContext buildContext(User user) {
        HealthProfile profile = healthProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("HealthProfile", "userId", user.getId()));
        Goal goal = goalRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Goal", "userId", user.getId()));
        FoodPreference prefs = foodPreferenceRepository.findByUserId(user.getId()).orElse(null);

        // ⚠️ ADAPT: match your TargetCalculator API (method name + args + return type).
        TargetCalculator.Targets targets = targetCalculator.calculate(profile, goal);

        return new UserHealthContext(
                user.getFirstName(),
                profile.getAge(),
                profile.getGender(),
                profile.getHeightCm(),
                profile.getWeightKg(),
                profile.getActivityLevel(),
                String.valueOf(profile.getSleepHours()),
                String.valueOf(profile.getWaterIntakeLiters()),
                profile.getMedicalConditions(),
                goal.getGoalType(),
                targets.getTdee(),
                targets.getTargetCalories(),
                targets.getProteinG(),
                targets.getCarbsG(),
                targets.getFatG(),
                prefs != null ? prefs.getDietType() : "no preference",
                prefs != null ? prefs.getBudgetLevel() : "medium",
                prefs != null ? prefs.getRegion() : "not specified",
                prefs != null ? prefs.getAllergies() : List.of(),
                prefs != null ? prefs.getExcludedFoods() : List.of());
    }

    private String modelName() {
        return "gemini-2.5-flash";
    }

    /** Identity ALWAYS comes from the JWT principal, never from client input. */
    private User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new UnauthorizedException("User is not authenticated");
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", authentication.getName()));
    }
}