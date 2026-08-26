package com.nutriguideai.service.impl;

import com.nutriguideai.dto.request.TrackingRequest;
import com.nutriguideai.dto.response.*;
import com.nutriguideai.entity.FoodDiaryEntry;
import com.nutriguideai.entity.MealPlan;
import com.nutriguideai.entity.ProgressTracking;
import com.nutriguideai.entity.User;
import com.nutriguideai.entity.UserGoal;
import com.nutriguideai.exception.BadRequestException;
import com.nutriguideai.exception.ResourceNotFoundException;
import com.nutriguideai.exception.UnauthorizedException;
import com.nutriguideai.repository.*;
import com.nutriguideai.service.ProgressService;
import com.nutriguideai.service.TargetCalculator;
import com.nutriguideai.service.TargetCalculator.Targets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProgressServiceImpl implements ProgressService {

    private final ProgressTrackingRepository progressRepo;
    private final UserRepository userRepo;
    private final UserGoalRepository goalRepo;
    private final FoodDiaryEntryRepository diaryRepo;
    private final MealPlanRepository mealPlanRepo;

    /* ── Dashboard Summary ────────────────────── */

    @Override
    public DashboardSummaryResponse getSummary() {
        User user = currentUser();
        UserGoal goal = goalRepo.findByUserId(user.getId()).orElse(null);
        ProgressTracking latest = progressRepo.findFirstByUserIdOrderByRecordedDateDesc(user.getId()).orElse(null);

        // Today's diary totals
        List<FoodDiaryEntry> todayEntries = diaryRepo
                .findByUserIdAndLoggedDateOrderByCreatedAtDesc(user.getId(), LocalDate.now());
        int caloriesToday = todayEntries.stream()
                .mapToInt(e -> e.getCalories() != null ? e.getCalories().intValue() : 0)
                .sum();

        // Compute streak (consecutive days with tracking or diary entries)
        int streak = computeStreak(user.getId());

        // Targets from goal
        Double targetCalories = null;
        Integer waterTarget = null;
        Double sleepTarget = null;
        if (goal != null) {
            Targets targets = TargetCalculator.calculateTargets(user, goal.getPrimaryGoal(), goal.getActivityLevel());
            if (targets != null) targetCalories = targets.dailyCalories();
            waterTarget = goal.getWaterIntakeMl();
            sleepTarget = goal.getSleepHours();
        }

        return DashboardSummaryResponse.builder()
                .currentWeightKg(latest != null ? latest.getWeightKg() : user.getWeight())
                .bmi(latest != null ? latest.getBmi() : computeBmi(user))
                .targetCalories(Double.valueOf(targetCalories != null ? (int) Math.round(targetCalories) : null))
                .caloriesConsumedToday(caloriesToday)
                .waterIntakeMl(latest != null ? latest.getWaterIntakeMl() : null)
                .sleepHours(latest != null ? latest.getSleepHours() : null)
                .activeStreakDays(streak)
                .lastTrackedDate(latest != null ? latest.getRecordedDate() : null)
                .build();
    }

    /* ── Weight Trend ─────────────────────────── */

    @Override
    public WeightTrendResponse getWeightTrend(Integer days) {
        if (days == null || days < 1 || days > 365) days = 30;
        User user = currentUser();
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(days);

        List<ProgressTracking> records = progressRepo
                .findByUserIdAndRecordedDateBetweenOrderByRecordedDateAsc(user.getId(), from, to);

        List<WeightTrendResponse.WeightPoint> points = records.stream()
                .filter(r -> r.getWeightKg() != null)
                .map(r -> new WeightTrendResponse.WeightPoint(r.getRecordedDate(), r.getWeightKg()))
                .toList();

        String trend = "STABLE";
        if (points.size() >= 2) {
            double first = points.get(0).getWeightKg();
            double last = points.get(points.size() - 1).getWeightKg();
            if (last < first - 0.5) trend = "DOWN";
            else if (last > first + 0.5) trend = "UP";
        }

        return WeightTrendResponse.builder()
                .dataPoints(points)
                .trendDirection(trend)
                .build();
    }

    /* ── Calorie Trend ────────────────────────── */

    @Override
    public CalorieTrendResponse getCalorieTrend(Integer days) {
        if (days == null || days < 1 || days > 90) days = 7;
        User user = currentUser();
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(days);

        // Get calorie target from meal plans or goals
        Integer target = getDailyCalorieTarget(user);

        // Aggregate diary entries by date
        List<FoodDiaryEntry> entries = diaryRepo
                .findByUserIdAndLoggedDateBetweenOrderByLoggedDateDesc(user.getId(), from, to);

        // Build a map of date → total calories
        java.util.Map<LocalDate, Integer> dailyTotals = new java.util.LinkedHashMap<>();
        for (FoodDiaryEntry e : entries) {
            dailyTotals.merge(e.getLoggedDate(),
                    e.getCalories() != null ? e.getCalories().intValue() : 0,
                    Integer::sum);
        }

        List<CalorieTrendResponse.CaloriePoint> points = new ArrayList<>();
        double sumConsumed = 0;
        for (int i = 0; i < days; i++) {
            LocalDate date = from.plusDays(i);
            int consumed = dailyTotals.getOrDefault(date, 0);
            sumConsumed += consumed;
            points.add(new CalorieTrendResponse.CaloriePoint(date, consumed, target));
        }

        return CalorieTrendResponse.builder()
                .dataPoints(points)
                .averageConsumed(days > 0 ? sumConsumed / days : 0.0)
                .build();
    }

    /* ── Macro Breakdown ──────────────────────── */

    @Override
    public MacroBreakdownResponse getMacros() {
        User user = currentUser();
        UserGoal goal = goalRepo.findByUserId(user.getId()).orElse(null);

        // Today's totals from diary
        List<FoodDiaryEntry> entries = diaryRepo
                .findByUserIdAndLoggedDateOrderByCreatedAtDesc(user.getId(), LocalDate.now());

        double protein = entries.stream().mapToDouble(e -> e.getProteinG() != null ? e.getProteinG() : 0).sum();
        double carbs = entries.stream().mapToDouble(e -> e.getCarbsG() != null ? e.getCarbsG() : 0).sum();
        double fat = entries.stream().mapToDouble(e -> e.getFatG() != null ? e.getFatG() : 0).sum();

        // Targets from goal
        double proteinTarget = 0, carbsTarget = 0, fatTarget = 0;
        if (goal != null) {
            Targets targets = TargetCalculator.calculateTargets(user, goal.getPrimaryGoal(), goal.getActivityLevel());
            if (targets != null) {
                proteinTarget = targets.proteinGrams();
                carbsTarget = targets.carbsGrams();
                fatTarget = targets.fatGrams();
            }
        }

        return MacroBreakdownResponse.builder()
                .proteinConsumedG(round1(protein))
                .proteinTargetG(round1(proteinTarget))
                .proteinPercent(targetPercent(protein, proteinTarget))
                .carbsConsumedG(round1(carbs))
                .carbsTargetG(round1(carbsTarget))
                .carbsPercent(targetPercent(carbs, carbsTarget))
                .fatConsumedG(round1(fat))
                .fatTargetG(round1(fatTarget))
                .fatPercent(targetPercent(fat, fatTarget))
                .build();
    }

    /* ── Track Daily ──────────────────────────── */

    @Override
    @Transactional
    public TrackingResponse trackDaily(TrackingRequest request) {
        User user = currentUser();
        LocalDate today = LocalDate.now();

        // Upsert: find existing today's record or create new
        ProgressTracking existing = progressRepo
                .findByUserIdAndRecordedDate(user.getId(), today)
                .orElse(null);

        ProgressTracking record = existing != null ? existing : ProgressTracking.builder()
                .userId(user.getId())
                .recordedDate(today)
                .build();

        // Update provided fields
        if (request.getWeightKg() != null) {
            record.setWeightKg(request.getWeightKg());
            double heightM = user.getHeight() != null ? user.getHeight() / 100.0 : 0;
            if (heightM > 0) {
                record.setBmi(Math.round(request.getWeightKg() / (heightM * heightM) * 10.0) / 10.0);
            }
        }
        if (request.getWaterIntakeMl() != null) record.setWaterIntakeMl(request.getWaterIntakeMl());
        if (request.getSleepHours() != null) record.setSleepHours(request.getSleepHours());
        if (request.getNotes() != null) record.setNotes(request.getNotes());

        ProgressTracking saved = progressRepo.save(record);
        log.info("Progress tracked for user {} on {}", user.getEmail(), today);

        return TrackingResponse.builder()
                .id(saved.getId())
                .recordedDate(saved.getRecordedDate())
                .weightKg(saved.getWeightKg())
                .bmi(saved.getBmi())
                .waterIntakeMl(saved.getWaterIntakeMl())
                .sleepHours(saved.getSleepHours())
                .notes(saved.getNotes())
                .build();
    }

    /* ── Private helpers ──────────────────────── */

    private int computeStreak(Long userId) {
        int streak = 0;
        LocalDate date = LocalDate.now();
        for (int i = 0; i < 365; i++) {
            boolean hasData = progressRepo.findByUserIdAndRecordedDate(userId, date).isPresent()
                    || !diaryRepo.findByUserIdAndLoggedDateOrderByCreatedAtDesc(userId, date).isEmpty();
            if (hasData) {
                streak++;
                date = date.minusDays(1);
            } else {
                break;
            }
        }
        return streak;
    }

    private Integer getDailyCalorieTarget(User user) {
        // Check latest meal plan first
        List<MealPlan> plans = mealPlanRepo
                .findByUserIdAndPlanDateBetweenOrderByPlanDateDesc(
                        user.getId(), LocalDate.now().minusDays(7), LocalDate.now());
        if (!plans.isEmpty()) return plans.get(0).getTotalCalories();

        // Fallback to goal targets
        UserGoal goal = goalRepo.findByUserId(user.getId()).orElse(null);
        if (goal != null) {
            Targets targets = TargetCalculator.calculateTargets(user, goal.getPrimaryGoal(), goal.getActivityLevel());
            if (targets != null) return (int) Math.round(targets.dailyCalories());
        }
        return 2000; // default
    }

    private Double computeBmi(User user) {
        if (user.getHeight() == null || user.getWeight() == null) return null;
        double heightM = user.getHeight() / 100.0;
        return Math.round(user.getWeight() / (heightM * heightM) * 10.0) / 10.0;
    }

    private double targetPercent(double consumed, double target) {
        if (target <= 0) return 0;
        return Math.round(consumed / target * 1000.0) / 10.0;
    }

    private double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null)
            throw new UnauthorizedException("User is not authenticated");
        return userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", auth.getName()));
    }
}