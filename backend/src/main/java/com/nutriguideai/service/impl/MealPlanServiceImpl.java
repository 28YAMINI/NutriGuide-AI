package com.nutriguideai.service.impl;

import com.nutriguideai.dto.request.MealPlanRequest;
import com.nutriguideai.dto.response.MealPlanDetailResponse;
import com.nutriguideai.dto.response.MealPlanHistoryResponse;
import com.nutriguideai.dto.response.MealPlanResponse;
import com.nutriguideai.entity.MealPlan;
import com.nutriguideai.entity.User;
import com.nutriguideai.exception.BadRequestException;
import com.nutriguideai.exception.DuplicatePlanException;
import com.nutriguideai.exception.ResourceNotFoundException;
import com.nutriguideai.exception.UnauthorizedException;
import com.nutriguideai.repository.MealPlanRepository;
import com.nutriguideai.repository.UserRepository;
import com.nutriguideai.service.AiNutritionService;
import com.nutriguideai.service.MealPlanService;
import com.nutriguideai.service.TargetCalculator.Targets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MealPlanServiceImpl implements MealPlanService {

    private static final long MAX_HISTORY_RANGE_DAYS = 7;

    private final MealPlanRepository mealPlanRepository;
    private final AiNutritionService aiNutritionService;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public MealPlanDetailResponse generate(MealPlanRequest request) {
        User user = currentUser();
        LocalDate today = LocalDate.now();

        if (mealPlanRepository.existsByUserIdAndPlanDate(user.getId(), today)) {
            throw new DuplicatePlanException(
                    "A meal plan already exists for " + today + ". Delete it or choose another day.");
        }

        MealPlanResponse aiResponse = aiNutritionService.generateMealPlan(request);
        Targets targets = aiResponse.getTargets();
        if (targets == null) {
            throw new IllegalStateException(
                    "Complete your profile (age, height, weight) before generating a meal plan.");
        }

        MealPlan plan = MealPlan.builder()
                .userId(user.getId())
                .planDate(today)
                .totalCalories((int) Math.round(targets.dailyCalories()))
                .totalProteinG(targets.proteinGrams())
                .totalCarbsG(targets.carbsGrams())
                .totalFatG(targets.fatGrams())
                .planText(aiResponse.getPlan())
                .isGenerated(true)
                .build();

        MealPlan saved = mealPlanRepository.save(plan);
        log.info("Meal plan {} saved for user {}", saved.getId(), user.getEmail());

        return toResponse(saved);
    }

    @Override
    public MealPlanDetailResponse getByDate(LocalDate date) {
        validateDateRange(date);
        User user = currentUser();
        MealPlan plan = mealPlanRepository.findByUserIdAndPlanDate(user.getId(), date)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "MealPlan", "date", date.toString()));
        return toResponse(plan);
    }

    @Override
    public MealPlanDetailResponse getById(Long planId) {
        User user = currentUser();
        MealPlan plan = mealPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("MealPlan", "id", planId));
        // Ownership check: hide other users' plans (no existence leak).
        if (!plan.getUserId().equals(user.getId())) {
            throw new ResourceNotFoundException("MealPlan", "id", planId);
        }
        return toResponse(plan);
    }

    @Override
    public MealPlanHistoryResponse getHistory(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new BadRequestException("Both 'from' and 'to' dates are required.");
        }
        if (from.isAfter(to)) {
            throw new BadRequestException("'from' must be on or before 'to'.");
        }
        if (ChronoUnit.DAYS.between(from, to) > MAX_HISTORY_RANGE_DAYS) {
            throw new BadRequestException("History range cannot exceed " + MAX_HISTORY_RANGE_DAYS + " days.");
        }
        User user = currentUser();
        List<MealPlan> plans = mealPlanRepository
                .findByUserIdAndPlanDateBetweenOrderByPlanDateDesc(user.getId(), from, to);
        List<MealPlanDetailResponse> items = plans.stream().map(this::toResponse).toList();
        return MealPlanHistoryResponse.builder().plans(items).total(items.size()).build();
    }

    private MealPlanDetailResponse toResponse(MealPlan plan) {
        return MealPlanDetailResponse.builder()
                .id(plan.getId())
                .planDate(plan.getPlanDate())
                .totalCalories(plan.getTotalCalories())
                .totalProteinG(plan.getTotalProteinG())
                .totalCarbsG(plan.getTotalCarbsG())
                .totalFatG(plan.getTotalFatG())
                .plan(plan.getPlanText())
                .generatedAt(plan.getCreatedAt())
                .build();
    }

    private void validateDateRange(LocalDate date) {
        LocalDate today = LocalDate.now();
        if (date.isBefore(today.minusDays(MAX_HISTORY_RANGE_DAYS))
                || date.isAfter(today.plusDays(MAX_HISTORY_RANGE_DAYS))) {
            throw new BadRequestException(
                    "Date must be within " + MAX_HISTORY_RANGE_DAYS + " days of today.");
        }
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