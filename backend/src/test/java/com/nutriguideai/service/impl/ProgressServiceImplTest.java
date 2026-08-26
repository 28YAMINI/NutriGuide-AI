package com.nutriguideai.service.impl;

import com.nutriguideai.dto.request.TrackingRequest;
import com.nutriguideai.dto.response.*;
import com.nutriguideai.entity.*;
import com.nutriguideai.enums.PrimaryGoal;
import com.nutriguideai.enums.ActivityLevel;
import com.nutriguideai.exception.UnauthorizedException;
import com.nutriguideai.repository.*;
import com.nutriguideai.service.TargetCalculator.Targets;
import org.junit.jupiter.api.*;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class ProgressServiceImplTest {

    private static final String EMAIL = "test@example.com";
    private static final Long USER_ID = 1L;

    private ProgressServiceImpl service;

    @BeforeEach
    void setUp() {
        ProgressTrackingRepository progressRepo = createProgressRepoStub();
        UserRepository userRepo = createUserRepoStub();
        UserGoalRepository goalRepo = createGoalRepoStub();
        FoodDiaryEntryRepository diaryRepo = createDiaryRepoStub();
        MealPlanRepository mealPlanRepo = createMealPlanRepoStub();

        service = new ProgressServiceImpl(progressRepo, userRepo, goalRepo, diaryRepo, mealPlanRepo);

        SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication
                        .UsernamePasswordAuthenticationToken(EMAIL, "pw", List.of()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ── getSummary() ──────────────────────────

    @Test
    @DisplayName("getSummary — returns weight, target calories, today's intake")
    void getSummary_withData_returnsCorrectValues() {
        DashboardSummaryResponse summary = service.getSummary();

        assertNotNull(summary);
        assertEquals(75.0, summary.getCurrentWeightKg()); // from seeded user
        assertNotNull(summary.getTargetCalories());
        assertTrue(summary.getTargetCalories() > 0); // from goal
        assertEquals(0, summary.getCaloriesConsumedToday()); // no diary entries yet
    }

    @Test
    @DisplayName("getSummary — with tracking record includes streak")
    void getSummary_withTracking_showsStreak() {
        // Log today's tracking
        service.trackDaily(TrackingRequest.builder().weightKg(74.5).waterIntakeMl(2000).build());

        DashboardSummaryResponse summary = service.getSummary();

        assertNotNull(summary);
        assertEquals(74.5, summary.getCurrentWeightKg());
        assertTrue(summary.getActiveStreakDays() >= 1);
    }

    // ── getWeightTrend() ─────────────────────

    @Test
    @DisplayName("getWeightTrend — returns data points and direction")
    void getWeightTrend_withRecords_returnsPoints() {
        service.trackDaily(TrackingRequest.builder().weightKg(75.0).build());
        service.trackDaily(TrackingRequest.builder().weightKg(74.5).build());

        WeightTrendResponse trend = service.getWeightTrend(30);

        assertNotNull(trend);
        assertNotNull(trend.getDataPoints());
        assertTrue(trend.getDataPoints().size() >= 1);
        assertNotNull(trend.getTrendDirection());
    }

    @Test
    @DisplayName("getWeightTrend — no records returns empty list")
    void getWeightTrend_noRecords_returnsEmpty() {
        WeightTrendResponse trend = service.getWeightTrend(7);

        assertNotNull(trend);
        assertTrue(trend.getDataPoints().isEmpty());
        assertEquals("STABLE", trend.getTrendDirection());
    }

    // ── getCalorieTrend() ────────────────────

    @Test
    @DisplayName("getCalorieTrend — returns data points with target")
    void getCalorieTrend_returnsTargetPerDay() {
        CalorieTrendResponse trend = service.getCalorieTrend(7);

        assertNotNull(trend);
        assertEquals(7, trend.getDataPoints().size());
        // Each point should have a target from the goal
        assertTrue(trend.getDataPoints().get(0).getTarget() > 0);
    }

    // ── getMacros() ──────────────────────────

    @Test
    @DisplayName("getMacros — returns zero consumed when no diary entries")
    void getMacros_noDiary_returnsZeros() {
        MacroBreakdownResponse macros = service.getMacros();

        assertNotNull(macros);
        assertEquals(0.0, macros.getProteinConsumedG());
        assertEquals(0.0, macros.getCarbsConsumedG());
        assertEquals(0.0, macros.getFatConsumedG());
        // Targets should be computed from goal
        assertTrue(macros.getProteinTargetG() > 0);
    }

    // ── trackDaily() ─────────────────────────

    @Test
    @DisplayName("trackDaily — creates new record with BMI")
    void trackDaily_newRecord_createsWithBmi() {
        TrackingRequest req = TrackingRequest.builder()
                .weightKg(80.0)
                .waterIntakeMl(2500)
                .sleepHours(7.5)
                .notes("Felt great")
                .build();

        TrackingResponse response = service.trackDaily(req);

        assertNotNull(response);
        assertEquals(80.0, response.getWeightKg());
        assertNotNull(response.getBmi());
        assertEquals(2500, response.getWaterIntakeMl());
        assertEquals(7.5, response.getSleepHours());
        assertEquals("Felt great", response.getNotes());
        assertEquals(LocalDate.now(), response.getRecordedDate());
    }

    @Test
    @DisplayName("trackDaily — no auth throws UnauthorizedException")
    void trackDaily_noAuth_throwsUnauthorized() {
        SecurityContextHolder.clearContext();

        assertThrows(UnauthorizedException.class,
                () -> service.trackDaily(TrackingRequest.builder().weightKg(70.0).build()));
    }

    // ═══════════════════════════════════════════
    // Dynamic-proxy stubs
    // ═══════════════════════════════════════════

    private ProgressTrackingRepository createProgressRepoStub() {
        Map<Long, ProgressTracking> store = new LinkedHashMap<>();
        AtomicLong idGen = new AtomicLong(1);

        return (ProgressTrackingRepository) Proxy.newProxyInstance(
                ProgressTrackingRepository.class.getClassLoader(),
                new Class<?>[]{ProgressTrackingRepository.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "save" -> {
                        ProgressTracking e = (ProgressTracking) args[0];
                        if (e.getId() == null) e.setId(idGen.getAndIncrement());
                        store.put(e.getId(), e);
                        yield e;
                    }
                    case "findById" -> Optional.ofNullable(store.get(args[0]));
                    case "findFirstByUserIdOrderByRecordedDateDesc" -> {
                        Long uid = (Long) args[0];
                        yield store.values().stream()
                                .filter(r -> r.getUserId().equals(uid))
                                .max(Comparator.comparing(ProgressTracking::getRecordedDate));
                    }
                    case "findByUserIdAndRecordedDate" -> {
                        Long uid = (Long) args[0];
                        LocalDate date = (LocalDate) args[1];
                        yield store.values().stream()
                                .filter(r -> r.getUserId().equals(uid) && r.getRecordedDate().equals(date))
                                .findFirst();
                    }
                    case "findByUserIdAndRecordedDateBetweenOrderByRecordedDateAsc" -> {
                        Long uid = (Long) args[0];
                        LocalDate from = (LocalDate) args[1];
                        LocalDate to = (LocalDate) args[2];
                        yield store.values().stream()
                                .filter(r -> r.getUserId().equals(uid))
                                .filter(r -> !r.getRecordedDate().isBefore(from) && !r.getRecordedDate().isAfter(to))
                                .sorted(Comparator.comparing(ProgressTracking::getRecordedDate))
                                .toList();
                    }
                    case "findByUserIdAndRecordedDateBetweenOrderByRecordedDateDesc" -> {
                        Long uid = (Long) args[0];
                        LocalDate from = (LocalDate) args[1];
                        LocalDate to = (LocalDate) args[2];
                        yield store.values().stream()
                                .filter(r -> r.getUserId().equals(uid))
                                .filter(r -> !r.getRecordedDate().isBefore(from) && !r.getRecordedDate().isAfter(to))
                                .sorted(Comparator.comparing(ProgressTracking::getRecordedDate).reversed())
                                .toList();
                    }
                    default -> throw new UnsupportedOperationException("Stub: " + method.getName());
                });
    }

    private UserRepository createUserRepoStub() {
        Map<String, User> byEmail = new HashMap<>();
        byEmail.put(EMAIL, User.builder()
                .id(USER_ID).email(EMAIL)
                .height(175.0).weight(75.0).age(28)
                .gender(null)
                .build());
        return (UserRepository) Proxy.newProxyInstance(
                UserRepository.class.getClassLoader(),
                new Class<?>[]{UserRepository.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "findByEmail" -> Optional.ofNullable(byEmail.get(args[0]));
                    default -> throw new UnsupportedOperationException("Stub: " + method.getName());
                });
    }

    private UserGoalRepository createGoalRepoStub() {
        Map<Long, UserGoal> store = new HashMap<>();
        User stubUser = User.builder()
                .id(USER_ID).email(EMAIL)
                .height(175.0).weight(75.0).age(28)
                .build();

        store.put(USER_ID, UserGoal.builder()
                .user(stubUser)     // ← CORRECT — matches @OneToOne User field
                .primaryGoal(PrimaryGoal.WEIGHT_LOSS)
                .activityLevel(ActivityLevel.MODERATE)
                .sleepHours(7.0)
                .waterIntakeMl(2500)
                .build());

        return (UserGoalRepository) Proxy.newProxyInstance(
                UserGoalRepository.class.getClassLoader(),
                new Class<?>[]{UserGoalRepository.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "findByUserId" -> Optional.ofNullable(store.get(args[0]));
                    default -> throw new UnsupportedOperationException("Stub: " + method.getName());
                });
    }

    private FoodDiaryEntryRepository createDiaryRepoStub() {
        Map<Long, FoodDiaryEntry> store = new HashMap<>();
        AtomicLong idGen = new AtomicLong(1);

        return (FoodDiaryEntryRepository) Proxy.newProxyInstance(
                FoodDiaryEntryRepository.class.getClassLoader(),
                new Class<?>[]{FoodDiaryEntryRepository.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "findByUserIdAndLoggedDateOrderByCreatedAtDesc" -> {
                        Long uid = (Long) args[0];
                        LocalDate date = (LocalDate) args[1];
                        yield store.values().stream()
                                .filter(e -> e.getUserId().equals(uid) && e.getLoggedDate().equals(date))
                                .toList();
                    }
                    case "findByUserIdAndLoggedDateBetweenOrderByLoggedDateDesc" -> {
                        Long uid = (Long) args[0];
                        LocalDate from = (LocalDate) args[1];
                        LocalDate to = (LocalDate) args[2];
                        yield store.values().stream()
                                .filter(e -> e.getUserId().equals(uid))
                                .filter(e -> !e.getLoggedDate().isBefore(from) && !e.getLoggedDate().isAfter(to))
                                .sorted(Comparator.comparing(FoodDiaryEntry::getLoggedDate).reversed())
                                .toList();
                    }
                    case "save" -> {
                        FoodDiaryEntry e = (FoodDiaryEntry) args[0];
                        if (e.getId() == null) e.setId(idGen.getAndIncrement());
                        store.put(e.getId(), e);
                        yield e;
                    }
                    default -> throw new UnsupportedOperationException("Stub: " + method.getName());
                });
    }

    private MealPlanRepository createMealPlanRepoStub() {
        return (MealPlanRepository) Proxy.newProxyInstance(
                MealPlanRepository.class.getClassLoader(),
                new Class<?>[]{MealPlanRepository.class},
                (proxy, method, args) -> {
                    if ("findByUserIdAndPlanDateBetweenOrderByPlanDateDesc".equals(method.getName())) {
                        return List.<MealPlan>of();
                    }
                    if ("save".equals(method.getName())) return args[0];
                    throw new UnsupportedOperationException("Stub: " + method.getName());
                });
    }
}