package com.nutriguideai.service.impl;

import com.nutriguideai.exception.BadRequestException;
import com.nutriguideai.exception.ResourceNotFoundException;
import com.nutriguideai.service.AiNutritionService;
import com.nutriguideai.dto.request.MealPlanRequest;
import com.nutriguideai.dto.response.MealPlanDetailResponse;
import com.nutriguideai.dto.response.MealPlanHistoryResponse;
import com.nutriguideai.entity.MealPlan;
import com.nutriguideai.entity.User;
import com.nutriguideai.exception.DuplicatePlanException;
import com.nutriguideai.repository.MealPlanRepository;
import com.nutriguideai.repository.UserRepository;
import com.nutriguideai.service.MealPlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MealPlanServiceImpl implements MealPlanService {

    private final MealPlanRepository mealPlanRepository;
    private final UserRepository userRepository;
    private final AiNutritionService aiNutritionService;

    @Override
    public MealPlanDetailResponse generate(MealPlanRequest request) {
        User user = getCurrentUser();

        // Check for duplicate plan today
        if (mealPlanRepository.existsByUserIdAndPlanDate(user.getId(), LocalDate.now())) {
            throw new DuplicatePlanException("Meal plan for today already exists");
        }

        // Call AI service
        MealPlanDetailResponse aiResponse = aiNutritionService.generateMealPlan(request);

        // Validate
        if (aiResponse == null || aiResponse.getPlan() == null) {
            throw new IllegalStateException("AI service returned empty meal plan");
        }

        // Persist
        MealPlan plan = MealPlan.builder()
                .userId(user.getId())
                .planDate(LocalDate.now())
                .totalCalories((int) aiResponse.getTotalCalories())
                .totalProteinG(aiResponse.getTotalProtein())
                .totalCarbsG(aiResponse.getTotalCarbs())
                .totalFatG(aiResponse.getTotalFat())
                .planText(aiResponse.getPlan())
                .createdAt(LocalDateTime.now())
                .build();
        mealPlanRepository.save(plan);

        return mapToDetailResponse(plan);
    }

    @Override
    public MealPlanDetailResponse getByDate(LocalDate date) {
        LocalDate today = LocalDate.now();
        if (date.isBefore(today.minusDays(7)) || date.isAfter(today.plusDays(7))) {
            throw new BadRequestException("Date must be within 7-day window");
        }
        User user = getCurrentUser();
        log.debug("Fetching meal plan for user={}, date={}", user.getId(), date);

        MealPlan plan = mealPlanRepository
                .findByUserIdAndPlanDate(user.getId(), date)
                .orElseThrow(() -> new ResourceNotFoundException("No meal plan found for " + date));

        return mapToDetailResponse(plan);
    }


    @Override
    public MealPlanDetailResponse getById(Long planId) {
        User user = getCurrentUser();
        log.debug("Fetching meal plan id={} for user={}", planId, user.getId());

        MealPlan plan = mealPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Meal plan not found: " + planId));

        if (!plan.getUserId().equals(user.getId())) {
            throw new ResourceNotFoundException("Access denied: plan does not belong to you");
        }

        return mapToDetailResponse(plan);
    }

    @Override
    public MealPlanHistoryResponse getHistory(LocalDate from, LocalDate to) {


        if (from == null || to == null) {
            throw new BadRequestException("Date range from and to are required");
        }
        if (from.isAfter(to)) {
            throw new BadRequestException("'from' date must be before 'to' date");
        }
        User user = getCurrentUser();
        long daysBetween = ChronoUnit.DAYS.between(from, to);
        if (daysBetween > 7) {
            throw new BadRequestException("History range cannot exceed 7 days");
        }

        log.debug("Fetching meal plan history for user={}, from={}, to={}",
                user.getId(), from, to);

        List<MealPlan> plans = mealPlanRepository
                .findByUserIdAndPlanDateBetweenOrderByPlanDateDesc(user.getId(), from, to);

        List<MealPlanDetailResponse> planResponses = plans.stream()
                .map(this::mapToDetailResponse)
                .collect(Collectors.toList());

        return MealPlanHistoryResponse.builder()
                .plans(planResponses)
                .total(planResponses.size())
                .build();
    }

    // ─── Helpers ───────────────────────────────────────────────────────

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }

    private MealPlanDetailResponse mapToDetailResponse(MealPlan plan) {
        return MealPlanDetailResponse.builder()
                .id(plan.getId())
                .planDate(LocalDate.now())
                .totalCalories(plan.getTotalCalories())
                .totalProtein(plan.getTotalProteinG())
                .totalCarbs(plan.getTotalCarbsG())
                .totalFat(plan.getTotalFatG())
                .plan(plan.getPlanText())
                .generatedAt(plan.getCreatedAt())
                .build();
    }
}