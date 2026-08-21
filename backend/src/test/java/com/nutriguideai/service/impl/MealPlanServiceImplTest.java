package com.nutriguideai.service.impl;

import com.nutriguideai.dto.request.AiChatRequest;
import com.nutriguideai.dto.request.MealPlanRequest;
import com.nutriguideai.dto.response.AiChatResponse;
import com.nutriguideai.dto.response.MealPlanDetailResponse;
import com.nutriguideai.dto.response.MealPlanHistoryResponse;
import com.nutriguideai.dto.response.MealPlanResponse;
import com.nutriguideai.entity.MealPlan;
import com.nutriguideai.entity.User;
import com.nutriguideai.exception.BadRequestException;
import com.nutriguideai.exception.DuplicatePlanException;
import com.nutriguideai.exception.ResourceNotFoundException;
import com.nutriguideai.repository.MealPlanRepository;
import com.nutriguideai.repository.UserRepository;
import com.nutriguideai.service.AiNutritionService;
import org.junit.jupiter.api.*;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link MealPlanServiceImpl} — zero Mockito.
 */
class MealPlanServiceImplTest {

    private static final String EMAIL = "test@example.com";
    private static final Long USER_ID = 1L;

    private MealPlanServiceImpl service;
    private StubAiNutritionService aiService;

    @BeforeEach
    void setUp() {
        MealPlanRepository mealPlanRepo = createMealPlanRepoStub();
        UserRepository userRepo = createUserRepoStub();
        aiService = new StubAiNutritionService();

        // FIXED: correct argument order (repo, userRepo, aiService)
        service = new MealPlanServiceImpl(mealPlanRepo, userRepo, (com.nutriguideai.service.AiNutritionService) aiService);

        SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication
                        .UsernamePasswordAuthenticationToken(EMAIL, "pw", List.of()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    /* ═══════════════════════════════════════════════
       generate()
       ═══════════════════════════════════════════════ */

    @Test
    @DisplayName("generate — success persists plan and returns response")
    void generate_success_persistsPlanAndReturnsResponse() {
        aiService.setNextResponse(buildAiResponse(2000.0, 90.0, 240.0, 60.0, "Eat oats"));

        MealPlanDetailResponse response = service.generate(buildRequest());

        assertNotNull(response);
        assertEquals(LocalDate.now(), response.getPlanDate());
        assertEquals(2000, response.getTotalCalories());
        assertEquals(90.0, response.getTotalProtein());
        assertEquals(240.0, response.getTotalCarbs());
        assertEquals(60.0, response.getTotalFat());
        assertEquals("Eat oats", response.getPlan());
    }

    @Test
    @DisplayName("generate — duplicate date throws DuplicatePlanException")
    void generate_duplicateDate_throwsDuplicatePlanException() {
        aiService.setNextResponse(buildAiResponse(1800.0, 80.0, 200.0, 55.0, "Plan"));
        service.generate(buildRequest());

        DuplicatePlanException ex = assertThrows(
                DuplicatePlanException.class, () -> service.generate(buildRequest()));
        assertTrue(ex.getMessage().contains("already exists"));
    }

    @Test
    @DisplayName("generate — null plan text from AI throws IllegalStateException")
    void generate_nullPlanText_throwsIllegalStateException() {
        aiService.setNextResponse(MealPlanDetailResponse.builder()
                .plan(null)
                .totalCalories(2000)
                .totalProtein(90)
                .totalCarbs(240)
                .totalFat(60)
                .generatedAt(LocalDateTime.now())
                .build());

        assertThrows(IllegalStateException.class, () -> service.generate(buildRequest()));
    }

    /* ═══════════════════════════════════════════════
       getByDate()
       ═══════════════════════════════════════════════ */

    @Test
    @DisplayName("getByDate — found returns plan")
    void getByDate_found_returnsPlan() {
        aiService.setNextResponse(buildAiResponse(1800.0, 80.0, 200.0, 55.0, "Lunch"));
        service.generate(buildRequest());

        MealPlanDetailResponse response = service.getByDate(LocalDate.now());

        assertNotNull(response);
        assertEquals(1800, response.getTotalCalories());
        assertEquals("Lunch", response.getPlan());
    }

    @Test
    @DisplayName("getByDate — not found throws ResourceNotFoundException")
    void getByDate_notFound_throwsResourceNotFoundException() {
        assertThrows(ResourceNotFoundException.class,
                () -> service.getByDate(LocalDate.now()));
    }

    @Test
    @DisplayName("getByDate — date before 7-day window throws BadRequestException")
    void getByDate_outside7DayWindow_throwsBadRequestException() {
        assertThrows(BadRequestException.class,
                () -> service.getByDate(LocalDate.now().minusDays(8)));
    }

    @Test
    @DisplayName("getByDate — date beyond 7-day window throws BadRequestException")
    void getByDate_futureBeyondWindow_throwsBadRequestException() {
        assertThrows(BadRequestException.class,
                () -> service.getByDate(LocalDate.now().plusDays(8)));
    }

    /* ═══════════════════════════════════════════════
       getById()
       ═══════════════════════════════════════════════ */

    @Test
    @DisplayName("getById — found returns plan")
    void getById_found_returnsPlan() {
        aiService.setNextResponse(buildAiResponse(2100.0, 95.0, 260.0, 65.0, "Dinner"));
        MealPlanDetailResponse generated = service.generate(buildRequest());

        MealPlanDetailResponse response = service.getById(generated.getId());

        assertNotNull(response);
        assertEquals("Dinner", response.getPlan());
    }

    @Test
    @DisplayName("getById — not found throws ResourceNotFoundException")
    void getById_notFound_throwsResourceNotFoundException() {
        assertThrows(ResourceNotFoundException.class,
                () -> service.getById(999L));
    }

    @Test
    @DisplayName("getById — other user's plan throws ResourceNotFoundException")
    void getById_otherUsersPlan_throwsResourceNotFoundException() {
        assertThrows(ResourceNotFoundException.class,
                () -> service.getById(500L));
    }

    /* ═══════════════════════════════════════════════
       getHistory()
       ═══════════════════════════════════════════════ */

    @Test
    @DisplayName("getHistory — valid range returns plans ordered desc")
    void getHistory_validRange_returnsPlans() {
        aiService.setNextResponse(buildAiResponse(1700.0, 75.0, 200.0, 50.0, "Plan A"));
        service.generate(buildRequest());

        MealPlanHistoryResponse response =
                service.getHistory(LocalDate.now(), LocalDate.now());

        assertNotNull(response);
        assertEquals(1, response.getTotal());
        assertEquals(1, response.getPlans().size());
        assertEquals(LocalDate.now(), response.getPlans().get(0).getPlanDate());
    }

    @Test
    @DisplayName("getHistory — null from throws BadRequestException")
    void getHistory_nullFrom_throwsBadRequestException() {
        assertThrows(BadRequestException.class,
                () -> service.getHistory(null, LocalDate.now()));
    }

    @Test
    @DisplayName("getHistory — null to throws BadRequestException")
    void getHistory_nullTo_throwsBadRequestException() {
        assertThrows(BadRequestException.class,
                () -> service.getHistory(LocalDate.now(), null));
    }

    @Test
    @DisplayName("getHistory — from after to throws BadRequestException")
    void getHistory_fromAfterTo_throwsBadRequestException() {
        LocalDate today = LocalDate.now();
        assertThrows(BadRequestException.class,
                () -> service.getHistory(today, today.minusDays(1)));
    }

    @Test
    @DisplayName("getHistory — range exceeds 7 days throws BadRequestException")
    void getHistory_rangeExceeds7Days_throwsBadRequestException() {
        LocalDate today = LocalDate.now();
        assertThrows(BadRequestException.class,
                () -> service.getHistory(today.minusDays(8), today));
    }

    @Test
    @DisplayName("getHistory — empty range returns zero plans")
    void getHistory_emptyRange_returnsZeroPlans() {
        LocalDate today = LocalDate.now();
        MealPlanHistoryResponse response =
                service.getHistory(today.minusDays(2), today.minusDays(1));

        assertNotNull(response);
        assertEquals(0, response.getTotal());
        assertTrue(response.getPlans().isEmpty());
    }

    /* ═══════════════════════════════════════════════
       Helpers
       ═══════════════════════════════════════════════ */

    private MealPlanRequest buildRequest() {
        MealPlanRequest req = new MealPlanRequest();
        req.setDays(1);
        req.setMealsPerDay(3);
        return req;
    }

    // FIXED: returns MealPlanDetailResponse instead of MealPlanResponse
    private MealPlanDetailResponse buildAiResponse(
            double cal, double protein, double carbs, double fat, String plan) {
        return MealPlanDetailResponse.builder()
                .plan(plan)
                .planDate(LocalDate.now())
                .totalCalories(cal)
                .totalProtein(protein)
                .totalCarbs(carbs)
                .totalFat(fat)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    /* ═══════════════════════════════════════════════
       Dynamic-proxy stubs (no Mockito)
       ═══════════════════════════════════════════════ */

    private MealPlanRepository createMealPlanRepoStub() {
        Map<Long, MealPlan> store = new LinkedHashMap<>();
        AtomicLong idGen = new AtomicLong(1);

        return (MealPlanRepository) Proxy.newProxyInstance(
                MealPlanRepository.class.getClassLoader(),
                new Class<?>[]{MealPlanRepository.class},
                (proxy, method, args) -> switch (method.getName()) {

                    case "save" -> {
                        MealPlan entity = (MealPlan) args[0];
                        if (entity.getId() == null) entity.setId(idGen.getAndIncrement());
                        store.put(entity.getId(), entity);
                        yield entity;
                    }

                    case "findById" -> Optional.ofNullable(store.get(args[0]));

                    case "existsByUserIdAndPlanDate" -> {
                        Long uid = (Long) args[0];
                        LocalDate date = (LocalDate) args[1];
                        yield store.values().stream()
                                .anyMatch(p -> p.getUserId().equals(uid)
                                        && p.getPlanDate().equals(date));
                    }

                    case "findByUserIdAndPlanDate" -> {
                        Long uid = (Long) args[0];
                        LocalDate date = (LocalDate) args[1];
                        yield store.values().stream()
                                .filter(p -> p.getUserId().equals(uid)
                                        && p.getPlanDate().equals(date))
                                .findFirst();
                    }

                    case "findByUserIdAndPlanDateBetweenOrderByPlanDateDesc" -> {
                        Long uid = (Long) args[0];
                        LocalDate from = (LocalDate) args[1];
                        LocalDate to = (LocalDate) args[2];
                        yield store.values().stream()
                                .filter(p -> p.getUserId().equals(uid))
                                .filter(p -> !p.getPlanDate().isBefore(from)
                                        && !p.getPlanDate().isAfter(to))
                                .sorted(Comparator.comparing(MealPlan::getPlanDate).reversed())
                                .toList();
                    }

                    default -> throw new UnsupportedOperationException(
                            "Stub: " + method.getName());
                });
    }

    private UserRepository createUserRepoStub() {
        Map<String, User> byEmail = new HashMap<>();
        byEmail.put(EMAIL, User.builder().id(USER_ID).email(EMAIL).build());

        return (UserRepository) Proxy.newProxyInstance(
                UserRepository.class.getClassLoader(),
                new Class<?>[]{UserRepository.class},
                (proxy, method, args) -> switch (method.getName()) {

                    case "findByEmail" -> Optional.ofNullable(byEmail.get(args[0]));

                    case "save" -> {
                        User u = (User) args[0];
                        byEmail.put(u.getEmail(), u);
                        yield u;
                    }

                    default -> throw new UnsupportedOperationException(
                            "Stub: " + method.getName());
                });
    }

    // FIXED: returns MealPlanDetailResponse, no more Targets
    private static class StubAiNutritionService implements AiNutritionService {

        private MealPlanDetailResponse nextResponse;

        void setNextResponse(MealPlanDetailResponse response) {
            this.nextResponse = response;
        }

        @Override
        public MealPlanDetailResponse generateMealPlan(MealPlanRequest request) {
            if (nextResponse == null) {
                throw new IllegalStateException("No stub response configured");
            }
            return nextResponse;
        }

        @Override
        public AiChatResponse chat(AiChatRequest request) {
            return null;
        }
    }
}