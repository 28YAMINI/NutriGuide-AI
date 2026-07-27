# 03 —  ARCHITECTURE

| **Field**             | **Value**                                    |
| --------------------- | -------------------------------------------- |
| **Document Title**    | System Architecture Document                 |
| **Project Name**      | NutriGuide AI                                |
| **Project Type**      | AI-Powered Personalized Nutrition & Meal Planning System |
| **Version**           | 1.0                                          |
| **Author**            | Software Architecture Team                   |
| **Created On**        | 2026-07-27                                   |
| **Last Updated**      | 2026-07-27                                   |
| **Status**            | Draft                                        |
| **Classification**    | Internal                                     |

---

## Table of Contents

1. [Document Information](#1-document-information)
2. [Table of Contents](#2-table-of-contents)
3. [High-Level Architecture](#3-high-level-architecture)
4. [Architecture Diagram](#4-architecture-diagram)
5. [Layer Responsibilities](#5-layer-responsibilities)
6. [Frontend Architecture](#6-frontend-architecture)
7. [Backend Architecture](#7-backend-architecture)
8. [Recommendation Engine Architecture](#8-recommendation-engine-architecture)
9. [Database Architecture](#9-database-architecture)
10. [Redis Caching Architecture](#10-redis-caching-architecture)
11. [Authentication Flow](#11-authentication-flow)
12. [Request-Response Flow](#12-request-response-flow)
13. [Component Interaction Diagram](#13-component-interaction-diagram)
14. [Folder Structure](#14-folder-structure)
15. [Technology Decisions](#15-technology-decisions)
16. [Design Patterns Used](#16-design-patterns-used)
17. [Deployment Architecture](#17-deployment-architecture)
18. [Security Architecture](#18-security-architecture)
19. [Scalability Considerations](#19-scalability-considerations)
20. [Future Enhancements](#20-future-enhancements)
21. [Conclusion](#21-conclusion)

---

## 1. Document Information

### 1.1 Purpose

This document defines the system architecture for **NutriGuide AI**, an AI-Powered Personalized Nutrition & Meal Planning System. It provides a comprehensive overview of the system's structural design, component responsibilities, data flow, technology choices, and deployment strategy. The architecture follows a **Layered Monolithic** style with clear separation of concerns, designed to be production-ready while remaining realistic for a team-based engineering project.

### 1.2 Scope

This document covers:

- Overall system architecture and layer responsibilities
- Frontend and backend structural design
- Recommendation Engine design (rule-based, within the Service Layer)
- Database and caching strategy
- Authentication and security approach
- Deployment and future scalability
- Technology and design pattern decisions

### 1.3 Intended Audience

- Development Team (implementing the system)
- Project Reviewers and Evaluators
- Technical Architects
- Quality Assurance Team

### 1.4 References

| **Document**              | **Location**           |
| ------------------------- | ---------------------- |
| Project Context           | `docs/01_PROJECT_CONTEXT.md` |
| Requirements Document     | `docs/02_REQUIREMENTS.md` |
| Database Design Document  | `docs/04_DATABASE.md`  |

---

## 3. High-Level Architecture

### 3.1 Architecture Style

NutriGuide AI follows a **Layered Monolithic Architecture**. The entire application is deployed as a single deployable unit (Spring Boot JAR for backend, static build for frontend), but internally it is strictly divided into layers with well-defined responsibilities and communication rules.

### 3.2 Why Layered Monolithic?

| **Factor**              | **Assessment**                                          |
| ----------------------- | ------------------------------------------------------- |
| **Team Size**           | Small team (3–5 developers)                             |
| **Deployment Complexity** | Low — single deployable unit                           |
| **Performance**         | No network latency between layers                       |
| **Development Speed**   | Fast — shared codebase, no service boundary overhead    |
| **Testing**             | Simple — integration tests across layers within one JVM |
| **Future Extraction**   | Layered structure enables future microservice extraction|

### 3.3 Architecture Principles

| **Principle**               | **Application**                                          |
| --------------------------- | -------------------------------------------------------- |
| **Separation of Concerns**  | Each layer has a single, well-defined responsibility     |
| **Dependency Inversion**    | High-level modules do not depend on low-level modules    |
| **Encapsulation**           | Layers communicate only through defined interfaces       |
| **Stateless Design**        | Backend services are stateless; session data is not stored|
| **Fail Fast**               | Validation at the earliest possible boundary             |
| **Cache First**             | Read-heavy data accesses check cache before database     |

---

## 4. Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                              PRESENTATION LAYER                                      │
│                                                                                      │
│   ┌──────────────────────────────────────────────────────────────────────────────┐   │
│   │                           React Single Page Application                       │   │
│   │                                                                              │   │
│   │   ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────────┐ │   │
│   │   │  Auth    │  │Dashboard │  │Meal Plan │  │  Food    │  │   Profile    │ │   │
│   │   │  Pages   │  │  Pages   │  │  Pages   │  │  Diary   │  │   Pages      │ │   │
│   │   └──────────┘  └──────────┘  └──────────┘  └──────────┘  └──────────────┘ │   │
│   │                                                                              │   │
│   │   ┌──────────────────────────────────────────────────────────────────────┐   │   │
│   │   │              Axios HTTP Layer + JWT Authorization Header             │   │   │
│   │   └──────────────────────────────────────────────────────────────────────┘   │   │
│   └──────────────────────────────────────────────────────────────────────────────┘   │
└────────────────────────────────────┬─────────────────────────────────────────────────┘
                                     │ HTTPS / JSON / JWT
                                     │
┌────────────────────────────────────▼─────────────────────────────────────────────────┐
│                              CONTROLLER LAYER                                         │
│                                                                                      │
│   ┌──────────────────────────────────────────────────────────────────────────────┐   │
│   │                          REST Controllers                                    │   │
│   │                                                                              │   │
│   │   ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────────┐ │   │
│   │   │  Auth    │  │  User    │  │ Health   │  │Nutrition │  │   Admin      │ │   │
│   │   │  Ctrl    │  │  Ctrl    │  │ Vitals   │  │  Ctrl    │  │   Ctrl       │ │   │
│   │   └──────────┘  └──────────┘  │  Ctrl    │  └──────────┘  └──────────────┘ │   │
│   │                                └──────────┘  ┌──────────┐  ┌──────────────┐ │   │
│   │                                                │ MealPlan │  │   Food      │ │   │
│   │                                                │  Ctrl    │  │   Diary Ctrl│ │   │
│   │                                                └──────────┘  └──────────────┘ │   │
│   └──────────────────────────────────────────────────────────────────────────────┘   │
└────────────────────────────────────┬─────────────────────────────────────────────────┘
                                     │
┌────────────────────────────────────▼─────────────────────────────────────────────────┐
│                              SERVICE LAYER                                            │
│                                                                                      │
│   ┌──────────────────────────────────────────────────────────────────────────────┐   │
│   │                          Business Logic Services                              │   │
│   │                                                                              │   │
│   │   ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────────┐ │   │
│   │   │AuthService│  │UserSvc   │  │Health    │  │Nutrition │  │   Admin     │ │   │
│   │   │          │  │          │  │VitalsSvc │  │Service   │  │   Service   │ │   │
│   │   └──────────┘  └──────────┘  └──────────┘  └──────────┘  └──────────────┘ │   │
│   │                                                                              │   │
│   │   ┌──────────────────────────────────────────────────────────────────────┐   │   │
│   │   │                   Recommendation Engine                                │   │   │
│   │   │  ┌─────────────────────────────────────────────────────────────────┐ │   │   │
│   │   │  │  Rule-Based Meal Plan Generator                                  │ │   │   │
│   │   │  │                                                                  │ │   │   │
│   │   │  │  Input: UserProfile + HealthVitals + Goals + Preferences        │ │   │   │
│   │   │  │  Logic: Condition → Meal Rules → Nutrition Scoring              │ │   │   │
│   │   │  │  Output: Structured MealPlan with nutritional breakdown         │ │   │   │
│   │   │  └─────────────────────────────────────────────────────────────────┘ │   │   │
│   │   └──────────────────────────────────────────────────────────────────────┘   │   │
│   │                                                                              │   │
│   │   ┌──────────────────────────────────────────────────────────────────────┐   │   │
│   │   │                        Caching Service                                │   │   │
│   │   │           (Abstraction over Redis for cache operations)               │   │   │
│   │   └──────────────────────────────────────────────────────────────────────┘   │   │
│   └──────────────────────────────────────────────────────────────────────────────┘   │
└────────────────────────────────────┬─────────────────────────────────────────────────┘
                                     │
┌────────────────────────────────────▼─────────────────────────────────────────────────┐
│                            REPOSITORY LAYER                                           │
│                                                                                      │
│   ┌──────────────────────────────────────────────────────────────────────────────┐   │
│   │                       Spring Data JPA Repositories                            │   │
│   │                                                                              │   │
│   │   ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────────┐ │   │
│   │   │  User    │  │  Profile │  │  Health  │  │  Meal    │  │   Food       │ │   │
│   │   │  Repo    │  │  Repo    │  │  Vitals  │  │  Plan    │  │   Catalog    │ │   │
│   │   │          │  │          │  │  Repo    │  │  Repo    │  │   Repo       │ │   │
│   │   └──────────┘  └──────────┘  └──────────┘  └──────────┘  └──────────────┘ │   │
│   │   ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐                    │   │
│   │   │ FoodDiary│  │ Progress │  │ UserGoal │  │ Food     │                    │   │
│   │   │ Repo     │  │ Repo     │  │ Repo     │  │ Pref Repo│                    │   │
│   │   └──────────┘  └──────────┘  └──────────┘  └──────────┘                    │   │
│   └──────────────────────────────────────────────────────────────────────────────┘   │
└────────────────────────────────────┬─────────────────────────────────────────────────┘
                                     │
            ┌────────────────────────┼────────────────────────┐
            │                        │                        │
┌───────────▼──────────┐  ┌──────────▼──────────┐  ┌─────────▼──────────┐
│     DATABASE LAYER   │  │   CACHING LAYER     │  │   EXTERNAL        │
│                      │  │                      │  │   SERVICES        │
│  ┌────────────────┐  │  │  ┌────────────────┐  │  │  (None in v1)    │
│  │    MySQL 8.0   │  │  │  │    Redis       │  │  │                   │
│  │                │  │  │  │                │  │  │                   │
│  │  Primary store │  │  │  │  Cache for:    │  │  │                   │
│  │  for all       │  │  │  │  - Food Catalog│  │  │                   │
│  │  persistent    │  │  │  │  - Meal Plans  │  │  │                   │
│  │  data          │  │  │  │  - Dashboard   │  │  │                   │
│  │                │  │  │  │    Stats        │  │  │                   │
│  └────────────────┘  │  │  └────────────────┘  │  │                   │
│                      │  │                      │  │                   │
└──────────────────────┘  └──────────────────────┘  └───────────────────┘
```

---

## 5. Layer Responsibilities

### 5.1 Layer Overview

| **Layer**                  | **Responsibility**                                           | **Technology**        |
| -------------------------- | ------------------------------------------------------------ | --------------------- |
| **Presentation Layer**     | User interface, routing, form handling, data visualization   | React, Tailwind CSS   |
| **Controller Layer**       | HTTP request handling, input validation, response formatting | Spring Boot REST      |
| **Service Layer**          | Business logic, recommendation engine, orchestration         | Spring Services       |
| **Repository Layer**       | Data access, query execution, ORM mapping                    | Spring Data JPA       |
| **Database Layer**         | Persistent data storage, data integrity                      | MySQL                 |
| **Caching Layer**          | Read-heavy data caching, cache invalidation                  | Redis                 |

### 5.2 Layer Communication Rules

| **Layer**          | **Can Call**                          | **Cannot Call**                                    |
| ------------------ | ------------------------------------- | -------------------------------------------------- |
| Presentation       | Controller Layer (via HTTP)           | Service Layer, Repository Layer, Database directly |
| Controller         | Service Layer only                    | Repository Layer, Entity Layer directly            |
| Service            | Repository Layer, Caching Service     | Controller Layer, Presentation Layer               |
| Repository         | Database Layer (via JPA)              | Service Layer, Controller Layer                    |
| Caching Service    | Redis (via Spring Data Redis)         | Repository Layer, Database Layer                   |

### 5.3 Data Flow Rules

```
Request Path (read):
    Client → Controller → Service → Repository → Database
                                 └→ Caching Service → Redis (cache check)

Request Path (write):
    Client → Controller → Service → Repository → Database
                                 └→ Caching Service → Redis (invalidate)

Response Path:
    Database ← Repository ← Service ← Controller ← Client
                    ↓
               DTO Mapping (Service Layer)
```

---

## 6. Frontend Architecture

### 6.1 Frontend Structure

The frontend is a **React Single Page Application** that communicates with the backend exclusively through REST API calls. No server-side rendering is used.

### 6.2 Frontend Component Responsibilities

| **Layer**              | **Responsibility**                                        |
| ---------------------- | --------------------------------------------------------- |
| **Pages**              | Top-level route components composing multiple components  |
| **Components**         | Reusable UI units (forms, charts, cards, layouts)         |
| **API Layer**          | Axios instance with JWT interceptor, per-module API calls |
| **Context**            | React Context for auth state management                   |
| **Custom Hooks**       | Encapsulated data fetching, loading, error handling       |
| **Utils**              | Constants, helper functions, formatting utilities         |

### 6.3 Page-to-Component Mapping

| **Page**            | **Key Components**                                              |
| ------------------- | --------------------------------------------------------------- |
| Landing Page        | HeroSection, FeatureCards, CallToAction, Footer                 |
| Login Page          | LoginForm (email, password), ValidationFeedback                 |
| Register Page       | RegisterForm, HealthProfileWizard (multi-step)                  |
| Dashboard Page      | SummaryCards, WeightTrendChart, CalorieBarChart, MacroDoughnut  |
| Profile Page        | ProfileForm, HealthVitalsForm, GoalSelector, PreferencesForm    |
| Meal Plan Page      | DailyMealCard, MealPlanGrid, GeneratePlanButton, NutritionTable |
| Food Diary Page     | DiaryEntryForm, CalendarView, DailySummary, HistoryList         |
| Progress Page       | WeightChart, BMIChart, CalorieTrend, MacroHistory, StreakBadge  |
| Admin Page          | UserTable, SystemHealthCard, FoodCatalogManager                 |

### 6.4 UI State Handling

Every data-fetching component follows a consistent state machine:

```
┌──────────┐
│  IDLE    │  ──►  Component initialized, no action yet
└────┬─────┘
     │ User triggers action
     ▼
┌──────────┐
│ LOADING  │  ──►  Spinner or skeleton shown
└────┬─────┘
     │
     ├──► Success → ┌─────────┐
     │              │  DATA   │  ──►  Render actual content
     │              │  VIEW   │
     │              └─────────┘
     │
     ├──► Error   → ┌─────────┐
     │              │  ERROR  │  ──►  Error message + retry button
     │              └─────────┘
     │
     └──► Empty   → ┌─────────┐
                    │  EMPTY  │  ──►  "No data yet" + action prompt
                    └─────────┘
```

---

## 7. Backend Architecture

### 7.1 Module Breakdown

The backend is organized into **domain modules**, each following the same layered structure:

| **Module**              | **Primary Responsibility**                                     |
| ----------------------- | -------------------------------------------------------------- |
| **Authentication**      | User registration, login, JWT token generation and validation   |
| **User Profile**        | User demographic data management (age, gender, height, weight)  |
| **Health Vitals**       | Medical conditions, BP, sugar levels, cholesterol, BMI tracking |
| **Goals & Lifestyle**   | Health goals, activity level, sleep, water intake               |
| **Food Preferences**    | Diet type, allergies, budget level, regional preferences        |
| **Recommendation Engine**| Rule-based meal plan generation (core business logic)          |
| **Meal Plan**           | Generated meal plan storage and retrieval                       |
| **Food Diary**          | Daily meal logging, calorie/macro tracking                     |
| **Progress Dashboard**  | Aggregated data for charts and statistics                       |
| **Food Catalog**        | Reference data for foods with nutritional information           |
| **Caching Service**     | Redis cache abstraction for read-heavy data                    |
| **Admin**               | User management, system health, food catalog management         |

### 7.2 Service Layer Design

Every service follows a consistent pattern:

```
┌──────────────────────────────────────────────┐
│           Service Interface                   │
│  (contract for all service operations)        │
└──────────────────┬───────────────────────────┘
                   │ implements
┌──────────────────▼───────────────────────────┐
│           Service Implementation              │
│                                               │
│  - Business logic validation                  │
│  - Cross-cutting concerns (logging, cache)   │
│  - Transaction management (@Transactional)   │
│  - DTO ↔ Entity mapping via MapStruct        │
│  - Custom exception throwing                  │
└──────────────────────────────────────────────┘
```

### 7.3 Controller Layer Design

```
┌──────────────────────────────────────────────┐
│           REST Controller                     │
│                                               │
│  - @RestController annotation                 │
│  - @RequestMapping("/api/v1/...")            │
│  - @Valid on request DTOs                    │
│  - Proper HTTP status codes                  │
│  - Swagger documentation annotations         │
│  - No business logic                         │
└──────────────────────────────────────────────┘
```

---

## 8. Recommendation Engine Architecture

### 8.1 Overview

The **Recommendation Engine** is the core business logic component of NutriGuide AI. It is implemented entirely within the **Service Layer** as a rule-based system. It does not use any external AI or LLM services.

### 8.2 Engine Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    RECOMMENDATION ENGINE                                      │
│                                                                              │
│   ┌──────────────────────────────────────────────────────────────────────┐   │
│   │                         Input Collector                               │   │
│   │                                                                      │   │
│   │   ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────┐ │   │
│   │   │ Profile  │  │ Health  │  │  Goals   │  │  Prefs   │  │ Food │ │   │
│   │   │ Data     │  │ Vitals  │  │          │  │          │  │Catalog│ │   │
│   │   └──────────┘  └──────────┘  └──────────┘  └──────────┘  └──────┘ │   │
│   └──────────────────────────────────────────────────────────────────────┘   │
│                                     │                                        │
│                                     ▼                                        │
│   ┌──────────────────────────────────────────────────────────────────────┐   │
│   │                      Rule Engine Core                                 │   │
│   │                                                                      │   │
│   │   ┌──────────────────────────────────────────────────────────────┐   │   │
│   │   │  Rule Categories                                              │   │   │
│   │   │                                                              │   │   │
│   │   │  ┌──────────────┐   ┌──────────────┐   ┌──────────────┐     │   │   │
│   │   │  │ Calorie Rules│   │  Macro Rules │   │ Condition    │     │   │   │
│   │   │  │ (BMR based)  │   │  (ratio calc)│   │  Rules       │     │   │   │
│   │   │  └──────────────┘   └──────────────┘   └──────────────┘     │   │   │
│   │   │                                                              │   │   │
│   │   │  ┌──────────────┐   ┌──────────────┐   ┌──────────────┐     │   │   │
│   │   │  │ Preference   │   │  Budget      │   │  Meal        │     │   │   │
│   │   │  │ Rules        │   │  Rules       │   │  Composition │     │   │   │
│   │   │  └──────────────┘   └──────────────┘   └──────────────┘     │   │   │
│   │   └──────────────────────────────────────────────────────────────┘   │   │
│   └──────────────────────────────────────────────────────────────────────┘   │
│                                     │                                        │
│                                     ▼                                        │
│   ┌──────────────────────────────────────────────────────────────────────┐   │
│   │                      Meal Plan Generator                             │   │
│   │                                                                      │   │
│   │   - Select breakfast items (based on calorie allocation)            │   │
│   │   - Select lunch items (based on macro requirements)                │   │
│   │   - Select dinner items (based on remaining allowance)              │   │
│   │   - Select snacks (based on meal interval gaps)                     │   │
│   │   - Apply condition-specific restrictions                          │   │
│   │   - Ensure budget constraints                                      │   │
│   │   - Verify nutritional balance                                      │   │
│   └──────────────────────────────────────────────────────────────────────┘   │
│                                     │                                        │
│                                     ▼                                        │
│   ┌──────────────────────────────────────────────────────────────────────┐   │
│   │                      Output Formatter                                │   │
│   │                                                                      │   │
│   │   - Structure meal plan with per-meal breakdown                     │   │
│   │   - Calculate total daily nutrition                                  │   │
│   │   - Generate dietary tips based on conditions                       │   │
│   │   - Return MealPlan DTO                                              │   │
│   └──────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 8.3 Rule Categories

| **Rule Category**       | **Purpose**                                                      | **Decision Factors**                          |
| ----------------------- | ---------------------------------------------------------------- | --------------------------------------------- |
| **Calorie Rules**       | Determine daily caloric target based on BMR and activity level   | Age, gender, height, weight, activity level, goal |
| **Macro Rules**         | Calculate protein, carbs, fat ratio based on goal and conditions | Goal type, medical conditions                 |
| **Condition Rules**     | Apply food restrictions and recommendations per medical condition| Medical conditions, allergy constraints        |
| **Preference Rules**    | Filter food catalog by diet type and preferences                 | Diet type (veg/non-veg/vegan), allergies       |
| **Budget Rules**        | Select cost-appropriate food options                             | Budget level (low/medium/high)                 |
| **Meal Composition**    | Distribute calories across meals (breakfast/lunch/dinner/snacks) | Meal type, remaining daily allowance           |

### 8.4 Rule Execution Flow

```
User clicks "Generate Meal Plan"
            │
            ▼
    1. Load User Context (Profile + Vitals + Goals + Preferences)
            │
            ▼
    2. Check Cache for existing plan (same context, same date)
       ├── Cache hit → Return cached meal plan
       └── Cache miss → Continue
            │
            ▼
    3. Calculate BMR (Harris-Benedict Equation)
       │   BMR = 10 × weight(kg) + 6.25 × height(cm) - 5 × age(y) - 161 (female)
       │   BMR = 10 × weight(kg) + 6.25 × height(cm) - 5 × age(y) + 5 (male)
            │
            ▼
    4. Apply Activity Multiplier
       │   TDEE = BMR × Activity Factor
       │   (Sedentary: 1.2, Light: 1.375, Moderate: 1.55, Active: 1.725, Very Active: 1.9)
            │
            ▼
    5. Apply Goal Adjustment
       │   Weight Loss: TDEE - 500
       │   Weight Gain: TDEE + 300
       │   Maintenance: TDEE
       │   Muscle Gain: TDEE + 200 (with higher protein ratio)
            │
            ▼
    6. Determine Macro Split
       │   Default: Protein 30%, Carbs 40%, Fat 30%
       │   Diabetes: Lower carbs (25%), Higher protein (35%)
       │   Muscle Gain: Higher protein (35%)
       │   etc.
            │
            ▼
    7. Apply Medical Condition Filters
       │   Diabetes: Avoid high-sugar foods, prefer complex carbs
       │   Hypertension: Low-sodium options
       │   High Cholesterol: Low saturated fat, high fiber
       │   PCOS: Low glycemic index, anti-inflammatory foods
       │   Thyroid: Ensure adequate iodine, selenium
       │   etc.
            │
            ▼
    8. Select Meals from Catalog
       │   For each meal slot (Breakfast, Lunch, Dinner, Snacks):
       │   - Filter Food Catalog by Preference + Condition + Budget
       │   - Score each item by nutritional fit
       │   - Select best items within calorie allocation for that meal
       │   - Ensure variety (no repeated items in same day)
            │
            ▼
    9. Build Meal Plan Structure
       │   - 4 meal slots with selected items
       │   - Total nutritional summary
       │   - Condition-specific tips
            │
            ▼
    10. Cache the generated plan (Redis)
            │
            ▼
    11. Return MealPlan DTO to Controller
```

### 8.5 Scoring Algorithm (Conceptual)

Each food item in the catalog is scored against the user's profile:

```
Score = NutritionalFit × ConditionCompatibility × PreferenceMatch × BudgetFit × VarietyBoost

Where:
- NutritionalFit: How well the item's macros match the target ratio
- ConditionCompatibility: 1.0 (fully compatible) to 0.0 (contraindicated)
- PreferenceMatch: 1.0 (matches diet type) or 0.0 (does not match)
- BudgetFit: 1.0 (within budget) to 0.5 (slightly above) to 0.0 (too expensive)
- VarietyBoost: Inverse of how many times this item appeared recently (encourages variety)
```

---

## 9. Database Architecture

### 9.1 Database Role

MySQL 8.0 serves as the **single source of truth** for all persistent data in NutriGuide AI. It is a relational database that ensures ACID compliance, referential integrity, and data consistency.

### 9.2 What MySQL Stores

| **Data Category**          | **Description**                                              |
| -------------------------- | ------------------------------------------------------------ |
| **User & Authentication**  | User credentials (hashed passwords), roles, account status   |
| **Profile Data**           | Demographics (age, gender, height, weight, calculated BMI)   |
| **Health Vitals**          | Medical conditions, blood pressure, sugar levels, cholesterol|
| **Goals & Lifestyle**      | Primary goal, activity level, sleep hours, water intake      |
| **Food Preferences**       | Diet type, allergies, budget level, regional preference      |
| **Food Catalog**           | Reference food items with nutritional values per 100g        |
| **Meal Plans**             | Generated meal plans with per-meal breakdown                 |
| **Food Diary**             | Daily meal logs with calorie and macro tracking              |
| **Progress Tracking**      | Historical weight, BMI, and nutrition data for dashboards    |

### 9.3 What MySQL Does NOT Handle

| **Capability**       | **Handled By** | **Rationale**                                    |
| -------------------- | -------------- | ------------------------------------------------ |
| Caching              | Redis          | Read-heavy data needs faster access than disk    |
| File Storage         | N/A (v1)       | v1 does not support file uploads                 |
| Schema Migrations    | Manual SQL     | Schema is stable for v1 scope                    |

### 9.4 MySQL Configuration Responsibilities

- **Connection Pooling**: Managed by HikariCP (configured in `application.yml`)
- **Query Optimization**: Indexed columns for frequently queried fields
- **Data Integrity**: Foreign keys and unique constraints
- **Backup**: Managed through Azure Database for MySQL automated backups

---

## 10. Redis Caching Architecture

### 10.1 Redis Role

Redis is used exclusively as a **cache layer** to reduce database load for read-heavy operations. It is **not** used for session storage, pub/sub messaging, or any purpose other than caching.

### 10.2 Cache Targets

| **Cache Target**              | **Cache Key Pattern**              | **TTL**   | **Cache Strategy**       |
| ----------------------------- | ---------------------------------- | --------- | ------------------------ |
| **Food Catalog**              | `food:catalog:{dietType}`          | 1 hour    | Cache-Aside              |
| **Individual Meal Plan**      | `mealplan:{userId}:{date}`         | 6 hours   | Cache-Aside              |
| **User Dashboard Stats**       | `dashboard:{userId}:{period}`      | 30 minutes| Cache-Aside              |
| **Nutritional Summary**       | `nutrition:summary:{userId}:{date}`| 1 hour    | Cache-Aside              |

### 10.3 Cache Flow

```
Read Operation:
    Service Layer
        │
        ├──► 1. Check Redis Cache
        │        ├── Cache HIT  → Return cached data (fast)
        │        └── Cache MISS → Continue
        │
        ├──► 2. Query Database
        │
        └──► 3. Store result in Redis (with TTL)
             │
             └──► 4. Return data

Write Operation (meal plan creation, food diary entry):
    Service Layer
        │
        ├──► 1. Save to Database
        │
        └──► 2. Invalidate Related Cache Keys
             │    (e.g., mealplan:{userId}:{date}, dashboard:{userId}:*)
             │
             └──► 3. Return success
```

### 10.4 Cache Invalidation Events

| **Event**                 | **Cache Keys Invalidated**                                  |
| ------------------------- | ----------------------------------------------------------- |
| New meal plan generated   | `mealplan:{userId}:{date}`, `dashboard:{userId}:*`          |
| Food diary entry added    | `nutrition:summary:{userId}:{date}`, `dashboard:{userId}:*` |
| Food catalog updated      | `food:catalog:*`                                            |
| User profile updated      | `dashboard:{userId}:*`                                      |

### 10.5 Why Not Use Redis for Sessions

- NutriGuide AI uses **JWT for authentication**, which is stateless by design
- JWT tokens are self-contained (user identity and role encoded in the token)
- No server-side session state needs to be stored
- This eliminates the need for session affinity or centralized session storage

---

## 11. Authentication Flow

### 11.1 Authentication Mechanism

NutriGuide AI uses **JWT (JSON Web Token)** for stateless authentication. Only **Access Tokens** are used — there are no refresh tokens. This simplifies the architecture while maintaining security for v1.

### 11.2 Complete Authentication Flow

```
┌─────────┐          ┌──────────────────┐          ┌──────────────────┐          ┌────────┐
│  Client │          │  Spring Security │          │  AuthService     │          │  DB    │
│  (React)│          │  + JWT Filter    │          │                  │          │        │
└────┬────┘          └────────┬─────────┘          └────────┬─────────┘          └───┬────┘
     │                        │                            │                        │
     │                        │                            │                        │
     │  POST /api/v1/auth/    │                            │                        │
     │  register              │                            │                        │
     │  {email, password,     │                            │                        │
     │   name}                │                            │                        │
     │───────────────────────►│                            │                        │
     │                        │  permitAll                 │                        │
     │                        │───────────────────────────►│                        │
     │                        │                            │  Check email uniqueness│
     │                        │                            │───────────────────────►│
     │                        │                            │◄────── Available ──────│
     │                        │                            │                        │
     │                        │                            │  Hash password (BCrypt) │
     │                        │                            │  Save user to database  │
     │                        │                            │───────────────────────►│
     │                        │                            │◄────── User saved ─────│
     │                        │                            │                        │
     │                        │                            │  Generate JWT Token    │
     │                        │                            │  (HS256, 24h expiry)   │
     │                        │                            │                        │
     │  201 Created           │                            │                        │
     │  {token, user}         │                            │                        │
     │◄───────────────────────┤                            │                        │
     │                        │                            │                        │
     │                        │                            │                        │
     │  POST /api/v1/auth/    │                            │                        │
     │  login                 │                            │                        │
     │  {email, password}     │                            │                        │
     │───────────────────────►│                            │                        │
     │                        │  permitAll                 │                        │
     │                        │───────────────────────────►│                        │
     │                        │                            │  Find user by email    │
     │                        │                            │───────────────────────►│
     │                        │                            │◄────── User found ─────│
     │                        │                            │                        │
     │                        │                            │  Verify password       │
     │                        │                            │  (BCrypt.matches)      │
     │                        │                            │                        │
     │                        │                            │  Generate JWT Token    │
     │                        │                            │  (HS256, 24h expiry)   │
     │                        │                            │                        │
     │  200 OK                │                            │                        │
     │  {token, user}         │                            │                        │
     │◄───────────────────────┤                            │                        │
     │                        │                            │                        │
     │                        │                            │                        │
     │  GET /api/v1/profile   │                            │                        │
     │  Authorization:        │                            │                        │
     │  Bearer <token>        │                            │                        │
     │───────────────────────►│                            │                        │
     │                        │                            │                        │
     │                        │  JWT Authentication Filter │                        │
     │                        │  1. Extract token from     │                        │
     │                        │     Authorization header   │                        │
     │                        │  2. Validate signature     │                        │
     │                        │     (HMAC-SHA256)          │                        │
     │                        │  3. Check expiration       │                        │
     │                        │  4. Extract user email     │                        │
     │                        │     and role from claims   │                        │
     │                        │  5. Create Authentication  │                        │
     │                        │     object                 │                        │
     │                        │  6. Set SecurityContext    │                        │
     │                        │                            │                        │
     │                        │  Request forwarded with    │                        │
     │                        │  authenticated context     │                        │
     │                        │───────────────────────────►│                        │
     │                        │                            │  Fetch profile data    │
     │                        │                            │───────────────────────►│
     │                        │                            │◄──── Return data ──────│
     │                        │                            │                        │
     │  200 OK                │                            │                        │
     │  {profile data}        │                            │                        │
     │◄───────────────────────┤                            │                        │
     │                        │                            │                        │
     │                        │                            │                        │
     │  GET /api/v1/profile   │                            │                        │
     │  (no token)            │                            │                        │
     │───────────────────────►│                            │                        │
     │                        │                            │                        │
     │  401 Unauthorized      │                            │                        │
     │◄───────────────────────┤                            │                        │
```

### 11.3 JWT Token Structure

```json
Decoded JWT Payload
{
  "sub": "user@example.com",
  "userId": 42,
  "role": "USER",
  "iat": 1722076800,
  "exp": 1722163200
}
```

| **Claim** | **Purpose**                                      |
| --------- | ------------------------------------------------ |
| `sub`     | Subject — user's email address                   |
| `userId`  | User's unique database identifier                |
| `role`    | User role (USER, ADMIN) for authorization         |
| `iat`     | Issued at timestamp                               |
| `exp`     | Expiration timestamp (24 hours from issuance)    |

### 11.4 Frontend Token Handling

```
Login/Register Success
        │
        ▼
Store JWT in localStorage
        │
        ▼
Axios Interceptor attaches token
to every subsequent request:
    Authorization: Bearer <token>
        │
        ▼
On 401 response → Redirect to login page
(User must re-authenticate)
```

### 11.5 Protected Resources

| **Resource Category** | **Authentication Required** | **Token Validation** |
| --------------------- | --------------------------- | -------------------- |
| Registration, Login   | No (permitAll)              | N/A                  |
| User Profile          | Yes                         | Valid JWT required   |
| Health Vitals         | Yes                         | Valid JWT required   |
| Meal Plans            | Yes                         | Valid JWT required   |
| Food Diary            | Yes                         | Valid JWT required   |
| Dashboard Progress    | Yes                         | Valid JWT required   |
| Admin Endpoints       | Yes + ADMIN role            | Valid JWT + role check |

---

## 12. Request-Response Flow

### 12.1 Standard Request-Response Cycle

```
┌───────────┐         ┌────────────┐         ┌──────────┐         ┌───────────┐         ┌────────┐
│  Browser  │         │  React App │         │  Spring  │         │  Service  │         │ MySQL/ │
│           │         │  + Axios  │         │  Boot    │         │  Layer    │         │ Redis  │
└─────┬─────┘         └─────┬──────┘         └────┬─────┘         └─────┬─────┘         └───┬────┘
      │                     │                      │                     │                     │
      │ User clicks         │                      │                     │                     │
      │ "Generate Plan"     │                      │                     │                     │
      │────────────────────►│                      │                     │                     │
      │                     │                      │                     │                     │
      │                     │ Show loading state   │                     │                     │
      │                     │ (spinner/skeleton)   │                     │                     │
      │                     │                      │                     │                     │
      │                     │ POST /api/v1/        │                     │                     │
      │                     │ meal-plan            │                     │                     │
      │                     │ Authorization:       │                     │                     │
      │                     │ Bearer <token>       │                     │                     │
      │                     │─────────────────────►│                     │                     │
      │                     │                      │                     │                     │
      │                     │                      │ JWT Filter validates │                     │
      │                     │                      │ token → SecurityCtx  │                     │
      │                     │                      │                     │                     │
      │                     │                      │ Request + Auth Ctx  │                     │
      │                     │                      │────────────────────►│                     │
      │                     │                      │                     │                     │
      │                     │                      │                     │ Check Redis cache   │
      │                     │                      │                     │────────────────────►│
      │                     │                      │                     │                     │
      │                     │                      │                     │  Cache Miss ◄───────│
      │                     │                      │                     │                     │
      │                     │                      │                     │ Execute business    │
      │                     │                      │                     │ logic (Recommend.  │
      │                     │                      │                     │ Engine)             │
      │                     │                      │                     │                     │
      │                     │                      │                     │ Query database      │
      │                     │                      │                     │────────────────────►│
      │                     │                      │                     │                     │
      │                     │                      │                     │  Result ◄────────────│
      │                     │                      │                     │                     │
      │                     │                      │                     │ Store in Redis      │
      │                     │                      │                     │ (for next request)  │
      │                     │                      │                     │                     │
      │                     │                      │  Response DTO ◄─────┤                     │
      │                     │                      │                     │                     │
      │                     │  200 OK              │                     │                     │
      │                     │  {mealPlan}          │                     │                     │
      │                     │◄─────────────────────┤                     │                     │
      │                     │                      │                     │                     │
      │                     │ Render meal plan     │                     │                     │
      │                     │ (hide loading state) │                     │                     │
      │                     │                      │                     │                     │
      │  User sees plan ◄───┤                      │                     │                     │
      │                     │                      │                     │                     │
```

### 12.2 Error Response Flow

```
Service Layer throws:                     Controller Layer catches:
    ResourceNotFoundException      ──►    GlobalExceptionHandler
    BadRequestException                   (@RestControllerAdvice)
    UnauthorizedException                      │
    DataIntegrityViolationException           ▼
                                        Structured error JSON:
                                        {
                                          "success": false,
                                          "error": {
                                            "code": "RESOURCE_NOT_FOUND",
                                            "message": "...",
                                            "timestamp": "...",
                                            "path": "/api/v1/..."
                                          }
                                        }
```

---

## 13. Component Interaction Diagram

```
                        ┌───────────────────────┐
                        │     React SPA         │
                        │                       │
                        │  ┌─────────────────┐  │
                        │  │  Axios HTTP     │  │
                        │  │  Client + JWT   │  │
                        │  └────────┬────────┘  │
                        └───────────┼───────────┘
                                    │ HTTPS / JSON
                                    ▼
                        ┌───────────────────────┐
                        │   Spring Security      │
                        │   Filter Chain         │
                        │                       │
                        │  ┌─────────────────┐  │
                        │  │ JWT Auth Filter │  │
                        │  └────────┬────────┘  │
                        └───────────┼───────────┘
                                    │
                  ┌─────────────────┼─────────────────┐
                  │                 │                  │
                  ▼                 ▼                  ▼
        ┌─────────────────┐ ┌──────────────┐ ┌─────────────────┐
        │   Auth          │ │  User        │ │  Nutrition      │
        │   Controller    │ │  Controller  │ │  Controller     │
        │                 │ │              │ │                 │
        │ /auth/*         │ │ /users/*     │ │ /meal-plans/*   │
        │                 │ │ /profiles/*  │ │ /food-diary/*   │
        └────────┬────────┘ │ /vitals/*    │ │ /recommend/*    │
                 │          └──────┬───────┘ └────────┬────────┘
                 │                 │                   │
                 ▼                 ▼                   ▼
        ┌─────────────────┐ ┌──────────────┐ ┌─────────────────┐
        │   AuthService   │ │  UserService │ │  NutritionSvc   │
        │                 │ │              │ │                 │
        │ Register, Login │ │ Profile CRUD │ │ Recommendation  │
        │ JWT Generation  │ │ Health Vitals│ │ Engine Call     │
        └────────┬────────┘ │ Goals, Prefs │ │ Meal Plan Mgmt  │
                 │          └──────┬───────┘ └────────┬────────┘
                 │                 │                   │
                 ▼                 ▼                   ▼
        ┌─────────────────────────────────────────────────────┐
        │                  Repository Layer                    │
        │                                                      │
        │  UserRepo   ProfileRepo  HealthRepo  MealPlanRepo   │
        │  GoalRepo   PrefRepo     FoodRepo    DiaryRepo      │
        │  ProgressRepo                                       │
        └──────────────┬──────────────────┬───────────────────┘
                       │                  │
                       ▼                  ▼
              ┌──────────────┐    ┌──────────────┐
              │    MySQL     │    │    Redis     │
              │  (Primary)   │    │  (Cache)     │
              └──────────────┘    └──────────────┘
```

---

## 14. Folder Structure

### 14.1 Repository Root

```
nutriguide-ai/
├── backend/                  ← Spring Boot Application
├── frontend/                 ← React Application
├── docker/                   ← Docker configuration files
├── postman/                  ← Postman API collection
├── .github/workflows/        ← CI/CD pipeline definitions
├── docs/                     ← Project documentation
├── README.md                 ← Project overview
└── .gitignore                ← Git ignore rules
```

### 14.2 Backend Package Structure

```
backend/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/nutriguideai/
    │   │   ├── NutriGuideAiApplication.java         ← Entry point
    │   │   │
    │   │   ├── config/
    │   │   │   ├── SecurityConfig.java              ← Spring Security config
    │   │   │   ├── CorsConfig.java                  ← CORS configuration
    │   │   │   ├── RedisConfig.java                 ← Redis connection config
    │   │   │   └── OpenApiConfig.java               ← Swagger/OpenAPI config
    │   │   │
    │   │   ├── controller/
    │   │   │   ├── AuthController.java
    │   │   │   ├── UserController.java
    │   │   │   ├── HealthVitalsController.java
    │   │   │   ├── NutritionController.java         ← Recommendation trigger
    │   │   │   ├── MealPlanController.java
    │   │   │   ├── FoodDiaryController.java
    │   │   │   ├── ProgressController.java
    │   │   │   └── AdminController.java
    │   │   │
    │   │   ├── dto/
    │   │   │   ├── request/
    │   │   │   │   ├── RegisterRequest.java
    │   │   │   │   ├── LoginRequest.java
    │   │   │   │   ├── HealthVitalsRequest.java
    │   │   │   │   ├── MealPlanRequest.java
    │   │   │   │   ├── FoodDiaryEntryRequest.java
    │   │   │   │   └── GoalUpdateRequest.java
    │   │   │   └── response/
    │   │   │       ├── AuthResponse.java
    │   │   │       ├── UserResponse.java
    │   │   │       ├── MealPlanResponse.java
    │   │   │       ├── ProgressResponse.java
    │   │   │       └── ApiErrorResponse.java
    │   │   │
    │   │   ├── entity/
    │   │   │   ├── User.java
    │   │   │   ├── UserProfile.java
    │   │   │   ├── HealthVitals.java
    │   │   │   ├── UserGoal.java
    │   │   │   ├── FoodPreference.java
    │   │   │   ├── MealPlan.java
    │   │   │   ├── MealPlanItem.java
    │   │   │   ├── FoodDiaryEntry.java
    │   │   │   ├── FoodItem.java
    │   │   │   └── ProgressTracking.java
    │   │   │
    │   │   ├── enums/
    │   │   │   ├── Role.java
    │   │   │   ├── GoalType.java
    │   │   │   ├── DietType.java
    │   │   │   ├── ActivityLevel.java
    │   │   │   ├── BudgetLevel.java
    │   │   │   ├── MealType.java
    │   │   │   └── MedicalCondition.java
    │   │   │
    │   │   ├── exception/
    │   │   │   ├── GlobalExceptionHandler.java
    │   │   │   ├── ResourceNotFoundException.java
    │   │   │   ├── UnauthorizedException.java
    │   │   │   ├── BadRequestException.java
    │   │   │   └── RecommendationException.java
    │   │   │
    │   │   ├── mapper/
    │   │   │   ├── UserMapper.java
    │   │   │   ├── HealthVitalsMapper.java
    │   │   │   └── MealPlanMapper.java
    │   │   │
    │   │   ├── repository/
    │   │   │   ├── UserRepository.java
    │   │   │   ├── UserProfileRepository.java
    │   │   │   ├── HealthVitalsRepository.java
    │   │   │   ├── UserGoalRepository.java
    │   │   │   ├── FoodPreferenceRepository.java
    │   │   │   ├── MealPlanRepository.java
    │   │   │   ├── MealPlanItemRepository.java
    │   │   │   ├── FoodDiaryEntryRepository.java
    │   │   │   ├── FoodItemRepository.java
    │   │   │   └── ProgressTrackingRepository.java
    │   │   │
    │   │   ├── security/
    │   │   │   ├── JwtTokenProvider.java             ← JWT create/validate
    │   │   │   ├── JwtAuthenticationFilter.java      ← JWT filter
    │   │   │   └── CustomUserDetailsService.java     ← Load user for auth
    │   │   │
    │   │   ├── service/
    │   │   │   ├── AuthService.java
    │   │   │   ├── UserService.java
    │   │   │   ├── HealthVitalsService.java
    │   │   │   ├── NutritionService.java
    │   │   │   ├── RecommendationEngine.java         ← Core rule engine
    │   │   │   ├── MealPlanService.java
    │   │   │   ├── FoodDiaryService.java
    │   │   │   ├── ProgressService.java
    │   │   │   ├── CacheService.java                 ← Redis cache abstraction
    │   │   │   └── AdminService.java
    │   │   │
    │   │   └── util/
    │   │       ├── BmiCalculator.java
    │   │       ├── CalorieCalculator.java
    │   │       └── ValidationUtils.java
    │   │
    │   └── resources/
    │       ├── application.yml                       ← Common config
    │       ├── application-dev.yml                   ← Dev profile
    │       ├── application-prod.yml                  ← Production profile
    │       └── data/
    │           ├── food-catalog.json                 ← Seed food data
    │           └── medical-rules.json                ← Condition-specific rules
    │
    └── test/
        └── java/com/nutriguideai/
            ├── controller/
            ├── service/
            │   └── RecommendationEngineTest.java     ← Core rule engine tests
            └── repository/
```

### 14.3 Frontend Folder Structure

```
frontend/
├── package.json
├── tailwind.config.js
├── postcss.config.js
├── public/
│   └── index.html
│
└── src/
    ├── index.js
    ├── App.js
    │
    ├── api/
    │   ├── axios.js                  ← Axios config + JWT interceptor
    │   ├── authApi.js
    │   ├── userApi.js
    │   ├── nutritionApi.js
    │   ├── mealPlanApi.js
    │   ├── foodDiaryApi.js
    │   └── progressApi.js
    │
    ├── components/
    │   ├── common/
    │   │   ├── Navbar.jsx
    │   │   ├── Sidebar.jsx
    │   │   ├── LoadingSpinner.jsx
    │   │   ├── ErrorState.jsx
    │   │   ├── EmptyState.jsx
    │   │   ├── ProtectedRoute.jsx
    │   │   └── Footer.jsx
    │   │
    │   ├── auth/
    │   │   ├── LoginForm.jsx
    │   │   └── RegisterForm.jsx
    │   │
    │   ├── dashboard/
    │   │   ├── SummaryCards.jsx
    │   │   ├── WeightTrendChart.jsx
    │   │   ├── CalorieBarChart.jsx
    │   │   └── MacroDoughnutChart.jsx
    │   │
    │   ├── meal-plan/
    │   │   ├── DailyMealCard.jsx
    │   │   ├── MealPlanGrid.jsx
    │   │   ├── NutritionTable.jsx
    │   │   └── GeneratePlanButton.jsx
    │   │
    │   ├── food-diary/
    │   │   ├── DiaryEntryForm.jsx
    │   │   ├── CalendarView.jsx
    │   │   └── DailySummary.jsx
    │   │
    │   └── profile/
    │       ├── HealthVitalsForm.jsx
    │       ├── GoalSelector.jsx
    │       └── PreferencesForm.jsx
    │
    ├── pages/
    │   ├── LandingPage.jsx
    │   ├── LoginPage.jsx
    │   ├── RegisterPage.jsx
    │   ├── DashboardPage.jsx
    │   ├── ProfilePage.jsx
    │   ├── MealPlanPage.jsx
    │   ├── FoodDiaryPage.jsx
    │   ├── ProgressPage.jsx
    │   └── AdminPage.jsx
    │
    ├── context/
    │   └── AuthContext.jsx
    │
    ├── hooks/
    │   ├── useAuth.js
    │   ├── useMealPlan.js
    │   └── useProgress.js
    │
    └── utils/
        ├── constants.js
        └── helpers.js
```

---

## 15. Technology Decisions

### 15.1 Frontend Technology Decisions

| **Technology**          | **Chosen**      | **Alternatives**       | **Rationale**                                                |
| ----------------------- | --------------- | ---------------------- | ------------------------------------------------------------ |
| **UI Framework**        | React.js        | Vue.js, Angular        | Component-based architecture, largest ecosystem, strong industry demand. Simplifies building interactive dashboards and multi-step forms. |
| **Styling**             | Tailwind CSS    | Bootstrap, MUI         | Utility-first approach enables rapid prototyping and consistent design. Purged build keeps bundle small (~10KB gzipped). Responsive by default. |
| **Routing**             | React Router    | Reach Router           | De facto standard for React SPAs. Nested routes, lazy loading, protected route patterns. |
| **HTTP Client**         | Axios           | Fetch API              | Request/response interceptors for JWT handling. Automatic JSON parsing. Request cancellation. |
| **Charts**              | Recharts        | Chart.js, D3.js        | React-native charting. Declarative JSX components compose naturally with React. Built on D3 but at a higher abstraction level. |
| **Forms**               | React Hook Form | Formik                 | Minimizes re-renders. Simple integration with validation schemas. Better performance for multi-step health profile forms. |

### 15.2 Backend Technology Decisions

| **Technology**              | **Chosen**       | **Alternatives**        | **Rationale**                                                |
| --------------------------- | ---------------- | ----------------------- | ------------------------------------------------------------ |
| **Programming Language**    | Java 21 (LTS)    | Java 17, Kotlin         | Long-term support, pattern matching, record types. Industry standard for enterprise applications. |
| **Application Framework**   | Spring Boot 3    | Quarkus, Micronaut      | Rich ecosystem for JPA, Security, Redis integration. Auto-configuration reduces setup time. Largest community and reference resources. |
| **Security**                | Spring Security  | Apache Shiro            | First-class JWT support via filters. Method-level `@PreAuthorize`. Tightly integrated with Spring Boot. |
| **ORM**                     | Hibernate + JPA  | MyBatis, jOOQ           | Declarative repositories. Handles 80% of CRUD use cases with zero SQL. JPQL for complex queries. |
| **Build Tool**              | Maven            | Gradle                  | Declarative builds, mature dependency management. More predictable than Gradle for v1. |
| **Object Mapping**          | MapStruct        | ModelMapper             | Compile-time code generation, zero runtime overhead. Type-safe. |
| **Boilerplate Reduction**   | Lombok           | Manual code             | `@Data`, `@Builder`, `@AllArgsConstructor` on entities and DTOs eliminates hundreds of lines of getters/setters. |
| **API Documentation**       | Swagger/OpenAPI  | Spring REST Docs        | Auto-generated from annotations. Interactive Swagger UI for testing. No separate AsciiDoc files needed. |
| **Testing**                 | JUnit 5 + Mockito| TestNG                  | Industry standard. Parameterized tests, Mockito for service layer mocking. |

### 15.3 Database & Caching Decisions

| **Technology**    | **Chosen** | **Alternatives**    | **Rationale**                                                |
| ----------------- | ---------- | ------------------ | ------------------------------------------------------------ |
| **Primary Database** | MySQL 8 | PostgreSQL          | ACID compliant, well-understood, excellent Spring Boot support. Azure MySQL managed service available. |
| **Cache Layer**   | Redis      | Memcached           | Data structures (strings, hashes for cache entries). Built-in TTL. Excellent Spring Data Redis integration. |

### 15.4 Infrastructure Decisions

| **Component**             | **Choice**         | **Rationale**                                                |
| ------------------------- | ------------------ | ------------------------------------------------------------ |
| **Containerization**      | Docker             | Consistent environments across dev/staging/prod. Eliminates environment-specific bugs. |
| **Backend Hosting**       | Azure App Service  | PaaS with built-in scaling, deployment slots, managed SSL. Good Spring Boot support. |
| **Database Hosting**      | Azure DB for MySQL | Managed MySQL with automated backups, patching, and monitoring. |
| **Frontend Hosting**      | Azure Static Web Apps / Vercel | CDN-hosted static build, global edge distribution, custom domain. |
| **CI/CD**                 | GitHub Actions     | Tightly integrated with GitHub repository. Free tier for public repos. |

---

## 16. Design Patterns Used

### 16.1 Pattern Overview

| **Pattern**               | **Category**       | **Where Used**                                     | **Why**                                                      |
| ------------------------- | ------------------ | -------------------------------------------------- | ------------------------------------------------------------ |
| **Layered Architecture**  | Architectural      | Entire backend (Controller → Service → Repository) | Separates concerns, enables independent testing, allows future microservice extraction. |
| **Repository Pattern**    | Structural         | `repository/` package                              | Abstracts data access behind interfaces. Spring Data JPA auto-implements CRUD. Swap database without affecting services. |
| **DTO Pattern**           | Structural         | `dto/` package                                     | Prevents entity exposure to clients. Controls exactly what data is sent. Breaks circular serialization. |
| **Dependency Injection**  | Structural         | Constructor injection in all services              | Loose coupling, testability (easy mocking), explicit dependencies. No `@Autowired` field injection. |
| **MVC**                   | Architectural      | Both frontend and backend                          | Backend: Controller → Service → View (JSON). Frontend: React components (View) → API calls (Controller) → State (Model). |
| **Builder Pattern**       | Creational         | Lombok `@Builder` on DTOs and entities            | Constructs complex objects with many optional fields without telescoping constructors. |
| **Singleton**             | Creational         | Spring Beans (default scope)                       | All services, repositories are stateless singletons. Thread-safe due to immutability. |

### 16.2 Where Each Pattern Applies

```
┌─────────────────────────────────────────────────────────────────────┐
│                        LAYERED ARCHITECTURE                          │
│                                                                     │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │  MVC Pattern                          │ Repository Pattern   │   │
│  │                                       │                      │   │
│  │  ┌────────────┐  ┌──────────────┐    │  ┌────────────────┐ │   │
│  │  │ Controller │  │    View      │    │  │ UserRepository │ │   │
│  │  │ (Spring)   │──│ (JSON Response)│   │  │ extends        │ │   │
│  │  └──────┬─────┘  └──────────────┘    │  │ JpaRepository  │ │   │
│  │         │          Model              │  └────────────────┘ │   │
│  │         ▼          (DTO)             │                      │   │
│  │  ┌────────────┐                      │  ┌────────────────┐ │   │
│  │  │  Service   │ ◄── DTO Pattern ────  │  │ ProfileRepo   │ │   │
│  │  │  Layer     │                      │  └────────────────┘ │   │
│  │  └────────────┘                      │                      │   │
│  └─────────────────────────────────────┘──────────────────────┘   │
│                                                                     │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │  Dependency Injection (Constructor)                          │   │
│  │                                                              │   │
│  │  public class NutritionService {                             │   │
│  │      private final MealPlanRepository mealPlanRepo;          │   │
│  │      private final RecommendationEngine engine;             │   │
│  │      private final CacheService cacheService;               │   │
│  │                                                              │   │
│  │      public NutritionService(                                │   │
│  │          MealPlanRepository mealPlanRepo,     ← Injected    │   │
│  │          RecommendationEngine engine,          ← Injected   │   │
│  │          CacheService cacheService             ← Injected   │   │
│  │      ) { ... }                                               │   │
│  │  }                                                           │   │
│  └─────────────────────────────────────────────────────────────┘   │
│                                                                     │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │  Builder Pattern (Lombok)                                     │   │
│  │                                                              │   │
│  │  @Data                                                       │   │
│  │  @Builder          ← Generates Builder inner class           │   │
│  │  public class HealthVitalsRequest {                          │   │
│  │      private Double bloodPressureSystolic;                   │   │
│  │      private Double bloodPressureDiastolic;                  │   │
│  │      private Double fastingSugar;                            │   │
│  │      private Double hba1c;                                    │   │
│  │      private Set<MedicalCondition> conditions;               │   │
│  │  }                                                           │   │
│  │                                                              │   │
│  │  // Usage:                                                   │   │
│  │  HealthVitalsRequest request = HealthVitalsRequest.builder() │   │
│  │      .bloodPressureSystolic(120.0)                           │   │
│  │      .conditions(Set.of(DIABETES))                           │   │
│  │      .build();                                               │   │
│  └─────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 17. Deployment Architecture

### 17.1 Deployment Overview

NutriGuide AI is deployed as a **containerized monolithic application** on Azure. The deployment consists of three primary components: the Spring Boot backend, the React frontend (static build), and the MySQL database.

### 17.2 Deployment Diagram

```
┌──────────────────────────────────────────────────────────────────────────┐
│                        AZURE CLOUD                                       │
│                                                                          │
│   ┌──────────────────────────────────────────────────────────────────┐   │
│   │                     Azure App Service                            │   │
│   │                     (Spring Boot Backend)                        │   │
│   │                                                                  │   │
│   │   ┌──────────────────────────────────────────────────────────┐   │   │
│   │   │  Docker Container: nutriguideai-backend:latest           │   │   │
│   │   │                                                          │   │   │
│   │   │  - Java 21 JRE (eclipse-temurin:21-jre-alpine)           │   │   │
│   │   │  - Port: 8080                                            │   │   │
│   │   │  - Health check: /api/actuator/health                    │   │   │
│   │   │                                                          │   │   │
│   │   │  Environment Variables:                                  │   │   │
│   │   │  - SPRING_DATASOURCE_URL                                 │   │   │
│   │   │  - SPRING_DATASOURCE_USERNAME                           │   │   │
│   │   │  - SPRING_DATASOURCE_PASSWORD                           │   │   │
│   │   │  - JWT_SECRET                                            │   │   │
│   │   │  - SPRING_REDIS_HOST                                     │   │   │
│   │   │  - SPRING_REDIS_PORT                                     │   │   │
│   │   └──────────────────────────────────────────────────────────┘   │   │
│   └──────────────────────────────────────────────────────────────────┘   │
│                                                                          │
│   ┌──────────────────────────────────────────────────────────────────┐   │
│   │               Azure Static Web Apps (or Vercel)                  │   │
│   │               (React Frontend - Static Build)                    │   │
│   │                                                                  │   │
│   │   ┌──────────────────────────────────────────────────────────┐   │   │
│   │   │  - Static assets served via global CDN                   │   │   │
│   │   │  - API requests proxied to Azure App Service             │   │   │
│   │   │  - Environment variable: REACT_APP_API_URL               │   │   │
│   │   └──────────────────────────────────────────────────────────┘   │   │
│   └──────────────────────────────────────────────────────────────────┘   │
│                                                                          │
│   ┌──────────────────────────────────────────────────────────────────┐   │
│   │               Azure Database for MySQL                          │   │
│   │               (Flexible Server)                                  │   │
│   │                                                                  │   │
│   │   ┌──────────────────────────────────────────────────────────┐   │   │
│   │   │  - Database: nutriguideai                                │   │   │
│   │   │  - Tier: Burstable (B1ms - 1 vCore, 2 GB RAM)           │   │   │
│   │   │  - Storage: 20 GB SSD                                    │   │   │
│   │   │  - Automated backups: 7-day retention                    │   │   │
│   │   │  - SSL enforcement enabled                                │   │   │
│   │   └──────────────────────────────────────────────────────────┘   │   │
│   └──────────────────────────────────────────────────────────────────┘   │
│                                                                          │
│   ┌──────────────────────────────────────────────────────────────────┐   │
│   │               Azure Cache for Redis                             │   │
│   │               (or self-hosted Redis via Docker)                 │   │
│   │                                                                  │   │
│   │   ┌──────────────────────────────────────────────────────────┐   │   │
│   │   │  - SKU: Basic C0 (250 MB cache) for dev/test             │   │   │
│   │   │  - Standard C1 (1 GB cache) for production               │   │   │
│   │   │  - TLS encryption enabled                                 │   │   │
│   │   └──────────────────────────────────────────────────────────┘   │   │
│   └──────────────────────────────────────────────────────────────────┘   │
│                                                                          │
└──────────────────────────────────────────────────────────────────────────┘


┌──────────────────────────────────────────────────────────────────────────┐
│                        LOCAL DEVELOPMENT                                 │
│                                                                          │
│   ┌──────────────────────────────────────────────────────────────────┐   │
│   │                    Docker Compose                                  │   │
│   │                                                                  │   │
│   │   ┌──────────────────┐     ┌──────────────────┐                 │   │
│   │   │  Backend         │     │  Frontend        │                 │   │
│   │   │  (Spring Boot)   │     │  (Vite Dev)      │                 │   │
│   │   │  Port: 8080      │     │  Port: 5173      │                 │   │
│   │   └──────────────────┘     └──────────────────┘                 │   │
│   │                                                                  │   │
│   │   ┌──────────────────┐     ┌──────────────────┐                 │   │
│   │   │  MySQL 8.0       │     │  Redis 7.x       │                 │   │
│   │   │  Port: 3306      │     │  Port: 6379      │                 │   │
│   │   └──────────────────┘     └──────────────────┘                 │   │
│   └──────────────────────────────────────────────────────────────────┘   │
│                                                                          │
└──────────────────────────────────────────────────────────────────────────┘
```

### 17.3 CI/CD Pipeline

```
GitHub Push (feature/* or main)
        │
        ▼
   ┌───────────┐
   │   Build   │  ──  mvn clean compile
   └─────┬─────┘
         │
         ▼
   ┌───────────┐
   │   Test    │  ──  mvn test (JUnit + Mockito)
   └─────┬─────┘
         │
         ▼
   ┌───────────┐
   │  Package  │  ──  mvn package → JAR
   └─────┬─────┘
         │
         ▼
   ┌───────────┐
   │  Docker   │  ──  docker build → Image
   └─────┬─────┘
         │
         ▼
   ┌───────────┐
   │  Deploy   │  ──  Azure App Service deploy
   └─────┬─────┘
         │
         ▼
   ┌───────────┐
   │  Health   │  ──  GET /api/actuator/health → 200
   │  Check    │
   └───────────┘
```

### 17.4 Future Scope (Azure Blob Storage)

In future versions, Azure Blob Storage may be added for:

- User-uploaded medical report images
- User profile pictures
- System-generated report exports (PDF)

These are explicitly **not part of v1**.

---

## 18. Security Architecture

### 18.1 Security Layers

```
┌─────────────────────────────────────────────────────────────────────┐
│                     SECURITY ARCHITECTURE                            │
│                                                                     │
│   Layer 1: Transport Security                                       │
│   ┌─────────────────────────────────────────────────────────────┐   │
│   │  - HTTPS enforced (TLS 1.2+)                                 │   │
│   │  - HSTS headers                                              │   │
│   │  - No HTTP traffic allowed in production                     │   │
│   └─────────────────────────────────────────────────────────────┘   │
│                                                                     │
│   Layer 2: Authentication                                           │
│   ┌─────────────────────────────────────────────────────────────┐   │
│   │  - JWT Bearer Token (HS256 signed)                          │   │
│   │  - Token in Authorization header                            │   │
│   │  - 24-hour token expiry                                     │   │
│   │  - Passwords hashed with BCrypt (strength 10)               │   │
│   └─────────────────────────────────────────────────────────────┘   │
│                                                                     │
│   Layer 3: Authorization                                            │
│   ┌─────────────────────────────────────────────────────────────┐   │
│   │  - Role-based access control (USER, ADMIN)                  │   │
│   │  - Method-level @PreAuthorize annotations                   │   │
│   │  - Resource ownership checks (user can access own data only)│   │
│   └─────────────────────────────────────────────────────────────┘   │
│                                                                     │
│   Layer 4: Input Validation                                         │
│   ┌─────────────────────────────────────────────────────────────┐   │
│   │  - @Valid on all request DTOs                               │   │
│   │  - Custom validators for health-specific fields             │   │
│   │  - XSS prevention via input sanitization                    │   │
│   └─────────────────────────────────────────────────────────────┘   │
│                                                                     │
│   Layer 5: Output Security                                          │
│   ┌─────────────────────────────────────────────────────────────┐   │
│   │  - DTO Pattern prevents entity exposure                     │   │
│   │  - No sensitive data in logs (password tokens masked)       │   │
│   │  - CORS configuration allows only frontend origin           │   │
│   └─────────────────────────────────────────────────────────────┘   │
│                                                                     │
│   Layer 6: Infrastructure Security                                  │
│   ┌─────────────────────────────────────────────────────────────┐   │
│   │  - Docker containers run as non-root user                   │   │
│   │  - MySQL SSL/TLS enforced                                   │   │
│   │  - Redis password-protected (production)                    │   │
│   │  - Environment variables for secrets (no hardcoded values)  │   │
│   └─────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────┘
```

### 18.2 OWASP Mitigations

| **OWASP Risk**                | **Mitigation**                                               |
| ----------------------------- | ------------------------------------------------------------ |
| Broken Authentication         | BCrypt password hashing, JWT with proper expiry, rate limiting on login |
| Sensitive Data Exposure       | HTTPS enforced, passwords never logged, DTO pattern prevents over-exposure |
| SQL Injection                 | Parameterized queries via JPA/JPQL, no raw SQL concatenation  |
| Cross-Site Scripting (XSS)    | React's built-in XSS protection, Content Security Policy headers |
| Cross-Site Request Forgery    | JWT in Authorization header (not cookies), SameSite policy   |
| Security Misconfiguration     | Minimal Spring Security config, CORS whitelist, disabled unused endpoints |

---

## 19. Scalability Considerations

### 19.1 Vertical Scaling (v1)

| **Resource**          | **Scale Up Option**                                          |
| --------------------- | ------------------------------------------------------------ |
| **App Service**       | Increase tier (B1 → B2 → B3 → S1) for more CPU and RAM      |
| **Azure DB for MySQL**| Increase vCores (1 → 2 → 4) and storage (20 GB → 100 GB)    |
| **Azure Cache for Redis**| Upgrade SKU (C0 250MB → C1 1GB → C2 2.5GB)              |

### 19.2 Horizontal Scaling (v2+)

| **Component**         | **Scale Out Option**                                         |
| --------------------- | ------------------------------------------------------------ |
| **App Service**       | Enable auto-scaling (2–5 instances behind load balancer)     |
| **MySQL**             | Add read replicas for dashboard queries                      |
| **Redis**             | Enable clustering for larger cache datasets                  |

### 19.3 Performance Optimization in v1

| **Area**              | **Optimization Strategy**                                      |
| --------------------- | ------------------------------------------------------------ |
| **Database Queries**  | Indexed columns on frequently queried fields (user_id, date, email) |
| **Caching**           | Redis for food catalog, meal plans, dashboard stats           |
| **API Pagination**    | All listing endpoints return paginated results               |
| **Connection Pooling**| HikariCP tuned for optimal connection management              |
| **Frontend Bundle**   | Code splitting, lazy loading for route-based chunks           |

---

## 20. Future Enhancements

The following enhancements are explicitly **not part of v1** but are identified for future releases:

| **Enhancement**                    | **Impact**                                     | **Architectural Change**                |
| ---------------------------------- | ---------------------------------------------- | --------------------------------------- |
| **Medical Report Upload**          | Users upload lab reports for auto-fill         | Azure Blob Storage, optional OCR        |
| **Grocery List Generation**        | Auto-generate shopping lists from meal plans  | New service method, no structural change |
| **Weekly Meal Planner**            | Full 7-day view with adjustments              | New controller/service, no structural change |
| **Notifications & Reminders**      | Email/push for meals, water, exercise         | Push notification service integration   |
| **Social Features**                | Challenges, streaks, community sharing        | Social module, relational tables        |
| **Mobile App**                     | React Native or Flutter client                | New frontend, shares same backend API   |
| **Advanced Admin Dashboard**       | Analytics, content management                 | Extended Admin module                   |

---

## 21. Conclusion

### 21.1 Architecture Summary

NutriGuide AI is designed as a **Layered Monolithic Application** with clear separation of concerns across five layers: Presentation, Controller, Service, Repository, and Database. The architecture prioritizes:

| **Quality Attribute** | **How It Is Achieved**                                      |
| --------------------- | ------------------------------------------------------------ |
| **Maintainability**   | Strict layer separation, consistent DTO pattern, modular package structure |
| **Testability**       | Constructor injection enables easy mocking; service layer isolated from HTTP |
| **Performance**       | Redis caching for read-heavy data, optimized database queries, frontend code splitting |
| **Security**          | JWT authentication, BCrypt hashing, role-based access, input validation |
| **Scalability**       | Vertical scaling for v1, horizontal scaling path for v2+      |
| **Simplicity**        | Monolithic deployment avoids microservice complexity; rule-based engine avoids AI integration costs |

### 21.2 Key Architectural Decisions

1. **Rule-based recommendation engine** over LLM/AI: Eliminates API costs, latency, hallucination risks, and external dependencies while providing deterministic, auditable recommendations.

2. **JWT with Access Token only** (no refresh tokens): Simplifies authentication flow, eliminates refresh token storage and rotation complexity, sufficient for v1 scope.

3. **Redis for caching only** (not sessions): Maintains stateless backend design, reduces database load for read-heavy operations, simple cache-aside pattern.

4. **Layered Monolithic** over Microservices: Appropriate for current team size and project scope, enables fast development, can be extracted into microservices later if needed.

5. **DTO Pattern** throughout: Prevents entity exposure, controls API contract independently of database schema, enables field-level versioning.

### 21.3 Production Readiness

The architecture is designed to be **production-ready** while remaining achievable as an engineering project. All components are well-understood, widely documented, and have strong community support. The deployment targets Azure's free/paid tiers, making it accessible for both development and production use.

---

## Document Version History

| **Version** | **Date**     | **Author** | **Changes**          |
| ----------- | ------------ | ---------- | -------------------- |
| 1.0         | 2026-07-27   | Architect  | Initial draft        |

---

*End of Document — 03_ARCHITECTURE.md*
