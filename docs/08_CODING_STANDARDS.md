# 08 — CODING STANDARDS

| **Field**             | **Value**                                    |
| --------------------- | -------------------------------------------- |
| **Project Name**      | NutriGuide AI                                |
| **Document Title**    | Coding Standards & Best Practices            |
| **Version**           | 1.0                                          |
| **Author**            | Software Architecture Team                   |
| **Created On**        | 2026-07-27                                   |
| **Last Updated**      | 2026-07-27                                   |
| **Status**            | Draft                                        |
| **References**        | `01_PROJECT_CONTEXT.md`, `03_SYSTEM_ARCHITECTURE.md` |

---

## Table of Contents

1. [Java Standards](#1-java-standards)
2. [Spring Boot Standards](#2-spring-boot-standards)
3. [REST API Naming](#3-rest-api-naming)
4. [Exception Handling](#4-exception-handling)
5. [DTO Standards](#5-dto-standards)
6. [Repository Standards](#6-repository-standards)
7. [Service Standards](#7-service-standards)
8. [Controller Standards](#8-controller-standards)
9. [React Standards](#9-react-standards)
10. [Folder Naming](#10-folder-naming)
11. [Git Commit Standards](#11-git-commit-standards)
12. [Branch Naming](#12-branch-naming)
13. [Swagger Standards](#13-swagger-standards)
14. [Logging Standards](#14-logging-standards)
15. [Validation Standards](#15-validation-standards)
16. [Testing Standards](#16-testing-standards)
17. [SOLID Principles](#17-solid-principles)
18. [Best Practices](#18-best-practices)

---

## 1. Java Standards

### 1.1 Language Version

- **Java 21 (LTS)** — Use record classes, pattern matching, sealed classes, text blocks, and virtual threads where appropriate.

### 1.2 Naming Conventions

| **Element**           | **Convention**                  | **Example**                    | **Enforcement**          |
| --------------------- | ------------------------------- | ------------------------------ | ------------------------ |
| Classes               | PascalCase, noun                | `UserService`, `MealPlan`      | Checkstyle               |
| Interfaces            | PascalCase, adjective/noun      | `UserRepository`, `AiAdapter`  | Checkstyle               |
| Methods               | camelCase, verb                 | `generateMealPlan()`, `findById()` | Checkstyle           |
| Variables             | camelCase                       | `userId`, `mealPlanList`       | Checkstyle               |
| Constants             | UPPER_SNAKE_CASE                | `MAX_CALORIES_PER_MEAL`        | Checkstyle               |
| Packages              | lowercase, dot-separated        | `com.nutriguideai.service`     | Maven convention         |
| Enum values           | UPPER_SNAKE_CASE                | `WEIGHT_LOSS`, `DIABETES`      | Checkstyle               |
| Type parameters       | Single uppercase letter         | `T`, `E`, `K`, `V`            | IDE convention           |
| Boolean methods       | `is`/`has`/`can` prefix         | `isActive()`, `hasAllergies()` | Manual review            |
| Collection variables  | Plural noun                     | `users`, `mealPlans`           | Manual review            |

### 1.3 File Organization

Every Java source file must follow this order:

```java
// 1. Package declaration
package com.nutriguideai.service;

// 2. Import statements (no wildcard imports)
import com.nutriguideai.dto.request.MealPlanRequest;
import com.nutriguideai.entity.MealPlan;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// 3. Class-level Javadoc
/**
 * Service responsible for generating personalized meal plans.
 * Delegates to the RecommendationEngine for rule-based generation.
 */
// 4. Class declaration
@Service
@RequiredArgsConstructor
public class MealPlanService {

    // 5. Constants
    private static final int MAX_RETRY_ATTEMPTS = 3;

    // 6. Dependencies (constructor injection)
    private final MealPlanRepository mealPlanRepository;
    private final RecommendationEngine recommendationEngine;

    // 7. Public methods
    public MealPlanResponse generateMealPlan(Long userId) { ... }

    // 8. Private helper methods
    private void validateMealPlanRequest(MealPlanRequest request) { ... }
}
```

### 1.4 Prohibited Patterns

| **Pattern**                          | **Why**                                                  | **Alternative**                      |
| ------------------------------------ | -------------------------------------------------------- | ------------------------------------- |
| `System.out.println()`               | Not loggable, no context                                 | SLF4J logger                          |
| `e.printStackTrace()`                | Swallows stack trace in production                       | Logger with full exception            |
| `@Autowired` field injection         | Invisible dependencies, breaks testing                   | Constructor injection                 |
| Wildcard imports (`import foo.*`)    | Hides where classes come from                            | Explicit single imports               |
| Magic numbers                        | Untraceable, hard to change                              | Named constants                       |
| `catch (Exception e)`                | Catches everything, hides real errors                    | Catch specific exceptions             |
| `if (x == true)`                     | Redundant                                                | `if (x)`                              |
| Raw types (`List` instead of `List<String>`) | Unsafe, unchecked warnings                       | Parameterized types                   |
| Mutable static fields                | Thread-unsafe, unpredictable behavior                    | Final constants or instance variables |
| Double-checked locking               | Error-prone in Java pre-5                                | Use `Atomic` classes or synchronized  |

### 1.5 Method Design

| **Rule**                              | **Guideline**                                          |
| ------------------------------------- | ------------------------------------------------------ |
| Maximum lines per method              | ≤ 30 lines                                             |
| Maximum parameters                    | ≤ 4 (use a DTO if more needed)                         |
| Single responsibility                 | One method = one logical operation                     |
| Return type                           | Never return `null` — use `Optional<T>` or empty collections |
| Boolean parameters                    | Avoid — prefer enums or separate methods               |
| Nesting depth                         | Maximum 3 levels                                       |
| Early return                          | Fail fast: validate and return early                   |

### 1.6 Optional Usage Rules

```java
// ✅ CORRECT: Use Optional for return types that may be empty
public Optional<User> findByEmail(String email) {
    return userRepository.findByEmail(email);
}

// ✅ CORRECT: Chain Optional operations
public String getDisplayName(Long userId) {
    return userRepository.findById(userId)
        .map(User::getFullName)
        .orElse("Unknown User");
}

// ❌ WRONG: Never call get() without checking
User user = userRepository.findById(userId).get();

// ❌ WRONG: Never use Optional as a method parameter
public void processUser(Optional<User> user) { ... }

// ❌ WRONG: Never use Optional as a field type
public class MealPlan {
    private Optional<String> notes; // WRONG
}
```

---

## 2. Spring Boot Standards

### 2.1 Application Structure

```yaml
# application.yml — mandatory profiles
spring:
  profiles:
    active: dev  # dev, prod

# application-dev.yml — local development
# application-prod.yml — production
```

### 2.2 Bean Configuration

| **Rule**                                  | **Guideline**                                           |
| ----------------------------------------- | ------------------------------------------------------- |
| Configuration classes                     | Use `@Configuration` with `@Bean` for third-party beans |
| Component scanning                        | Default (root package `com.nutriguideai` is sufficient) |
| Externalized configuration                | All configurable values in `application.yml` or env vars |
| Property binding                          | Use `@ConfigurationProperties` for grouped properties   |
| Conditional beans                         | Use `@ConditionalOnProperty` for optional services (e.g., Redis) |
| Profile-specific beans                    | Use `@Profile("dev")` for development-only components   |

### 2.3 Transaction Management

```java
@Service
@Transactional(readOnly = true)  // Default: read-only
public class MealPlanService {

    private final MealPlanRepository mealPlanRepository;

    // Read-only methods inherit the class-level @Transactional
    public MealPlanResponse getMealPlan(Long planId) {
        return mealPlanRepository.findById(planId)
            .map(mealPlanMapper::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("MealPlan", planId));
    }

    // Write operations override with readOnly = false
    @Transactional
    public MealPlanResponse generateMealPlan(MealPlanRequest request) {
        // ... generation logic
        return mealPlanMapper.toResponse(savedPlan);
    }
}
```

| **Rule**                                        | **Guideline**                                          |
| ----------------------------------------------- | ------------------------------------------------------ |
| `@Transactional(readOnly = true)`               | Default at class level for all service classes         |
| `@Transactional` (writable)                     | Only on specific write methods                         |
| Transactional boundaries                        | Always at Service layer, never in Controller or Repository |
| Propagation                                     | Use `REQUIRED` (default) — avoid `REQUIRES_NEW` unless necessary |
| Rollback                                        | Always roll back on runtime exceptions (default behavior) |
| Isolation level                                 | `READ_COMMITTED` (default) is sufficient for v1        |

### 2.4 Dependency Injection Rules

```java
// ✅ CORRECT: Constructor injection with Lombok
@Service
@RequiredArgsConstructor  // Generates constructor for all final fields
public class MealPlanService {
    private final MealPlanRepository mealPlanRepository;
    private final RecommendationEngine recommendationEngine;
}

// ❌ WRONG: Field injection
@Service
public class MealPlanService {
    @Autowired  // Never use @Autowired on fields
    private MealPlanRepository mealPlanRepository;
}

// ❌ WRONG: Setter injection
@Service
public class MealPlanService {
    private MealPlanRepository mealPlanRepository;

    @Autowired  // Never use setter injection
    public void setMealPlanRepository(MealPlanRepository repo) {
        this.mealPlanRepository = repo;
    }
}
```

### 2.5 CORS Configuration

```java
@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/v1/**")
                    .allowedOrigins(
                        "http://localhost:5173",          // Vite dev
                        "https://nutriguideai.vercel.app" // Production
                    )
                    .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE")
                    .allowedHeaders("Authorization", "Content-Type")
                    .exposedHeaders("Authorization");
            }
        };
    }
}
```

---

## 3. REST API Naming

### 3.1 URL Patterns

| **Resource**       | **GET**                          | **POST**                  | **PUT/PATCH**                | **DELETE**               |
| ------------------ | -------------------------------- | ------------------------- | ---------------------------- | ------------------------ |
| Users              | `/api/v1/users`                  | `/api/v1/users`           | `/api/v1/users/{id}`         | `/api/v1/users/{id}`     |
| Profiles           | `/api/v1/profiles/{userId}`      | —                         | `/api/v1/profiles/{userId}`  | —                        |
| Health Vitals      | `/api/v1/vitals/{userId}`        | —                         | `/api/v1/vitals/{userId}`    | —                        |
| Meal Plans         | `/api/v1/meal-plans/{date}`      | `/api/v1/meal-plans`      | `/api/v1/meal-plans/{id}`    | —                        |
| Food Diary         | `/api/v1/diary?date={date}`      | `/api/v1/diary`           | —                            | `/api/v1/diary/{id}`     |
| Progress           | `/api/v1/progress/summary`       | —                         | —                            | —                        |
| Auth               | —                                | `/api/v1/auth/register`   | —                            | —                        |
|                    |                                  | `/api/v1/auth/login`      |                              |                          |
| Admin              | `/api/v1/admin/users`            | —                         | `/api/v1/admin/users/{id}`   | —                        |

### 3.2 Naming Rules

| **Rule**                                      | **Correct**                             | **Incorrect**                        |
| --------------------------------------------- | --------------------------------------- | ------------------------------------ |
| Use nouns, not verbs                          | `/api/v1/users`                         | `/api/v1/getUsers`                   |
| Plural nouns for collections                  | `/api/v1/meal-plans`                    | `/api/v1/mealplan`                   |
| Hyphen-separated for multi-word resources     | `/api/v1/food-items`                    | `/api/v1/foodItems` / `/api/v1/food_items` |
| Version in URL                                | `/api/v1/...`                           | `/api/...` (no version)              |
| Query params for filtering                    | `/api/v1/meal-plans?date=2026-07-27`    | `/api/v1/meal-plans/2026-07-27`      |
| Path params for resource identity             | `/api/v1/users/{userId}`                | `/api/v1/users?userId=42`            |
| No trailing slashes                           | `/api/v1/users`                         | `/api/v1/users/`                     |
| Lowercase only                                | `/api/v1/auth/login`                    | `/api/v1/Auth/Login`                 |

### 3.3 HTTP Methods and Status Codes

| **Operation** | **HTTP Method** | **Success Code**          | **Error Codes**                                  |
| ------------- | --------------- | ------------------------- | ------------------------------------------------ |
| Create        | `POST`          | `201 Created`             | `400 Bad Request`, `409 Conflict`                |
| Read (single) | `GET`           | `200 OK`                  | `404 Not Found`                                  |
| Read (list)   | `GET`           | `200 OK`                  | `400 Bad Request` (invalid params)               |
| Update (full) | `PUT`           | `200 OK`                  | `400 Bad Request`, `404 Not Found`               |
| Update (partial)| `PATCH`       | `200 OK`                  | `400 Bad Request`, `404 Not Found`               |
| Delete        | `DELETE`        | `204 No Content`          | `404 Not Found`                                  |
| Auth          | `POST`          | `200 OK`                  | `401 Unauthorized`, `400 Bad Request`            |

### 3.4 Response Structure

```json
// ✅ SUCCESS RESPONSE
{
    "status": "success",
    "data": {
        "userId": 1,
        "email": "john@example.com",
        "profile": { ... }
    },
    "timestamp": "2026-07-27T10:30:00Z"
}

// ✅ ERROR RESPONSE
{
    "status": "error",
    "error": {
        "code": "RESOURCE_NOT_FOUND",
        "message": "User not found with id: 42",
        "details": null,
        "path": "/api/v1/users/42",
        "timestamp": "2026-07-27T10:30:00Z"
    }
}

// ✅ PAGINATION RESPONSE
{
    "status": "success",
    "data": [ ... ],
    "pagination": {
        "page": 0,
        "size": 20,
        "totalElements": 150,
        "totalPages": 8
    }
}
```

---

## 4. Exception Handling

### 4.1 Exception Hierarchy

```
RuntimeException
├── ResourceNotFoundException    (404)
├── BadRequestException          (400)
├── UnauthorizedException        (401)
├── ForbiddenException           (403)
├── ConflictException            (409)
├── DuplicateResourceException   (409)
├── ValidationException          (400)
├── AIServiceException           (502)
└── FileStorageException         (500)
```

### 4.2 Global Exception Handler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException ex, WebRequest request) {
        ErrorResponse error = ErrorResponse.builder()
            .code("RESOURCE_NOT_FOUND")
            .message(ex.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .timestamp(LocalDateTime.now())
            .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, WebRequest request) {
        List<FieldError> fieldErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(fe -> new FieldError(fe.getField(), fe.getDefaultMessage()))
            .toList();
        ErrorResponse error = ErrorResponse.builder()
            .code("VALIDATION_FAILED")
            .message("Input validation failed")
            .details(fieldErrors)
            .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(
            Exception ex, WebRequest request) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        ErrorResponse error = ErrorResponse.builder()
            .code("INTERNAL_ERROR")
            .message("An unexpected error occurred")
            .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

### 4.3 Custom Exception Template

```java
public class ResourceNotFoundException extends RuntimeException {
    private final String resourceName;
    private final Object resourceId;

    public ResourceNotFoundException(String resourceName, Object resourceId) {
        super(String.format("%s not found with identifier: %s", resourceName, resourceId));
        this.resourceName = resourceName;
        this.resourceId = resourceId;
    }

    public String getResourceName() { return resourceName; }
    public Object getResourceId() { return resourceId; }
}
```

### 4.4 Error Response Envelope

```java
@Data
@Builder
public class ErrorResponse {
    private String code;
    private String message;
    private Object details;
    private String path;
    private LocalDateTime timestamp;
}
```

### 4.5 Exception Handling Rules

| **Rule**                                    | **Guideline**                                          |
| ------------------------------------------- | ------------------------------------------------------ |
| Throw early, catch late                     | Validate at controller boundary, handle in `@RestControllerAdvice` |
| Specific exceptions                         | Never throw `RuntimeException` — always use/extend custom types |
| Business exceptions in service layer        | Services throw domain exceptions like `ResourceNotFoundException` |
| Validation exceptions in controller layer   | `@Valid` on request DTOs triggers `MethodArgumentNotValidException` |
| Never catch `Exception`                     | Catch the most specific exception type possible        |
| Log at point of origin                      | Log the error where it occurs, not where it's handled  |
| Never swallow exceptions                    | Empty catch blocks are forbidden                       |

---

## 5. DTO Standards

### 5.1 DTO Rules

| **Rule**                                        | **Guideline**                                          |
| ----------------------------------------------- | ------------------------------------------------------ |
| Never expose entities to the client             | Always map Entity → DTO before sending as response     |
| Request DTOs in `dto/request/`                  | `CreateUserRequest`, `LoginRequest`, `MealPlanRequest` |
| Response DTOs in `dto/response/`                | `UserResponse`, `MealPlanResponse`, `AuthResponse`     |
| Use `record` for simple DTOs (Java 21)           | `public record UserResponse(Long id, String email) {}` |
| Use `@Builder` for complex DTOs                 | Lombok `@Data @Builder` for DTOs with many fields      |
| Validation annotations on request DTOs          | `@NotBlank`, `@Email`, `@Min`, `@Max`, `@Pattern`      |
| No business logic in DTOs                       | DTOs are pure data carriers, no methods with logic      |
| Use MapStruct for Entity↔DTO mapping            | Compile-time generated mappers, zero runtime overhead   |

### 5.2 DTO Examples

```java
// ✅ CORRECT: Request DTO with validation
public record CreateMealPlanRequest(
    @NotNull(message = "Date is required")
    @FutureOrPresent(message = "Date must be today or in the future")
    LocalDate planDate,

    @NotNull(message = "Diet type is required")
    DietType dietType,

    @NotNull(message = "Budget level is required")
    BudgetLevel budgetLevel
) {}

// ✅ CORRECT: Response DTO
@Data
@Builder
public class MealPlanResponse {
    private Long planId;
    private LocalDate planDate;
    private Integer totalCalories;
    private Double totalProteinG;
    private Double totalCarbsG;
    private Double totalFatG;
    private List<MealItemResponse> meals;
    private List<String> dietaryTips;
    private LocalDateTime createdAt;
}

// ❌ WRONG: Never expose JPA entity directly
// Never do this:
@GetMapping("/{id}")
public User getUser(@PathVariable Long id) {
    return userRepository.findById(id).orElseThrow(); // Exposes entity!
}
```

### 5.3 MapStruct Mapper Pattern

```java
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface MealPlanMapper {

    MealPlanResponse toResponse(MealPlan entity);

    @Mapping(target = "planId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    MealPlan toEntity(CreateMealPlanRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(CreateMealPlanRequest request, @MappingTarget MealPlan entity);
}
```

---

## 6. Repository Standards

### 6.1 Repository Naming

| **Entity**          | **Repository Name**              | **Extends**                |
| ------------------- | -------------------------------- | -------------------------- |
| User                | `UserRepository`                 | `JpaRepository<User, Long>` |
| UserProfile         | `UserProfileRepository`          | `JpaRepository<UserProfile, Long>` |
| MealPlan            | `MealPlanRepository`             | `JpaRepository<MealPlan, Long>` |
| FoodItem            | `FoodItemRepository`             | `JpaRepository<FoodItem, Long>` |
| FoodDiaryEntry      | `FoodDiaryEntryRepository`       | `JpaRepository<FoodDiaryEntry, Long>` |

### 6.2 Query Method Naming

| **Pattern**                        | **Example**                                    |
| ---------------------------------- | ---------------------------------------------- |
| `findBy{Field}`                    | `findByEmail(String email)`                    |
| `findBy{Field}And{Field}`          | `findByUserIdAndPlanDate(Long userId, LocalDate date)` |
| `findBy{Field}Between`             | `findByCreatedAtBetween(LocalDate start, LocalDate end)` |
| `findBy{Field}OrderBy{Field}Desc`  | `findByUserIdOrderByRecordedDateDesc(Long userId)` |
| `countBy{Field}`                   | `countByUserId(Long userId)`                   |
| `existsBy{Field}`                  | `existsByEmail(String email)`                  |
| `deleteBy{Field}`                  | `deleteByUserId(Long userId)`                  |

### 6.3 Repository Rules

| **Rule**                                        | **Guideline**                                          |
| ----------------------------------------------- | ------------------------------------------------------ |
| Use derived query methods for simple queries    | Let Spring Data JPA generate the implementation        |
| Use `@Query` for complex queries (JPQL)         | Write explicit JPQL for multi-table joins or aggregations |
| Use `Specification` for dynamic filtering       | Build queries dynamically with reusable specifications  |
| Always use parameterized queries                | Never concatenate strings into JPQL/SQL                |
| Return `Optional` for single-result queries     | `Optional<User>` instead of `User` that could be null   |
| Return `Page<T>` for paginated queries          | Use `Pageable` parameter for listing endpoints         |
| Never call repositories from controllers        | Repository calls only allowed in Service layer         |

### 6.4 Repository Examples

```java
public interface MealPlanRepository extends JpaRepository<MealPlan, Long> {

    // Derived query
    Optional<MealPlan> findByUserIdAndPlanDate(Long userId, LocalDate planDate);

    // Derived query with ordering
    List<MealPlan> findByUserIdOrderByPlanDateDesc(Long userId);

    // Custom JPQL query
    @Query("SELECT mp FROM MealPlan mp WHERE mp.user.id = :userId " +
           "AND mp.planDate BETWEEN :startDate AND :endDate " +
           "ORDER BY mp.planDate ASC")
    List<MealPlan> findMealPlansByDateRange(
        @Param("userId") Long userId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    // Paginated query
    Page<MealPlan> findByUserId(Long userId, Pageable pageable);

    // Existence check
    boolean existsByUserIdAndPlanDate(Long userId, LocalDate planDate);
}

public interface FoodItemRepository extends JpaRepository<FoodItem, Long>,
                                            JpaSpecificationExecutor<FoodItem> {

    // Full-text search
    @Query(value = "SELECT * FROM food_items WHERE MATCH(food_name) AGAINST(:keyword IN BOOLEAN MODE)",
           nativeQuery = true)
    List<FoodItem> searchByFoodName(@Param("keyword") String keyword);

    // Filter by category
    List<FoodItem> findByCategoryAndIsActiveTrue(String category);
}
```

---

## 7. Service Standards

### 7.1 Service Structure

```java
// Interface (contract)
public interface MealPlanService {
    MealPlanResponse generateMealPlan(Long userId, CreateMealPlanRequest request);
    MealPlanResponse getMealPlanByDate(Long userId, LocalDate date);
    Page<MealPlanResponse> getMealPlanHistory(Long userId, Pageable pageable);
}

// Implementation
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MealPlanServiceImpl implements MealPlanService {

    private final MealPlanRepository mealPlanRepository;
    private final RecommendationEngine recommendationEngine;
    private final MealPlanMapper mealPlanMapper;
    private final CacheService cacheService;

    @Override
    @Transactional
    public MealPlanResponse generateMealPlan(Long userId, CreateMealPlanRequest request) {
        // 1. Validate business rules
        validateGenerationEligibility(userId, request.planDate());

        // 2. Execute core logic
        MealPlan plan = recommendationEngine.generate(userId, request);

        // 3. Persist
        MealPlan savedPlan = mealPlanRepository.save(plan);

        // 4. Invalidate cache
        cacheService.evictMealPlan(userId, request.planDate());

        // 5. Map to DTO and return
        return mealPlanMapper.toResponse(savedPlan);
    }

    @Override
    public MealPlanResponse getMealPlanByDate(Long userId, LocalDate date) {
        // Check cache first
        return cacheService.getMealPlan(userId, date)
            .orElseGet(() -> {
                MealPlan plan = mealPlanRepository
                    .findByUserIdAndPlanDate(userId, date)
                    .orElseThrow(() -> new ResourceNotFoundException("MealPlan", userId + ":" + date));
                return mealPlanMapper.toResponse(plan);
            });
    }

    private void validateGenerationEligibility(Long userId, LocalDate date) {
        if (mealPlanRepository.existsByUserIdAndPlanDate(userId, date)) {
            throw new ConflictException("Meal plan already exists for this date");
        }
    }
}
```

### 7.2 Service Layer Rules

| **Rule**                                        | **Guideline**                                          |
| ----------------------------------------------- | ------------------------------------------------------ |
| Interface + Implementation pattern              | Define interface first, implement with `Impl` suffix   |
| Business logic only in service layer            | Controllers orchestrate, services execute              |
| Transactional boundaries at service layer       | `@Transactional` on service methods, not controllers   |
| Cache interactions in service layer             | Services check/evict cache; repositories never do      |
| DTO ↔ Entity mapping in service layer           | Use MapStruct mappers injected into services           |
| Cross-cutting concerns (logging, audit) in service | Aspect-oriented or explicit in service methods        |
| Service methods named as business operations    | `generateMealPlan()`, not `createMealPlanRecord()`     |
| Single service responsibility                   | Each service handles one domain aggregate              |

### 7.3 Service Layer Prohibited Patterns

```java
// ❌ WRONG: Service calling another service that then calls repository
// Creates unnecessary coupling
@Service
public class NutritionService {
    public void generate(Long userId) {
        userService.getUser(userId);  // ❌ Don't call services for simple data access
        mealPlanService.checkExisting(); // ❌
    }
}

// ✅ CORRECT: Service accesses repositories it needs directly
@Service
public class NutritionService {
    private final UserRepository userRepository;
    private final MealPlanRepository mealPlanRepository;

    public void generate(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        // ... logic
    }
}
```

---

## 8. Controller Standards

### 8.1 Controller Structure

```java
@RestController
@RequestMapping("/api/v1/meal-plans")
@RequiredArgsConstructor
@Tag(name = "Meal Plans", description = "APIs for meal plan generation and retrieval")
public class MealPlanController {

    private final MealPlanService mealPlanService;

    @PostMapping
    @Operation(summary = "Generate a personalized meal plan")
    @ApiResponse(responseCode = "201", description = "Meal plan generated successfully")
    public ResponseEntity<ApiResponse<MealPlanResponse>> generateMealPlan(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CreateMealPlanRequest request) {
        MealPlanResponse response = mealPlanService.generateMealPlan(userId, request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(response));
    }

    @GetMapping("/{date}")
    @Operation(summary = "Get meal plan for a specific date")
    public ResponseEntity<ApiResponse<MealPlanResponse>> getMealPlanByDate(
            @AuthenticationPrincipal Long userId,
            @PathVariable @DateTimeFormat(iso = ISO.DATE) LocalDate date) {
        MealPlanResponse response = mealPlanService.getMealPlanByDate(userId, date);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "Get paginated meal plan history")
    public ResponseEntity<ApiResponse<Page<MealPlanResponse>>> getMealPlanHistory(
            @AuthenticationPrincipal Long userId,
            @PageableDefault(size = 20, sort = "planDate", direction = Sort.Direction.DESC)
            Pageable pageable) {
        Page<MealPlanResponse> history = mealPlanService.getMealPlanHistory(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(history));
    }
}
```

### 8.2 Controller Rules

| **Rule**                                        | **Guideline**                                          |
| ----------------------------------------------- | ------------------------------------------------------ |
| No business logic in controllers                | Controllers only validate input and delegate to service |
| `@Valid` on all request body DTOs               | Let Spring Validation filter invalid input             |
| `@AuthenticationPrincipal` for current user      | Extract authenticated user ID from JWT via custom argument resolver |
| Consistent response envelope                    | Always wrap responses in `ApiResponse.success(data)`   |
| HTTP status codes aligned with REST standards   | 201 for create, 200 for read/update, 204 for delete    |
| `@Tag` and `@Operation` on every endpoint       | Enable Swagger documentation generation               |
| `@RequestMapping` at class level                | Define base path once, not on every method             |
| No `@Transactional` on controller methods       | Transactions belong in the service layer               |
| No repository injections in controllers         | Controllers only access services                       |

### 8.3 Standard API Response Envelope

```java
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private String status;
    private T data;
    private String message;
    private LocalDateTime timestamp;

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.status = "success";
        response.data = data;
        response.timestamp = LocalDateTime.now();
        return response;
    }

    public static <T> ApiResponse<T> success(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.status = "success";
        response.message = message;
        response.timestamp = LocalDateTime.now();
        return response;
    }
}
```

---

## 9. React Standards

### 9.1 Component Structure

```tsx
// ✅ CORRECT: Component file structure
import { useState, useEffect } from 'react';
import { apiClient } from '@/api/axios';
import { LoadingSpinner } from '@/components/common/LoadingSpinner';
import { ErrorState } from '@/components/common/ErrorState';
import { EmptyState } from '@/components/common/EmptyState';

interface MealPlanCardProps {
    planId: number;
    date: string;
    totalCalories: number;
}

export function MealPlanCard({ planId, date, totalCalories }: MealPlanCardProps) {
    // State at top
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    // Effects
    useEffect(() => {
        // fetch data
    }, [planId]);

    // Event handlers
    const handleRefresh = async () => { /* ... */ };

    // Conditional rendering
    if (loading) return <LoadingSpinner />;
    if (error) return <ErrorState message={error} onRetry={handleRefresh} />;

    return (
        <div className="rounded-lg border p-4">
            <h3 className="text-lg font-medium">{date}</h3>
            <p className="text-muted-foreground">{totalCalories} kcal</p>
        </div>
    );
}
```

### 9.2 Component Rules

| **Rule**                                        | **Guideline**                                          |
| ----------------------------------------------- | ------------------------------------------------------ |
| One component per file                          | Each `.tsx` file exports exactly one component         |
| Use functional components with hooks            | No class components                                    |
| Props typed with TypeScript `interface`         | Always define `interface ComponentNameProps`           |
| Consistent naming: PascalCase for components    | `MealPlanCard`, `UserProfile`                          |
| Consistent naming: camelCase for hooks/files    | `useAuth`, `useMealPlan`                               |
| Custom hooks for reusable logic                 | Encapsulate API calls + loading/error states in hooks  |
| Always handle 4 states: loading, error, empty, data | Every data-fetching component handles all states  |
| Avoid prop drilling                             | Use Context for auth state; keep domain data in hooks  |
| `useEffect` dependencies explicit               | Always list all dependencies; avoid `[]` unless intentional |

### 9.3 Hook Pattern

```tsx
// ✅ CORRECT: Custom hook pattern
import { useState, useEffect } from 'react';
import { nutritionApi } from '@/api/nutritionApi';
import type { MealPlan } from '@/types';

interface UseMealPlanResult {
    mealPlan: MealPlan | null;
    loading: boolean;
    error: string | null;
    refresh: () => void;
}

export function useMealPlan(date: string): UseMealPlanResult {
    const [mealPlan, setMealPlan] = useState<MealPlan | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const fetchMealPlan = async () => {
        setLoading(true);
        setError(null);
        try {
            const data = await nutritionApi.getMealPlan(date);
            setMealPlan(data);
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Failed to load meal plan');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchMealPlan();
    }, [date]);

    return { mealPlan, loading, error, refresh: fetchMealPlan };
}
```

### 9.4 CSS/Styling Rules

| **Rule**                                        | **Guideline**                                          |
| ----------------------------------------------- | ------------------------------------------------------ |
| Use Tailwind utility classes                    | No custom CSS files unless absolutely necessary        |
| Extract repeated utility groups into components | Create reusable styled components (e.g., `Card`, `Button`) |
| Responsive design                               | Use Tailwind breakpoints: `sm:`, `md:`, `lg:`, `xl:`  |
| Consistent spacing                              | Use Tailwind spacing scale: `p-4`, `gap-3`, `space-y-2` |
| Theme colors only                                | Use CSS variables from `index.css`, not hardcoded colors |
| No inline styles                                | Use Tailwind classes or CSS modules                    |

### 9.5 Axios Interceptor Pattern

```tsx
// src/api/axios.ts
import axios from 'axios';

const apiClient = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1',
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json',
    },
});

// Request interceptor — attach JWT
apiClient.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('accessToken');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);

// Response interceptor — handle 401
apiClient.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401) {
            localStorage.removeItem('accessToken');
            window.location.href = '/auth?expired=true';
        }
        return Promise.reject(error);
    }
);

export { apiClient };
```

---

## 10. Folder Naming

### 10.1 Backend Package Structure

```
com.nutriguideai/
├── config/           ← Configuration classes (Security, CORS, Swagger, App)
├── controller/       ← REST controllers (one per domain)
├── dto/
│   ├── request/      ← Request DTOs (CreateXxxRequest, UpdateXxxRequest)
│   └── response/     ← Response DTOs (XxxResponse, XxxSummaryResponse)
├── entity/           ← JPA entities (one per database table)
├── enums/            ← Shared enum types
├── exception/        ← Custom exceptions + GlobalExceptionHandler
├── mapper/           ← MapStruct interfaces
├── repository/       ← Spring Data JPA repositories
├── security/         ← JWT provider, auth filter, user details
├── service/          ← Business logic interfaces + implementations
└── util/             ← Utility/helper classes
```

### 10.2 Frontend Folder Structure

```
src/
├── api/              ← Axios instance + per-module API functions
├── components/
│   ├── common/       ← Shared components (Button, Card, Modal, LoadingSpinner)
│   ├── auth/         ← Login form, Register form
│   ├── dashboard/    ← Charts, summary cards
│   ├── meal-plan/    ← Meal cards, meal grid
│   ├── diary/        ← Diary entry form, calendar
│   ├── progress/     ← Weight chart, macro chart
│   └── profile/      ← Health form, preferences form
├── context/          ← React context providers
├── hooks/            ← Custom hooks (useAuth, useMealPlan, useProgress)
├── pages/            ← Page-level route components
├── types/            ← TypeScript interfaces/types
└── utils/            ← Constants, helpers, formatters
```

### 10.3 Naming Rules Summary

| **Element**            | **Case**        | **Example**                        |
| ---------------------- | --------------- | ---------------------------------- |
| Java packages          | lowercase       | `com.nutriguideai.service`         |
| Java classes           | PascalCase      | `MealPlanServiceImpl`              |
| Java interfaces        | PascalCase      | `MealPlanService`                  |
| Java methods/fields    | camelCase       | `generateMealPlan()`               |
| Java constants         | UPPER_SNAKE_CASE| `MAX_RETRY_ATTEMPTS`               |
| React components       | PascalCase      | `MealPlanCard.tsx`                 |
| React hooks            | camelCase       | `useMealPlan.ts`                   |
| API modules            | camelCase       | `nutritionApi.ts`                  |
| CSS/Tailwind files     | kebab-case      | `meal-plan-card.module.css`        |
| Database columns       | snake_case      | `user_id`, `plan_date`             |
| REST endpoints         | kebab-case      | `/api/v1/meal-plans`               |

---

## 11. Git Commit Standards

### 11.1 Commit Message Format

```
<type>(<scope>): <description>

[optional body]

[optional footer]
```

### 11.2 Commit Types

| **Type**     | **Usage**                                        | **Example**                                      |
| ------------ | ------------------------------------------------ | ------------------------------------------------ |
| `feat`       | New feature                                      | `feat(auth): add JWT token refresh endpoint`     |
| `fix`        | Bug fix                                          | `fix(meal-plan): handle null dietary tips`       |
| `docs`       | Documentation changes                            | `docs(api): update meal plan contract`           |
| `test`       | Adding or fixing tests                           | `test(service): add NutritionService unit tests` |
| `refactor`   | Code restructuring without behavior change       | `refactor(dto): extract base response class`     |
| `style`      | Formatting, whitespace, imports                  | `style: fix indentation in SecurityConfig`       |
| `chore`      | Build, dependencies, configuration               | `chore(deps): upgrade Spring Boot to 3.2`        |
| `perf`       | Performance improvement                          | `perf(cache): add Redis caching for food catalog`|
| `ci`         | CI/CD configuration                              | `ci: add GitHub Actions workflow`                |
| `security`   | Security fix                                     | `security: sanitize user input in profile`       |

### 11.3 Commit Rules

| **Rule**                                    | **Guideline**                                          |
| ------------------------------------------- | ------------------------------------------------------ |
| One commit = one logical change             | Don't mix unrelated changes in a single commit         |
| Imperative mood                            | "Add feature" not "Added feature" or "Adding feature"  |
| Subject line ≤ 72 characters               | Keep it concise, use body for details                  |
| Body explains what and why, not how        | Focus on the motivation and reasoning                  |
| Reference issue/PR numbers in footer       | `Closes #42` or `Related to #15`                       |
| No `-m "fixed stuff"` commits              | Every commit message must be meaningful                |
| Commit related files together              | Don't split a single logical change across commits     |
| No binary files except build outputs       | Keep repo lean — use `.gitignore`                      |

### 11.4 Example Commits

```bash
# ✅ GOOD COMMITS
feat(auth): add user registration endpoint

- Implement POST /api/v1/auth/register
- Add password validation with BCrypt hashing
- Return JWT token on successful registration

Closes #23

fix(meal-plan): handle empty food catalog gracefully

When no foods match the user's diet type and budget,
return a 200 with meaningful message instead of 500 error.

Fixes #47

docs(api): update meal plan API contract

- Add pagination parameters to GET /meal-plans
- Document error responses for all endpoints

# ❌ BAD COMMITS
fixed stuff
update
WIP
asdasd
```

---

## 12. Branch Naming

### 12.1 Branch Naming Convention

```
<type>/<short-description>
```

### 12.2 Branch Types

| **Type**       | **Pattern**                          | **Example**                              |
| -------------- | ------------------------------------ | ---------------------------------------- |
| Feature        | `feature/<module>-<description>`     | `feature/user-authentication`            |
| Bug Fix        | `fix/<issue-id>-<description>`       | `fix/42-null-pointer-meal-plan`          |
| Hotfix         | `hotfix/<issue-id>-<description>`    | `hotfix/58-security-vulnerability`       |
| Release        | `release/v<version>`                 | `release/v1.0.0`                         |
| Documentation  | `docs/<what-is-being-documented>`    | `docs/api-contract-update`               |
| Refactor       | `refactor/<module-or-description>`   | `refactor/extract-cache-service`         |
| Chore          | `chore/<description>`                | `chore/upgrade-spring-boot`              |

### 12.3 Branch Rules

| **Rule**                                    | **Guideline**                                          |
| ------------------------------------------- | ------------------------------------------------------ |
| Always branch from `develop`                | Never branch from `main` directly                      |
| Use hyphens, not underscores                | `feature/user-auth` not `feature/user_auth`            |
| Keep branch names short but descriptive     | `feature/user-auth` over `feature/implementing-user-authentication-functionality` |
| Delete branch after merge                   | Clean up remote branches after PR approval              |
| No personal branch names                    | Use feature/fix/descriptive names, not author names     |
| One feature = one branch                    | Don't combine unrelated features in one branch          |
| Use lowercase only                          | `feature/MealPlan` is incorrect — use `feature/meal-plan` |

---

## 13. Swagger Standards

### 13.1 Swagger/OpenAPI Configuration

```java
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI nutriguideAiOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("NutriGuide AI API")
                .description("REST API for AI-powered personalized nutrition and meal planning")
                .version("1.0.0")
                .contact(new Contact()
                    .name("Development Team")
                    .email("dev@nutriguideai.com"))
                .license(new License()
                    .name("Private")
                    .url("https://nutriguideai.com/license")))
            .addSecurityItem(new SecurityRequirement()
                .addList("bearerAuth"))
            .components(new Components()
                .addSecuritySchemes("bearerAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Enter JWT token: Bearer <token>")));
    }
}
```

### 13.2 Controller Annotations

| **Annotation**             | **Required** | **Purpose**                              |
| -------------------------- | ------------ | ---------------------------------------- |
| `@Tag(name, description)`  | Always       | Groups endpoints by domain               |
| `@Operation(summary)`      | Always       | Describes what the endpoint does         |
| `@ApiResponse`             | Always       | Documents response codes and schemas     |
| `@Parameter`               | For params   | Describes path/query parameters          |
| `@Schema`                  | For DTOs     | Documents field descriptions and examples |

### 13.3 DTO Schema Annotations

```java
@Data
@Builder
@Schema(description = "Response object for meal plan")
public class MealPlanResponse {

    @Schema(description = "Unique plan identifier", example = "42")
    private Long planId;

    @Schema(description = "Date the plan is for", example = "2026-07-27")
    private LocalDate planDate;

    @Schema(description = "Total daily calories", example = "1850")
    private Integer totalCalories;

    @Schema(description = "Daily nutritional tips")
    private List<String> dietaryTips;
}
```

### 13.4 Swagger Rules

| **Rule**                                        | **Guideline**                                          |
| ----------------------------------------------- | ------------------------------------------------------ |
| Every public endpoint must have `@Operation`    | No undocumented endpoints in production                |
| Every request/response DTO must have `@Schema`  | Examples must be realistic, not placeholder values      |
| Every controller must have `@Tag`               | Tags must be consistent with module names               |
| Security scheme must be configured globally     | JWT Bearer token as default security requirement        |
| Deprecated endpoints annotated with `@Deprecated` | Mark endpoints that should not be used in new integrations |
| Response codes documented for all states        | 200/201/204 for success, 400/401/404/500 for errors     |

---

## 14. Logging Standards

### 14.1 Logger Declaration

```java
// ✅ CORRECT: Use Lombok @Slf4j
@Slf4j
@Service
public class MealPlanService {
    // log field auto-generated by Lombok
    public void generateMealPlan(Long userId) {
        log.info("Generating meal plan for user: {}", userId);
    }
}

// ✅ CORRECT: Manual declaration (if not using Lombok)
@Service
public class MealPlanService {
    private static final Logger log = LoggerFactory.getLogger(MealPlanService.class);
}
```

### 14.2 Log Levels

| **Level** | **Usage**                                          | **Example**                                      |
| --------- | -------------------------------------------------- | ------------------------------------------------ |
| `ERROR`   | System is broken — action required                 | Database connection failed, external API down    |
| `WARN`    | Something unexpected but system can continue       | Cache miss, retry attempt, deprecated API used   |
| `INFO`    | Significant business events                        | User registered, meal plan generated, order placed |
| `DEBUG`   | Detailed diagnostics for developers                | SQL queries, method entry/exit, request details  |
| `TRACE`   | Lowest-level details (rarely used)                 | Loop iterations, fine-grained flow               |

### 14.3 Logging Rules

| **Rule**                                        | **Guideline**                                          |
| ----------------------------------------------- | ------------------------------------------------------ |
| Use parameterized logging                       | `log.info("User: {}", userId)` — never concatenate with `+` |
| Log at the point of origin                      | Log errors where they occur, not where they're caught  |
| Never log sensitive data                        | No passwords, tokens, health data in logs              |
| Include correlation/request IDs                 | Use MDC for traceability across requests               |
| Log entry/exit only at DEBUG level              | Don't log every method call at INFO                    |
| Include context in log messages                 | "User 42: meal plan generated" not "Plan generated"    |
| Structured logging (JSON) in production         | Easy to parse with log aggregation tools               |
| No `System.out.println()`                       | Never — use logger                     |

### 14.4 Logging Examples

```java
// ✅ GOOD: Parameterized, contextual
log.info("Generating meal plan for user: {}, date: {}", userId, planDate);

// ✅ GOOD: Log with exception
try {
    // risky operation
} catch (DataAccessException e) {
    log.error("Failed to save meal plan for user: {}", userId, e);
    throw new AIServiceException("Failed to generate meal plan", e);
}

// ❌ BAD: String concatenation (performance hit)
log.info("Generating meal plan for user: " + userId + " date: " + planDate);

// ❌ BAD: Logging sensitive data
log.info("User password: {}", passwordHash); // Never log password hashes

// ❌ BAD: Empty catch with no log
try { ... } catch (Exception e) {} // Silent failure
```

### 14.5 Logback Configuration Rules

```xml
<!-- logback-spring.xml rules -->
- Console appender: INFO+ in dev, ERROR+ in prod
- File appender: Rotate daily, keep 30 days
- Pattern: %d{ISO8601} [%thread] %-5level %logger{36} - %msg%n
- No logging of health check endpoints (/actuator/health)
- Package-level thresholds: com.nutriguideai=DEBUG in dev
```

---

## 15. Validation Standards

### 15.1 Validation Layers

```java
// Layer 1: Controller — Request body validation
@PostMapping
public ResponseEntity<ApiResponse<MealPlanResponse>> generateMealPlan(
    @Valid @RequestBody CreateMealPlanRequest request) { ... }

// Layer 2: Service — Business rule validation
@Service
public class MealPlanServiceImpl implements MealPlanService {
    public MealPlanResponse generateMealPlan(Long userId, CreateMealPlanRequest request) {
        validateGenerationEligibility(userId, request.planDate());
        // ...
    }
}

// Layer 3: Entity — Database constraints
// NOT NULL, UNIQUE, CHECK constraints in MySQL
```

### 15.2 Bean Validation Annotations

| **Annotation**             | **Usage**                    | **Example**                                    |
| -------------------------- | ---------------------------- | ---------------------------------------------- |
| `@NotBlank`                | Strings that must not be null/empty | `@NotBlank String email`                |
| `@NotNull`                 | Required fields              | `@NotNull Long userId`                         |
| `@Email`                   | Valid email format           | `@Email String email`                          |
| `@Min` / `@Max`            | Numeric range                | `@Min(50) @Max(300) Double heightCm`           |
| `@Positive`                | Positive numeric values      | `@Positive Double weightKg`                    |
| `@Size`                    | String/collection length     | `@Size(min = 8, max = 100) String password`    |
| `@Pattern`                 | Regex validation             | `@Pattern(regexp = "^\\+?[1-9]\\d{1,14}$") String phone` |
| `@Past` / `@Future`        | Date validation              | `@Past LocalDate dateOfBirth`                  |
| `@FutureOrPresent`         | Date must be today or later  | `@FutureOrPresent LocalDate planDate`          |
| `@Valid`                   | Cascade validation to nested objects | `@Valid List<@Valid MealItemDto> items` |

### 15.3 Custom Validator Pattern

```java
// ✅ Custom annotation
@Target({FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = ValidMacroRatioValidator.class)
@Documented
public @interface ValidMacroRatio {
    String message() default "Macronutrient percentages must sum to 100%";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

// ✅ Validator implementation
public class ValidMacroRatioValidator
        implements ConstraintValidator<ValidMacroRatio, GoalRequest> {
    @Override
    public boolean isValid(GoalRequest request, ConstraintValidatorContext context) {
        if (request == null) return true;
        int total = request.carbsPercent() + request.proteinPercent() + request.fatPercent();
        return total == 100;
    }
}
```

### 15.4 Validation Rules

| **Rule**                                        | **Guideline**                                          |
| ----------------------------------------------- | ------------------------------------------------------ |
| Validate at the controller boundary             | First line of defense — reject invalid input early     |
| Validate business rules in service layer        | Check for duplication, conflicting data, state violations |
| Database-level constraints as last resort       | NOT NULL, UNIQUE, CHECK constraints for data integrity  |
| Client-side validation is not security          | Always validate server-side; frontend validation is UX only |
| Use consistent error messages                   | "Field 'email': must be a valid email address"         |
| Group related fields with cross-field validators | Macro totals, date ranges, password confirmation       |
| Internationalize error messages (ResourceBundle) | Prepare for multi-language support                      |

---

## 16. Testing Standards

### 16.1 Test Structure

```
src/test/java/com/nutriguideai/
├── controller/      ← Web MVC tests (@WebMvcTest)
├── service/         ← Unit tests with mocked dependencies
├── repository/      ← Data JPA tests (@DataJpaTest)
└── integration/     ← Full context tests (@SpringBootTest)
```

### 16.2 Test Naming

```java
// ✅ CORRECT: MethodName_Should_ExpectedBehavior_When_Condition
@Test
void generateMealPlan_ShouldReturnPlan_WhenValidRequest() { ... }

@Test
void generateMealPlan_ShouldThrowException_WhenDuplicateDate() { ... }

@Test
void findById_ShouldReturnEmpty_WhenUserNotFound() { ... }
```

### 16.3 Unit Test Standards (Service Layer)

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
        // Arrange
        Long userId = 1L;
        CreateMealPlanRequest request = new CreateMealPlanRequest(
            LocalDate.of(2026, 7, 27),
            DietType.VEGETARIAN,
            BudgetLevel.MEDIUM
        );
        MealPlan expectedPlan = MealPlan.builder()
            .planId(1L)
            .planDate(request.planDate())
            .totalCalories(1850)
            .build();

        when(recommendationEngine.generate(eq(userId), any())).thenReturn(expectedPlan);
        when(mealPlanRepository.save(any())).thenReturn(expectedPlan);

        // Act
        MealPlanResponse response = mealPlanService.generateMealPlan(userId, request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.planDate()).isEqualTo(request.planDate());
        verify(mealPlanRepository).save(any());
    }

    @Test
    void generateMealPlan_ShouldThrowConflict_WhenPlanExists() {
        // Arrange
        Long userId = 1L;
        LocalDate date = LocalDate.of(2026, 7, 27);
        when(mealPlanRepository.existsByUserIdAndPlanDate(userId, date)).thenReturn(true);

        CreateMealPlanRequest request = new CreateMealPlanRequest(date, null, null);

        // Act & Assert
        assertThrows(ConflictException.class,
            () -> mealPlanService.generateMealPlan(userId, request));
    }
}
```

### 16.4 Controller Test Standards

```java
@WebMvcTest(MealPlanController.class)
class MealPlanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MealPlanService mealPlanService;

    @Test
    void generateMealPlan_ShouldReturn201_WhenValidRequest() throws Exception {
        // Arrange
        CreateMealPlanRequest request = new CreateMealPlanRequest(
            LocalDate.of(2026, 7, 27),
            DietType.VEGETARIAN,
            BudgetLevel.MEDIUM
        );
        MealPlanResponse response = MealPlanResponse.builder()
            .planId(1L)
            .planDate(request.planDate())
            .totalCalories(1850)
            .build();

        when(mealPlanService.generateMealPlan(anyLong(), any())).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/meal-plans")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "planDate": "2026-07-27",
                        "dietType": "VEGETARIAN",
                        "budgetLevel": "MEDIUM"
                    }
                    """)
                .header("Authorization", "Bearer test-token"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("success"))
            .andExpect(jsonPath("$.data.totalCalories").value(1850));
    }
}
```

### 16.5 Testing Rules

| **Rule**                                        | **Guideline**                                          |
| ----------------------------------------------- | ------------------------------------------------------ |
| Test coverage target: Service layer ≥ 85%       | Core business logic must be thoroughly tested          |
| Test coverage target: Controller layer ≥ 70%    | Request validation, response formatting, status codes  |
| Test coverage target: Repository layer ≥ 50%    | Custom queries, pagination, sorting                    |
| One assertion concept per test method           | Test one behavior, one scenario per method             |
| Use descriptive test names                      | `methodName_shouldExpectedBehavior_whenCondition`      |
| Follow AAA pattern                              | Arrange, Act, Assert (clearly separated)               |
| Mock external dependencies only                 | Don't mock what you don't own — integration test instead |
| No `Thread.sleep()` in tests                    | Use `awaitility` for async testing                     |
| Database tests with `@DataJpaTest`              | In-memory database (H2) for repository tests           |
| Integration tests with `@SpringBootTest`        | Full context tests for critical flows (auth, meal plan generation) |

---

## 17. SOLID Principles

### 17.1 Single Responsibility Principle (SRP)

> A class should have only one reason to change.

```java

@Service
public class UserService {
    public void register(User user) { /* ... */ }
    public void updateProfile(UserProfile profile) { /* ... */ }
    public void generateMealPlan(MealPlan plan) { /* ... */ } // Wrong!
}

// ✅ CORRECT: Separate classes for separate responsibilities
@Service
public class AuthService { /* Registration, login, token */ }

@Service
public class UserProfileService { /* Profile CRUD */ }

@Service
public class MealPlanService { /* Meal plan generation */ }
```

### 17.2 Open/Closed Principle (OCP)

> Classes should be open for extension but closed for modification.

```java
// ✅ CORRECT: Extend via interface, not modification
public interface MealGenerator {
    MealPlan generate(Long userId, MealPlanRequest request);
}

// Existing implementation
@Component
public class RuleBasedMealGenerator implements MealGenerator { ... }

// New implementation — no existing code changed
@Component
@ConditionalOnProperty(name = "nutriguideai.meals.generator", havingValue = "ai")
public class AiMealGenerator implements MealGenerator { ... }
```

### 17.3 Liskov Substitution Principle (LSP)

> Subtypes must be substitutable for their base types.

```java
// ✅ CORRECT: Subtypes maintain base class contract
public abstract class MealPlanExporter {
    public abstract byte[] export(MealPlan plan);
}

public class PdfMealPlanExporter extends MealPlanExporter {
    @Override
    public byte[] export(MealPlan plan) {
        // Returns valid PDF bytes — never throws or returns null
    }
}

// ❌ WRONG: Violates LSP by throwing unexpected exception
public class XmlMealPlanExporter extends MealPlanExporter {
    @Override
    public byte[] export(MealPlan plan) {
        throw new UnsupportedOperationException("XML not supported");
        // Violates LSP — callers don't expect this
    }
}
```

### 17.4 Interface Segregation Principle (ISP)

> Clients should not be forced to depend on interfaces they do not use.

```java
// ❌ WRONG: Fat interface forces all implementations to implement unused methods
public interface UserService {
    void register(User user);
    void updateProfile(UserProfile profile);
    void resetPassword(String email);
    void deactivateAccount(Long userId);
    MealPlan generateMealPlan(Long userId);
    List<ProgressTracking> getProgress(Long userId);
}

// ✅ CORRECT: Segregated interfaces
public interface AuthOperations {
    void register(User user);
    void resetPassword(String email);
}

public interface ProfileOperations {
    void updateProfile(UserProfile profile);
    void deactivateAccount(Long userId);
}

public interface NutritionOperations {
    MealPlan generateMealPlan(Long userId);
    List<ProgressTracking> getProgress(Long userId);
}
```

### 17.5 Dependency Inversion Principle (DIP)

> Depend on abstractions, not concretions.

```java
// ✅ CORRECT: Depend on interface
@Service
public class MealPlanService {
    private final MealGenerator mealGenerator;  // Depends on interface
    private final MealPlanRepository repository; // Depends on interface

    public MealPlanService(
            MealGenerator mealGenerator,       // Receives any implementation
            MealPlanRepository repository) {
        this.mealGenerator = mealGenerator;
        this.repository = repository;
    }
}

// ❌ WRONG: Depend on concrete class
@Service
public class MealPlanService {
    private final RuleBasedMealGenerator mealGenerator; // Concrete class
    private final JpaMealPlanRepository repository;     // Concrete class
}
```

---

## 18. Best Practices

### 18.1 Database Best Practices

| **Practice**                                    | **Rationale**                                          |
| ----------------------------------------------- | ------------------------------------------------------ |
| Use Flyway for schema migrations                | Version-controlled, reproducible database schema       |
| Index all foreign key columns                   | Prevents full table scans on JOINs                     |
| Use `EXPLAIN` on complex queries                | Verify query performance before production             |
| Prefer `BETWEEN` over `>=` and `<=` for ranges  | More readable, optimizer-friendly                      |
| Use `LIMIT` on all collection queries           | Prevent accidental table scans                         |
| Never use `SELECT *`                            | Always specify exact columns needed                    |
| Avoid N+1 queries — use JOIN FETCH or `@EntityGraph` | Prevent lazy loading performance issues               |
| Use connection pooling (HikariCP)               | Default in Spring Boot — tune pool size                |

### 18.2 Security Best Practices

| **Practice**                                    | **Rationale**                                          |
| ----------------------------------------------- | ------------------------------------------------------ |
| Always hash passwords with BCrypt               | Never store plain text or MD5/SHA hashes               |
| Use parameterized queries (JPQL)                | Prevent SQL injection                                  |
| Enable CSRF protection for state-changing ops   | Protect against cross-site request forgery             |
| Use `@PreAuthorize` for role-based access       | `@PreAuthorize("hasRole('ADMIN')")`                    |
| Sanitize all user input                         | Prevent XSS and injection attacks                      |
| Use HTTPS in production                         | Encrypt all traffic between client and server          |
| Store secrets in environment variables          | Never hardcode API keys or database passwords          |
| Set JWT expiry (24h for access tokens)          | Limit damage window if token is compromised            |
| Implement rate limiting on auth endpoints       | Prevent brute force attacks on login                   |

### 18.3 Performance Best Practices

| **Practice**                                    | **Rationale**                                          |
| ----------------------------------------------- | ------------------------------------------------------ |
| Use pagination for all listing endpoints        | Never return unbounded lists                           |
| Cache read-heavy, rarely-modified data          | Food catalog, user profile (via Redis)                 |
| Use lazy loading for JPA relationships          | Avoid loading entire object graphs unnecessarily       |
| Enable Hibernate query caching                  | Cache frequently executed queries                      |
| Use DTO projections for read-only queries       | `SELECT new com.nutriguideai.dto.UserSummary(...)`     |
| Tune HikariCP connection pool                   | Start with `maximum-pool-size: 10`                     |
| Monitor slow queries with `spring.jpa.show-sql` | Only in dev — never in production                      |
| Use batch processing for bulk operations        | `spring.jpa.properties.hibernate.jdbc.batch_size`      |

### 18.4 Code Review Best Practices

| **Practice**                                    | **Rationale**                                          |
| ----------------------------------------------- | ------------------------------------------------------ |
| Every PR requires at least one approval         | Two eyes catch more bugs than one                      |
| PR size ≤ 400 lines                             | Large PRs are hard to review thoroughly                |
| No self-approvals                               | Always have a different person review your code        |
| Check for security issues in every PR           | SQL injection, XSS, auth bypass                        |
| Verify test coverage in PR                      | New code should include corresponding tests            |
| Run CI pipeline before merging                  | Ensure all tests pass and build succeeds               |
| Resolve all comments before merging             | Don't leave open questions in PRs                      |
| Use PR templates                                | Consistent format for all pull requests                |

### 18.5 API Design Best Practices

| **Practice**                                    | **Rationale**                                          |
| ----------------------------------------------- | ------------------------------------------------------ |
| Always version your API                          | `/api/v1/...` allows breaking changes in future       |
| Use consistent error response format            | Clients can parse errors programmatically              |
| Return meaningful HTTP status codes              | 201 for create, 204 for delete, 422 for validation    |
| Support partial responses (fields parameter)    | Let clients request only the fields they need          |
| Use `ETag` headers for caching                  | Reduce bandwidth for unchanged resources               |
| Document rate limits in response headers         | `X-RateLimit-Remaining`, `X-RateLimit-Reset`          |
| Use `202 Accepted` for async operations         | When processing is deferred (future queue-based)       |
| Never expose internal IDs in URLs               | Use UUIDs or composite keys for public endpoints       |

### 18.6 Development Workflow Best Practices

| **Practice**                                    | **Guideline**                                          |
| ----------------------------------------------- | ------------------------------------------------------ |
| Pull from `develop` daily                       | Avoid large merge conflicts                            |
| Commit frequently with meaningful messages      | Small commits are easier to review and revert          |
| Run tests before pushing                        | Don't push broken code                                 |
| Rebase feature branches before PR               | Keep a clean, linear commit history                    |
| Link PRs to JIRA/issues                         | Traceability from requirement to implementation        |
| Keep documentation up to date with code changes | Update docs when API contracts or architecture changes |
| Use feature flags for incomplete features       | Merge early, hide behind flags, release when ready     |

---

## Document Version History

| **Version** | **Date**     | **Author** | **Changes**          |
| ----------- | ------------ | ---------- | -------------------- |
| 1.0         | 2026-07-27   | Architect  | Initial draft        |

---

*End of Document — 08_CODING_STANDARDS.md*
