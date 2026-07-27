# 09 — TESTING STRATEGY

| **Field**             | **Value**                                    |
| --------------------- | -------------------------------------------- |
| **Project Name**      | NutriGuide AI                                |
| **Document Title**    | Testing Strategy & Quality Assurance Plan    |
| **Version**           | 1.0                                          |
| **Author**            | Software Architecture Team                   |
| **Created On**        | 2026-07-27                                   |
| **Last Updated**      | 2026-07-27                                   |
| **Status**            | Draft                                        |
| **References**        | `01_PROJECT_CONTEXT.md`, `05_API_CONTRACT.md`, `07_TASKS.md`, `08_CODING_STANDARDS.md` |

---

## Table of Contents

1. [Testing Levels](#1-testing-levels)
2. [Unit Testing](#2-unit-testing)
3. [Integration Testing](#3-integration-testing)
4. [API Testing](#4-api-testing)
5. [UI Testing](#5-ui-testing)
6. [Manual Testing](#6-manual-testing)
7. [Test Cases](#7-test-cases)
8. [Bug Reporting Format](#8-bug-reporting-format)
9. [Regression Testing](#9-regression-testing)
10. [Performance Testing](#10-performance-testing)
11. [Security Testing](#11-security-testing)
12. [Definition of Done](#12-definition-of-done)

---

## 1. Testing Levels

### 1.1 Testing Pyramid

NutriGuide AI follows the standard testing pyramid approach:

```
                    ╱╲
                   ╱  ╲
                  ╱ UI ╲              ← E2E / UI Tests (5%)
                 ╱ Tests╲
                ╱────────╲
               ╱  API /   ╲           ← Integration / API Tests (20%)
              ╱ Integration╲
             ╱──────────────╲
            ╱   Unit Tests    ╲       ← Unit Tests (75%)
           ╱────────────────────╲
```

| **Level**          | **Scope**                        | **Tools**                | **Coverage Target** | **Run Frequency**       |
| ------------------ | -------------------------------- | ------------------------ | ------------------- | ----------------------- |
| Unit               | Individual classes, methods      | JUnit 5, Mockito         | ≥ 85% (service)     | Every commit            |
| Integration        | Service ↔ Repository, DB interactions | @SpringBootTest, H2  | ≥ 60%               | Every push              |
| API                | REST endpoints, request/response | MockMvc, Postman         | 100% of endpoints   | Every push              |
| UI / E2E           | User flows in browser            | Playwright / Selenium    | Critical paths      | Before release          |
| Manual             | Exploratory, usability, edge cases | Manual checklist       | Ad hoc              | Before release          |

### 1.2 Environment Strategy

| **Environment** | **Purpose**                  | **Database**    | **Data**              | **Who Runs**      |
| --------------- | ---------------------------- | --------------- | --------------------- | ----------------- |
| **Local**       | Development + Unit tests     | H2 (in-memory)  | Flyway migrations     | Developer         |
| **CI**          | Automated test suite         | H2 (in-memory)  | Flyway migrations     | GitHub Actions    |
| **Staging**     | Integration + Manual testing | MySQL 8.0       | Seed data + anonymized | QA Team          |
| **Production**  | Live verification            | MySQL 8.0       | Real user data         | Monitoring + alerts |

### 1.3 Traceability Matrix

| **Module**              | Unit Tests | Integration Tests | API Tests | UI Tests | Manual Tests |
| ----------------------- | ---------- | ----------------- | --------- | -------- | ------------ |
| Authentication          | ✅         | ✅                | ✅        | ✅       | ✅           |
| User Profile            | ✅         | ✅                | ✅        | ✅       | ✅           |
| Health Vitals           | ✅         | ✅                | ✅        | ✅       | ✅           |
| Goals & Preferences     | ✅         | ✅                | ✅        | ✅       | ✅           |
| Recommendation Engine   | ✅         | ✅                | —         | —        | ✅           |
| Meal Plans              | ✅         | ✅                | ✅        | ✅       | ✅           |
| Food Diary              | ✅         | ✅                | ✅        | ✅       | ✅           |
| Progress Dashboard      | ✅         | ✅                | ✅        | ✅       | ✅           |
| Food Catalog            | ✅         | ✅                | ✅        | —        | ✅           |
| Admin Panel             | ✅         | ✅                | ✅        | ✅       | ✅           |

---

## 2. Unit Testing

### 2.1 Scope

Unit tests verify the **smallest testable units** of the application in isolation. All external dependencies (repositories, other services, AI adapter) are mocked.

### 2.2 Coverage Targets

| **Layer**          | **Target** | **Measurement**                     | **Exclusions**                               |
| ------------------ | ---------- | ----------------------------------- | -------------------------------------------- |
| Service Layer      | ≥ 85%      | Line coverage + Branch coverage     | Generated DTOs, entity boilerplate           |
| Controller Layer   | ≥ 70%      | Line coverage                       | Only `@ExceptionHandler` methods excluded    |
| Utility Classes    | ≥ 80%      | Line coverage                       | Simple getters/setters                       |
| Recommendation Engine | ≥ 90%   | Branch coverage (critical rules)    | Complex scoring rules manually verified      |

### 2.3 Unit Test Standards

#### 2.3.1 Test Structure (AAA Pattern)

```java
@ExtendWith(MockitoExtension.class)
class MealPlanServiceTest {

    @Mock
    private MealPlanRepository mealPlanRepository;

    @Mock
    private RecommendationEngine recommendationEngine;

    @InjectMocks
    private MealPlanServiceImpl mealPlanService;

    @Test
    void generateMealPlan_ShouldReturnResponse_WhenValidRequest() {
        // ── Arrange ──
        Long userId = 1L;
        LocalDate planDate = LocalDate.of(2026, 7, 27);
        CreateMealPlanRequest request = new CreateMealPlanRequest(planDate, DietType.VEGETARIAN, BudgetLevel.MEDIUM);
        MealPlan expectedPlan = MealPlan.builder().planId(1L).planDate(planDate).totalCalories(1850).build();

        when(recommendationEngine.generate(anyLong(), any())).thenReturn(expectedPlan);
        when(mealPlanRepository.save(any())).thenReturn(expectedPlan);

        // ── Act ──
        MealPlanResponse response = mealPlanService.generateMealPlan(userId, request);

        // ── Assert ──
        assertThat(response).isNotNull();
        assertThat(response.planDate()).isEqualTo(planDate);
        assertThat(response.totalCalories()).isEqualTo(1850);
        verify(mealPlanRepository).save(any());
    }
}
```

#### 2.3.2 Test Naming Convention

```
{MethodName}_Should{ExpectedBehavior}_When{Condition}
```

| **Pattern**                          | **Example**                                                   |
| ------------------------------------ | ------------------------------------------------------------- |
| Happy path                           | `generateMealPlan_ShouldReturnResponse_WhenValidRequest()`    |
| Validation error                     | `generateMealPlan_ShouldThrowException_WhenInvalidDate()`     |
| Null input                           | `findById_ShouldThrowException_WhenIdIsNull()`                |
| Empty result                         | `findByEmail_ShouldReturnEmpty_WhenUserNotFound()`            |
| Boundary condition                   | `calculateCalories_ShouldReturnMax_WhenServingAtUpperLimit()` |
| Authorization failure                | `getUserProfile_ShouldThrowForbidden_WhenWrongUserId()`       |

#### 2.3.3 What to Test

| **Test Category**        | **Examples**                                            |
| ------------------------ | ------------------------------------------------------- |
| Business logic           | Calorie calculation, macro distribution, BMR formula    |
| Validation rules         | Date ranges, required fields, enum values               |
| Edge cases               | Empty inputs, boundary values, null fields              |
| Exception paths          | Resource not found, conflict, unauthorized access       |
| State transitions        | Plan status change (draft → generated → modified)       |
| Cache interactions       | Cache hit, cache miss, cache eviction                   |
| Mapper transforms        | DTO ↔ Entity field mapping accuracy                     |

#### 2.3.4 What NOT to Test

| **Do Not Test**              | **Why**                                                | **Instead**                              |
| ---------------------------- | ------------------------------------------------------ | ---------------------------------------- |
| Generated code (Lombok)      | Already tested by Lombok library                       | Trust the library                        |
| JPA repository derived queries | Spring Data JPA tested by framework                    | Integration test custom `@Query` methods |
| Simple getters/setters       | Boilerplate with zero logic                            | Focus on methods with branching          |
| Third-party library behavior | Mock external libraries, test your usage of them       | Trust the library + integration test     |
| Configuration classes        | Typically no business logic                            | Verify only if custom routing/logic      |

### 2.4 Unit Testing by Module

#### 2.4.1 Auth Service (12+ tests)

| **Test Case**                                       | **Scenarios**                                         |
| --------------------------------------------------- | ----------------------------------------------------- |
| `register_ShouldReturnToken_WhenValidInput()`        | Valid email, strong password, unique email            |
| `register_ShouldThrowException_WhenEmailExists()`    | Duplicate email → 409 Conflict                        |
| `register_ShouldThrowException_WhenWeakPassword()`   | Password < 8 chars, no uppercase, no number           |
| `login_ShouldReturnToken_WhenValidCredentials()`     | Correct email + password                              |
| `login_ShouldThrowException_WhenInvalidPassword()`   | Correct email, wrong password → 401                   |
| `login_ShouldThrowException_WhenUserInactive()`      | User deactivated → 401                                |
| `validateToken_ShouldReturnTrue_WhenValidToken()`     | Unexpired, correctly signed JWT                       |
| `validateToken_ShouldReturnFalse_WhenExpiredToken()`  | Token past expiry date                                |
| `validateToken_ShouldReturnFalse_WhenTamperedToken()` | Modified payload, wrong signature                     |

#### 2.4.2 Recommendation Engine (20+ tests)

| **Test Case**                                       | **Scenarios**                                         |
| --------------------------------------------------- | ----------------------------------------------------- |
| `calculateBMR_ShouldUseCorrectFormula_ForMale()`     | Male user → Mifflin-St Jeor male formula              |
| `calculateBMR_ShouldUseCorrectFormula_ForFemale()`   | Female user → Mifflin-St Jeor female formula          |
| `calculateTDEE_ShouldApplyMultiplier_ForActivity()`  | 5 activity levels → 5 different multipliers           |
| `calculateCalorieTarget_ShouldReturnDeficit_ForWeightLoss()` | Weight loss → -500 kcal deficit            |
| `calculateCalorieTarget_ShouldReturnSurplus_ForWeightGain()` | Weight gain → +300 kcal surplus           |
| `calculateMacroSplit_ShouldPrioritizeProtein_ForMuscleGain()` | Muscle gain → 35% protein                |
| `calculateMacroSplit_ShouldLimitCarbs_ForDiabetes()` | Diabetes → < 40% carbs, low sugar                     |
| `filterFoods_ShouldExclude_WhenAllergy()`            | Lactose allergy → exclude dairy items                 |
| `filterFoods_ShouldRespectDietType()`                | Vegetarian → exclude meat/fish                        |
| `scoreMeal_ShouldPreferBudgetFriendly_WhenLowBudget()`| Budget LOW → prefer low-cost ingredients              |
| `generateMealPlan_ShouldCoverAllMealSlots()`         | 3 meals + 2 snacks per day                            |
| `generateMealPlan_ShouldMeetCalorieTargetWithinRange()` | Total calories within ±10% of target               |
| `generateMealPlan_ShouldMeetMacroTargets()`          | Protein, carbs, fat within ±15% of targets            |

#### 2.4.3 Health Vitals Service (8+ tests)

| **Test Case**                                       | **Scenarios**                                         |
| --------------------------------------------------- | ----------------------------------------------------- |
| `saveVitals_ShouldCalculateBmi_FromHeightAndWeight()` | BMI = weight / (height²)                             |
| `saveVitals_ShouldValidateBloodPressureRange()`      | Sys < 60 or > 250 → validation error                  |
| `saveVitals_ShouldValidateBloodSugarRange()`         | Fasting sugar > 500 → validation error                |
| `saveVitals_ShouldAllowPartialUpdates()`             | Update BP only, keep existing sugar values            |
| `getConditions_ShouldReturnAll_ForUser()`            | 3 conditions → list of 3                              |
| `addCondition_ShouldThrow_WhenDuplicate()`           | Same condition already exists → 409                   |
| `addCondition_ShouldAllowMultipleConditions()`       | Add diabetes + hypertension → both saved               |

---

## 3. Integration Testing

### 3.1 Scope

Integration tests verify the interaction between **real components** — service with database, repository with real queries, and cross-module flows.

### 3.2 Stack

| **Component**         | **Test Technology**                  |
| --------------------- | ------------------------------------ |
| Database              | `@DataJpaTest` with H2 in-memory DB |
| Full Spring Context   | `@SpringBootTest` with `@TestContainers` (optional) |
| REST Layer            | `@WebMvcTest` with MockMvc          |
| Redis Cache           | `@TestContainers` for Redis (optional) |

### 3.3 @DataJpaTest Standards

```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
class MealPlanRepositoryTest {

    @Autowired
    private MealPlanRepository mealPlanRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = userRepository.save(User.builder()
            .email("test@example.com")
            .passwordHash("$2a$10$...")
            .role(Role.USER)
            .build());
    }

    @Test
    void findByUserIdAndPlanDate_ShouldReturnPlan_WhenExists() {
        // Arrange
        LocalDate today = LocalDate.now();
        mealPlanRepository.save(MealPlan.builder()
            .user(testUser)
            .planDate(today)
            .totalCalories(1850)
            .build());

        // Act
        Optional<MealPlan> found = mealPlanRepository
            .findByUserIdAndPlanDate(testUser.getUserId(), today);

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getTotalCalories()).isEqualTo(1850);
    }

    @Test
    void findByUserIdAndPlanDate_ShouldReturnEmpty_WhenNotFound() {
        Optional<MealPlan> found = mealPlanRepository
            .findByUserIdAndPlanDate(999L, LocalDate.now());
        assertThat(found).isEmpty();
    }
}
```

### 3.4 Integration Test Scenarios

| **Scenario**                               | **Module**            | **Verification**                                     |
| ------------------------------------------ | --------------------- | ---------------------------------------------------- |
| Full auth flow: register → login → access protected endpoint | Auth + User | JWT issued, endpoint returns 200, rejected without token |
| Create profile → add vitals → generate meal plan | Profile + Vitals + Engine | Meal plan respects health constraints           |
| Generate plan → log meals → view dashboard | Meal Plan + Diary + Dashboard | Charts show correct aggregated data         |
| Multiple concurrent plan generations       | Recommendation Engine | Thread-safe, no duplicate plans per user per day    |
| Redis cache: cache miss → DB query → cache subsequent read | Caching | Second read is faster, DB not queried again |
| Food catalog search with fulltext          | Food Catalog         | Partial match, category filter, pagination works    |

---

## 4. API Testing

### 4.1 API Test Strategy

Every REST endpoint must be tested at two levels:

1. **Automated (MockMvc):** Runs in CI pipeline, validates request/response contracts
2. **Manual (Postman):** Human-readable collection for exploratory and regression testing

### 4.2 MockMvc Test Template

```java
@WebMvcTest(MealPlanController.class)
class MealPlanControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MealPlanService mealPlanService;

    private String validToken;

    @BeforeEach
    void setUp() {
        validToken = "Bearer eyJhbGciOiJIUzI1NiIs..."; // Pre-generated test token
    }

    @Test
    void generateMealPlan_ShouldReturn201_WhenValidRequest() throws Exception {
        // Arrange
        String requestBody = """
            {
                "planDate": "2026-07-27",
                "dietType": "VEGETARIAN",
                "budgetLevel": "MEDIUM"
            }
            """;

        MealPlanResponse response = MealPlanResponse.builder()
            .planId(1L)
            .planDate(LocalDate.of(2026, 7, 27))
            .totalCalories(1850)
            .build();

        when(mealPlanService.generateMealPlan(anyLong(), any())).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/meal-plans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .header("Authorization", validToken))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("success"))
            .andExpect(jsonPath("$.data.totalCalories").value(1850))
            .andExpect(jsonPath("$.data.planId").value(1));
    }

    @Test
    void generateMealPlan_ShouldReturn400_WhenMissingFields() throws Exception {
        String invalidBody = """
            {
                "planDate": null,
                "dietType": "INVALID_TYPE"
            }
            """;

        mockMvc.perform(post("/api/v1/meal-plans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidBody)
                .header("Authorization", validToken))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    void generateMealPlan_ShouldReturn401_WhenNoToken() throws Exception {
        mockMvc.perform(post("/api/v1/meal-plans")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized());
    }
}
```

### 4.3 API Test Coverage Matrix

| **Endpoint**                       | **200** | **201** | **204** | **400** | **401** | **403** | **404** | **409** | **500** |
| ---------------------------------- | ------ | ------ | ------ | ------ | ------ | ------ | ------ | ------ | ------ |
| `POST /api/v1/auth/register`       | —      | ✅     | —      | ✅     | —      | —      | —      | ✅     | ✅     |
| `POST /api/v1/auth/login`          | ✅     | —      | —      | ✅     | ✅     | —      | —      | —      | ✅     |
| `GET /api/v1/auth/validate`        | ✅     | —      | —      | —      | ✅     | —      | —      | —      | —      |
| `GET /api/v1/profiles/{userId}`    | ✅     | —      | —      | ✅     | ✅     | ✅     | ✅     | —      | ✅     |
| `PUT /api/v1/profiles/{userId}`    | ✅     | —      | —      | ✅     | ✅     | ✅     | ✅     | —      | ✅     |
| `GET /api/v1/vitals/{userId}`      | ✅     | —      | —      | ✅     | ✅     | ✅     | ✅     | —      | ✅     |
| `PUT /api/v1/vitals/{userId}`      | ✅     | —      | —      | ✅     | ✅     | ✅     | ✅     | —      | ✅     |
| `POST /api/v1/meal-plans`          | —      | ✅     | —      | ✅     | ✅     | —      | —      | ✅     | ✅     |
| `GET /api/v1/meal-plans/{date}`    | ✅     | —      | —      | ✅     | ✅     | —      | ✅     | —      | ✅     |
| `GET /api/v1/meal-plans`           | ✅     | —      | —      | ✅     | ✅     | —      | —      | —      | ✅     |
| `POST /api/v1/diary`               | —      | ✅     | —      | ✅     | ✅     | —      | ✅     | —      | ✅     |
| `GET /api/v1/diary?date={date}`    | ✅     | —      | —      | ✅     | ✅     | —      | —      | —      | ✅     |
| `DELETE /api/v1/diary/{id}`        | —      | —      | ✅     | ✅     | ✅     | —      | ✅     | —      | ✅     |
| `GET /api/v1/progress/summary`     | ✅     | —      | —      | ✅     | ✅     | —      | —      | —      | ✅     |
| `GET /api/v1/progress/weight`      | ✅     | —      | —      | ✅     | ✅     | —      | —      | —      | ✅     |
| `GET /api/v1/foods?q={query}`      | ✅     | —      | —      | ✅     | ✅     | —      | —      | —      | ✅     |
| `POST /api/v1/admin/foods`         | ✅     | —      | —      | ✅     | ✅     | ✅     | —      | ✅     | ✅     |

### 4.4 Postman Collection Standards

```
postman/
├── NutriGuideAI.postman_collection.json     ← Master collection
├── environments/
│   ├── local.postman_environment.json       ← http://localhost:8080
│   ├── staging.postman_environment.json     ← https://api.staging.nutriguideai.com
│   └── production.postman_environment.json  ← https://api.nutriguideai.com
└── README.md                                ← How to import and use
```

Each Postman request must include:

- ✅ Pre-request script to auto-fetch JWT token for protected endpoints
- ✅ Tests for status code, response time, and JSON schema validation
- ✅ Environment variables for base URL, token, and common IDs
- ✅ Example responses stored for documentation

---

## 5. UI Testing

### 5.1 Scope

UI tests verify that the React frontend renders correctly, handles user interactions, and communicates properly with the backend.

### 5.2 UI Testing Layers

| **Layer**       | **Tool**         | **Scope**                           | **What It Tests**                     |
| --------------- | ---------------- | ----------------------------------- | ------------------------------------- |
| Component       | Vitest + React Testing Library | Individual components in isolation | Rendering, props, state, user events  |
| Page            | Vitest + MSW     | Full page with mocked API           | Data flow, loading/error/empty states |
| E2E             | Playwright       | Complete user journeys              | Auth flow, meal plan, dashboard       |

### 5.3 Component Test Template

```tsx
import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { MealPlanCard } from './MealPlanCard';

describe('MealPlanCard', () => {
    const defaultProps = {
        planId: 1,
        date: '2026-07-27',
        totalCalories: 1850,
        onViewDetails: vi.fn(),
    };

    it('renders meal date and calories', () => {
        render(<MealPlanCard {...defaultProps} />);
        expect(screen.getByText('2026-07-27')).toBeInTheDocument();
        expect(screen.getByText('1,850 kcal')).toBeInTheDocument();
    });

    it('calls onViewDetails when view button clicked', () => {
        const onViewDetails = vi.fn();
        render(<MealPlanCard {...defaultProps} onViewDetails={onViewDetails} />);
        fireEvent.click(screen.getByRole('button', { name: /view details/i }));
        expect(onViewDetails).toHaveBeenCalledWith(1);
    });

    it('shows loading skeleton when loading prop is true', () => {
        render(<MealPlanCard {...defaultProps} loading={true} />);
        expect(screen.getByTestId('skeleton-loader')).toBeInTheDocument();
    });

    it('shows error state when error prop is provided', () => {
        render(<MealPlanCard {...defaultProps} error="Failed to load" />);
        expect(screen.getByText(/failed to load/i)).toBeInTheDocument();
        expect(screen.getByRole('button', { name: /retry/i })).toBeInTheDocument();
    });
});
```

### 5.4 E2E Test Scenarios (Playwright)

| **Test Case**                            | **Steps**                                                                 | **Verification**                              |
| ---------------------------------------- | ------------------------------------------------------------------------- | --------------------------------------------- |
| **User Registration → Login**            | 1. Navigate to `/register`<br>2. Fill form<br>3. Submit<br>4. Login      | Redirected to dashboard, user name displayed  |
| **Complete Health Profile**              | 1. Navigate to `/profile`<br>2. Fill vitals<br>3. Set goals<br>4. Save   | Success toast, data persists on reload        |
| **Generate Meal Plan**                   | 1. Go to `/meal-plan`<br>2. Click "Generate"<br>3. Wait for result        | 5 meals displayed, total calories shown       |
| **Log Food in Diary**                    | 1. Go to `/diary`<br>2. Search food<br>3. Select serving<br>4. Save      | Entry appears in diary, daily summary updates |
| **View Progress Dashboard**              | 1. Go to `/progress`<br>2. View weight chart<br>3. Change time range     | Chart renders, data updates with range        |
| **Access Protected Route Without Auth**  | 1. Clear token<br>2. Navigate to `/dashboard`                             | Redirected to `/auth` with return URL         |

### 5.5 UI State Testing Checklist

Every data-fetching page must test 4 states:

```tsx
// ✅ Must test ALL 4 states:
describe('DashboardPage', () => {
    it('shows loading spinner while fetching data');       // 1. Loading
    it('renders charts and summary cards when data loaded'); // 2. Data
    it('shows error message with retry button on failure');  // 3. Error
    it('shows empty state when no data available');          // 4. Empty
});
```

---

## 6. Manual Testing

### 6.1 Manual Test Checklist

Manual testing is performed before every release on the staging environment.

#### 6.1.1 Authentication

- [ ] Register with valid email and password
- [ ] Register with existing email → error message shown
- [ ] Register with weak password → validation error shown
- [ ] Login with correct credentials → redirected to dashboard
- [ ] Login with wrong password → error message shown
- [ ] Access protected route → redirected to login
- [ ] Log out → redirected to landing page, token cleared
- [ ] Browser back/forward after login/logout → no auth leaks

#### 6.1.2 Health Profile

- [ ] Create profile with all fields → saved successfully
- [ ] Edit profile → changes persisted after page reload
- [ ] Enter invalid height (< 50 cm) → validation error
- [ ] Enter invalid weight (> 500 kg) → validation error
- [ ] BMI auto-calculated when height and weight entered
- [ ] Add medical conditions → appears in condition list
- [ ] Remove medical condition → removed from list

#### 6.1.3 Meal Plans

- [ ] Generate meal plan → 5 meals displayed (breakfast, lunch, dinner, 2 snacks)
- [ ] Regenerate meal plan → new plan replaces old one
- [ ] Generated plan respects diet type (vegetarian → no meat)
- [ ] Generated plan respects allergies (lactose → no dairy)
- [ ] Generated plan respects budget level
- [ ] View meal plan for previous date → shows correct plan
- [ ] Meal plan shows correct total calories and macros

#### 6.1.4 Food Diary

- [ ] Search for food item → results appear within 2 seconds
- [ ] Select food + portion → entry logged with correct nutrition
- [ ] Log multiple meals in one day → all appear in list
- [ ] Daily summary shows correct totals
- [ ] Delete entry → removed from list, totals updated
- [ ] Date picker shows diary for selected date

#### 6.1.5 Dashboard

- [ ] Weight trend chart renders with correct data
- [ ] Calorie bar chart shows actual vs target
- [ ] Macro doughnut chart shows protein/carbs/fat split
- [ ] Summary cards show today's data correctly
- [ ] Changing time range (7/30/90 days) updates charts
- [ ] Empty state when no progress data exists

#### 6.1.6 Admin Panel (if implemented)

- [ ] User list displays all registered users
- [ ] Deactivate user → user cannot log in
- [ ] System health shows green for all services
- [ ] Add food item → appears in catalog
- [ ] Edit food item → updated in catalog

#### 6.1.7 Responsive Design

- [ ] Desktop (1920×1080) → full layout, all elements visible
- [ ] Tablet (768×1024) → sidebar collapses, charts resize
- [ ] Mobile (375×667) → hamburger menu, stacked layout
- [ ] All forms usable on mobile (inputs not cut off)

#### 6.1.8 Error Handling

- [ ] Network error → friendly error message, retry button
- [ ] Server error (500) → friendly message, error logged
- [ ] Token expired → auto-redirect to login
- [ ] Invalid URL → 404 page with navigation link
- [ ] No results → empty state with action prompt

### 6.2 Cross-Browser Testing Matrix

| **Browser**       | **Version** | **OS**        | **Priority** |
| ----------------- | ----------- | ------------- | ------------ |
| Chrome            | Latest 2    | Windows, macOS, Linux | P0     |
| Firefox           | Latest 2    | Windows, macOS | P0           |
| Safari            | Latest 2    | macOS         | P1           |
| Edge              | Latest 2    | Windows       | P1           |
| Chrome (mobile)   | Latest 2    | Android       | P1           |
| Safari (mobile)   | Latest 2    | iOS           | P1           |

---

## 7. Test Cases

### 7.1 Test Case Template

Each test case follows this format:

```markdown
---
ID: TC-AUTH-001
Title: User registers with valid email and password
Module: Authentication
Priority: P0
Type: Functional
Preconditions:
  - User is not logged in
  - No account exists with this email
Test Data:
  - Email: "newuser@example.com"
  - Password: "SecurePass123!"
  - Name: "John Doe"
Steps:
  1. Navigate to /register
  2. Enter email "newuser@example.com"
  3. Enter password "SecurePass123!"
  4. Enter name "John Doe"
  5. Click "Register" button
Expected Result:
  - HTTP 201 Created returned
  - JWT token received
  - User redirected to onboarding page
  - Confirmation toast shown
Postconditions:
  - User record exists in database with role 'USER'
  - UserProfile record created
Postman Test:
  - POST /api/v1/auth/register → 201
```

### 7.2 Test Case Inventory

| **Module**              | **ID Prefix** | **Test Cases** | **P0** | **P1** | **P2** |
| ----------------------- | ------------- | -------------- | ----- | ------ | ------ |
| Authentication          | TC-AUTH       | 15             | 5     | 6      | 4      |
| User Profile            | TC-PROF       | 10             | 4     | 4      | 2      |
| Health Vitals           | TC-VIT        | 12             | 4     | 5      | 3      |
| Goals & Preferences     | TC-GOA        | 8              | 3     | 3      | 2      |
| Recommendation Engine   | TC-ENG        | 25             | 10    | 10     | 5      |
| Meal Plans              | TC-MPL        | 15             | 5     | 6      | 4      |
| Food Diary              | TC-DIA        | 12             | 4     | 5      | 3      |
| Progress Dashboard      | TC-PRO        | 10             | 3     | 4      | 3      |
| Food Catalog            | TC-FOO        | 8              | 2     | 4      | 2      |
| Admin Panel             | TC-ADM        | 10             | 3     | 4      | 3      |
| Security                | TC-SEC        | 10             | 5     | 3      | 2      |
| Performance             | TC-PER        | 6              | 3     | 2      | 1      |
| **Total**               |               | **141**        | **51**| **56** | **34** |

### 7.3 Priority Definitions

| **Priority** | **Definition**                        | **Required Before Release?** |
| ------------ | ------------------------------------- | ---------------------------- |
| **P0**       | Critical — blocks core functionality  | Yes — must pass              |
| **P1**       | High — important feature, non-blocking | Yes — should pass            |
| **P2**       | Medium — nice to have, edge cases     | Ideally — known issues documented |

### 7.4 Sample Test Cases

#### TC-AUTH-001: Successful Registration

| **Field**         | **Value**                                      |
| ----------------- | ---------------------------------------------- |
| **ID**            | TC-AUTH-001                                    |
| **Title**         | User registers with valid email and password   |
| **Module**        | Authentication                                 |
| **Priority**      | P0                                             |
| **Preconditions** | User is on /register page. No existing account |
| **Steps**         | 1. Enter email "john@example.com"<br>2. Enter password "SecurePass123!"<br>3. Enter name "John Doe"<br>4. Click "Register" |
| **Expected**      | 201 Created. JWT returned. Redirected to onboarding. |
| **Automated**     | `POST /api/v1/auth/register` → 201            |

#### TC-AUTH-005: Login with Invalid Password

| **Field**         | **Value**                                      |
| ----------------- | ---------------------------------------------- |
| **ID**            | TC-AUTH-005                                    |
| **Title**         | Login fails with incorrect password            |
| **Module**        | Authentication                                 |
| **Priority**      | P0                                             |
| **Preconditions** | User has registered account. On /login page.   |
| **Steps**         | 1. Enter email "john@example.com"<br>2. Enter password "WrongPassword1"<br>3. Click "Login" |
| **Expected**      | 401 Unauthorized. Error message "Invalid email or password". No redirect. |
| **Automated**     | `POST /api/v1/auth/login` → 401                |

#### TC-MPL-003: Meal Plan Respects Diet Type

| **Field**         | **Value**                                      |
| ----------------- | ---------------------------------------------- |
| **ID**            | TC-MPL-003                                     |
| **Title**         | Generated meal plan excludes non-vegetarian foods for vegetarian user |
| **Module**        | Meal Plans                                     |
| **Priority**      | P0                                             |
| **Preconditions** | User profile with diet_type=VEGETARIAN. Food catalog has both veg and non-veg items. |
| **Steps**         | 1. Send POST /api/v1/meal-plans with valid data<br>2. Inspect generated meals |
| **Expected**      | All meal items have diet_type containing VEGETARIAN. No meat/fish/seafood items. |
| **Automated**     | `MealPlanServiceTest.generateMealPlan_ShouldRespectDietType_ForVegetarian()` |

#### TC-SEC-002: Unauthenticated Access Blocked

| **Field**         | **Value**                                      |
| ----------------- | ---------------------------------------------- |
| **ID**            | TC-SEC-002                                     |
| **Title**         | Protected endpoints return 401 without JWT     |
| **Module**        | Security                                       |
| **Priority**      | P0                                             |
| **Preconditions** | No Authorization header in request             |
| **Steps**         | 1. Send GET /api/v1/profiles/1 without token<br>2. Send POST /api/v1/meal-plans without token<br>3. Send GET /api/v1/progress/summary without token |
| **Expected**      | All return 401 Unauthorized with error response |
| **Automated**     | `MealPlanControllerApiTest.generateMealPlan_ShouldReturn401_WhenNoToken()` |

---

## 8. Bug Reporting Format

### 8.1 Bug Report Template

```markdown
---
ID: BUG-042
Title: [Brief descriptive title]
Severity: [Critical / Major / Minor / Trivial]
Priority: [P0 / P1 / P2 / P3]
Module: [Affected module]
Environment: [Local / Staging / Production]
Browser/OS: [Chrome 126 / Windows 11]
Reported By: [Tester name]
Reported Date: 2026-07-27
Status: [Open / In Progress / Fixed / Verified / Closed]
---

## Description
[Clear, concise description of the bug]

## Steps to Reproduce
1. Go to ...
2. Click on ...
3. Scroll down to ...
4. See error

## Expected Behavior
[What should happen]

## Actual Behavior
[What actually happens — include error messages]

## Screenshots / Logs
[Attach screenshots, console logs, or network tab captures]

## Test Data
- Email: test@example.com
- Plan ID: 42
- Date: 2026-07-27

## Additional Context
- Frequency: Always / Intermittent / Rare
- First appeared: [Date]
- Related to: [BUG-041, PR #127]
```

### 8.2 Severity Definitions

| **Severity** | **Definition**                                               | **Example**                                        |
| ------------ | ------------------------------------------------------------ | -------------------------------------------------- |
| **Critical** | System crash, data loss, security breach, core feature broken | Cannot login, meal plan generation returns 500     |
| **Major**    | Feature not working as expected, significant workaround needed | Wrong calorie calculation, diet type ignored       |
| **Minor**    | Feature works but with minor issues                          | Misaligned button, typo in text                    |
| **Trivial**  | Cosmetic issue, low impact                                   | Font size slightly off, color shade incorrect      |

### 8.3 Bug Lifecycle

```
┌──────────┐    ┌──────────────┐    ┌───────────┐    ┌──────────┐    ┌──────────┐
│  New     │───►│  Triaged     │───►│  In       │───►│  Fixed   │───►│  Closed  │
│          │    │  (PM/dev)    │    │  Progress │    │          │    │          │
└──────────┘    └──────────────┘    └───────────┘    └──────────┘    └──────────┘
                                                           │
                                                           ▼
                                                    ┌──────────┐
                                                    │ Verified │
                                                    │ (QA)     │
                                                    └──────────┘
                                                           │
                                                    ┌──────────┐
                                                    │ Reopened │  ← If verification fails
                                                    └──────────┘
```

---

## 9. Regression Testing

### 9.1 Regression Strategy

| **Trigger**                      | **Scope**                      | **Time Budget** | **Automated** |
| -------------------------------- | ------------------------------ | --------------- | ------------- |
| Daily build                      | Smoke tests (50 core tests)    | 10 minutes      | ✅ 100%       |
| Feature branch merge             | Affected module + dependencies | 30 minutes      | ✅ 80%        |
| Release candidate (weekly)       | Full regression suite          | 2 hours         | ✅ 80%        |
| Production hotfix                | Critical path only             | 15 minutes      | ✅ 100%       |

### 9.2 Regression Test Selection

```markdown
Rules for selecting regression tests:
1. ALL P0 (critical) test cases — always run
2. ALL tests in the modified module — always run
3. Tests in dependent modules — run if interfaces changed
4. Tests for previously fixed bugs — run if related code changed
5. Last 10 high-severity bug regression tests — always run
```

### 9.3 Regression Test Suite

| **Suite**           | **Test Count** | **Duration** | **When**            |
| ------------------- | -------------- | ------------ | ------------------- |
| Smoke Suite         | 50             | ~10 min      | Every build         |
| Critical Path Suite | 120            | ~30 min      | Every PR merge      |
| Full Regression     | 350+           | ~2 hours     | Before release      |

---

## 10. Performance Testing

### 10.1 Performance Requirements

| **Metric**                     | **Target**           | **Measurement**          |
| ------------------------------ | -------------------- | ------------------------ |
| API response time (p95)        | < 500 ms             | JMeter / k6             |
| Meal plan generation time      | < 3 seconds          | Custom timer in service  |
| Database query time (p95)      | < 100 ms             | Hibernate statistics     |
| Frontend page load time        | < 2 seconds          | Lighthouse               |
| API throughput                  | 500 req/s            | JMeter / k6              |
| Concurrent users               | 100 simultaneous     | JMeter / k6              |
| First meaningful paint         | < 1.5 seconds        | Lighthouse               |
| Time to interactive            | < 3 seconds          | Lighthouse               |

### 10.2 Load Test Scenarios

| **Scenario**                | **Virtual Users** | **Duration** | **Ramp-Up** | **Endpoints Targeted**            |
| --------------------------- | ----------------- | ------------ | ----------- | --------------------------------- |
| User login burst            | 50 → 200          | 5 minutes    | 30 seconds  | `POST /auth/login`                |
| Meal plan generation        | 10 → 50           | 5 minutes    | 1 minute    | `POST /meal-plans`                |
| Dashboard queries           | 50 → 200          | 5 minutes    | 30 seconds  | `GET /progress/summary`           |
| Food diary logging          | 50 → 100          | 5 minutes    | 30 seconds  | `POST /diary`                     |
| Food catalog search         | 50 → 200          | 5 minutes    | 30 seconds  | `GET /foods?q={query}`            |
| Combined user activity      | 100               | 10 minutes   | 2 minutes   | Mix of all endpoints              |
| Endurance (soak test)       | 50                | 1 hour       | 5 minutes   | Mix of read + write endpoints     |

### 10.3 Performance Test Tools

| **Tool**     | **Purpose**                     | **Configuration**                          |
| ------------ | ------------------------------- | ------------------------------------------ |
| **JMeter**   | Load testing, throughput        | Thread group, HTTP requests, assertions    |
| **k6**       | Lightweight load testing (CI)   | JavaScript test scripts, metrics output    |
| **Lighthouse** | Frontend performance audit    | Built into Chrome DevTools                 |
| **Spring Actuator** | Backend metrics           | `/actuator/metrics`, `/actuator/health`    |

### 10.4 Performance Test Thresholds

| **Threshold**              | **Warning**          | **Critical**         |
| -------------------------- | -------------------- | -------------------- |
| API p95 response time      | > 300 ms             | > 500 ms             |
| Error rate                 | > 1%                 | > 5%                 |
| CPU usage                  | > 70%                | > 90%                |
| Memory usage               | > 75%                | > 90%                |
| Database connection pool   | > 70% utilization    | > 90% utilization    |
| Active threads             | > 80% of max         | > 95% of max         |

---

## 11. Security Testing

### 11.1 Security Test Areas

| **Area**                  | **What We Test**                                       | **Tools**                          |
| ------------------------- | ------------------------------------------------------ | ---------------------------------- |
| Authentication            | JWT expiry, token tampering, brute force, session management | OWASP ZAP, manual review   |
| Authorization             | Role-based access, horizontal privilege escalation     | Manual penetration test            |
| Input Validation          | SQL injection, XSS, command injection                  | OWASP ZAP, manual review           |
| Data Protection           | Password storage, sensitive data in logs, HTTPS        | Code review, dependency check      |
| API Security              | Rate limiting, CORS configuration, HTTP methods        | OWASP ZAP, manual review           |
| Dependency Vulnerabilities| Known CVEs in libraries                                | OWASP Dependency-Check, Snyk       |

### 11.2 Security Test Cases

| **ID**      | **Test Case**                                     | **Expected**                                |
| ----------- | ------------------------------------------------- | ------------------------------------------- |
| SEC-001     | SQL injection on login email field                | Query fails safely, no data exposed         |
| SEC-002     | XSS in user profile name field                   | Script tags escaped, not executed           |
| SEC-003     | JWT token with forged signature                   | 401 Unauthorized, not decoded               |
| SEC-004     | JWT token with expired date                       | 401 Unauthorized, token rejected            |
| SEC-005     | User A accessing User B's profile via ID          | 403 Forbidden, access denied                |
| SEC-006     | Non-admin accessing admin endpoints               | 403 Forbidden, not authorized               |
| SEC-007     | Brute force login (100 attempts in 1 minute)      | 429 Too Many Requests after 5 failures      |
| SEC-008     | Password stored in logs                           | Password masked, never logged               |
| SEC-009     | CORS request from unauthorized origin             | Blocked by CORS policy                      |
| SEC-010     | Sensitive data in URL query parameters            | No tokens, passwords, or PII in URLs        |

### 11.3 OWASP Top 10 Mitigation Verification

| **OWASP Risk**                            | **Test**                                  | **Verification**                             |
| ----------------------------------------- | ----------------------------------------- | -------------------------------------------- |
| A01: Broken Access Control                | Try accessing other user's data           | Resource ownership check enforced            |
| A02: Cryptographic Failures               | Verify BCrypt strength, HTTPS, JWT signing | passwords hashed, TLS 1.2+                  |
| A03: Injection                            | SQLi tests on all input fields            | Parameterized queries in all repositories    |
| A04: Insecure Design                      | Architecture review                       | Layered security, rate limiting              |
| A05: Security Misconfiguration            | Check default creds, debug endpoints      | All defaults overridden, debug mode disabled |
| A06: Vulnerable Components                | Dependency scan                           | Zero critical/known CVEs                     |
| A07: Identification & Auth Failures       | Test session management, password policy  | JWT expiry enforced, password complexity     |
| A08: Software & Data Integrity Failures   | Verify CI/CD pipeline integrity           | No unsigned dependencies                     |
| A09: Security Logging & Monitoring        | Check log content                         | Auth failures logged, no sensitive data      |
| A10: Server-Side Request Forgery (SSRF)   | Test external URL inputs                  | No raw external URL fetching                 |

---

## 12. Definition of Done

### 12.1 Feature Done Checklist

A feature is **complete** only when ALL of the following are true:

```markdown
## Definition of Done — Feature
- [ ] Business requirement implemented and verified
- [ ] Backend code written following coding standards
- [ ] Frontend code written following coding standards
- [ ] All unit tests pass (≥ 85% service coverage)
- [ ] Integration tests pass (≥ 60% coverage)
- [ ] API endpoint tested via Postman/MockMvc
- [ ] API response envelope matches contract
- [ ] Validation working for all input fields
- [ ] Error states handled and tested
- [ ] Loading states implemented on frontend
- [ ] Empty states implemented on frontend
- [ ] Swagger documentation updated
- [ ] Logging added at appropriate levels
- [ ] Security review passed (authentication, authorization, input sanitization)
- [ ] Postman collection updated
- [ ] Manual testing completed on staging
- [ ] No P0/P1 bugs open
- [ ] Code reviewed and approved
- [ ] Merged to develop branch
```

### 12.2 Sprint Done Checklist

```markdown
## Definition of Done — Sprint
- [ ] All committed user stories meet Feature DoD
- [ ] All automated tests pass in CI pipeline
- [ ] Test coverage ≥ targets (service: 85%, controller: 70%)
- [ ] No critical or high-severity bugs open
- [ ] Postman collection updated with any new endpoints
- [ ] API documentation (Swagger) regenerated
- [ ] Database migration scripts versioned and tested
- [ ] Performance tests show no regression
- [ ] Security scan completed (no new critical CVEs)
- [ ] Release notes drafted
- [ ] Demo-ready (if applicable)
```

### 12.3 Release Done Checklist

```markdown
## Definition of Done — Release v1.0
- [ ] All Sprint DoD items complete
- [ ] Full regression suite passed
- [ ] Manual smoke test on staging passed
- [ ] Performance benchmarks met (p95 API < 500ms)
- [ ] Security audit completed
- [ ] All known P0/P1 bugs resolved
- [ ] Database migrations tested rollback
- [ ] Docker images built and pushed
- [ ] CI/CD pipeline green on release branch
- [ ] Deployment to production successful
- [ ] Smoke tests run on production
- [ ] Monitoring and alerting configured
- [ ] Rollback plan documented
- [ ] Release notes published
```

### 12.4 Quality Gates

| **Gate**           | **Criteria**                                      | **Blocking** |
| ------------------ | ------------------------------------------------- | ------------ |
| Code Review        | ≥ 1 approval, no unresolved comments              | Yes          |
| CI Build           | Compilation + tests pass                          | Yes          |
| Test Coverage      | Service ≥ 85%, Controller ≥ 70%                   | Yes          |
| Security Scan      | No critical CVEs                                  | Yes          |
| Performance        | p95 < 500ms, error rate < 1%                      | Warning      |
| Manual Testing     | All P0/P1 test cases pass on staging              | Yes          |

---

## Document Version History

| **Version** | **Date**     | **Author** | **Changes**          |
| ----------- | ------------ | ---------- | -------------------- |
| 1.0         | 2026-07-27   | Architect  | Initial draft        |

---

*End of Document — 09_TESTING.md*
