package com.nutriguideai.service.impl;

import com.nutriguideai.dto.request.FoodDiaryEntryRequest;
import com.nutriguideai.dto.response.DailyDiaryResponse;
import com.nutriguideai.dto.response.FoodDiaryEntryResponse;
import com.nutriguideai.entity.FoodDiaryEntry;
import com.nutriguideai.enums.MealType;
import com.nutriguideai.exception.BadRequestException;
import com.nutriguideai.exception.ResourceNotFoundException;
import com.nutriguideai.repository.FoodDiaryEntryRepository;
import com.nutriguideai.repository.FoodItemRepository;
import com.nutriguideai.repository.UserRepository;
import com.nutriguideai.entity.User;
import com.nutriguideai.entity.FoodItem;
import com.nutriguideai.enums.FoodCategory;
import org.junit.jupiter.api.*;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class FoodDiaryServiceImplTest {

    private static final String EMAIL = "test@example.com";
    private static final Long USER_ID = 1L;

    private FoodDiaryServiceImpl service;

    @BeforeEach
    void setUp() {
        FoodDiaryEntryRepository diaryRepo = createDiaryRepoStub();
        UserRepository userRepo = createUserRepoStub();
        FoodItemRepository foodRepo = createFoodItemRepoStub();

        service = new FoodDiaryServiceImpl(diaryRepo, foodRepo, userRepo);

        SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication
                        .UsernamePasswordAuthenticationToken(EMAIL, "pw", List.of()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ── logEntry() ──────────────────────────────

    @Test
    @DisplayName("logEntry — success with food catalog item auto-fills nutrition")
    void logEntry_withFoodId_autoFillsNutrition() {
        FoodDiaryEntryRequest req = FoodDiaryEntryRequest.builder()
                .foodId(10L)
                .mealType(MealType.LUNCH)
                .quantity(2.0)
                .build();

        FoodDiaryEntryResponse response = service.logEntry(req);

        assertNotNull(response);
        assertEquals("Grilled Chicken", response.getFoodName());
        assertEquals(MealType.LUNCH, response.getMealType());
        assertEquals(2.0, response.getQuantity());
        assertEquals(500.0, response.getCalories()); // 250 × 2
        assertEquals(60.0, response.getProteinG());  // 30 × 2
    }

    @Test
    @DisplayName("logEntry — custom food without foodId uses provided values")
    void logEntry_customFood_usesProvidedValues() {
        FoodDiaryEntryRequest req = FoodDiaryEntryRequest.builder()
                .foodName("Homemade Smoothie")
                .mealType(MealType.BREAKFAST)
                .servingSize("1 glass")
                .quantity(1.0)
                .calories(300.0)
                .proteinG(10.0)
                .carbsG(50.0)
                .fatG(5.0)
                .build();

        FoodDiaryEntryResponse response = service.logEntry(req);

        assertNotNull(response);
        assertEquals("Homemade Smoothie", response.getFoodName());
        assertEquals(300.0, response.getCalories());
    }

    @Test
    @DisplayName("logEntry — future date throws BadRequestException")
    void logEntry_futureDate_throwsBadRequest() {
        FoodDiaryEntryRequest req = FoodDiaryEntryRequest.builder()
                .foodName("Pizza")
                .mealType(MealType.DINNER)
                .servingSize("2 slices")
                .quantity(1.0)
                .loggedDate(LocalDate.now().plusDays(1))
                .build();

        assertThrows(BadRequestException.class, () -> service.logEntry(req));
    }

    @Test
    @DisplayName("logEntry — invalid foodId throws ResourceNotFoundException")
    void logEntry_invalidFoodId_throwsNotFound() {
        FoodDiaryEntryRequest req = FoodDiaryEntryRequest.builder()
                .foodId(999L)
                .mealType(MealType.SNACKS)
                .quantity(1.0)
                .build();

        assertThrows(ResourceNotFoundException.class, () -> service.logEntry(req));
    }

    // ── getEntriesByDate() ──────────────────────

    @Test
    @DisplayName("getEntriesByDate — returns entries with daily totals")
    void getEntriesByDate_withEntries_returnsTotals() {
        // Log two entries
        service.logEntry(FoodDiaryEntryRequest.builder()
                .foodId(10L).mealType(MealType.LUNCH).quantity(1.0).build());
        service.logEntry(FoodDiaryEntryRequest.builder()
                .foodName("Rice").mealType(MealType.DINNER)
                .servingSize("1 cup").quantity(1.0)
                .calories(200.0).proteinG(4.0).carbsG(45.0).fatG(0.5).build());

        DailyDiaryResponse response = service.getEntriesByDate(LocalDate.now());

        assertNotNull(response);
        assertEquals(2, response.getTotalEntries());
        assertEquals(450.0, response.getTotalCalories()); // 250 + 200
    }

    @Test
    @DisplayName("getEntriesByDate — no entries returns empty with zero totals")
    void getEntriesByDate_empty_returnsZeros() {
        DailyDiaryResponse response = service.getEntriesByDate(LocalDate.now().minusDays(10));

        assertNotNull(response);
        assertEquals(0, response.getTotalEntries());
        assertEquals(0.0, response.getTotalCalories());
    }

    // ── deleteEntry() ───────────────────────────

    @Test
    @DisplayName("deleteEntry — success removes entry")
    void deleteEntry_success() {
        FoodDiaryEntryResponse logged = service.logEntry(
                FoodDiaryEntryRequest.builder()
                        .foodName("Apple").mealType(MealType.SNACKS)
                        .servingSize("1 medium").quantity(1.0)
                        .calories(95.0).proteinG(0.5).carbsG(25.0).fatG(0.3)
                        .build());

        assertDoesNotThrow(() -> service.deleteEntry(logged.getId()));
    }

    @Test
    @DisplayName("deleteEntry — not found throws ResourceNotFoundException")
    void deleteEntry_notFound_throwsNotFound() {
        assertThrows(ResourceNotFoundException.class,
                () -> service.deleteEntry(999L));
    }

    // ═══════════════════════════════════════════
    // Dynamic-proxy stubs
    // ═══════════════════════════════════════════

    private FoodDiaryEntryRepository createDiaryRepoStub() {
        Map<Long, FoodDiaryEntry> store = new LinkedHashMap<>();
        AtomicLong idGen = new AtomicLong(1);

        return (FoodDiaryEntryRepository) Proxy.newProxyInstance(
                FoodDiaryEntryRepository.class.getClassLoader(),
                new Class<?>[]{FoodDiaryEntryRepository.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "save" -> {
                        FoodDiaryEntry e = (FoodDiaryEntry) args[0];
                        if (e.getId() == null) e.setId(idGen.getAndIncrement());
                        store.put(e.getId(), e);
                        yield e;
                    }
                    case "findById" -> Optional.ofNullable(store.get(args[0]));
                    case "deleteById" -> store.remove(args[0]);
                    case "findByUserIdAndLoggedDateOrderByCreatedAtDesc" -> {
                        Long uid = (Long) args[0];
                        LocalDate date = (LocalDate) args[1];
                        yield store.values().stream()
                                .filter(e -> e.getUserId().equals(uid)
                                        && e.getLoggedDate().equals(date))
                                .toList();
                    }
                    default -> throw new UnsupportedOperationException("Stub: " + method.getName());
                });
    }

    private FoodItemRepository createFoodItemRepoStub() {
        Map<Long, FoodItem> store = new HashMap<>();
        store.put(10L, FoodItem.builder()
                .id(10L).name("Grilled Chicken").category(FoodCategory.MEAT)
                .calories(250.0).protein(30.0).carbohydrates(0.0).fat(5.0)
                .servingSize("1 breast").vegetarian(false).build());

        return (FoodItemRepository) Proxy.newProxyInstance(
                FoodItemRepository.class.getClassLoader(),
                new Class<?>[]{FoodItemRepository.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "findById" -> Optional.ofNullable(store.get(args[0]));
                    default -> throw new UnsupportedOperationException("Stub: " + method.getName());
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
                    default -> throw new UnsupportedOperationException("Stub: " + method.getName());
                });
    }
}