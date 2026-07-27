# 04 — DATABASE DESIGN

| **Field**             | **Value**                                    |
| --------------------- | -------------------------------------------- |
| **Project Name**      | NutriGuide AI                                |
| **Document Title**    | Database Design Document                     |
| **Version**           | 1.0                                          |
| **Author**            | Software Architecture Team                   |
| **Created On**        | 2026-07-27                                   |
| **Last Updated**      | 2026-07-27                                   |
| **DBMS**              | MySQL 8.0                                    |
| **Status**            | Draft                                        |
| **References**        | `01_PROJECT_CONTEXT.md`, `03_SYSTEM_ARCHITECTURE.md` |

---

## Table of Contents

1. [Introduction](#1-introduction)
2. [Entity Relationship Diagram](#2-entity-relationship-diagram)
3. [Entity Dictionary](#3-entity-dictionary)
4. [Table Specifications](#4-table-specifications)
5. [Relationship Matrix](#5-relationship-matrix)
6. [Normalization](#6-normalization)
7. [Indexes](#7-indexes)
8. [Constraints](#8-constraints)
9. [Data Types Summary](#9-data-types-summary)
10. [Enum Reference Values](#10-enum-reference-values)
11. [Data Integrity Rules](#11-data-integrity-rules)
12. [Performance Considerations](#12-performance-considerations)

---

## 1. Introduction

### 1.1 Purpose

This document defines the complete database design for NutriGuide AI. It serves as the authoritative reference for all database-related development decisions, including table structures, relationships, constraints, indexing strategy, and data integrity rules.

### 1.2 Database Role

MySQL 8.0 serves as the **single source of truth** for all persistent data. It stores user profiles, health vitals, meal plans, food catalogs, food diary entries, and progress tracking data. Redis is used as a separate caching layer (see `03_SYSTEM_ARCHITECTURE.md`) and does not store authoritative data.

### 1.3 Design Principles

| **Principle**            | **Application**                                               |
| ------------------------ | ------------------------------------------------------------- |
| **Normalization**        | 3NF (Third Normal Form) for transactional tables              |
| **Referential Integrity**| Foreign keys enforced at the database level                   |
| **Consistent Naming**    | `snake_case` for tables and columns                           |
| **Audit Trail**          | All tables have `created_at` and `updated_at` timestamps       |
| **Soft Delete**          | `is_active` flag for user-related tables where applicable     |
| **Index Strategy**       | Index foreign keys + frequently queried columns               |

---

## 2. Entity Relationship Diagram

### 2.1 ER Diagram (Text Representation)

```
 ┌────────────────────────────────────────────────────────────────────────────────────┐
 │                              ENTITY RELATIONSHIP DIAGRAM                            │
 │                                                                                    │
 │                                                                                    │
 │     ┌──────────────┐          ┌──────────────────┐          ┌──────────────────┐  │
 │     │     User     │ 1      1 │   UserProfile    │ 1      1 │   HealthVitals   │  │
 │     │──────────────│──────────│──────────────────│──────────│──────────────────│  │
 │     │ user_id (PK) │◄────────►│ user_id (PK,FK)  │◄────────►│ user_id (PK,FK)  │  │
 │     │ email        │          │ first_name       │          │ blood_pressure_sys│  │
 │     │ password_hash│          │ last_name        │          │ blood_pressure_dia│  │
 │     │ role         │          │ date_of_birth    │          │ fasting_sugar     │  │
 │     │ is_active    │          │ gender           │          │ post_meal_sugar   │  │
 │     │ created_at   │          │ height_cm        │          │ hba1c             │  │
 │     │ updated_at   │          │ weight_kg        │          │ cholesterol_ldl   │  │
 │     └──────┬───────┘          │ bmi              │          │ cholesterol_hdl   │  │
 │            │                  │ created_at       │          │ triglycerides     │  │
 │            │                  │ updated_at       │          │ created_at        │  │
 │            │                  └──────────────────┘          │ updated_at        │  │
 │            │                                                 └──────────────────┘  │
 │            │                                                                       │
 │            │ 1                                                                     │
 │            │                                                                       │
 │            ▼                                                                       │
 │     ┌──────────────────┐          ┌──────────────────┐                            │
 │     │   UserGoal       │ 1      1 │  FoodPreference  │                            │
 │     │──────────────────│──────────│──────────────────│                            │
 │     │ user_id (PK,FK)  │◄────────►│ user_id (PK,FK)  │                            │
 │     │ primary_goal     │          │ diet_type        │                            │
 │     │ activity_level   │          │ budget_level     │                            │
 │     │ target_calories  │          │ region           │                            │
 │     │ target_protein_g │          │ allergies_json   │                            │
 │     │ target_carbs_g   │          │ excluded_foods   │                            │
 │     │ target_fat_g     │          │ created_at       │                            │
 │     │ sleep_hours      │          │ updated_at       │                            │
 │     │ water_intake_ml  │          └──────────────────┘                            │
 │     │ created_at       │                                                          │
 │     │ updated_at       │                                                          │
 │     └──────────────────┘                                                          │
 │            │                                                                       │
 │            │ 1                                                                     │
 │            │                                                                       │
 │            ▼                                                                       │
 │     ┌──────────────────┐          ┌──────────────────┐          ┌────────────────┐ │
 │     │   MealPlan       │ 1      N │  MealPlanItem    │ N      1 │   FoodItem    │ │
 │     │──────────────────│──────────│──────────────────│──────────│────────────────│ │
 │     │ plan_id (PK)     │◄────────►│ item_id (PK)     │◄────────►│ food_id (PK)  │ │
 │     │ user_id (FK)     │          │ plan_id (FK)     │          │ food_name      │ │
 │     │ plan_date        │          │ meal_type        │          │ category       │ │
 │     │ total_calories   │          │ food_id (FK)     │          │ calories_100g   │ │
 │     │ total_protein_g  │          │ serving_size_g   │          │ protein_100g   │ │
 │     │ total_carbs_g    │          │ calories         │          │ carbs_100g     │ │
 │     │ total_fat_g      │          │ protein_g        │          │ fat_100g       │ │
 │     │ dietary_tips_json│          │ carbs_g          │          │ fiber_100g     │ │
 │     │ is_generated     │          │ fat_g            │          │ sugar_100g     │ │
 │     │ created_at       │          │ serving_unit     │          │ diet_type      │ │
 │     │ updated_at       │          └──────────────────┘          │ budget_level   │ │
 │     └──────────────────┘                                        │ is_active      │ │
 │                                                                  │ created_at     │ │
 │            │                                                      │ updated_at     │ │
 │            │ 1                                                    └────────────────┘ │
 │            ▼                                                                       │
 │     ┌──────────────────┐          ┌──────────────────┐                            │
 │     │  FoodDiaryEntry  │ N      1 │  FoodItem        │                            │
 │     │──────────────────│──────────│──────────────────│                            │
 │     │ entry_id (PK)    │◄────────►│ food_id (PK)     │                            │
 │     │ user_id (FK)     │          │ (reused from     │                            │
 │     │ food_id (FK)     │          │  above)          │                            │
 │     │ meal_type        │          └──────────────────┘                            │
 │     │ serving_size_g   │                                                          │
 │     │ calories         │                                                          │
 │     │ protein_g        │                                                          │
 │     │ carbs_g          │                                                          │
 │     │ fat_g            │                                                          │
 │     │ logged_date      │                                                          │
 │     │ logged_time      │                                                          │
 │     │ notes            │                                                          │
 │     │ created_at       │                                                          │
 │     └──────────────────┘                                                          │
 │                                                                                    │
 │     ┌──────────────────┐          ┌──────────────────┐                            │
 │     │ ProgressTracking │ N      1 │  UserProfile     │                            │
 │     │──────────────────│──────────│──────────────────│                            │
 │     │ tracking_id (PK) │◄────────►│ user_id (PK,FK)  │                            │
 │     │ user_id (FK)     │          └──────────────────┘                            │
 │     │ recorded_date    │                                                          │
 │     │ weight_kg        │                                                          │
 │     │ bmi              │                                                          │
 │     │ calories_consumed│                                                          │
 │     │ protein_consumed │                                                          │
 │     │ carbs_consumed   │                                                          │
 │     │ fat_consumed     │                                                          │
 │     │ water_intake_ml  │                                                          │
 │     │ sleep_hours      │                                                          │
 │     │ notes            │                                                          │
 │     │ created_at       │                                                          │
 │     └──────────────────┘                                                          │
 │                                                                                    │
 │     ┌──────────────────┐          ┌──────────────────────┐                       │
 │     │ UserCondition    │ N      1 │  UserHealthVitals   │                        │
 │     │──────────────────│──────────│──────────────────────│ (many-to-many          │
 │     │ condition_id(PK) │◄────────►│  resolved via        │  between user          │
 │     │ user_id (FK)     │          │  UserCondition table │  and medical           │
 │     │ condition (ENUM) │          └──────────────────────┘  conditions)           │
 │     │ severity         │                                                          │
 │     │ diagnosed_date   │                                                          │
 │     │ notes            │                                                          │
 │     │ created_at       │                                                          │
 │     └──────────────────┘                                                          │
 │                                                                                    │
 └────────────────────────────────────────────────────────────────────────────────────┘
```

### 2.2 Relationship Summary (Text)

```
User (1) ──── (1) UserProfile
User (1) ──── (1) HealthVitals
User (1) ──── (N) UserCondition
User (1) ──── (1) UserGoal
User (1) ──── (1) FoodPreference
User (1) ──── (N) MealPlan
User (1) ──── (N) FoodDiaryEntry
User (1) ──── (N) ProgressTracking

MealPlan (1) ──── (N) MealPlanItem
MealPlanItem (N) ──── (1) FoodItem

FoodDiaryEntry (N) ──── (1) FoodItem

ProgressTracking (N) ──── (1) UserProfile (via user_id)
```

---

## 3. Entity Dictionary

| **#** | **Entity**          | **Description**                                          | **Type**         |
| ----- | ------------------- | -------------------------------------------------------- | ---------------- |
| 1     | **User**            | Core authentication entity. Stores login credentials and role. | Master      |
| 2     | **UserProfile**     | Demographic information: name, age, gender, height, weight, BMI. | Detail (1:1) |
| 3     | **HealthVitals**    | Medical readings: blood pressure, sugar, cholesterol, triglycerides. | Detail (1:1) |
| 4     | **UserCondition**   | Medical conditions associated with a user (supports multiple conditions). | Detail (1:N) |
| 5     | **UserGoal**        | Health goals, activity level, target calories/macros, lifestyle data. | Detail (1:1) |
| 6     | **FoodPreference**  | Dietary preferences: diet type, budget, region, allergies. | Detail (1:1) |
| 7     | **MealPlan**        | Generated daily meal plan (one per user per day).        | Transactional   |
| 8     | **MealPlanItem**    | Individual meal entries within a meal plan.              | Transactional   |
| 9     | **FoodItem**        | Reference catalog of foods with nutritional values.      | Reference       |
| 10    | **FoodDiaryEntry**  | User-logged food consumption for manual tracking.        | Transactional   |
| 11    | **ProgressTracking**| Daily snapshot of weight, nutrition, and water/sleep data for dashboards. | Transactional |

---

## 4. Table Specifications

### 4.1 `users`

Core authentication table. Stores credentials and account metadata.

| **Column**       | **Data Type**        | **Constraints**                  | **Description**                          |
| ---------------- | -------------------- | -------------------------------- | ---------------------------------------- |
| `user_id`        | `BIGINT UNSIGNED`    | `PRIMARY KEY`, `AUTO_INCREMENT`  | Unique user identifier                   |
| `email`          | `VARCHAR(255)`       | `NOT NULL`, `UNIQUE`             | User email address (used as login ID)     |
| `password_hash`  | `VARCHAR(255)`       | `NOT NULL`                       | BCrypt-encoded password hash             |
| `role`           | `ENUM('USER','ADMIN')`| `NOT NULL`, `DEFAULT 'USER'`    | User role for authorization              |
| `is_active`      | `TINYINT(1)`         | `NOT NULL`, `DEFAULT 1`          | Soft delete flag (1 = active, 0 = inactive) |
| `last_login_at`  | `TIMESTAMP`          | `NULL`                           | Timestamp of last successful login       |
| `created_at`     | `TIMESTAMP`          | `NOT NULL`, `DEFAULT CURRENT_TIMESTAMP` | Record creation timestamp         |
| `updated_at`     | `TIMESTAMP`          | `NOT NULL`, `DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` | Last update timestamp |

**Indexes:**
- `PRIMARY KEY` on `user_id`
- `UNIQUE INDEX` on `email`
- `INDEX` on `role`

**Sample Row:**
```
user_id: 1
email: "john@example.com"
password_hash: "$2a$10$8K1p/a0dL1LXMIgoEDFrwOfMQkf4nO0x3Q..."
role: "USER"
is_active: 1
last_login_at: 2026-07-27 09:15:00
created_at: 2026-07-20 14:30:00
updated_at: 2026-07-27 09:15:00
```

---

### 4.2 `user_profiles`

Demographic information. One-to-one with `users`.

| **Column**       | **Data Type**        | **Constraints**                  | **Description**                          |
| ---------------- | -------------------- | -------------------------------- | ---------------------------------------- |
| `user_id`        | `BIGINT UNSIGNED`    | `PRIMARY KEY`, `FOREIGN KEY REFERENCES users(user_id)` | Matches user ID |
| `first_name`     | `VARCHAR(100)`       | `NOT NULL`                       | User's first name                        |
| `last_name`      | `VARCHAR(100)`       | `NOT NULL`                       | User's last name                         |
| `date_of_birth`  | `DATE`               | `NOT NULL`                       | Date of birth for age calculation         |
| `gender`         | `ENUM('MALE','FEMALE','OTHER')`| `NOT NULL`             | Gender for BMR calculation               |
| `height_cm`      | `DECIMAL(5,1)`       | `NOT NULL`, `CHECK (height_cm > 50 AND height_cm < 300)` | Height in centimeters |
| `weight_kg`      | `DECIMAL(5,1)`       | `NOT NULL`, `CHECK (weight_kg > 10 AND weight_kg < 500)` | Current weight in kilograms |
| `bmi`            | `DECIMAL(4,1)`       | `NULL`                           | Calculated BMI (weight_kg / (height_m)²)  |
| `phone`          | `VARCHAR(20)`        | `NULL`                           | Contact phone number                      |
| `created_at`     | `TIMESTAMP`          | `NOT NULL`, `DEFAULT CURRENT_TIMESTAMP` | Record creation timestamp         |
| `updated_at`     | `TIMESTAMP`          | `NOT NULL`, `DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` | Last update timestamp |

**Indexes:**
- `PRIMARY KEY` on `user_id`
- `INDEX` on `height_cm`, `weight_kg` (for aggregate queries)

**Foreign Key:**
- `user_id` → `users(user_id)` with `ON DELETE CASCADE`

**Sample Row:**
```
user_id: 1
first_name: "John"
last_name: "Doe"
date_of_birth: 1995-03-15
gender: "MALE"
height_cm: 175.0
weight_kg: 78.5
bmi: 25.6
phone: "+1-555-0123"
created_at: 2026-07-20 14:30:00
updated_at: 2026-07-27 09:15:00
```

---

### 4.3 `health_vitals`

Medical readings and lab values. One-to-one with `users`.

| **Column**              | **Data Type**      | **Constraints**                     | **Description**                             |
| ----------------------- | ------------------ | ----------------------------------- | ------------------------------------------- |
| `user_id`               | `BIGINT UNSIGNED`  | `PRIMARY KEY`, `FOREIGN KEY REFERENCES users(user_id)` | Matches user ID        |
| `blood_pressure_sys`    | `SMALLINT UNSIGNED`| `NULL`, `CHECK (bp_sys >= 60 AND bp_sys <= 250)` | Systolic BP in mmHg         |
| `blood_pressure_dia`    | `SMALLINT UNSIGNED`| `NULL`, `CHECK (bp_dia >= 30 AND bp_dia <= 150)` | Diastolic BP in mmHg        |
| `fasting_sugar`         | `DECIMAL(5,1)`     | `NULL`, `CHECK (fasting_sugar >= 20 AND fasting_sugar <= 500)` | Fasting blood glucose in mg/dL |
| `post_meal_sugar`       | `DECIMAL(5,1)`     | `NULL`, `CHECK (post_meal_sugar >= 20 AND post_meal_sugar <= 600)` | Postprandial glucose in mg/dL |
| `hba1c`                 | `DECIMAL(4,1)`     | `NULL`, `CHECK (hba1c >= 2.0 AND hba1c <= 20.0)` | Glycated hemoglobin in %      |
| `cholesterol_ldl`       | `DECIMAL(5,1)`     | `NULL`, `CHECK (cholesterol_ldl >= 10 AND cholesterol_ldl <= 500)` | LDL cholesterol in mg/dL |
| `cholesterol_hdl`       | `DECIMAL(5,1)`     | `NULL`, `CHECK (cholesterol_hdl >= 5 AND cholesterol_hdl <= 150)` | HDL cholesterol in mg/dL |
| `triglycerides`         | `DECIMAL(5,1)`     | `NULL`, `CHECK (triglycerides >= 10 AND triglycerides <= 2000)` | Triglycerides in mg/dL |
| `created_at`            | `TIMESTAMP`        | `NOT NULL`, `DEFAULT CURRENT_TIMESTAMP`    | Record creation timestamp            |
| `updated_at`            | `TIMESTAMP`        | `NOT NULL`, `DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` | Last update timestamp |

**Indexes:**
- `PRIMARY KEY` on `user_id`

**Foreign Key:**
- `user_id` → `users(user_id)` with `ON DELETE CASCADE`

**Sample Row:**
```
user_id: 1
blood_pressure_sys: 120
blood_pressure_dia: 80
fasting_sugar: 92.0
post_meal_sugar: 135.0
hba1c: 5.4
cholesterol_ldl: 100.0
cholesterol_hdl: 55.0
triglycerides: 120.0
created_at: 2026-07-20 14:35:00
updated_at: 2026-07-27 09:20:00
```

---

### 4.4 `user_conditions`

Medical conditions associated with a user. Supports multiple conditions per user (normalized many-to-many).

| **Column**        | **Data Type**        | **Constraints**                  | **Description**                          |
| ----------------- | -------------------- | -------------------------------- | ---------------------------------------- |
| `condition_id`    | `BIGINT UNSIGNED`    | `PRIMARY KEY`, `AUTO_INCREMENT`  | Unique condition record ID               |
| `user_id`         | `BIGINT UNSIGNED`    | `NOT NULL`, `FOREIGN KEY REFERENCES users(user_id)` | Associated user |
| `condition`       | `ENUM('DIABETES','HYPERTENSION','PCOD','THYROID','HIGH_CHOLESTEROL','FATTY_LIVER','KIDNEY_DISEASE','ANEMIA','NONE')` | `NOT NULL` | Medical condition |
| `severity`        | `ENUM('MILD','MODERATE','SEVERE')`| `NULL`              | Condition severity level                 |
| `diagnosed_date`  | `DATE`               | `NULL`                           | Date of diagnosis                        |
| `notes`           | `TEXT`               | `NULL`                           | Additional notes about the condition     |
| `created_at`      | `TIMESTAMP`          | `NOT NULL`, `DEFAULT CURRENT_TIMESTAMP` | Record creation timestamp         |
| `updated_at`      | `TIMESTAMP`          | `NOT NULL`, `DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` | Last update timestamp |

**Indexes:**
- `PRIMARY KEY` on `condition_id`
- `UNIQUE INDEX` on `(user_id, condition)` — a user cannot have duplicate condition entries
- `INDEX` on `user_id`
- `INDEX` on `condition`

**Foreign Key:**
- `user_id` → `users(user_id)` with `ON DELETE CASCADE`

**Sample Row:**
```
condition_id: 1
user_id: 1
condition: "DIABETES"
severity: "MODERATE"
diagnosed_date: 2024-01-15
notes: "Type 2 diabetes, controlled with diet"
created_at: 2026-07-20 14:35:00
updated_at: 2026-07-20 14:35:00
```

---

### 4.5 `user_goals`

Health goals and lifestyle data. One-to-one with `users`.

| **Column**          | **Data Type**         | **Constraints**                     | **Description**                           |
| ------------------- | --------------------- | ----------------------------------- | ----------------------------------------- |
| `user_id`           | `BIGINT UNSIGNED`     | `PRIMARY KEY`, `FOREIGN KEY REFERENCES users(user_id)` | Matches user ID        |
| `primary_goal`      | `ENUM('WEIGHT_LOSS','WEIGHT_GAIN','MUSCLE_GAIN','MAINTENANCE','HEALTHY_LIFESTYLE')` | `NOT NULL` | User's primary health goal |
| `activity_level`    | `ENUM('SEDENTARY','LIGHT','MODERATE','ACTIVE','VERY_ACTIVE')` | `NOT NULL` | Physical activity level for TDEE calculation |
| `target_calories`   | `SMALLINT UNSIGNED`   | `NULL`, `CHECK (target_calories >= 800 AND target_calories <= 5000)` | Recommended daily calorie target |
| `target_protein_g`  | `SMALLINT UNSIGNED`   | `NULL`, `CHECK (target_protein_g >= 10 AND target_protein_g <= 400)` | Recommended daily protein in grams |
| `target_carbs_g`    | `SMALLINT UNSIGNED`   | `NULL`, `CHECK (target_carbs_g >= 10 AND target_carbs_g <= 600)` | Recommended daily carbs in grams |
| `target_fat_g`      | `SMALLINT UNSIGNED`   | `NULL`, `CHECK (target_fat_g >= 5 AND target_fat_g <= 300)` | Recommended daily fat in grams |
| `sleep_hours`       | `DECIMAL(3,1)`        | `NULL`, `CHECK (sleep_hours >= 2 AND sleep_hours <= 16)` | Average daily sleep in hours |
| `water_intake_ml`   | `SMALLINT UNSIGNED`   | `NULL`, `CHECK (water_intake_ml >= 200 AND water_intake_ml <= 10000)` | Daily water intake target in mL |
| `created_at`        | `TIMESTAMP`           | `NOT NULL`, `DEFAULT CURRENT_TIMESTAMP`    | Record creation timestamp            |
| `updated_at`        | `TIMESTAMP`           | `NOT NULL`, `DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` | Last update timestamp |

**Indexes:**
- `PRIMARY KEY` on `user_id`
- `INDEX` on `primary_goal` (for aggregate analytics)

**Foreign Key:**
- `user_id` → `users(user_id)` with `ON DELETE CASCADE`

**Sample Row:**
```
user_id: 1
primary_goal: "WEIGHT_LOSS"
activity_level: "MODERATE"
target_calories: 2100
target_protein_g: 105
target_carbs_g: 210
target_fat_g: 70
sleep_hours: 7.5
water_intake_ml: 2500
created_at: 2026-07-20 14:35:00
updated_at: 2026-07-27 09:20:00
```

---

### 4.6 `food_preferences`

Dietary preferences and restrictions. One-to-one with `users`.

| **Column**        | **Data Type**        | **Constraints**                  | **Description**                          |
| ----------------- | -------------------- | -------------------------------- | ---------------------------------------- |
| `user_id`         | `BIGINT UNSIGNED`    | `PRIMARY KEY`, `FOREIGN KEY REFERENCES users(user_id)` | Matches user ID |
| `diet_type`       | `ENUM('VEGETARIAN','NON_VEGETARIAN','VEGAN','EGGETARIAN','FLEXITARIAN')` | `NOT NULL` | Dietary preference |
| `budget_level`    | `ENUM('LOW','MEDIUM','HIGH')`| `NOT NULL`                  | Budget for meal cost                     |
| `region`          | `VARCHAR(100)`       | `NULL`                           | Geographic region for local food suggestions |
| `allergies_json`  | `JSON`               | `NULL`                           | Array of allergies (e.g., ["PEANUTS","LACTOSE","GLUTEN"]) |
| `excluded_foods`  | `TEXT`               | `NULL`                           | Free-text list of foods to exclude       |
| `created_at`      | `TIMESTAMP`          | `NOT NULL`, `DEFAULT CURRENT_TIMESTAMP` | Record creation timestamp         |
| `updated_at`      | `TIMESTAMP`          | `NOT NULL`, `DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` | Last update timestamp |

**Indexes:**
- `PRIMARY KEY` on `user_id`
- `INDEX` on `diet_type` (for catalog filtering queries)

**Foreign Key:**
- `user_id` → `users(user_id)` with `ON DELETE CASCADE`

**Sample Row:**
```
user_id: 1
diet_type: "VEGETARIAN"
budget_level: "MEDIUM"
region: "North India"
allergies_json: '["LACTOSE"]'
excluded_foods: "Mushrooms, Okra"
created_at: 2026-07-20 14:40:00
updated_at: 2026-07-27 09:20:00
```

---

### 4.7 `meal_plans`

Generated daily meal plans. One user can have multiple meal plans (one per day).

| **Column**          | **Data Type**        | **Constraints**                  | **Description**                          |
| ------------------- | -------------------- | -------------------------------- | ---------------------------------------- |
| `plan_id`           | `BIGINT UNSIGNED`    | `PRIMARY KEY`, `AUTO_INCREMENT`  | Unique plan identifier                   |
| `user_id`           | `BIGINT UNSIGNED`    | `NOT NULL`, `FOREIGN KEY REFERENCES users(user_id)` | Owner of this plan |
| `plan_date`         | `DATE`               | `NOT NULL`                       | Date this plan is for                    |
| `total_calories`    | `SMALLINT UNSIGNED`  | `NOT NULL`, `CHECK (total_calories >= 0)` | Total daily calories           |
| `total_protein_g`   | `SMALLINT UNSIGNED`  | `NOT NULL`                       | Total daily protein in grams             |
| `total_carbs_g`     | `SMALLINT UNSIGNED`  | `NOT NULL`                       | Total daily carbs in grams               |
| `total_fat_g`       | `SMALLINT UNSIGNED`  | `NOT NULL`                       | Total daily fat in grams                 |
| `dietary_tips_json` | `JSON`               | `NULL`                           | Array of dietary tips for this plan      |
| `is_generated`      | `TINYINT(1)`         | `NOT NULL`, `DEFAULT 1`          | 1 = AI/rule-generated, 0 = user-customized |
| `created_at`        | `TIMESTAMP`          | `NOT NULL`, `DEFAULT CURRENT_TIMESTAMP` | Record creation timestamp         |
| `updated_at`        | `TIMESTAMP`          | `NOT NULL`, `DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` | Last update timestamp |

**Indexes:**
- `PRIMARY KEY` on `plan_id`
- `UNIQUE INDEX` on `(user_id, plan_date)` — one plan per user per day
- `INDEX` on `user_id`
- `INDEX` on `plan_date`

**Foreign Key:**
- `user_id` → `users(user_id)` with `ON DELETE CASCADE`

**Sample Row:**
```
plan_id: 1
user_id: 1
plan_date: 2026-07-27
total_calories: 1850
total_protein_g: 85
total_carbs_g: 220
total_fat_g: 55
dietary_tips_json: '["Drink water 30 mins before meals", "Include leafy greens in lunch"]'
is_generated: 1
created_at: 2026-07-26 08:00:00
updated_at: 2026-07-26 08:00:00
```

---

### 4.8 `meal_plan_items`

Individual meals within a meal plan. Each plan has 4–5 items (breakfast, lunch, dinner, 1–2 snacks).

| **Column**        | **Data Type**        | **Constraints**                  | **Description**                          |
| ----------------- | -------------------- | -------------------------------- | ---------------------------------------- |
| `item_id`         | `BIGINT UNSIGNED`    | `PRIMARY KEY`, `AUTO_INCREMENT`  | Unique item identifier                   |
| `plan_id`         | `BIGINT UNSIGNED`    | `NOT NULL`, `FOREIGN KEY REFERENCES meal_plans(plan_id)` | Parent meal plan |
| `meal_type`       | `ENUM('BREAKFAST','LUNCH','DINNER','SNACK_MORNING','SNACK_EVENING')` | `NOT NULL` | Which meal slot this fills |
| `food_id`         | `BIGINT UNSIGNED`    | `NOT NULL`, `FOREIGN KEY REFERENCES food_items(food_id)` | Selected food item |
| `serving_size_g`  | `SMALLINT UNSIGNED`  | `NOT NULL`, `CHECK (serving_size_g > 0 AND serving_size_g <= 2000)` | Serving size in grams |
| `calories`        | `SMALLINT UNSIGNED`  | `NOT NULL`                       | Calories for this serving                |
| `protein_g`       | `DECIMAL(6,1)`       | `NOT NULL`                       | Protein for this serving in grams        |
| `carbs_g`         | `DECIMAL(6,1)`       | `NOT NULL`                       | Carbs for this serving in grams          |
| `fat_g`           | `DECIMAL(6,1)`       | `NOT NULL`                       | Fat for this serving in grams            |
| `serving_unit`    | `VARCHAR(50)`        | `NULL`                           | Human-readable serving (e.g., "1 bowl", "2 pieces") |
| `created_at`      | `TIMESTAMP`          | `NOT NULL`, `DEFAULT CURRENT_TIMESTAMP` | Record creation timestamp         |

**Indexes:**
- `PRIMARY KEY` on `item_id`
- `INDEX` on `plan_id`
- `INDEX` on `food_id`
- `INDEX` on `meal_type`

**Foreign Keys:**
- `plan_id` → `meal_plans(plan_id)` with `ON DELETE CASCADE`
- `food_id` → `food_items(food_id)` with `ON DELETE RESTRICT`

**Sample Row:**
```
item_id: 1
plan_id: 1
meal_type: "BREAKFAST"
food_id: 42
serving_size_g: 200
calories: 350
protein_g: 12.5
carbs_g: 45.0
fat_g: 14.0
serving_unit: "1 bowl"
created_at: 2026-07-26 08:00:00
```

---

### 4.9 `food_items`

Reference catalog of foods with standardized nutritional values per 100g.

| **Column**        | **Data Type**        | **Constraints**                  | **Description**                          |
| ----------------- | -------------------- | -------------------------------- | ---------------------------------------- |
| `food_id`         | `BIGINT UNSIGNED`    | `PRIMARY KEY`, `AUTO_INCREMENT`  | Unique food identifier                   |
| `food_name`       | `VARCHAR(200)`       | `NOT NULL`                       | Name of the food item                    |
| `category`        | `VARCHAR(100)`       | `NOT NULL`                       | Category (e.g., "Grains", "Vegetables", "Fruits", "Dairy", "Meat") |
| `calories_100g`   | `DECIMAL(7,1)`       | `NOT NULL`, `CHECK (calories_100g >= 0)` | Calories per 100g |
| `protein_100g`    | `DECIMAL(6,1)`       | `NOT NULL`, `CHECK (protein_100g >= 0)` | Protein in grams per 100g |
| `carbs_100g`      | `DECIMAL(6,1)`       | `NOT NULL`, `CHECK (carbs_100g >= 0)` | Carbs in grams per 100g |
| `fat_100g`        | `DECIMAL(6,1)`       | `NOT NULL`, `CHECK (fat_100g >= 0)` | Fat in grams per 100g |
| `fiber_100g`      | `DECIMAL(5,1)`       | `NULL`, `CHECK (fiber_100g >= 0)` | Fiber in grams per 100g |
| `sugar_100g`      | `DECIMAL(5,1)`       | `NULL`, `CHECK (sugar_100g >= 0)` | Sugar in grams per 100g |
| `diet_type`       | `SET('VEGETARIAN','NON_VEGETARIAN','VEGAN')` | `NOT NULL` | Which diet types this food is compatible with |
| `budget_level`    | `ENUM('LOW','MEDIUM','HIGH')` | `NOT NULL`              | Cost category of this food               |
| `region`          | `VARCHAR(100)`       | `NULL`                           | Region where this food is commonly available |
| `is_active`       | `TINYINT(1)`         | `NOT NULL`, `DEFAULT 1`          | Soft delete flag                         |
| `created_at`      | `TIMESTAMP`          | `NOT NULL`, `DEFAULT CURRENT_TIMESTAMP` | Record creation timestamp         |
| `updated_at`      | `TIMESTAMP`          | `NOT NULL`, `DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` | Last update timestamp |

**Indexes:**
- `PRIMARY KEY` on `food_id`
- `INDEX` on `category`
- `INDEX` on `diet_type`
- `INDEX` on `budget_level`
- `FULLTEXT INDEX` on `food_name` (for search)

**Sample Row:**
```
food_id: 42
food_name: "Oats (Rolled)"
category: "Grains"
calories_100g: 389.0
protein_100g: 16.9
carbs_100g: 66.3
fat_100g: 6.9
fiber_100g: 10.6
sugar_100g: 1.0
diet_type: "VEGETARIAN,VEGAN"
budget_level: "LOW"
region: "All"
is_active: 1
created_at: 2026-07-01 10:00:00
updated_at: 2026-07-01 10:00:00
```

---

### 4.10 `food_diary_entries`

User-logged food consumption records for manual tracking.

| **Column**        | **Data Type**        | **Constraints**                  | **Description**                          |
| ----------------- | -------------------- | -------------------------------- | ---------------------------------------- |
| `entry_id`        | `BIGINT UNSIGNED`    | `PRIMARY KEY`, `AUTO_INCREMENT`  | Unique entry identifier                  |
| `user_id`         | `BIGINT UNSIGNED`    | `NOT NULL`, `FOREIGN KEY REFERENCES users(user_id)` | User who logged this |
| `food_id`         | `BIGINT UNSIGNED`    | `NULL`, `FOREIGN KEY REFERENCES food_items(food_id)` | Associated food (nullable for custom entries) |
| `meal_type`       | `ENUM('BREAKFAST','LUNCH','DINNER','SNACK_MORNING','SNACK_EVENING')` | `NOT NULL` | Which meal this entry belongs to |
| `food_name`       | `VARCHAR(200)`       | `NOT NULL`                       | Name of the food (pre-filled from catalog or custom) |
| `serving_size_g`  | `SMALLINT UNSIGNED`  | `NOT NULL`, `CHECK (serving_size_g > 0 AND serving_size_g <= 5000)` | Portion size in grams |
| `calories`        | `SMALLINT UNSIGNED`  | `NOT NULL`                       | Calories consumed                       |
| `protein_g`       | `DECIMAL(6,1)`       | `NOT NULL`, `CHECK (protein_g >= 0)` | Protein consumed in grams               |
| `carbs_g`         | `DECIMAL(6,1)`       | `NOT NULL`, `CHECK (carbs_g >= 0)` | Carbs consumed in grams                  |
| `fat_g`           | `DECIMAL(6,1)`       | `NOT NULL`, `CHECK (fat_g >= 0)` | Fat consumed in grams                    |
| `logged_date`     | `DATE`               | `NOT NULL`                       | Date the food was consumed               |
| `logged_time`     | `TIME`               | `NULL`                           | Time the food was consumed               |
| `notes`           | `TEXT`               | `NULL`                           | User notes about this entry              |
| `created_at`      | `TIMESTAMP`          | `NOT NULL`, `DEFAULT CURRENT_TIMESTAMP` | Record creation timestamp         |

**Indexes:**
- `PRIMARY KEY` on `entry_id`
- `INDEX` on `user_id`
- `INDEX` on `food_id`
- `INDEX` on `logged_date`
- `INDEX` on `(user_id, logged_date)` — for daily diary queries
- `INDEX` on `(user_id, meal_type, logged_date)` — for per-meal analysis

**Foreign Keys:**
- `user_id` → `users(user_id)` with `ON DELETE CASCADE`
- `food_id` → `food_items(food_id)` with `ON DELETE SET NULL`

**Sample Row:**
```
entry_id: 1
user_id: 1
food_id: 42
meal_type: "BREAKFAST"
food_name: "Oats (Rolled)"
serving_size_g: 200
calories: 350
protein_g: 12.5
carbs_g: 45.0
fat_g: 14.0
logged_date: 2026-07-27
logged_time: 08:30:00
notes: "Cooked with water and topped with almonds"
created_at: 2026-07-27 08:35:00
```

---

### 4.11 `progress_tracking`

Daily snapshots of user metrics for dashboard visualization.

| **Column**           | **Data Type**        | **Constraints**                  | **Description**                          |
| -------------------- | -------------------- | -------------------------------- | ---------------------------------------- |
| `tracking_id`        | `BIGINT UNSIGNED`    | `PRIMARY KEY`, `AUTO_INCREMENT`  | Unique tracking record ID                |
| `user_id`            | `BIGINT UNSIGNED`    | `NOT NULL`, `FOREIGN KEY REFERENCES users(user_id)` | Associated user |
| `recorded_date`      | `DATE`               | `NOT NULL`                       | Date of this tracking record             |
| `weight_kg`          | `DECIMAL(5,1)`       | `NULL`, `CHECK (weight_kg > 10 AND weight_kg < 500)` | Weight on this date |
| `bmi`                | `DECIMAL(4,1)`       | `NULL`, `CHECK (bmi >= 10 AND bmi <= 60)` | Calculated BMI on this date |
| `calories_consumed`  | `SMALLINT UNSIGNED`  | `NULL`                           | Total calories consumed (from food diary) |
| `protein_consumed`   | `DECIMAL(6,1)`       | `NULL`                           | Total protein consumed (from food diary)  |
| `carbs_consumed`     | `DECIMAL(6,1)`       | `NULL`                           | Total carbs consumed (from food diary)    |
| `fat_consumed`       | `DECIMAL(6,1)`       | `NULL`                           | Total fat consumed (from food diary)      |
| `water_intake_ml`    | `SMALLINT UNSIGNED`  | `NULL`                           | Total water intake on this date           |
| `sleep_hours`        | `DECIMAL(3,1)`       | `NULL`, `CHECK (sleep_hours >= 0 AND sleep_hours <= 24)` | Sleep hours on this date |
| `notes`              | `TEXT`               | `NULL`                           | User notes for this day                  |
| `created_at`         | `TIMESTAMP`          | `NOT NULL`, `DEFAULT CURRENT_TIMESTAMP` | Record creation timestamp         |

**Indexes:**
- `PRIMARY KEY` on `tracking_id`
- `UNIQUE INDEX` on `(user_id, recorded_date)` — one record per user per day
- `INDEX` on `user_id`
- `INDEX` on `recorded_date`
- `INDEX` on `(user_id, recorded_date DESC)` — for dashboard trend queries

**Foreign Key:**
- `user_id` → `users(user_id)` with `ON DELETE CASCADE`

**Sample Row:**
```
tracking_id: 1
user_id: 1
recorded_date: 2026-07-27
weight_kg: 78.0
bmi: 25.5
calories_consumed: 1850
protein_consumed: 85.0
carbs_consumed: 220.0
fat_consumed: 55.0
water_intake_ml: 2500
sleep_hours: 7.5
notes: "Felt energetic today"
created_at: 2026-07-27 22:00:00
```

---

## 5. Relationship Matrix

### 5.1 Entity Relationship Summary

| **Entity**           | **Related Entity**    | **Relationship** | **Cardinality** | **Foreign Key**     | **Cascade Behavior**      |
| -------------------- | --------------------- | ---------------- | --------------- | ------------------- | ------------------------- |
| User                 | UserProfile           | Has              | 1 to 1          | UserProfile.user_id | CASCADE                   |
| User                 | HealthVitals          | Has              | 1 to 1          | HealthVitals.user_id | CASCADE                  |
| User                 | UserCondition         | Has              | 1 to N          | UserCondition.user_id | CASCADE                 |
| User                 | UserGoal              | Has              | 1 to 1          | UserGoal.user_id    | CASCADE                   |
| User                 | FoodPreference        | Has              | 1 to 1          | FoodPreference.user_id | CASCADE                 |
| User                 | MealPlan              | Owns             | 1 to N          | MealPlan.user_id    | CASCADE                   |
| User                 | FoodDiaryEntry        | Logs             | 1 to N          | FoodDiaryEntry.user_id | CASCADE                |
| User                 | ProgressTracking      | Tracks           | 1 to N          | ProgressTracking.user_id | CASCADE              |
| MealPlan             | MealPlanItem          | Contains         | 1 to N          | MealPlanItem.plan_id | CASCADE                  |
| MealPlanItem         | FoodItem              | References       | N to 1          | MealPlanItem.food_id | RESTRICT                 |
| FoodDiaryEntry       | FoodItem              | References       | N to 1          | FoodDiaryEntry.food_id | SET NULL               |

### 5.2 Cardinality Diagram (Text)

```
User ──┬── 1:1 ── UserProfile
       ├── 1:1 ── HealthVitals
       ├── 1:N ── UserCondition
       ├── 1:1 ── UserGoal
       ├── 1:1 ── FoodPreference
       ├── 1:N ── MealPlan ── 1:N ── MealPlanItem ── N:1 ── FoodItem
       ├── 1:N ── FoodDiaryEntry ── N:1 ── FoodItem
       └── 1:N ── ProgressTracking
```

---

## 6. Normalization

### 6.1 Normalization Status

| **Normal Form** | **Status** | **How We Achieve It**                                    |
| --------------- | ---------- | -------------------------------------------------------- |
| **1NF**         | ✅ Achieved | All columns are atomic. No repeating groups. Each row has a unique primary key. |
| **2NF**         | ✅ Achieved | All non-key columns are fully functionally dependent on the entire primary key. No partial dependencies. |
| **3NF**         | ✅ Achieved | No transitive dependencies. Non-key columns depend only on the primary key. |

### 6.2 Violation Checklist

| **Violation Type**                 | **Status** | **Explanation**                                          |
| ---------------------------------- | ---------- | -------------------------------------------------------- |
| Multi-valued attributes            | ✅ None    | `allergies_json` is JSON (single column with structured data, not repeating groups). |
| Partial dependency (2NF violation) | ✅ None    | Every non-key column depends on the full composite key (or the table has a single-column PK). |
| Transitive dependency (3NF violation)| ✅ None   | Calculated fields like `bmi` in `user_profiles` are stored as convenience (derived, not a dependency issue). |

### 6.3 Design Rationale for Stored Calculated Fields

| **Field**       | **Table**           | **Why Store When Calculable**                              |
| --------------- | ------------------- | ---------------------------------------------------------- |
| `bmi`           | `user_profiles`     | Avoids repeated calculation on every dashboard query. Recalculated on weight/height change. |
| `target_calories`| `user_goals`       | Stored after calculation by the recommendation engine so it can be manually adjusted by the user. |
| `calories`      | `meal_plan_items`   | Pre-calculated from serving size and food item's per-100g values. Avoids runtime calculation. |

---

## 7. Indexes

### 7.1 Complete Index List

| **Table**            | **Index Name**                          | **Column(s)**                | **Type**      | **Purpose**                                   |
| -------------------- | --------------------------------------- | ---------------------------- | ------------- | --------------------------------------------- |
| `users`              | `PRIMARY`                               | `user_id`                    | BTREE         | Primary key lookup                            |
| `users`              | `idx_users_email`                       | `email`                      | BTREE (UNIQUE)| Fast login by email                           |
| `users`              | `idx_users_role`                        | `role`                       | BTREE         | Admin role filtering                          |
| `user_profiles`      | `PRIMARY`                               | `user_id`                    | BTREE         | Primary key lookup                            |
| `user_profiles`      | `idx_profiles_height_weight`            | `height_cm`, `weight_kg`     | BTREE         | Aggregation queries for reporting             |
| `health_vitals`      | `PRIMARY`                               | `user_id`                    | BTREE         | Primary key lookup                            |
| `user_conditions`    | `PRIMARY`                               | `condition_id`               | BTREE         | Primary key lookup                            |
| `user_conditions`    | `idx_conditions_user`                   | `user_id`                    | BTREE         | Find all conditions for a user                |
| `user_conditions`    | `idx_conditions_user_condition`         | `user_id`, `condition`       | BTREE (UNIQUE)| Prevent duplicate condition entries           |
| `user_conditions`    | `idx_conditions_condition`              | `condition`                  | BTREE         | Aggregate stats by condition                  |
| `user_goals`         | `PRIMARY`                               | `user_id`                    | BTREE         | Primary key lookup                            |
| `user_goals`         | `idx_goals_primary`                     | `primary_goal`               | BTREE         | Analytics on goal distribution                |
| `food_preferences`   | `PRIMARY`                               | `user_id`                    | BTREE         | Primary key lookup                            |
| `food_preferences`   | `idx_prefs_diet_type`                   | `diet_type`                  | BTREE         | Filter users by diet type for admin           |
| `meal_plans`         | `PRIMARY`                               | `plan_id`                    | BTREE         | Primary key lookup                            |
| `meal_plans`         | `idx_mealplans_user_date`               | `user_id`, `plan_date`       | BTREE (UNIQUE)| Ensure one plan per user per day              |
| `meal_plans`         | `idx_mealplans_user`                    | `user_id`                    | BTREE         | Find all plans for a user                     |
| `meal_plans`         | `idx_mealplans_date`                    | `plan_date`                  | BTREE         | Admin queries by date                         |
| `meal_plan_items`    | `PRIMARY`                               | `item_id`                    | BTREE         | Primary key lookup                            |
| `meal_plan_items`    | `idx_mealitems_plan`                    | `plan_id`                    | BTREE         | Find all items in a plan                      |
| `meal_plan_items`    | `idx_mealitems_food`                    | `food_id`                    | BTREE         | Find which plans include a specific food       |
| `meal_plan_items`    | `idx_mealitems_meal_type`               | `meal_type`                  | BTREE         | Filter by meal type                           |
| `food_items`         | `PRIMARY`                               | `food_id`                    | BTREE         | Primary key lookup                            |
| `food_items`         | `idx_food_category`                     | `category`                   | BTREE         | Filter foods by category                      |
| `food_items`         | `idx_food_diet_type`                    | `diet_type`                  | BTREE         | Filter by diet compatibility                  |
| `food_items`         | `idx_food_budget`                       | `budget_level`               | BTREE         | Filter by budget level                        |
| `food_items`         | `idx_food_name_fulltext`                | `food_name`                  | FULLTEXT      | Text search for food names                    |
| `food_diary_entries` | `PRIMARY`                               | `entry_id`                   | BTREE         | Primary key lookup                            |
| `food_diary_entries` | `idx_diary_user`                        | `user_id`                    | BTREE         | Find all diary entries for a user             |
| `food_diary_entries` | `idx_diary_food`                        | `food_id`                    | BTREE         | Find entries by food                          |
| `food_diary_entries` | `idx_diary_date`                        | `logged_date`                | BTREE         | Filter by date                                |
| `food_diary_entries` | `idx_diary_user_date`                   | `user_id`, `logged_date`     | BTREE         | Daily diary query (most frequent pattern)     |
| `food_diary_entries` | `idx_diary_user_meal_date`              | `user_id`, `meal_type`, `logged_date` | BTREE    | Per-meal nutritional analysis                 |
| `progress_tracking`  | `PRIMARY`                               | `tracking_id`                | BTREE         | Primary key lookup                            |
| `progress_tracking`  | `idx_progress_user_date`                | `user_id`, `recorded_date`   | BTREE (UNIQUE)| One tracking record per user per day          |
| `progress_tracking`  | `idx_progress_user`                     | `user_id`                    | BTREE         | Find all records for a user                   |
| `progress_tracking`  | `idx_progress_date`                     | `recorded_date`              | BTREE         | Admin queries by date                         |
| `progress_tracking`  | `idx_progress_user_date_desc`           | `user_id`, `recorded_date DESC` | BTREE       | Dashboard trend queries (most recent first)   |

### 7.2 Index Strategy Summary

| **Strategy**                  | **Application**                                              |
| ----------------------------- | ------------------------------------------------------------ |
| **All Foreign Keys Indexed**  | Every `FOREIGN KEY` column has a corresponding `INDEX` to avoid full table scans on joins. |
| **Composite Indexes**         | Most frequent query patterns have composite indexes (e.g., `(user_id, logged_date)` for diary queries). |
| **Unique Constraints**        | Logical uniqueness enforced via `UNIQUE indexes` (e.g., one meal plan per user per day). |
| **Covering Indexes**          | Dashboard queries use indexes that cover selected columns without reading table rows. |
| **Full-Text Search**          | `food_items.food_name` uses `FULLTEXT INDEX` for fast food name search in the catalog. |
| **Descending Index**          | `progress_tracking` has a descending index for "most recent records first" dashboard queries. |

---

## 8. Constraints

### 8.1 Constraint Summary

| **Table**            | **Constraint Type** | **Constraint**                                              | **Purpose**                                     |
| -------------------- | ------------------- | ----------------------------------------------------------- | ----------------------------------------------- |
| `users`              | UNIQUE              | `email` must be unique                                      | Prevent duplicate registrations                 |
| `users`              | CHECK               | `role IN ('USER','ADMIN')`                                  | Valid role values                               |
| `user_profiles`      | CHECK               | `height_cm BETWEEN 50 AND 300`                              | Physically valid height range                   |
| `user_profiles`      | CHECK               | `weight_kg BETWEEN 10 AND 500`                              | Physically valid weight range                   |
| `health_vitals`      | CHECK               | `blood_pressure_sys BETWEEN 60 AND 250`                     | Clinically valid systolic BP                    |
| `health_vitals`      | CHECK               | `blood_pressure_dia BETWEEN 30 AND 150`                     | Clinically valid diastolic BP                   |
| `health_vitals`      | CHECK               | `fasting_sugar BETWEEN 20 AND 500`                          | Clinically valid glucose range                  |
| `health_vitals`      | CHECK               | `hba1c BETWEEN 2.0 AND 20.0`                                | Clinically valid HbA1c range                    |
| `health_vitals`      | CHECK               | `cholesterol_ldl BETWEEN 10 AND 500`                        | Clinically valid LDL range                      |
| `user_conditions`    | UNIQUE              | `(user_id, condition)` must be unique                       | Prevent duplicate condition entries             |
| `user_goals`         | CHECK               | `target_calories BETWEEN 800 AND 5000`                      | Valid calorie target range                      |
| `user_goals`         | CHECK               | `target_protein_g BETWEEN 10 AND 400`                       | Valid protein target range                      |
| `user_goals`         | CHECK               | `sleep_hours BETWEEN 2 AND 16`                              | Valid sleep duration                            |
| `user_goals`         | CHECK               | `water_intake_ml BETWEEN 200 AND 10000`                     | Valid water intake range                        |
| `meal_plans`         | UNIQUE              | `(user_id, plan_date)` must be unique                       | One plan per user per day                        |
| `meal_plans`         | CHECK               | `total_calories >= 0`                                       | Non-negative calorie count                      |
| `meal_plan_items`    | CHECK               | `serving_size_g BETWEEN 1 AND 2000`                         | Reasonable serving size                         |
| `food_diary_entries` | CHECK               | `serving_size_g BETWEEN 1 AND 5000`                         | Reasonable serving size                         |
| `food_diary_entries` | CHECK               | `calories >= 0`                                             | Non-negative calorie count                      |
| `food_items`         | CHECK               | `calories_100g >= 0`                                        | Non-negative nutritional values                 |
| `progress_tracking`  | UNIQUE              | `(user_id, recorded_date)` must be unique                   | One tracking record per user per day             |
| `progress_tracking`  | CHECK               | `bmi BETWEEN 10 AND 60`                                     | Valid BMI range                                 |
| `progress_tracking`  | CHECK               | `weight_kg BETWEEN 10 AND 500`                              | Valid weight range                              |
| `progress_tracking`  | CHECK               | `sleep_hours BETWEEN 0 AND 24`                              | Valid sleep duration                            |

### 8.2 Default Value Summary

| **Table**            | **Column**      | **Default**      | **Rationale**                                    |
| -------------------- | --------------- | ---------------- | ------------------------------------------------ |
| `users`              | `role`          | `'USER'`         | Most users are regular users; admin is assigned manually |
| `users`              | `is_active`     | `1`              | New accounts start active                        |
| `meal_plans`         | `is_generated`  | `1`              | Plans originate from the recommendation engine    |
| `food_items`         | `is_active`     | `1`              | Foods are active by default in the catalog        |
| `progress_tracking`  | `calories_consumed`| `NULL`         | Daily summary may not be calculated immediately   |

---

## 9. Data Types Summary

### 9.1 Data Type Usage

| **MySQL Data Type**       | **Used In**                                      | **Rationale**                                     |
| ------------------------- | ------------------------------------------------ | ------------------------------------------------- |
| `BIGINT UNSIGNED`         | All primary keys, foreign keys                   | Supports up to 18 quintillion rows. Unsigned for auto-increment IDs. |
| `VARCHAR(255)`            | Email, password hash, food name, role            | Variable length. 255 is standard for indexed string columns. |
| `VARCHAR(100)`            | Names, categories, region                        | Appropriate length for names and short text.      |
| `VARCHAR(50)`             | Serving unit, phone                              | Short strings.                                    |
| `VARCHAR(20)`             | Phone numbers                                    | Variable length for international formats.        |
| `DECIMAL(5,1)`            | Height, weight, protein, carbs, fat, sugar       | Precise decimal for medical/nutrition values. One decimal place is sufficient. |
| `DECIMAL(7,1)`            | Calories per 100g                                | Higher range for caloric density (up to 99999.9). |
| `DECIMAL(4,1)`            | BMI, HbA1c                                       | Precise BMI (10.0–60.0) and HbA1c (2.0–20.0).    |
| `DECIMAL(3,1)`            | Sleep hours                                      | 0.0–24.0 range.                                   |
| `DECIMAL(6,1)`            | Macronutrient amounts in diary entries           | Supports up to 9999.9g for daily totals.          |
| `SMALLINT UNSIGNED`       | Calories, target macros, BP, water intake        | 0–65535 range sufficient for calorie/macro values. |
| `TINYINT(1)`              | Boolean flags (is_active, is_generated)          | MySQL's boolean equivalent.                       |
| `DATE`                    | Plan dates, logged dates, recorded dates, DOB    | Date-only (no time needed).                       |
| `TIME`                    | Logged time (food diary)                         | Time of day for meal logging.                     |
| `TIMESTAMP`               | created_at, updated_at, last_login_at            | Automatic timestamping with timezone support.     |
| `ENUM`                    | Roles, genders, goal types, meal types, conditions| Fixed set of valid string values. More efficient than VARCHAR + CHECK. |
| `SET`                     | Diet type compatibility (food_items)             | Multiple values from a fixed set.                 |
| `JSON`                    | Allergies, dietary tips                          | Flexible structured data that doesn't need separate tables. |
| `TEXT`                    | Notes, excluded foods                            | Longer free-text (up to 65KB).                    |

### 9.2 Data Type Selection Rules

| **Rule**                                    | **Application**                                              |
| ------------------------------------------- | ------------------------------------------------------------ |
| Use `BIGINT UNSIGNED` for all primary keys  | Avoids overflow at scale. Auto-increment handles sequential IDs. |
| Use `DECIMAL` for exact numeric values      | Weight, height, nutritional values must not lose precision. Never use `FLOAT`/`DOUBLE` for health data. |
| Use `SMALLINT` for calorie ranges           | Calories fit within 0–65535. `INT` would waste space.        |
| Use `ENUM` for fixed, small sets            | Role (USER/ADMIN), meal types (5 values), conditions. Faster than lookup tables for sets that rarely change. |
| Use `SET` for multiple-choice fixed sets    | Diet type compatibility in food_items. Multiple selections from a known set. |
| Use `JSON` for sparse or flexible data      | Allergies, dietary tips. Avoids creating 10+ nullable columns. |
| Use `VARCHAR(255)` for indexed strings      | MySQL indexes have a length limit. 255 is the max for full index prefix. |

---

## 10. Enum Reference Values

### 10.1 `role` (users)

| **Value**  | **Description**                     |
| ---------- | ----------------------------------- |
| `USER`     | Regular end user                    |
| `ADMIN`    | Administrator with access to admin panel |

### 10.2 `gender` (user_profiles)

| **Value**  | **Description**                     |
| ---------- | ----------------------------------- |
| `MALE`     | Male                                |
| `FEMALE`   | Female                              |
| `OTHER`    | Non-binary or other                 |

### 10.3 `condition` (user_conditions)

| **Value**           | **Description**                     |
| ------------------- | ----------------------------------- |
| `DIABETES`          | Type 1 or Type 2 diabetes           |
| `HYPERTENSION`      | High blood pressure                 |
| `PCOD`              | Polycystic Ovarian Disease/PCOS     |
| `THYROID`           | Thyroid disorders                   |
| `HIGH_CHOLESTEROL`  | Hypercholesterolemia                |
| `FATTY_LIVER`       | Non-alcoholic fatty liver disease   |
| `KIDNEY_DISEASE`    | Chronic kidney disease              |
| `ANEMIA`            | Iron deficiency or other anemias    |
| `NONE`              | No known medical conditions         |

### 10.4 `severity` (user_conditions)

| **Value**   | **Description**                     |
| ----------- | ----------------------------------- |
| `MILD`      | Mild symptoms or well-controlled    |
| `MODERATE`  | Moderate symptoms                   |
| `SEVERE`    | Severe symptoms or poorly controlled|

### 10.5 `primary_goal` (user_goals)

| **Value**          | **Description**                     |
| ------------------ | ----------------------------------- |
| `WEIGHT_LOSS`      | Reduce body weight                  |
| `WEIGHT_GAIN`      | Increase body weight                |
| `MUSCLE_GAIN`      | Build muscle mass                   |
| `MAINTENANCE`       | Maintain current weight             |
| `HEALTHY_LIFESTYLE`| General health improvement          |

### 10.6 `activity_level` (user_goals)

| **Value**      | **Description**        | **TDEE Multiplier** |
| -------------- | ---------------------- | ------------------- |
| `SEDENTARY`    | Little/no exercise     | 1.200               |
| `LIGHT`        | Light exercise 1-3x/wk | 1.375               |
| `MODERATE`     | Moderate exercise 3-5x/wk | 1.550            |
| `ACTIVE`       | Hard exercise 6-7x/wk  | 1.725               |
| `VERY_ACTIVE`  | Very hard exercise/daily| 1.900               |

### 10.7 `diet_type` (food_preferences)

| **Value**          | **Description**                     |
| ------------------ | ----------------------------------- |
| `VEGETARIAN`       | No meat, but may include dairy/eggs |
| `NON_VEGETARIAN`   | Includes all food groups            |
| `VEGAN`            | No animal products                  |
| `EGGETARIAN`       | Vegetarian + eggs, no dairy         |
| `FLEXITARIAN`      | Primarily vegetarian, occasional meat|

### 10.8 `budget_level` (food_preferences, food_items)

| **Value**  | **Description**                     |
| ---------- | ----------------------------------- |
| `LOW`      | Budget-friendly options             |
| `MEDIUM`   | Mid-range options                   |
| `HIGH`     | Premium/prepared options            |

### 10.9 `meal_type` (meal_plan_items, food_diary_entries)

| **Value**        | **Description**                     |
| ---------------- | ----------------------------------- |
| `BREAKFAST`      | Morning meal                        |
| `LUNCH`          | Mid-day meal                        |
| `DINNER`         | Evening meal                        |
| `SNACK_MORNING`  | Mid-morning snack                   |
| `SNACK_EVENING`  | Mid-afternoon or evening snack      |

### 10.10 `diet_type` SET (food_items) — Compatibility

| **Value Set**                  | **Compatible With**                                  |
| ------------------------------ | ---------------------------------------------------- |
| `VEGETARIAN,VEGAN`             | Fruits, vegetables, grains, legumes, nuts            |
| `VEGETARIAN`                   | Dairy products, eggs                                 |
| `NON_VEGETARIAN,VEGETARIAN`    | Foods that could include meat but have veg versions  |
| `NON_VEGETARIAN`               | Meat, poultry, fish, seafood                         |
| `VEGETARIAN,NON_VEGETARIAN,VEGAN` | Universal foods (water, basic grains, most fruits) |

---

## 11. Data Integrity Rules

### 11.1 Referential Integrity

| **Rule**                                      | **Enforcement**                                         |
| --------------------------------------------- | ------------------------------------------------------- |
| A user cannot have a profile without existing | `FOREIGN KEY` with `ON DELETE CASCADE`                 |
| Deleting a user deletes all related data      | `ON DELETE CASCADE` on all child tables                 |
| A meal plan item must reference a valid plan  | `FOREIGN KEY` with `ON DELETE CASCADE`                 |
| A meal plan item must reference a valid food  | `FOREIGN KEY` with `ON DELETE RESTRICT`                |
| A food diary entry can reference a deleted food | `FOREIGN KEY` with `ON DELETE SET NULL`                |

### 11.2 Domain Integrity

| **Rule**                                      | **Enforcement**                                         |
| --------------------------------------------- | ------------------------------------------------------- |
| Email must be unique                          | `UNIQUE INDEX` on `users.email`                         |
| One meal plan per user per day                | `UNIQUE INDEX` on `meal_plans(user_id, plan_date)`      |
| One tracking record per user per day          | `UNIQUE INDEX` on `progress_tracking(user_id, recorded_date)` |
| No duplicate condition entries for same user  | `UNIQUE INDEX` on `user_conditions(user_id, condition)`  |
| All health values within clinically valid ranges | `CHECK` constraints on health vitals                |

### 11.3 Entity Integrity

| **Rule**                                      | **Enforcement**                                         |
| --------------------------------------------- | ------------------------------------------------------- |
| Every row must have a unique identifier       | `PRIMARY KEY` on every table                             |
| Every row must have a creation timestamp      | `created_at` with `DEFAULT CURRENT_TIMESTAMP`            |
| Every row must have an update timestamp       | `updated_at` with `ON UPDATE CURRENT_TIMESTAMP`          |

### 11.4 Business Rules Enforced at Database Level

| **Rule**                                                  | **Implementation**                                       |
| --------------------------------------------------------- | -------------------------------------------------------- |
| User must have email and password to register             | `NOT NULL` on `email` and `password_hash`                 |
| User must provide height and weight for BMI calculation   | `NOT NULL` on `height_cm` and `weight_kg`                 |
| Profile weight must be realistic (10–500 kg)              | `CHECK` constraint on `weight_kg`                         |
| Target daily calories must be within safe range (800–5000)| `CHECK` constraint on `target_calories`                   |
| Food item calories per 100g cannot be negative            | `CHECK` constraint on `calories_100g`                     |

---

## 12. Performance Considerations

### 12.1 Query Patterns and Index Coverage

| **Query Pattern**                                    | **Index Used**                                          | **Expected Performance**     |
| ---------------------------------------------------- | ------------------------------------------------------- | ---------------------------- |
| Login by email                                       | `idx_users_email` (UNIQUE BTREE)                        | Instant (unique lookup)      |
| Load user dashboard (progress + diary aggregates)    | `idx_progress_user_date_desc` (BTREE DESC)              | Sub-millisecond              |
| Get today's meal plan for user                       | `idx_mealplans_user_date` (UNIQUE BTREE)                | Instant (unique lookup)      |
| Get food diary for a date range                      | `idx_diary_user_date` (BTREE)                           | Sub-millisecond              |
| Search food catalog by name                          | `idx_food_name_fulltext` (FULLTEXT)                     | Fast (full-text)             |
| Filter food by category + budget                     | `idx_food_category` + `idx_food_budget` combined         | Fast (index merge)           |
| Get all conditions for a user                        | `idx_conditions_user` (BTREE)                           | Instant                      |
| Weekly weight trend for dashboard chart              | `idx_progress_user_date_desc` (BTREE DESC)              | Sub-millisecond (7 rows)     |
| Admin view: count users by role                      | `idx_users_role` (BTREE)                                | Fast (index-only scan)       |

### 12.2 Estimated Table Sizes (at 10,000 Users)

| **Table**            | **Rows**          | **Row Size (est.)** | **Total Size (est.)** |
| -------------------- | ----------------- | ------------------- | --------------------- |
| `users`              | 10,000            | ~120 bytes          | ~1.2 MB               |
| `user_profiles`      | 10,000            | ~150 bytes          | ~1.5 MB               |
| `health_vitals`      | 10,000            | ~80 bytes           | ~0.8 MB               |
| `user_conditions`    | 15,000            | ~100 bytes          | ~1.5 MB               |
| `user_goals`         | 10,000            | ~80 bytes           | ~0.8 MB               |
| `food_preferences`   | 10,000            | ~120 bytes          | ~1.2 MB               |
| `meal_plans`         | 3,650,000         | ~100 bytes          | ~365 MB               |
| `meal_plan_items`    | 14,600,000        | ~80 bytes           | ~1.17 GB              |
| `food_items`         | 500               | ~150 bytes          | ~75 KB                |
| `food_diary_entries` | 10,950,000        | ~120 bytes          | ~1.31 GB              |
| `progress_tracking`  | 3,650,000         | ~100 bytes          | ~365 MB               |
| **Total**            |                   |                     | **~3.2 GB**           |

*Note: 10,000 users × 365 days = 3.65M meal plans and progress records. 10,000 users × 3 diary entries/day × 365 = 10.95M diary entries. These estimates assume daily active usage.*

### 12.3 Connection Management

| **Setting**               | **Value** | **Rationale**                                    |
| ------------------------- | --------- | ------------------------------------------------ |
| Connection pool size      | 10        | Sufficient for 10,000 users with typical request volume |
| Connection timeout        | 30 seconds| Standard timeout for database connections         |
| Maximum active connections| 10        | Matches HikariCP default for Spring Boot          |

### 12.4 Query Optimization Recommendations

| **Area**                | **Recommendation**                                          |
| ----------------------- | ----------------------------------------------------------- |
| **Dashboard queries**   | Use `LIMIT` with descending date order to fetch only most recent N records for charts. |
| **Food diary history**  | Always filter by `user_id` + `logged_date` range (indexed). Never query diary without user_id. |
| **Meal plan retrieval** | Fetch plan + items in a single query using `JOIN` rather than separate queries. |
| **Food search**         | Use `MATCH ... AGAINST` with the FULLTEXT index instead of `LIKE '%term%'`. |
| **Admin reporting**     | Schedule heavy aggregation queries during off-peak hours.   |

---

## Document Version History

| **Version** | **Date**     | **Author** | **Changes**          |
| ----------- | ------------ | ---------- | -------------------- |
| 1.0         | 2026-07-27   | Architect  | Initial draft        |

---

*End of Document — 04_DATABASE.md*
