# 07 — TASKS

| **Field**             | **Value**                                    |
| --------------------- | -------------------------------------------- |
| **Project Name**      | NutriGuide AI                                |
| **Document Title**    | Task Breakdown & Development Plan            |
| **Version**           | 1.0                                          |
| **Author**            | Software Architecture Team                   |
| **Created On**        | 2026-07-27                                   |
| **Last Updated**      | 2026-07-27                                   |
| **Total Tasks**       | 49                                           |
| **Total Estimated Hours**| ~359 hours                                |
| **Phases**            | 6                                            |
| **Status**            | Draft                                        |

---

## Table of Contents

1. [Introduction](#1-introduction)
2. [Development Phases Overview](#2-development-phases-overview)
3. [Phase 1: Project Foundation](#3-phase-1-project-foundation)
4. [Phase 2: Backend Core](#4-phase-2-backend-core)
5. [Phase 3: Frontend Core](#5-phase-3-frontend-core)
6. [Phase 4: Integration & Features](#6-phase-4-integration--features)
7. [Phase 5: Quality Assurance](#7-phase-5-quality-assurance)
8. [Phase 6: Deployment & Delivery](#8-phase-6-deployment--delivery)
9. [Dependency Graph](#9-dependency-graph)
10. [Effort Summary](#10-effort-summary)

---

## 1. Introduction

### 1.1 Purpose

This document defines the complete task breakdown for the NutriGuide AI project v1.0. All tasks are arranged in development order across 6 phases. Each task includes its identifier, module, priority, estimated hours, dependencies, deliverables, and definition of done.

### 1.2 Priority Definitions

| **Priority** | **Meaning**                                                  |
| ------------ | ------------------------------------------------------------ |
| **P0**       | Critical — must be completed first; blocks all other work    |
| **P1**       | High — core feature; blocks subsequent related features      |
| **P2**       | Medium — important but can follow core implementation        |
| **P3**       | Low — nice to have; can be deferred if time-constrained      |

### 1.3 Development Order Strategy

Each module is built in the following order:

```
Backend always before Frontend for the same module
   │
   ├── Entity → Repository → DTO → Mapper → Service → Controller → Test
   │
   └── API Client → Page → Components → Custom Hooks → State Management
```

---

## 2. Development Phases Overview

| **Phase** | **Name**              | **Tasks** | **Hours** | **Focus**                                    |
| --------- | --------------------- | --------- | --------- | -------------------------------------------- |
| 1         | Project Foundation    | 6         | 40        | Setup, configuration, database, shared code  |
| 2         | Backend Core          | 10        | 106       | All backend modules Auth → Admin             |
| 3         | Frontend Core         | 10        | 108       | All frontend pages Auth → Admin              |
| 4         | Integration & Features| 5         | 30        | Auth flow, caching, Axios interceptor        |
| 5         | Quality Assurance     | 5         | 40        | Testing, security, performance               |
| 6         | Deployment & Delivery | 5         | 35        | Docker, CI/CD, deployment, documentation     |
| **Total** |                      | **49**    | **359**   |                                               |

---

## 3. Phase 1: Project Foundation

**Goal**: Set up the entire development environment, project structure, shared configurations, and database schema. No business logic is implemented in this phase.

---

### TASK-001: Backend Project Scaffolding

| **Field**        | **Detail**                                                  |
| ---------------- | ----------------------------------------------------------- |
| **Module**       | Infrastructure                                              |
| **Priority**     | P0 — Critical                                               |
| **Est. Hours**   | 6                                                           |
| **Dependencies** | None                                                        |
| **Branch**       | `feature/project-setup`                                     |

**Deliverables:**
- Spring Boot 3 project with Maven (`pom.xml`)
- Java 21 configuration
- Application entry point: `NutriGuideAiApplication.java`
- `application.yml` with MySQL, JPA, and security configurations
- `application-dev.yml` and `application-prod.yml` profiles
- `com.nutriguideai` package structure (all empty packages)
- `pom.xml` with all dependencies: Spring Boot Starter Web, JPA, Security, Validation, Lombok, MapStruct, jjwt, MySQL Connector, Flyway, Spring Doc OpenAPI, JUnit 5, Mockito, Spring Data Redis
- `.gitignore` for Java/Maven/IDE

> **Note:** Redis (via `Spring Data Redis`) is optional. If Redis is unavailable in your local development environment, the application will operate without caching. All caching-dependent features degrade gracefully to direct database queries.

**Definition of Done:**
- [ ] `mvn clean compile` succeeds with zero errors
- [ ] `mvn test` runs and passes (default Spring Boot test)
- [ ] Application starts on port 8080 without crashes
- [ ] All required packages exist under `com.nutriguideai`
- [ ] All pom.xml dependencies resolve without conflicts

---

### TASK-002: Frontend Project Scaffolding

| **Field**        | **Detail**                                                  |
| ---------------- | ----------------------------------------------------------- |
| **Module**       | Infrastructure                                              |
| **Priority**     | P0 — Critical                                               |
| **Est. Hours**   | 6                                                           |
| **Dependencies** | None                                                        |
| **Branch**       | `feature/project-setup`                                     |

**Deliverables:**
- React 18 project with Vite (or Create React App)
- Tailwind CSS 3 configuration with custom theme tokens (colors, spacing, typography)
- React Router 6 setup with all route placeholders
- Axios instance with base URL and JWT interceptor
- Folder structure: `api/`, `components/common/`, `components/auth/`, `components/dashboard/`, `components/meal-plan/`, `components/profile/`, `pages/`, `context/`, `hooks/`, `utils/`
- ESLint and Prettier configuration
- Basic `App.jsx` with router setup
- `AuthContext.jsx` placeholder with provider
- `ProtectedRoute.jsx` and `AdminRoute.jsx` components
- Custom `.gitignore` for React

**Definition of Done:**
- [ ] `npm run dev` starts without errors
- [ ] `npm run build` completes without warnings
- [ ] All empty page components render at their routes
- [ ] Tailwind styles apply correctly
- [ ] Axios instance is configured and exportable

---

### TASK-003: Database Schema — Flyway Migrations

| **Field**        | **Detail**                                                  |
| ---------------- | ----------------------------------------------------------- |
| **Module**       | Database                                                    |
| **Priority**     | P0 — Critical                                               |
| **Est. Hours**   | 8                                                           |
| **Dependencies** | TASK-001 (backend scaffolding)                              |
| **Branch**       | `feature/database-schema`                                   |

**Deliverables:**
- `V1__create_users_table.sql`
- `V2__create_user_profiles_table.sql`
- `V3__create_health_vitals_table.sql`
- `V4__create_user_conditions_table.sql`
- `V5__create_user_goals_table.sql`
- `V6__create_food_preferences_table.sql`
- `V7__create_meal_plans_table.sql`
- `V8__create_meal_plan_items_table.sql`
- `V9__create_food_items_table.sql`
- `V10__create_food_diary_entries_table.sql`
- `V11__create_progress_tracking_table.sql`
- Seed data migration: `V12__seed_food_catalog.sql` (50+ food items)

All migrations include:
- Proper column types and sizes (per `04_DATABASE.md`)
- Primary keys, foreign keys, unique constraints, check constraints
- Indexes on foreign keys and frequently queried columns
- Engine: InnoDB, Charset: utf8mb4, Collation: utf8mb4_unicode_ci

**Definition of Done:**
- [ ] `mvn flyway:migrate` runs successfully against local MySQL
- [ ] All 11 tables created with correct columns and types
- [ ] All foreign keys and constraints verified via `DESCRIBE` and `SHOW CREATE TABLE`
- [ ] Seed data inserts without errors
- [ ] Rollback scenario tested (schema can be recreated from scratch)

---

### TASK-004: Backend Shared Infrastructure

| **Field**        | **Detail**                                                  |
| ---------------- | ----------------------------------------------------------- |
| **Module**       | Infrastructure                                              |
| **Priority**     | P0 — Critical                                               |
| **Est. Hours**   | 8                                                           |
| **Dependencies** | TASK-001                                                    |
| **Branch**       | `feature/shared-infrastructure`                             |

**Deliverables:**
- **Enums:** `Role.java`, `GoalType.java`, `DietType.java`, `ActivityLevel.java`, `BudgetLevel.java`, `MealType.java`, `MedicalCondition.java`
- **Exceptions:** `GlobalExceptionHandler.java` (`@RestControllerAdvice`), `ResourceNotFoundException.java`, `UnauthorizedException.java`, `BadRequestException.java`, `RecommendationException.java`
- **Standard Response Envelope:** `ApiResponse.java` (generic wrapper), `ApiErrorResponse.java`
- **Config classes:** `SecurityConfig.java` (Spring Security + CORS), `CorsConfig.java`, `OpenApiConfig.java` (Swagger), `RedisConfig.java` (conditional — only loaded if Redis is available via `@ConditionalOnProperty`)
- **Security:** `JwtTokenProvider.java`, `JwtAuthenticationFilter.java`, `CustomUserDetailsService.java`, `SecurityConstants.java`
- **Utilities:** `BmiCalculator.java`, `CalorieCalculator.java` (Harris-Benedict equation), `ValidationUtils.java`
- **All entity classes** (`@Entity` annotations, relationships, `@Table` names)

**Definition of Done:**
- [ ] All enums compile with correct values
- [ ] `GlobalExceptionHandler` catches all custom exceptions and returns proper error JSON
- [ ] JwtTokenProvider generates and validates tokens correctly
- [ ] SecurityConfig permits public endpoints and secures authenticated endpoints
- [ ] Swagger UI accessible at `/swagger-ui.html`
- [ ] `BmiCalculator.calculate(weight, height)` returns correct values
- [ ] All entities have proper JPA annotations (relationships, cascade, fetch types)
- [ ] `mvn compile` succeeds

---

### TASK-005: Frontend Shared Infrastructure

| **Field**        | **Detail**                                                  |
| ---------------- | ----------------------------------------------------------- |
| **Module**       | Infrastructure                                              |
| **Priority**     | P0 — Critical                                               |
| **Est. Hours**   | 6                                                           |
| **Dependencies** | TASK-002                                                    |
| **Branch**       | `feature/shared-frontend`                                   |

**Deliverables:**
- **Reusable Components:**
    - `Navbar.jsx` — App logo, page title, user avatar dropdown
    - `Sidebar.jsx` — Navigation links with active state, collapsible on mobile
    - `Footer.jsx` — Simple footer with links
    - `LoadingSpinner.jsx` — Configurable size and color
    - `SkeletonLoader.jsx` — Card/table/chart/text variants
    - `ErrorState.jsx` — Error message with retry button
    - `EmptyState.jsx` — Icon, title, description, CTA button
    - `ProtectedRoute.jsx` — Redirect to login if not authenticated
    - `Modal.jsx` — Overlay modal with close button
    - `Toast.jsx` — Success/error/info notification
    - `PageHeader.jsx` — Title + subtitle + action buttons
    - `Card.jsx` — Generic container card
    - `SummaryCard.jsx` — Dashboard metric card with trend
    - `ProgressBar.jsx` — Horizontal progress bar with color variants
    - `Badge.jsx` — Status badge
    - `Button.jsx` — Primary/secondary/ghost/danger variants
    - `AppShell.jsx` — Layout wrapper (sidebar + navbar + content area)
- **`AuthContext.jsx`** — Full implementation (login, logout, token check, user state)
- **`useAuth.js`** — Custom hook for auth operations
- **`constants.js`** — All enum values matching backend enums
- **`helpers.js`** — Formatting utilities (dates, calories, macros)

**Definition of Done:**
- [ ] All components render correctly in isolation
- [ ] `AppShell` shows sidebar on desktop, hamburger on mobile
- [ ] `AuthContext` persists token in localStorage and provides to all children
- [ ] `ProtectedRoute` redirects to `/login` when no token
- [ ] `ErrorState` shows error messages and retry works
- [ ] `EmptyState` renders with icon, text, and button
- [ ] All visual states (loading, empty, error, success) demonstrated

---

### TASK-006: Docker Compose — Local Development

| **Field**        | **Detail**                                                  |
| ---------------- | ----------------------------------------------------------- |
| **Module**       | Infrastructure                                              |
| **Priority**     | P1 — High                                                   |
| **Est. Hours**   | 6                                                           |
| **Dependencies** | TASK-001, TASK-002, TASK-003                                |
| **Branch**       | `feature/docker-setup`                                      |

**Deliverables:**
- `docker-compose.yml` with services: MySQL 8.0, Redis 7.x *(optional)*, Backend, Frontend
- `backend/Dockerfile` (multi-stage: build with JDK, runtime with JRE)
- `frontend/Dockerfile` (build with Node, serve with nginx)
- `docker/nginx/default.conf` (reverse proxy configuration)
- `docker/mysql/init.sql` (basic database initialization)
- Docker `.dockerignore` files for backend and frontend

**Definition of Done:**
- [ ] `docker-compose up --build` starts all 4 services without errors
- [ ] Backend API accessible at `http://localhost:8080`
- [ ] Frontend accessible at `http://localhost:80`
- [ ] MySQL initializes with correct database
- [ ] Redis responds to ping
- [ ] Backend connects to MySQL and Redis containers

---

## 4. Phase 2: Backend Core

**Goal**: Implement all backend modules in dependency order. Each module includes Entity → Repository → DTO → Mapper → Service → Controller → Unit Tests.

---

### TASK-007: Authentication Module — Backend

| **Field**        | **Detail**                                                  |
| ---------------- | ----------------------------------------------------------- |
| **Module**       | Authentication                                              |
| **Priority**     | P0 — Critical                                               |
| **Est. Hours**   | 12                                                          |
| **Dependencies** | TASK-004 (shared infra, entities)                           |
| **Branch**       | `feature/auth-backend`                                      |

**Deliverables:**
- `AuthController.java` — `POST /register`, `POST /login`, `GET /validate`
- `AuthService.java` with `AuthServiceImpl.java`
- `RegisterRequest.java`, `LoginRequest.java` (with `@Valid` annotations)
- `AuthResponse.java`
- `UserRepository.java`
- Password encoding via BCrypt in `SecurityConfig`
- JWT token generation in `AuthService`
- Rate limiting on login endpoint (10 req/min)
- Validation: email format, password strength, unique email
- Swagger annotations on all endpoints

**Unit Tests:**
- `AuthServiceTest.java` — register success, duplicate email, invalid password
- `AuthControllerTest.java` — 201 on register, 409 on duplicate, 401 on wrong password

**Definition of Done:**
- [ ] `POST /api/v1/auth/register` creates user and returns JWT
- [ ] `POST /api/v1/auth/login` returns JWT for valid credentials
- [ ] `GET /api/v1/auth/validate` returns user info for valid token
- [ ] Duplicate email returns 409
- [ ] Weak password returns 400 with validation message
- [ ] All unit tests pass (≥ 90% coverage on AuthService)
- [ ] Swagger documentation visible for all 3 endpoints

---

### TASK-008: User Profile Module — Backend

| **Field**        | **Detail**                                                  |
| ---------------- | ----------------------------------------------------------- |
| **Module**       | User Profile                                                |
| **Priority**     | P0 — Critical                                               |
| **Est. Hours**   | 8                                                           |
| **Dependencies** | TASK-007 (auth must exist for authenticated endpoints)      |
| **Branch**       | `feature/profile-backend`                                   |

**Deliverables:**
- `UserController.java` — `GET /profile`, `PUT /profile`
- `UserService.java` with `UserServiceImpl.java`
- `UserProfileRepository.java`
- `UserProfileRequest.java`, `UserProfileResponse.java`
- `UserMapper.java` (MapStruct interface)
- BMI auto-calculation on profile save
- Upsert logic (create if not exists, update if exists)
- Validation: height 50–300cm, weight 10–500kg, valid gender enum

**Unit Tests:**
- Profile creation, update, get, not-found scenarios
- BMI calculation validation

**Definition of Done:**
- [ ] `GET /api/v1/users/profile` returns 404 if not created, 200 with data if exists
- [ ] `PUT /api/v1/users/profile` creates new or updates existing
- [ ] BMI is correctly calculated from height and weight
- [ ] Invalid height/weight returns 400
- [ ] Only authenticated users can access
- [ ] Unit tests pass

---

### TASK-009: Health Vitals Module — Backend

| **Field**        | **Detail**                                                  |
| ---------------- | ----------------------------------------------------------- |
| **Module**       | Health Vitals                                               |
| **Priority**     | P1 — High                                                   |
| **Est. Hours**   | 10                                                          |
| **Dependencies** | TASK-007 (auth)                                             |
| **Branch**       | `feature/health-backend`                                    |

**Deliverables:**
- `HealthVitalsController.java` — `GET /vitals`, `PUT /vitals`, `GET /conditions`, `POST /conditions`, `DELETE /conditions/{id}`
- `HealthVitalsService.java` with `HealthVitalsServiceImpl.java`
- `HealthVitalsRepository.java`, `UserConditionRepository.java`
- `HealthVitalsRequest.java`, `HealthVitalsResponse.java`
- `AddConditionRequest.java`, `ConditionResponse.java`
- `HealthVitalsMapper.java`
- Clinically valid range validation on all vitals fields
- Duplicate condition prevention (unique constraint on user_id + condition)
- Soft-enforced: all vitals fields are optional

**Unit Tests:**
- Health vitals CRUD, condition add/remove, duplicate prevention
- Range validation for BP, sugar, HbA1c, cholesterol

**Definition of Done:**
- [ ] All 5 endpoints work correctly
- [ ] Vitals upsert preserves existing values when partial update sent
- [ ] Adding same condition twice returns 409
- [ ] Deleting condition returns 204
- [ ] Ranges enforced: systolic 60–250, HbA1c 2.0–20.0, etc.
- [ ] Unit tests pass

---

### TASK-010: Goals & Preferences Module — Backend

| **Field**        | **Detail**                                                  |
| ---------------- | ----------------------------------------------------------- |
| **Module**       | Goals & Preferences                                         |
| **Priority**     | P1 — High                                                   |
| **Est. Hours**   | 8                                                           |
| **Dependencies** | TASK-007 (auth)                                             |
| **Branch**       | `feature/goals-preferences-backend`                         |

**Deliverables:**
- `UserGoalController.java` — `GET /goals`, `PUT /goals`
- `FoodPreferenceController.java` — `GET /preferences`, `PUT /preferences`
- `UserGoalService.java`, `FoodPreferenceService.java`
- `UserGoalRepository.java`, `FoodPreferenceRepository.java`
- `GoalRequest.java`, `GoalResponse.java`, `PreferenceRequest.java`, `PreferenceResponse.java`
- `UserGoalMapper.java`, `FoodPreferenceMapper.java`
- Target calorie/macro calculation on goal save (BMR → TDEE → Goal Adjustment)
- Validation: goal type enum, activity level enum, sleep 2–16hrs, water 200–10000ml

**Unit Tests:**
- Goal CRUD, preference CRUD, target calculation, validation

**Definition of Done:**
- [ ] Goals save and return calculated targets (calories, protein, carbs, fat)
- [ ] Preferences save diet type, budget, allergies, region
- [ ] Allergies stored as JSON array
- [ ] Invalid enum values return 400
- [ ] Only authenticated users can access
- [ ] Unit tests pass

---

### TASK-011: Food Catalog Module — Backend

| **Field**        | **Detail**                                                  |
| ---------------- | ----------------------------------------------------------- |
| **Module**       | Food Catalog                                                |
| **Priority**     | P1 — High                                                   |
| **Est. Hours**   | 8                                                           |
| **Dependencies** | TASK-007 (auth), TASK-003 (seed data exists)                |
| **Branch**       | `feature/food-catalog-backend`                              |

**Deliverables:**
- `FoodItemController.java` — `GET /search`, `GET /{foodId}`, `GET /category/{category}`
- `FoodItemService.java` with `FoodItemServiceImpl.java`
- `FoodItemRepository.java` with full-text search query
- `FoodSearchResponse.java`, `FoodItemResponse.java`
- Paginated search with full-text index on `food_name`
- Filter by category
- Cache integration (Redis) for catalog queries

**Unit Tests:**
- Search by name, get by ID, get by category, pagination, empty results

**Definition of Done:**
- [ ] Search returns filtered, paginated results
- [ ] Full-text search works with partial matches (e.g., "oat" finds "Oats")
- [ ] Category filter returns only foods in that category
- [ ] Individual food item returns full nutritional data
- [ ] Results are cached in Redis (2nd request is faster)
- [ ] Unit tests pass

---

### TASK-012: Recommendation Engine — Backend

| **Field**        | **Detail**                                                  |
| ---------------- | ----------------------------------------------------------- |
| **Module**       | Recommendation Engine                                       |
| **Priority**     | P1 — High                                                   |
| **Est. Hours**   | 20                                                          |
| **Dependencies** | TASK-008, TASK-009, TASK-010, TASK-011 (all input data)    |
| **Branch**       | `feature/recommendation-engine`                             |

**Deliverables:**
- `RecommendationEngine.java` — Core business logic class
- `MealPlanGenerator.java` — Orchestrates the generation process
- Rule categories (each as separate method or inner class):
    - `calculateBMR()` — Harris-Benedict equation
    - `calculateTDEE()` — BMR × activity multiplier
    - `applyGoalAdjustment()` — +/- calories based on goal
    - `determineMacroSplit()` — P/C/F ratios based on goal + conditions
    - `applyConditionFilters()` — Restrict foods based on medical conditions
    - `scoreFoodItem()` — Score food items by nutritional fit, preference, budget
    - `selectMeals()` — Select best items for each meal slot
    - `generateDietaryTips()` — Condition-specific tips
- Fallback plan generation (if no matching foods found)
- `RecommendationException.java` for engine failures
- `NutritionController.java` — `POST /meal-plans/generate` (calls engine)
- `NutritionService.java` — Orchestrates engine call + save

**Unit Tests:**
- `RecommendationEngineTest.java` — BMR calculation, TDEE, macro split for each goal type, condition filtering, food scoring, meal selection
- Edge cases: underweight + muscle gain, diabetic + vegetarian, all conditions selected

**Definition of Done:**
- [ ] Engine produces a valid meal plan for any valid combination of user data
- [ ] BMR matches Harris-Benedict formula (±0.1)
- [ ] TDEE correctly applies activity multiplier
- [ ] Calorie adjustment matches goal (loss: -500, gain: +300, etc.)
- [ ] Macro split varies by goal and conditions (diabetes → lower carbs)
- [ ] Condition filters remove incompatible foods
- [ ] Budget filter selects appropriate items
- [ ] Meal plan has exactly 5 meal slots with valid nutrition totals
- [ ] Dietary tips are generated based on conditions
- [ ] Engine handles edge cases (fallback plan when no foods match)
- [ ] All unit tests pass (≥ 90% coverage on engine)

---

### TASK-013: Meal Plan Module — Backend

| **Field**        | **Detail**                                                  |
| ---------------- | ----------------------------------------------------------- |
| **Module**       | Meal Plans                                                  |
| **Priority**     | P1 — High                                                   |
| **Est. Hours**   | 10                                                          |
| **Dependencies** | TASK-012 (engine must generate plans)                       |
| **Branch**       | `feature/meal-plan-backend`                                 |

**Deliverables:**
- `MealPlanController.java` — `POST /generate`, `GET ?date=`, `GET /{planId}`, `GET /history`
- `MealPlanService.java` with `MealPlanServiceImpl.java`
- `MealPlanRepository.java`, `MealPlanItemRepository.java`
- `MealPlanRequest.java`, `MealPlanResponse.java`, `MealPlanHistoryResponse.java`
- `MealPlanMapper.java`
- Integration with Recommendation Engine (via `NutritionService`)
- One plan per user per day enforcement (unique constraint)
- Date validation (not more than 7 days in past/future)
- Cache generated plans in Redis
- Plan history with date range filtering (max 7 days)

**Unit Tests:**
- Generate plan, retrieve by date, retrieve by ID, history, duplicate prevention, cache behavior

**Definition of Done:**
- [ ] `POST /meal-plans/generate` creates and returns a full meal plan with meals
- [ ] Duplicate generation for same date returns 400
- [ ] `GET /meal-plans?date=` returns existing plan
- [ ] `GET /meal-plans/history` returns paginated history for date range
- [ ] Plan includes all 5 meal types with correct nutrition totals
- [ ] Dietary tips are included
- [ ] Unit tests pass

---

### TASK-014: Food Diary Module — Backend

| **Field**        | **Detail**                                                  |
| ---------------- | ----------------------------------------------------------- |
| **Module**       | Food Diary                                                  |
| **Priority**     | P1 — High                                                   |
| **Est. Hours**   | 8                                                           |
| **Dependencies** | TASK-007 (auth), TASK-011 (food catalog)                    |
| **Branch**       | `feature/food-diary-backend`                                |

**Deliverables:**
- `FoodDiaryController.java` — `POST /entries`, `GET /entries`, `DELETE /entries/{id}`
- `FoodDiaryService.java` with `FoodDiaryServiceImpl.java`
- `FoodDiaryEntryRepository.java`
- `FoodDiaryEntryRequest.java`, `FoodDiaryEntryResponse.java`, `DailyDiaryResponse.java`
- `FoodDiaryMapper.java`
- Nutrition calculation from serving size × food item per-100g values
- Daily summary (total calories, protein, carbs, fat) on GET by date
- Logged date cannot be in the future

**Unit Tests:**
- Log entry, get by date (with daily totals), delete, future date validation

**Definition of Done:**
- [ ] `POST /entries` logs entry with auto-calculated nutrition
- [ ] `GET /entries?date=` returns entries with daily summary totals
- [ ] `DELETE /entries/{id}` removes entry
- [ ] Future date returns 400
- [ ] Daily summary totals match sum of individual entries
- [ ] Unit tests pass

---

### TASK-015: Progress Dashboard Module — Backend

| **Field**        | **Detail**                                                  |
| ---------------- | ----------------------------------------------------------- |
| **Module**       | Progress Dashboard                                          |
| **Priority**     | P2 — Medium                                                 |
| **Est. Hours**   | 12                                                          |
| **Dependencies** | TASK-008 (profile), TASK-009 (vitals), TASK-013 (meal plans), TASK-014 (diary) |
| **Branch**       | `feature/progress-backend`                                  |

**Deliverables:**
- `ProgressController.java` — `GET /summary`, `GET /weight`, `GET /calories`, `GET /macros`, `POST /tracking`
- `ProgressService.java` with `ProgressServiceImpl.java`
- `ProgressTrackingRepository.java`
- `DashboardSummaryResponse.java`, `WeightTrendResponse.java`, `CalorieTrendResponse.java`, `MacroBreakdownResponse.java`, `TrackingRequest.java`, `TrackingResponse.java`
- Aggregation queries for weight trend (30/90 days)
- Calorie trend from food diary + meal plan targets
- Macro breakdown with consumed vs target percentages
- Daily tracking record (one per user per day)

**Unit Tests:**
- Dashboard summary aggregation, weight trend, calorie trend, macro breakdown, tracking record

**Definition of Done:**
- [ ] `GET /summary` returns all dashboard metrics (weight, calories, macros, water, streak)
- [ ] `GET /weight?days=30` returns weight data points with trend direction
- [ ] `GET /calories?days=7` returns consumed vs target bar data
- [ ] `GET /macros` returns P/C/F breakdown with percentages
- [ ] `POST /tracking` creates daily tracking record
- [ ] One tracking record per user per day enforced
- [ ] Unit tests pass

---

### TASK-016: Admin Module — Backend

> **Optional for academic demonstrations.** The Admin module is not required for core user-facing functionality. It may be omitted if time-constrained or if the demonstration does not require administrative features.

| **Field**        | **Detail**                                                  |
| ---------------- | ----------------------------------------------------------- |
| **Module**       | Admin                                                       |
| **Priority**     | P2 — Medium                                                 |
| **Est. Hours**   | 10                                                          |
| **Dependencies** | TASK-007, TASK-008, TASK-011 (all other modules)            |
| **Branch**       | `feature/admin-backend`                                     |

**Deliverables:**
- `AdminController.java` — `GET /users`, `GET /users/{id}`, `PATCH /users/{id}/deactivate`, `GET /system/health`, `POST /foods`, `PUT /foods/{id}`, `DELETE /foods/{id}`
- `AdminService.java` with `AdminServiceImpl.java`
- `UserListResponse.java`, `UserDetailResponse.java`, `SystemHealthResponse.java`
- Role-based access (`@PreAuthorize("hasRole('ADMIN')")`)
- Pagination on user list with search by name/email
- Deactivate user (soft delete: `isActive = false`)
- System health aggregation (user counts, plan counts, diary counts)
- Food catalog CRUD for admin

**Unit Tests:**
- User list pagination, user detail, deactivate, system health, food CRUD

**Definition of Done:**
- [ ] Only ADMIN role can access any admin endpoint
- [ ] User list is paginated with search
- [ ] User detail shows profile + vitals + conditions + stats
- [ ] Deactivate sets isActive = false
- [ ] System health returns correct counts
- [ ] Food CRUD works (add, update, soft-delete)
- [ ] Unit tests pass

---

## 5. Phase 3: Frontend Core

**Goal**: Implement all frontend pages and components in the same order as backend modules.

---

### TASK-017: Authentication Pages — Frontend

| **Field**        | **Detail**                                                  |
| ---------------- | ----------------------------------------------------------- |
| **Module**       | Authentication                                              |
| **Priority**     | P0 — Critical                                               |
| **Est. Hours**   | 10                                                          |
| **Dependencies** | TASK-005 (shared components), TASK-007 (backend auth)       |
| **Branch**       | `feature/auth-frontend`                                     |

**Deliverables:**
- `LoginPage.jsx` — Email + password form, validation, error display, loading state
- `RegisterPage.jsx` — Registration form with password strength indicator
- `LoginForm.jsx` — Email input, password input, submit button
- `RegisterForm.jsx` — All registration fields with inline validation
- `PasswordStrengthBar.jsx` — Visual strength indicator
- `authApi.js` — `register()`, `login()`, `validateToken()` functions
- `useAuth.js` hook (full implementation)
- Auto-redirect to dashboard on success
- Error handling for 401, 409, validation errors

**Definition of Done:**
- [ ] Login form validates email format and non-empty password
- [ ] Invalid credentials show error banner
- [ ] Successful login stores token and redirects to dashboard
- [ ] Registration validates password strength
- [ ] Duplicate email shows error message
- [ ] Successful registration auto-logs in and redirects
- [ ] Password strength indicator updates in real-time
- [ ] All states work: loading, error, success, validation

---

### TASK-018: Landing Page — Frontend

| **Field**        | **Detail**                                                  |
| ---------------- | ----------------------------------------------------------- |
| **Module**       | Public Pages                                                |
| **Priority**     | P2 — Medium                                                 |
| **Est. Hours**   | 8                                                           |
| **Dependencies** | TASK-005 (shared components)                                |
| **Branch**       | `feature/landing-page`                                      |

**Deliverables:**
- `LandingPage.jsx` — Complete landing page with:
    - Hero section with headline, subtitle, and CTA buttons
    - Features grid (3 columns: Personalized Plans, Track Progress, Budget-Friendly)
    - How It Works section (4-step process)
    - Testimonials section (static, 2-3 quotes)
    - Final CTA section
    - Footer
- Responsive: single column on mobile, 3-column grid on desktop
- Smooth scroll to sections
- CTA buttons link to `/register` and `/login`

**Definition of Done:**
- [ ] Landing page renders with all sections
- [ ] "Get Started Free" links to `/register`
- [ ] "Sign In" links to `/login`
- [ ] Fully responsive (mobile, tablet, desktop)
- [ ] Tailwind styled with design system tokens

---

### TASK-019: Onboarding Wizard — Frontend

| **Field**        | **Detail**                                                  |
| ---------------- | ----------------------------------------------------------- |
| **Module**       | Onboarding                                                  |
| **Priority**     | P1 — High                                                   |
| **Est. Hours**   | 12                                                          |
| **Dependencies** | TASK-005 (shared), TASK-008, TASK-009, TASK-010 (backend APIs) |
| **Branch**       | `feature/onboarding-frontend`                               |

**Deliverables:**
- `OnboardingWizard.jsx` — Container with stepper, manages step state
- `StepperProgress.jsx` — 4-step horizontal progress indicator
- `ProfileStep.jsx` — Name, DOB, gender, height, weight form
- `HealthStep.jsx` — Condition checkboxes + vitals input (all optional)
- `GoalsStep.jsx` — Goal radio cards, activity dropdown, sleep, water
- `PreferencesStep.jsx` — Diet type, budget, region, allergies tag input
- `CompletionScreen.jsx` — Celebration + "Go to Dashboard" button
- `userApi.js` — Profile, health, goals, preferences API functions
- Multi-step form state (back/next, skip optional, save on complete)
- Calls backend APIs in sequence on final save

**Definition of Done:**
- [ ] Stepper shows current step and total progress
- [ ] Each step validates required fields before allowing "Next"
- [ ] Health step shows all optional fields with "Skip" option
- [ ] Goals step shows radio cards with icons for each goal type
- [ ] Preferences step has tag input for allergies
- [ ] "Complete Setup" saves all data and redirects to dashboard
- [ ] Works on mobile (full-width inputs, bottom navigation)
- [ ] Loading state during API calls, error handling per step

---

### TASK-020: Dashboard Page — Frontend

| **Field**        | **Detail**                                                  |
| ---------------- | ----------------------------------------------------------- |
| **Module**       | Dashboard                                                   |
| **Priority**     | P1 — High                                                   |
| **Est. Hours**   | 14                                                          |
| **Dependencies** | TASK-005 (shared), TASK-015 (backend progress)              |
| **Branch**       | `feature/dashboard-frontend`                                |

**Deliverables:**
- `DashboardPage.jsx` — Main dashboard layout
- `SummaryCardsRow.jsx` — 4 cards: weight (with trend arrow), calories (vs target), protein (progress), water (progress)
- `WeightTrendChart.jsx` — Recharts LineChart, 30-day data, tooltip on hover
- `CalorieBarChart.jsx` — Recharts BarChart, consumed vs target, 7-day, dual color
- `MacroDoughnutChart.jsx` — Recharts PieChart, protein/carbs/fat split
- `TodayMealSummary.jsx` — Quick list of today's meals from plan
- `StreakBadge.jsx` — Fire emoji + streak count
- `ActionButtons.jsx` — "View Today's Meal Plan", "Log Today's Meals"
- `progressApi.js` — `getSummary()`, `getWeightTrend()`, `getCalorieTrend()`, `getMacros()`
- `useProgress.js` hook
- Loading state: skeleton shimmer for all components
- Empty state: "Set up your profile to see your personalized dashboard!"
- Auto-refresh data on page focus

**Definition of Done:**
- [ ] All 4 summary cards display correct data from API
- [ ] Weight chart shows line with tooltip
- [ ] Calorie chart shows bars with consumed vs target
- [ ] Macro doughnut shows correct split with legend
- [ ] Today's meals quick view shows 3 most recent meals
- [ ] Streak badge shows correct count
- [ ] CTA buttons navigate to correct pages
- [ ] Skeleton shown during loading
- [ ] Empty state when no profile exists
- [ ] Responsive: cards 2x2 on tablet, 4x1 on desktop

---

### TASK-021: Meal Plan Page — Frontend

| **Field**        | **Detail**                                                  |
| ---------------- | ----------------------------------------------------------- |
| **Module**       | Meal Plans                                                  |
| **Priority**     | P1 — High                                                   |
| **Est. Hours**   | 12                                                          |
| **Dependencies** | TASK-005 (shared), TASK-013 (backend meal plans)            |
| **Branch**       | `feature/meal-plan-frontend`                                |

**Deliverables:**
- `MealPlanPage.jsx` — Main meal plan page
- `DateSelector.jsx` — Left/right arrows to navigate days, displays current date
- `NutritionSummaryBar.jsx` — Color-coded progress bar for calories + macro targets
- `MealCard.jsx` — Collapsible card for each meal: food name, serving, calories, P/C/F breakdown
- `MealPlanGrid.jsx` — Renders all 5 MealCards in order
- `DietaryTipsSection.jsx` — Expandable tips section at bottom
- `mealPlanApi.js` — `generatePlan()`, `getPlanByDate()`, `getPlanHistory()`
- `useMealPlan.js` hook
- Loading: skeleton cards
- Empty state: "No meal plan for today. Generate one!" with large button
- "Regenerate Plan" button with confirmation dialog
- Generate button triggers API and shows loading on button

**Definition of Done:**
- [ ] Date selector navigates between days
- [ ] Existing plan loads for selected date
- [ ] No plan shows empty state with generate button
- [ ] Meal cards show food name, serving unit, calories, P/C/F
- [ ] Cards are collapsible (expand/collapse on click)
- [ ] Nutrition summary bar shows progress toward targets
- [ ] Dietary tips section shows condition-specific tips
- [ ] "Regenerate" creates new plan and refreshes display
- [ ] Loading skeleton shown during API calls
- [ ] Responsive: single column on mobile

---

### TASK-022: Food Diary Page — Frontend

| **Field**        | **Detail**                                                  |
| ---------------- | ----------------------------------------------------------- |
| **Module**       | Food Diary                                                  |
| **Priority**     | P2 — Medium                                                 |
| **Est. Hours**   | 14                                                          |
| **Dependencies** | TASK-005 (shared), TASK-014 (backend diary), TASK-011 (food catalog) |
| **Branch**       | `feature/food-diary-frontend`                               |

**Deliverables:**
- `FoodDiaryPage.jsx` — Main food diary page
- `DateSelector.jsx` (reuse from meal plan)
- `DailySummaryBar.jsx` — Compact summary: total calories, protein, carbs, fat
- `MealSection.jsx` — Grouped by meal type (Breakfast, Lunch, Dinner, Snacks)
- `EntryCard.jsx` — Individual entry: food name, calories, time, delete icon
- `AddEntryModal.jsx` — Full modal with meal type dropdown, food search, serving size, auto-calculated nutrition, notes
- `FoodSearchInput.jsx` — Typeahead search with debounce, shows results dropdown
- `foodDiaryApi.js` — `logEntry()`, `getEntries()`, `deleteEntry()`
- `useFoodDiary.js` hook
- Loading: skeleton list
- Empty state: "No meals logged. Start tracking!" with add button
- Delete with confirmation

**Definition of Done:**
- [ ] Diary shows entries grouped by meal type for selected date
- [ ] Daily summary shows correct totals
- [ ] Add entry modal opens and closes
- [ ] Food search shows typeahead results with debounce
- [ ] Selecting a food auto-fills nutrition based on serving size
- [ ] Entry can be deleted with confirmation
- [ ] After adding, diary refreshes with new entry
- [ ] Loading skeleton during initial load
- [ ] Empty state when no entries
- [ ] Responsive: full-screen modal on mobile

---

### TASK-023: Progress Page — Frontend

| **Field**        | **Detail**                                                  |
| ---------------- | ----------------------------------------------------------- |
| **Module**       | Progress Dashboard                                          |
| **Priority**     | P2 — Medium                                                 |
| **Est. Hours**   | 12                                                          |
| **Dependencies** | TASK-005 (shared), TASK-015 (backend progress)              |
| **Branch**       | `feature/progress-frontend`                                 |

**Deliverables:**
- `ProgressPage.jsx` — Main progress page
- `TimeRangeTabs.jsx` — "7 Days", "30 Days", "90 Days" tabs
- `WeightLineChart.jsx` — Full-width Recharts LineChart with trend line, annotations
- `CalorieBarChart.jsx` — Recharts BarChart, consumed vs target, with average line
- `MacroDoughnutChart.jsx` — Recharts PieChart with P/C/F + percentage table
- `BmiMiniChart.jsx` — Small BMI history chart with healthy range overlay
- `StatsCardRow.jsx` — Total weight change, average daily calories, best streak
- Weight entry form (inline or modal) for quick weight logging
- Loading: skeleton charts
- Empty state: "Start tracking to see your progress!"

**Definition of Done:**
- [ ] Time range tabs switch between 7/30/90 day views
- [ ] Weight chart shows trend line with data points
- [ ] Calorie chart shows consumed vs target bars
- [ ] Macro chart shows doughnut + table with percentages
- [ ] BMI chart shows history with healthy range
- [ ] Stats cards show computed values
- [ ] Weight entry form works and refreshes chart
- [ ] Loading skeleton for all chart areas
- [ ] Empty state when no tracking data
- [ ] Responsive: charts stack vertically on mobile

---

### TASK-024: Profile Page — Frontend

| **Field**        | **Detail**                                                  |
| ---------------- | ----------------------------------------------------------- |
| **Module**       | Profile                                                     |
| **Priority**     | P2 — Medium                                                 |
| **Est. Hours**   | 10                                                          |
| **Dependencies** | TASK-005 (shared), TASK-008, TASK-009, TASK-010 (backend APIs) |
| **Branch**       | `feature/profile-frontend`                                  |

**Deliverables:**
- `ProfilePage.jsx` — Main profile page with tab navigation
- `ProfileTabs.jsx` — "Personal Info", "Health Vitals", "Goals & Lifestyle", "Food Preferences"
- `PersonalInfoForm.jsx` — Editable form with save button
- `HealthVitalsForm.jsx` — Vitals form with condition list (add/remove)
- `ConditionCheckboxGrid.jsx` — Grid of condition checkboxes
- `GoalsForm.jsx` — Goal selector, activity dropdown, sleep, water
- `PreferencesForm.jsx` — Diet type, budget, region, allergies, excluded foods
- `userApi.js` — Profile, health, goals, preferences API functions
- Each tab has its own "Save Changes" button
- Success toast on save
- Loading: skeleton form

**Definition of Done:**
- [ ] 4 tabs switch between profile sections
- [ ] Each tab loads existing data from API
- [ ] Save button per tab (not whole page)
- [ ] Successful save shows success toast
- [ ] Validation errors shown inline
- [ ] Condition grid allows multiple selections with add/remove
- [ ] Allergies uses tag input component
- [ ] Loading skeleton shown during data fetch
- [ ] Responsive: tabs become accordion on mobile

---

### TASK-025: Admin Page — Frontend

> **Optional for academic demonstrations.** This task may be skipped if the Admin module (TASK-016) was omitted or if the project scope does not require an admin interface.

| **Field**        | **Detail**                                                  |
| ---------------- | ----------------------------------------------------------- |
| **Module**       | Admin                                                       |
| **Priority**     | P3 — Low                                                    |
| **Est. Hours**   | 12                                                          |
| **Dependencies** | TASK-005 (shared), TASK-016 (backend admin)                 |
| **Branch**       | `feature/admin-frontend`                                    |

**Deliverables:**
- `AdminPage.jsx` — Main admin page with tab navigation
- `AdminTabs.jsx` — "Users", "Food Catalog", "System Health"
- `UsersTable.jsx` — Paginated table with search, sort, action buttons
- `UserDetailModal.jsx` — Modal showing complete user information
- `FoodCatalogTable.jsx` — Paginated table with search, CRUD actions
- `FoodFormModal.jsx` — Add/edit food item form modal
- `SystemHealthCards.jsx` — Grid of stat cards
- `adminApi.js` — All admin API functions
- Role-based rendering (only visible to ADMIN users)

**Definition of Done:**
- [ ] Only ADMIN users can access `/admin` route
- [ ] Users tab shows paginated table with search
- [ ] User detail modal shows complete profile + vitals + stats
- [ ] Food catalog tab shows paginated table with CRUD
- [ ] Add/edit food modal validates all fields
- [ ] System health cards show correct metrics
- [ ] All tables show loading, empty, and error states
- [ ] Responsive: tables horizontally scrollable on mobile

---

### TASK-026: 404 & Error Pages — Frontend

| **Field**        | **Detail**                                                  |
| ---------------- | ----------------------------------------------------------- |
| **Module**       | Public Pages                                                |
| **Priority**     | P2 — Medium                                                 |
| **Est. Hours**   | 4                                                           |
| **Dependencies** | TASK-005 (shared components)                                |
| **Branch**       | `feature/error-pages`                                       |

**Deliverables:**
- `NotFoundPage.jsx` — 404 page with illustration, message, "Go to Dashboard" button
- `ForbiddenPage.jsx` — 403 page with lock icon, message, contact admin note
- `ErrorBoundary.jsx` — React error boundary wrapping authenticated routes
- Routes for `*` (404 catch-all) and `/forbidden`

**Definition of Done:**
- [ ] Unknown routes show custom 404 page
- [ ] Unauthorized admin access shows 403 page
- [ ] Error boundary catches unexpected React errors
- [ ] Pages match design system styling

---

## 6. Phase 4: Integration & Features

**Goal**: Connect all modules, add cross-cutting features, and finalize the complete application flow.

---

### TASK-027: End-to-End Auth Flow Integration

| **Field**        | **Detail**                                                  |
| ---------------- | ----------------------------------------------------------- |
| **Module**       | Integration                                                 |
| **Priority**     | P1 — High                                                   |
| **Est. Hours**   | 6                                                           |
| **Dependencies** | TASK-017 (frontend auth), TASK-007 (backend auth)           |
| **Branch**       | `feature/auth-integration`                                  |

**Deliverables:**
- Full login → dashboard redirect flow tested
- Full register → onboarding redirect flow tested
- Token expiry handling: redirect to login on 401
- Token validation on app load (restore session from localStorage)
- Logout clears token and redirects to landing
- ProtectedRoute redirects unauthenticated users to `/login` with `returnTo` parameter
- AuthContext provides consistent `user`, `isAuthenticated`, `isAdmin` state

**Definition of Done:**
- [ ] Login → dashboard redirect works end-to-end
- [ ] Register → onboarding redirect works end-to-end
- [ ] Page refresh preserves authentication state
- [ ] Expired token shows login page
- [ ] Logout clears all state and redirects to landing
- [ ] Protected routes redirect to `/login` when not authenticated
- [ ] Return to original page after login

---

### TASK-028: Onboarding-to-Dashboard Flow

| **Field**        | **Detail**                                                  |
| ---------------- | ----------------------------------------------------------- |
| **Module**       | Integration                                                 |
| **Priority**     | P1 — High                                                   |
| **Est. Hours**   | 6                                                           |
| **Dependencies** | TASK-019 (onboarding), TASK-020 (dashboard)                 |
| **Branch**       | `feature/onboarding-flow`                                   |

**Deliverables:**
- Redirect new users to onboarding if profile is incomplete
- On successful onboarding completion → redirect to dashboard
- Dashboard checks if profile exists; shows "Complete your profile" if missing
- Meal plan page checks if all required data exists before allowing generation
- Error states when required data is missing (with links to profile page)

**Definition of Done:**
- [ ] First-time user goes to onboarding after registration
- [ ] User who skipped onboarding sees dashboard with "Complete Profile" prompt
- [ ] After completing onboarding, user lands on dashboard with all data populated
- [ ] Meal plan generate button shows appropriate message if profile incomplete
- [ ] All cross-module dependencies handled gracefully

---

### TASK-029: Caching Service Implementation

| **Field**        | **Detail**                                                  |
| ---------------- | ----------------------------------------------------------- |
| **Module**       | Caching                                                     |
| **Priority**     | P2 — Medium                                                 |
| **Est. Hours**   | 8                                                           |
| **Dependencies** | TASK-004 (Redis config), TASK-006 (Redis container)         |
| **Branch**       | `feature/caching`                                           |

**Deliverables:**
- `CacheService.java` — Abstraction over Redis with methods: `get()`, `put()`, `evict()`, `evictByPattern()`
- Cache key constants: `FOOD_CATALOG`, `MEAL_PLAN`, `DASHBOARD_STATS`
- Cache-aside pattern implementation
- TTL configuration per cache type (food: 1hr, meal plan: 6hr, dashboard: 30min)
- Cache invalidation on writes (invalidate relevant keys when data changes)
- Integration with Food Catalog (search results cached)
- Integration with Meal Plans (generated plans cached)
- Integration with Dashboard (summary stats cached)

**Definition of Done:**
- [ ] CacheService stores and retrieves data correctly
- [ ] TTL expires cache entries as configured
- [ ] Cache invalidation works on related data changes
- [ ] Food catalog queries return cached data on repeat requests (measurably faster)
- [ ] Meal plan regeneration invalidates old cached plan
- [ ] Dashboard summary invalidates on new diary entries

> **Note:** All caching is optional. If Redis is unavailable, the application falls back to direct database queries without errors. The `CacheService` uses Spring's `@ConditionalOnProperty` to disable itself when Redis is not configured.

---

### TASK-030: Axios Interceptor & Error Handling

| **Field**        | **Detail**                                                  |
| ---------------- | ----------------------------------------------------------- |
| **Module**       | Frontend Infrastructure                                     |
| **Priority**     | P1 — High                                                   |
| **Est. Hours**   | 4                                                           |
| **Dependencies** | TASK-005 (Axios setup)                                      |
| **Branch**       | `feature/axios-interceptor`                                 |

**Deliverables:**
- Request interceptor: attach JWT to every request
- Response interceptor: handle 401 (redirect to login), 403 (redirect to forbidden), 500 (show toast)
- Global error handler mapping error codes to user-friendly messages
- Retry logic for transient failures (optional)

**Definition of Done:**
- [ ] JWT attached to every authenticated request automatically
- [ ] 401 response clears token and redirects to login
- [ ] 403 response navigates to forbidden page
- [ ] 500 response shows error toast with "Something went wrong"
- [ ] Network errors show appropriate message

---

### TASK-031: Redis Caching — Integration Testing

| **Field**        | **Detail**                                                  |
| ---------------- | ----------------------------------------------------------- |
| **Module**       | Caching                                                     |
| **Priority**     | P3 — Low                                                    |
| **Est. Hours**   | 6                                                           |
| **Dependencies** | TASK-029 (caching service)                                  |
| **Branch**       | `feature/caching-testing`                                   |

**Deliverables:**
- Integration tests verifying cache behavior:
    - First call -> cache miss -> DB query -> cache set
    - Second call -> cache hit -> no DB query
    - Write operation -> cache invalidated -> next read -> cache miss
- Performance benchmarks (with cache vs without)

**Definition of Done:**
- [ ] Integration tests prove cache-aside pattern works
- [ ] Cache hit latency < 5ms, cache miss + DB query < 100ms
- [ ] Cache invalidation correctly triggers on writes

> **Note:** Redis caching integration tests only apply when Redis is available. If Redis is not configured, these tests are skipped (`@EnabledIf` or conditional test execution).

---

## 7. Phase 5: Quality Assurance

**Goal**: Ensure the application is thoroughly tested, secure, and performs well.

---

### TASK-032: Backend Unit Tests — Complete Suite

| **Field**        | **Detail**                                                  |
| ---------------- | ----------------------------------------------------------- |
| **Module**       | Testing                                                     |
| **Priority**     | P1 — High                                                   |
| **Est. Hours**   | 14                                                          |
| **Dependencies** | All backend modules (TASK-007 through TASK-016)             |
| **Branch**       | `feature/unit-tests`                                        |

**Deliverables:**
- Complete JUnit 5 + Mockito test suite for all services
- Minimum 85% code coverage on service layer
- Minimum 60% code coverage overall
- Test categories: Service tests, Controller tests, Repository tests
- Edge cases: null values, empty inputs, boundary values, unauthorized access
- `pom.xml` with JaCoCo for coverage reporting

**Test Files:**
- `AuthServiceTest.java` (5+ tests)
- `UserServiceTest.java` (5+ tests)
- `HealthVitalsServiceTest.java` (5+ tests)
- `UserGoalServiceTest.java` (3+ tests)
- `FoodPreferenceServiceTest.java` (3+ tests)
- `RecommendationEngineTest.java` (10+ tests — core logic)
- `MealPlanServiceTest.java` (5+ tests)
- `FoodDiaryServiceTest.java` (5+ tests)
- `ProgressServiceTest.java` (5+ tests)
- `AdminServiceTest.java` (3+ tests)
- `CacheServiceTest.java` (3+ tests)
- `BmiCalculatorTest.java` (3+ tests)
- `CalorieCalculatorTest.java` (3+ tests)

**Definition of Done:**
- [ ] `mvn test` passes with all tests green
- [ ] JaCoCo report shows ≥ 85% service layer coverage
- [ ] JaCoCo report shows ≥ 60% overall coverage
- [ ] All edge cases covered (null, empty, boundary, unauthorized)
- [ ] Test reports generated

---

### TASK-033: Frontend Component Tests

| **Field**        | **Detail**                                                  |
| ---------------- | ----------------------------------------------------------- |
| **Module**       | Testing                                                     |
| **Priority**     | P2 — Medium                                                 |
| **Est. Hours**   | 10                                                          |
| **Dependencies** | Frontend pages (TASK-017 through TASK-026)                  |
| **Branch**       | `feature/frontend-tests`                                    |

**Deliverables:**
- Jest + React Testing Library tests for key components:
    - `LoginForm.test.jsx` — renders, validates, submits
    - `RegisterForm.test.jsx` — password strength, validation
    - `SummaryCardsRow.test.jsx` — renders data, shows trends
    - `MealCard.test.jsx` — renders nutrition info, collapsible
    - `EmptyState.test.jsx` — renders icon, text, button
    - `ErrorState.test.jsx` — renders message, retry works
    - `ProtectedRoute.test.jsx` — redirects when not authenticated
    - `Sidebar.test.jsx` — shows correct links based on role

**Definition of Done:**
- [ ] `npm test` passes for all component tests
- [ ] Key user interactions tested (click, type, submit)
- [ ] Loading, error, and empty states tested
- [ ] Snapshot tests for stable components

---

### TASK-034: API Integration Tests

| **Field**        | **Detail**                                                  |
| ---------------- | ----------------------------------------------------------- |
| **Module**       | Testing                                                     |
| **Priority**     | P2 — Medium                                                 |
| **Est. Hours**   | 8                                                           |
| **Dependencies** | All backend modules                                         |
| **Branch**       | `feature/integration-tests`                                 |

**Deliverables:**
- `@SpringBootTest` integration tests with test database (H2 or Testcontainers)
- Full flow tests:
    - Register → Login → Get Profile → Update Profile
    - Register → Login → Set Goals → Add Vitals → Generate Meal Plan
    - Register → Login → Log Food Entry → Get Diary → Get Progress Summary
- Authentication tests (missing token, expired token, wrong role)
- Validation error tests (all invalid input combinations)

**Definition of Done:**
- [ ] All integration tests pass against real database
- [ ] End-to-end flows work correctly
- [ ] Security tests verify authentication/authorization
- [ ] Validation tests verify all constraint rules

---

### TASK-035: Postman Collection

| **Field**        | **Detail**                                                  |
| ---------------- | ----------------------------------------------------------- |
| **Module**       | Testing                                                     |
| **Priority**     | P2 — Medium                                                 |
| **Est. Hours**   | 4                                                           |
| **Dependencies** | All backend modules                                         |
| **Branch**       | `feature/postman-collection`                                |

**Deliverables:**
- Complete Postman collection with all 36 endpoints
- Organized by module (folder structure)
- Environment variables: `baseUrl`, `token`, `userId`
- Pre-request scripts for token management
- Test scripts for status code and response validation
- Example request bodies for every endpoint
- Export to `postman/nutriguide-ai.postman_collection.json`

**Definition of Done:**
- [ ] All 36 endpoints present in collection
- [ ] Collection runs end-to-end with environment variables
- [ ] Auth token auto-populates via login request
- [ ] Test scripts validate common response patterns

---

### TASK-036: Security Audit

| **Field**        | **Detail**                                                  |
| ---------------- | ----------------------------------------------------------- |
| **Module**       | Security                                                    |
| **Priority**     | P1 — High                                                   |
| **Est. Hours**   | 4                                                           |
| **Dependencies** | All backend modules                                         |
| **Branch**       | `feature/security-audit`                                    |

**Deliverables:**
- Verify all endpoints have correct authentication annotations
- Verify role-based access (USER vs ADMIN)
- Verify password encoding (BCrypt, strength 10)
- Verify JWT signing and validation (HS256)
- Verify CORS configuration (only allow frontend origin)
- Verify SQL injection prevention (JPA parameterized queries)
- Verify no sensitive data in logs (passwords masked)
- Verify HTTPS enforcement in production config
- Security checklist document

**Definition of Done:**
- [ ] Security checklist completed and signed off
- [ ] No endpoint accessible without correct authentication
- [ ] ADMIN endpoints inaccessible to USER role
- [ ] Password never returned in any response
- [ ] CORS restricted to frontend origin
- [ ] All queries use parameterized JPA methods

---

## 8. Phase 6: Deployment & Delivery

**Goal**: Containerize, configure CI/CD, deploy, and finalize documentation.

---

### TASK-037: Production Docker Configuration

| **Field**        | **Detail**                                                  |
| ---------------- | ----------------------------------------------------------- |
| **Module**       | Deployment                                                  |
| **Priority**     | P1 — High                                                   |
| **Est. Hours**   | 6                                                           |
| **Dependencies** | TASK-006 (Docker Compose)                                   |
| **Branch**       | `feature/production-docker`                                 |

**Deliverables:**
- Production Dockerfile (multi-stage, non-root user, health checks)
- Production `docker-compose.prod.yml` (without dev volumes, with restart policies)
- Health check endpoint: `GET /api/actuator/health`
- Resource limits in Docker Compose
- `.dockerignore` optimized for production
- Environment variable documentation

**Definition of Done:**
- [ ] Production Docker image builds without warnings (< 200MB)
- [ ] Container runs as non-root user
- [ ] Health check succeeds
- [ ] Resource limits prevent container from consuming all host memory

---

### TASK-038: GitHub Actions CI/CD Pipeline

| **Field**        | **Detail**                                                  |
| ---------------- | ----------------------------------------------------------- |
| **Module**       | Deployment                                                  |
| **Priority**     | P1 — High                                                   |
| **Est. Hours**   | 8                                                           |
| **Dependencies** | TASK-037 (prod Docker)                                      |
| **Branch**       | `feature/ci-cd`                                             |

**Deliverables:**
- `.github/workflows/ci.yml` — Build + Test on PR to develop
    - Trigger: pull_request to develop
    - Steps: Checkout → Setup Java 21 → Maven build → Maven test → Upload test reports
- `.github/workflows/deploy.yml` — Deploy to Azure on push to main
    - Trigger: push to main
    - Steps: Checkout → Build → Test → Docker build → Push to ACR → Deploy to Azure App Service → Health check
- Maven wrapper (`.mvn/wrapper/`)
- README badges for build status

**Definition of Done:**
- [ ] CI workflow triggers on PR and runs tests
- [ ] Deploy workflow builds Docker image and pushes to registry
- [ ] Health check passes after deployment
- [ ] All secrets configured in GitHub Secrets (not in code)

---

### TASK-039: Production Deployment Configuration

| **Field**        | **Detail**                                                  |
| ---------------- | ----------------------------------------------------------- |
| **Module**       | Deployment                                                  |
| **Priority**     | P2 — Medium                                                 |
| **Est. Hours**   | 8                                                           |
| **Dependencies** | TASK-037, TASK-038                                          |
| **Branch**       | `feature/deployment-config`                                 |

**Deliverables:**
- `application-prod.yml` with production database, Redis, and logging configs
- Azure App Service configuration documentation
- Azure MySQL connection string template
- Azure Redis connection string template
- Application Insights monitoring configuration
- Logging configuration (SLF4J + Logback, JSON format for production)
- Start-up scripts if needed

**Definition of Done:**
- [ ] Production config uses environment variables for all secrets
- [ ] Logging configured for production (JSON format, appropriate levels)
- [ ] Health endpoint responds correctly
- [ ] Application starts with `spring.profiles.active=prod`

---

### TASK-040: Final Documentation & README

| **Field**        | **Detail**                                                  |
| ---------------- | ----------------------------------------------------------- |
| **Module**       | Documentation                                               |
| **Priority**     | P2 — Medium                                                 |
| **Est. Hours**   | 8                                                           |
| **Dependencies** | All modules                                                 |
| **Branch**       | `docs/final-documentation`                                  |

**Deliverables:**
- `README.md` with:
    - Project overview and problem statement
    - Technology stack with version badges
    - Architecture overview (text)
    - Setup instructions (local development)
    - Docker quick start
    - API documentation link
    - Deployment instructions
    - Project structure tree
    - Contributing guidelines
- All `docs/` files reviewed and finalized:
    - `01_PROJECT_CONTEXT.md` ✅
    - `02_REQUIREMENTS.md` (created if missing)
    - `03_SYSTEM_ARCHITECTURE.md` ✅
    - `04_DATABASE.md` ✅
    - `05_API_CONTRACT.md` ✅
    - `06_UI_FLOW.md` ✅
    - `07_TASKS.md` ✅ (current)
    - `08_CODING_STANDARDS.md` (created if missing)
    - `09_TESTING.md` (created if missing)
    - `10_DEPLOYMENT.md` (created if missing)

**Definition of Done:**
- [ ] README.md complete and informative
- [ ] All docs/ files finalized and consistent with implementation
- [ ] Setup instructions work when followed step by step
- [ ] Docker quick start works with one command
- [ ] Documentation reviewed for consistency

---

### TASK-041: Final Integration Smoke Test

| **Field**        | **Detail**                                                  |
| ---------------- | ----------------------------------------------------------- |
| **Module**       | Testing                                                     |
| **Priority**     | P1 — High                                                   |
| **Est. Hours**   | 5                                                           |
| **Dependencies** | All implementation tasks                                    |
| **Branch**       | `release/v1.0.0`                                            |

**Deliverables:**
- Smoke test checklist executed against running application:
    - [ ] Register a new user
    - [ ] Complete full onboarding (all 4 steps)
    - [ ] Dashboard loads with correct summary data
    - [ ] Generate today's meal plan
    - [ ] View meal plan with all 5 meals
    - [ ] Log 3 food diary entries for today
    - [ ] View food diary with daily totals
    - [ ] View progress page with charts
    - [ ] Update profile (change weight)
    - [ ] Regenerate meal plan (sees new plan)
    - [ ] Admin: view user list
    - [ ] Admin: view system health
    - [ ] Logout and login again
    - [ ] Test 404 page (navigate to unknown route)
- Bug list created for any issues found
- Critical bugs fixed before release

**Definition of Done:**
- [ ] All smoke tests pass without errors
- [ ] No P0 or P1 bugs remaining
- [ ] P2 bugs documented and triaged
- [ ] Release candidate tagged as `v1.0.0-rc`

---

## 9. Dependency Graph

### 9.1 Task Dependency Map

```
Phase 1: Foundation
    TASK-001 (Backend Scaffolding) ──────────┐
    TASK-002 (Frontend Scaffolding) ───────┐ │
    TASK-003 (Database Schema) ────────────┤ ├──► TASK-004 (Shared Infra)
    TASK-005 (Shared Frontend) ◄───────────┘ │
    TASK-006 (Docker Compose) ◄──────────────┘

Phase 2: Backend Core
    TASK-004 ──► TASK-007 (Auth) ──► TASK-008 (Profile)
                                      TASK-009 (Health Vitals)
                                      TASK-010 (Goals & Preferences)
                                      TASK-011 (Food Catalog)

    TASK-008 + TASK-009 + TASK-010 + TASK-011 ──► TASK-012 (Recommendation Engine)
                                                          │
                                                          ▼
                                                  TASK-013 (Meal Plans)

    TASK-011 + TASK-014 (Food Diary)
    TASK-008 + TASK-009 + TASK-013 + TASK-014 ──► TASK-015 (Progress)
    TASK-007 + TASK-008 + TASK-011 ──► TASK-016 (Admin)

Phase 3: Frontend Core
    TASK-005 ──► TASK-017 (Auth Pages)
                  TASK-018 (Landing Page)
                  TASK-019 (Onboarding) ──► TASK-020 (Dashboard)
                  TASK-021 (Meal Plan Page)    TASK-022 (Food Diary)
                  TASK-023 (Progress Page)      TASK-024 (Profile Page)
                  TASK-025 (Admin Page)         TASK-026 (Error Pages)

Phase 4: Integration
    TASK-017 + TASK-007 ──► TASK-027 (Auth Integration)
    TASK-019 + TASK-020 ──► TASK-028 (Onboarding Flow)
    TASK-004 + TASK-006 ──► TASK-029 (Caching Service)
    TASK-005 ──► TASK-030 (Axios Interceptor)
    TASK-029 ──► TASK-031 (Cache Integration Tests)

Phase 5: Quality Assurance
    All Backend ──► TASK-032 (Backend Unit Tests)
    All Frontend ──► TASK-033 (Frontend Tests)
    All Backend ──► TASK-034 (Integration Tests)
    All Backend ──► TASK-035 (Postman Collection)
    All Backend ──► TASK-036 (Security Audit)

Phase 6: Deployment
    TASK-006 ──► TASK-037 (Prod Docker) ──► TASK-038 (CI/CD) ──► TASK-039 (Deploy Config)
    All ──► TASK-040 (Documentation)
    All ──► TASK-041 (Smoke Test)
```

---

## 10. Effort Summary

### 10.1 Effort by Module

| **Module**              | **Tasks** | **Backend Hours** | **Frontend Hours** | **Total Hours** |
| ----------------------- | --------- | ----------------- | ------------------ | --------------- |
| Infrastructure          | 6         | 20                | 12                 | 40              |
| Database                | 1         | 8                 | 0                  | 8               |
| Authentication          | 2         | 12                | 10                 | 22              |
| User Profile            | 2         | 8                 | 10                 | 18              |
| Health Vitals           | 2         | 10                | (in profile)       | 10              |
| Goals & Preferences     | 2         | 8                 | (in onboarding)    | 8               |
| Food Catalog            | 1         | 8                 | 0                  | 8               |
| Recommendation Engine   | 1         | 20                | 0                  | 20              |
| Meal Plans              | 2         | 10                | 12                 | 22              |
| Food Diary              | 2         | 8                 | 14                 | 22              |
| Progress Dashboard      | 2         | 12                | 12                 | 24              |
| Admin Panel             | 2         | 10                | 12                 | 22              |
| Public Pages            | 2         | 0                 | 12                 | 12              |
| Integration             | 5         | 14                | 10                 | 24              |
| Caching                 | 2         | 14                | 0                  | 14              |
| Testing                 | 5         | 26                | 10                 | 36              |
| Security                | 1         | 4                 | 0                  | 4               |
| Deployment              | 3         | 22                | 0                  | 22              |
| Documentation           | 1         | 8                 | 0                  | 8               |
| **Total**               | **49**    | **204**           | **155**            | **359**         |

### 10.2 Effort by Phase

| **Phase** | **Name**              | **Tasks** | **Hours** | **% of Total** |
| --------- | --------------------- | --------- | --------- | -------------- |
| 1         | Project Foundation    | 6         | 40        | 9%             |
| 2         | Backend Core          | 14        | 130       | 29%            |
| 3         | Frontend Core         | 14        | 130       | 29%            |
| 4         | Integration & Features| 5         | 30        | 7%             |
| 5         | Quality Assurance     | 5         | 40        | 9%             |
| 6         | Deployment & Delivery | 5         | 35        | 8%             |
| **Total**  |                       | **49**    | **359**   | **100%**       |

### 10.3 Team Sizing Estimates

| **Team Size** | **Estimated Timeline** | **Hours/Developer/Week** |
| ------------- | ---------------------- | ------------------------ |
| 1 developer   | ~14 weeks (3.5 months) | 32 hours/week            |
| 2 developers  | ~8 weeks (2 months)    | 28 hours/week each       |
| 3 developers  | ~6 weeks (1.5 months)  | 25 hours/week each       |
| 4 developers  | ~5 weeks (1.25 months) | 22 hours/week each       |

### 10.4 Parallelization Opportunities

| **Parallel Work Stream**         | **Tasks That Can Run In Parallel**                            |
| -------------------------------- | ------------------------------------------------------------- |
| **Stream 1: Backend**            | TASK-007 through TASK-016 (sequential within stream)          |
| **Stream 2: Frontend**           | TASK-017 through TASK-026 (sequential within stream)          |
| **Stream 3: Infrastructure**     | TASK-001, TASK-002, TASK-006 (can start immediately)          |
| **Stream 4: Testing**            | TASK-032 through TASK-036 (can start after respective modules)|

*Recommended for 3+ developer teams: Backend stream + Frontend stream + Infrastructure stream.*

---

## Document Version History

| **Version** | **Date**     | **Author** | **Changes**          |
| ----------- | ------------ | ---------- | -------------------- |
| 1.0         | 2026-07-27   | Architect  | Initial draft        |

---

*End of Document — 07_TASKS.md*
