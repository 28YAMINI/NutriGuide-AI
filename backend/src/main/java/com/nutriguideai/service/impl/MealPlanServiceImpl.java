package com.nutriguideai.service.impl;

import com.nutriguideai.dto.request.MealPlanRequest;
import com.nutriguideai.dto.response.MealPlanDetailResponse;
import com.nutriguideai.dto.response.MealPlanHistoryResponse;
import com.nutriguideai.dto.response.PagedResponse;
import com.nutriguideai.entity.MealPlan;
import com.nutriguideai.entity.User;
import com.nutriguideai.exception.BadRequestException;
import com.nutriguideai.exception.ResourceNotFoundException;
import com.nutriguideai.repository.MealPlanRepository;
import com.nutriguideai.repository.UserRepository;
import com.nutriguideai.service.AiNutritionService;
import com.nutriguideai.service.MealPlanService;
import com.nutriguideai.service.NotificationProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MealPlanServiceImpl implements MealPlanService {

    private final MealPlanRepository mealPlanRepository;
    private final UserRepository userRepository;
    private final AiNutritionService aiNutritionService;
    private final NotificationProducer notificationProducer;

    @Override
    public PagedResponse<MealPlanDetailResponse> getPagedMealPlans(Pageable pageable) {
        User user = getCurrentUser();
        log.debug("Fetching paged meal plans for user={}, page={}, size={}",
                user.getId(), pageable.getPageNumber(), pageable.getPageSize());

        org.springframework.data.domain.Page<MealPlan> page =
                mealPlanRepository.findByUserIdOrderByPlanDateDesc(user.getId(), pageable);

        List<MealPlanDetailResponse> responseList = page.getContent().stream()
                .map(this::mapToDetailResponse)
                .collect(Collectors.toList());

        return PagedResponse.<MealPlanDetailResponse>builder()
                .content(responseList)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isLast(page.isLast())
                .build();
    }

    @Override
    @Transactional
    public MealPlanDetailResponse generate(MealPlanRequest request) {
        User user = getCurrentUser();
        LocalDate today = LocalDate.now();

        if (request == null) {
            request = MealPlanRequest.builder().days(1).mealsPerDay(3).build();
        }
        if (request.getDays() == null) request.setDays(1);
        if (request.getMealsPerDay() == null) request.setMealsPerDay(3);

        log.info("Generating meal plan for userId={}, days={}, meals/day={}, focus={}",
                user.getId(), request.getDays(), request.getMealsPerDay(), request.getFocus());

        // 1. Call AI service to generate plan based on selected options
        MealPlanDetailResponse aiResponse = aiNutritionService.generateMealPlan(request);

        if (aiResponse == null || aiResponse.getPlan() == null) {
            throw new IllegalStateException("AI service returned an empty meal plan");
        }

        // Convert types cleanly
        int calories = (int) Math.round(aiResponse.getTotalCalories());
        double protein = aiResponse.getTotalProtein();
        double carbs = aiResponse.getTotalCarbs();
        double fat = aiResponse.getTotalFat();

        // 2. Upsert: If a plan already exists for today, update it; otherwise create a new one
        Optional<MealPlan> existingPlanOpt = mealPlanRepository.findByUserIdAndPlanDate(user.getId(), today);

        MealPlan plan;
        if (existingPlanOpt.isPresent()) {
            plan = existingPlanOpt.get();
            plan.setTotalCalories(calories);
            plan.setTotalProteinG(protein);
            plan.setTotalCarbsG(carbs);
            plan.setTotalFatG(fat);
            plan.setPlanText(aiResponse.getPlan());
            plan.setIsGenerated(true);
            log.info("Updated existing meal plan for user={}, date={}", user.getId(), today);
        } else {
            plan = MealPlan.builder()
                    .userId(user.getId())
                    .planDate(today)
                    .totalCalories(calories)
                    .totalProteinG(protein)
                    .totalCarbsG(carbs)
                    .totalFatG(fat)
                    .planText(aiResponse.getPlan())
                    .isGenerated(true)
                    .createdAt(LocalDateTime.now())
                    .build();
            log.info("Created new meal plan for user={}, date={}", user.getId(), today);
        }

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

        log.debug("Fetching meal plan history for user={}, from={}, to={}", user.getId(), from, to);

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
                .planDate(plan.getPlanDate())
                .totalCalories(plan.getTotalCalories() != null ? plan.getTotalCalories().doubleValue() : 0.0)
                .totalProtein(plan.getTotalProteinG() != null ? plan.getTotalProteinG() : 0.0)
                .totalCarbs(plan.getTotalCarbsG() != null ? plan.getTotalCarbsG() : 0.0)
                .totalFat(plan.getTotalFatG() != null ? plan.getTotalFatG() : 0.0)
                .waterIntake(2500)
                .plan(plan.getPlanText())
                .dietaryTips("Maintain hydration and adhere to your selected meal timings.")
                .items(List.of())
                .generatedAt(plan.getCreatedAt())
                .build();
    }
}