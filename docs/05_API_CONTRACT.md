# 05 — API CONTRACT

| **Field**             | **Value**                                    |
| --------------------- | -------------------------------------------- |
| **Project Name**      | NutriGuide AI                                |
| **Document Title**    | REST API Contract                            |
| **Version**           | 1.0                                          |
| **Author**            | Software Architecture Team                   |
| **Created On**        | 2026-07-27                                   |
| **Last Updated**      | 2026-07-27                                   |
| **Base URL**          | `http://localhost:8080/api/v1` (dev)         |
| **Content Type**      | `application/json`                           |
| **Status**            | Draft                                        |
| **References**        | `03_SYSTEM_ARCHITECTURE.md`, `04_DATABASE.md` |

---

## Table of Contents

1. [Introduction](#1-introduction)
2. [API Standards](#2-api-standards)
3. [Module 1: Authentication](#3-module-1-authentication)
4. [Module 2: User Profile](#4-module-2-user-profile)
5. [Module 3: Health Vitals](#5-module-3-health-vitals)
6. [Module 4: Goals & Lifestyle](#6-module-4-goals--lifestyle)
7. [Module 5: Food Preferences](#7-module-5-food-preferences)
8. [Module 6: Meal Plans & Recommendations](#8-module-6-meal-plans--recommendations)
9. [Module 7: Food Diary](#9-module-7-food-diary)
10. [Module 8: Progress Dashboard](#10-module-8-progress-dashboard)
11. [Module 9: Food Catalog](#11-module-9-food-catalog)
12. [Module 10: Admin](#12-module-10-admin)
13. [Common Error Codes](#13-common-error-codes)
14. [API Summary](#14-api-summary)

---

## 1. Introduction

### 1.1 Purpose

This document defines the complete REST API contract for NutriGuide AI. It is the authoritative reference for all frontend-backend communication. Every endpoint is documented with its HTTP method, path, authentication requirements, request/response schemas, validation rules, status codes, and sample payloads.

### 1.2 API Design Principles

| **Principle**             | **Application**                                              |
| ------------------------- | ------------------------------------------------------------ |
| **RESTful Resource Naming** | Nouns, not verbs. Plural for collections (`/users`, `/meal-plans`). |
| **Consistent Response Envelope** | All responses follow a standard JSON structure.          |
| **Stateless**             | Each request contains all information needed (JWT in header). |
| **Versioned**             | All endpoints prefixed with `/api/v1/`.                     |
| **Idempotent Where Possible** | GET, PUT, DELETE are idempotent. POST creates new resources. |

### 1.3 Standard Response Envelope

**Success Response:**

```json
{
  "success": true,
  "data": {
    "...":
  },
  "message": "Operation completed successfully",
  "timestamp": "2026-07-27T10:30:00Z",
  "path": "/api/v1/users/profile"
}
```

**Error Response:**
```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "RESOURCE_NOT_FOUND",
    "message": "User profile not found",
    "details": ["Profile with user ID 42 does not exist"],
    "timestamp": "2026-07-27T10:30:00Z",
    "path": "/api/v1/users/profile/42"
  }
}
```

### 1.4 Standard Headers

| **Header**            | **Required** | **Description**                        |
| --------------------- | ------------ | -------------------------------------- |
| `Content-Type`        | Yes          | `application/json`                    |
| `Authorization`       | For auth req | `Bearer <jwt-token>`                  |
| `Accept`              | No           | `application/json`                    |

---

## 2. API Standards

### 2.1 HTTP Status Code Usage

| **Status Code** | **Usage**                                      |
| --------------- | ---------------------------------------------- |
| `200 OK`        | Successful GET, PUT, PATCH                     |
| `201 Created`   | Successful POST (resource created)             |
| `204 No Content`| Successful DELETE                              |
| `400 Bad Request`| Validation failure, malformed request body    |
| `401 Unauthorized`| Missing or invalid JWT token                 |
| `403 Forbidden` | Valid JWT but insufficient role/permissions    |
| `404 Not Found` | Resource does not exist                        |
| `409 Conflict`  | Duplicate resource (e.g., email already exists)|
| `422 Unprocessable Entity` | Business rule violation              |
| `429 Too Many Requests` | Rate limit exceeded                    |
| `500 Internal Server Error` | Unexpected server error               |

### 2.2 Authorization Levels

| **Level**       | **Header Required**                    | **Description**                     |
| --------------- | -------------------------------------- | ----------------------------------- |
| `Public`        | None                                   | No authentication required          |
| `Authenticated` | `Authorization: Bearer <token>`        | Any logged-in user                  |
| `Admin`         | `Authorization: Bearer <admin-token>`  | User with ADMIN role only           |

### 2.3 Common Path Parameters

| **Parameter**  | **Type** | **Description**                    |
| -------------- | -------- | ---------------------------------- |
| `{userId}`     | Long     | Numeric user ID from JWT or path   |
| `{planId}`     | Long     | Meal plan ID                       |
| `{foodId}`     | Long     | Food catalog item ID               |
| `{entryId}`    | Long     | Food diary entry ID                |
| `{date}`       | String   | Date in `YYYY-MM-DD` format        |

---

## 3. Module 1: Authentication

Base Path: `/api/v1/auth`

### 3.1 Register User

Creates a new user account with email and password.

| **Field**       | **Detail**                                      |
| --------------- | ----------------------------------------------- |
| **Endpoint**    | `POST /api/v1/auth/register`                    |
| **Purpose**     | Register a new user account                     |
| **Auth**        | Public                                          |
| **Rate Limit**  | 5 requests per minute per IP                    |

**Request Body:**
```json
{
  "email": "john.doe@example.com",
  "password": "Secure@123",
  "firstName": "John",
  "lastName": "Doe"
}
```

**Validation Rules:**

| **Field**      | **Rule**                                                     |
| -------------- | ------------------------------------------------------------ |
| `email`        | Required. Valid email format. Max 255 characters. Unique.    |
| `password`     | Required. Min 8 chars. Must contain: 1 uppercase, 1 lowercase, 1 digit, 1 special character. |
| `firstName`    | Required. Min 1 char. Max 100 chars.                        |
| `lastName`     | Required. Min 1 char. Max 100 chars.                        |

**Success Response** — `201 Created`:
```json
{
  "success": true,
  "data": {
    "userId": 1,
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "role": "USER",
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huQGV4YW1wbGUuY29tIiwidXNlcklkIjoxLCJyb2xlIjoiVVNFUiIsImlhdCI6MTcyMjA3NjgwMCwiZXhwIjoxNzIyMTYzMjAwfQ.exampleSignature"
  },
  "message": "Registration successful",
  "timestamp": "2026-07-27T10:30:00Z",
  "path": "/api/v1/auth/register"
}
```

**Error Responses:**

| **Code** | **Scenario**                      | **Response Body**                                           |
| -------- | --------------------------------- | ----------------------------------------------------------- |
| `400`    | Missing required fields           | `{ "code": "VALIDATION_ERROR", "message": "Validation failed", "details": ["email is required"] }` |
| `400`    | Weak password                     | `{ "code": "VALIDATION_ERROR", "message": "Password must contain at least 1 uppercase letter, 1 lowercase letter, 1 digit, and 1 special character" }` |
| `409`    | Email already registered          | `{ "code": "DUPLICATE_EMAIL", "message": "An account with this email already exists" }` |

---

### 3.2 Login

Authenticates a user and returns a JWT token.

| **Field**       | **Detail**                                      |
| --------------- | ----------------------------------------------- |
| **Endpoint**    | `POST /api/v1/auth/login`                       |
| **Purpose**     | Authenticate user and issue JWT token           |
| **Auth**        | Public                                          |
| **Rate Limit**  | 10 requests per minute per IP                   |

**Request Body:**
```json
{
  "email": "john.doe@example.com",
  "password": "Secure@123"
}
```

**Validation Rules:**

| **Field**      | **Rule**                                                     |
| -------------- | ------------------------------------------------------------ |
| `email`        | Required. Valid email format.                                |
| `password`     | Required. Not empty.                                         |

**Success Response** — `200 OK`:
```json
{
  "success": true,
  "data": {
    "userId": 1,
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "role": "USER",
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huQGV4YW1wbGUuY29tIiwidXNlcklkIjoxLCJyb2xlIjoiVVNFUiIsImlhdCI6MTcyMjA3NjgwMCwiZXhwIjoxNzIyMTYzMjAwfQ.exampleSignature"
  },
  "message": "Login successful",
  "timestamp": "2026-07-27T10:30:00Z",
  "path": "/api/v1/auth/login"
}
```

**Error Responses:**

| **Code** | **Scenario**                      | **Response Body**                                           |
| -------- | --------------------------------- | ----------------------------------------------------------- |
| `401`    | Invalid email or password         | `{ "code": "INVALID_CREDENTIALS", "message": "Invalid email or password" }` |
| `401`    | Account deactivated               | `{ "code": "ACCOUNT_INACTIVE", "message": "Your account has been deactivated. Contact support." }` |

---

### 3.3 Validate Token

Checks if the current JWT token is still valid.

| **Field**       | **Detail**                                      |
| --------------- | ----------------------------------------------- |
| **Endpoint**    | `GET /api/v1/auth/validate`                     |
| **Purpose**     | Validate JWT token and return user info         |
| **Auth**        | Authenticated                                   |

**Headers:**
```
Authorization: Bearer <token>
```

**Success Response** — `200 OK`:
```json
{
  "success": true,
  "data": {
    "userId": 1,
    "email": "john.doe@example.com",
    "role": "USER",
    "expiresAt": "2026-07-28T10:30:00Z"
  },
  "message": "Token is valid",
  "timestamp": "2026-07-27T10:30:00Z",
  "path": "/api/v1/auth/validate"
}
```

**Error Responses:**

| **Code** | **Scenario**                      | **Response Body**                                           |
| -------- | --------------------------------- | ----------------------------------------------------------- |
| `401`    | Missing token                     | `{ "code": "MISSING_TOKEN", "message": "Authorization header is required" }` |
| `401`    | Expired token                     | `{ "code": "TOKEN_EXPIRED", "message": "Token has expired. Please login again." }` |
| `401`    | Invalid token                     | `{ "code": "INVALID_TOKEN", "message": "Token is invalid or tampered" }` |

---

## 4. Module 2: User Profile

Base Path: `/api/v1/users`

### 4.1 Get Profile

Retrieves the authenticated user's profile.

| **Field**       | **Detail**                                      |
| --------------- | ----------------------------------------------- |
| **Endpoint**    | `GET /api/v1/users/profile`                     |
| **Purpose**     | Get current user's demographic profile          |
| **Auth**        | Authenticated                                   |

**Success Response** — `200 OK`:
```json
{
  "success": true,
  "data": {
    "userId": 1,
    "firstName": "John",
    "lastName": "Doe",
    "dateOfBirth": "1995-03-15",
    "gender": "MALE",
    "heightCm": 175.0,
    "weightKg": 78.5,
    "bmi": 25.6,
    "phone": "+1-555-0123"
  },
  "message": "Profile retrieved successfully",
  "timestamp": "2026-07-27T10:30:00Z",
  "path": "/api/v1/users/profile"
}
```

**Error Responses:**

| **Code** | **Scenario**                      | **Response Body**                                           |
| -------- | --------------------------------- | ----------------------------------------------------------- |
| `404`    | Profile not created yet           | `{ "code": "RESOURCE_NOT_FOUND", "message": "Profile not found. Please create your profile first." }` |

---

### 4.2 Create or Update Profile

Creates or updates the user's demographic profile.

| **Field**       | **Detail**                                      |
| --------------- | ----------------------------------------------- |
| **Endpoint**    | `PUT /api/v1/users/profile`                     |
| **Purpose**     | Create or update user profile (upsert pattern)  |
| **Auth**        | Authenticated                                   |

**Request Body:**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "dateOfBirth": "1995-03-15",
  "gender": "MALE",
  "heightCm": 175.0,
  "weightKg": 78.5,
  "phone": "+1-555-0123"
}
```

**Validation Rules:**

| **Field**      | **Rule**                                                     |
| -------------- | ------------------------------------------------------------ |
| `firstName`    | Required. Min 1 char. Max 100 chars.                        |
| `lastName`     | Required. Min 1 char. Max 100 chars.                        |
| `dateOfBirth`  | Required. Must be a past date. User must be at least 10 years old. |
| `gender`       | Required. One of: `MALE`, `FEMALE`, `OTHER`.                |
| `heightCm`     | Required. Range: 50.0 – 300.0.                              |
| `weightKg`     | Required. Range: 10.0 – 500.0.                              |
| `phone`        | Optional. Max 20 chars.                                     |

**Success Response** — `200 OK` (updated) or `201 Created` (new):
```json
{
  "success": true,
  "data": {
    "userId": 1,
    "firstName": "John",
    "lastName": "Doe",
    "dateOfBirth": "1995-03-15",
    "gender": "MALE",
    "heightCm": 175.0,
    "weightKg": 78.5,
    "bmi": 25.6,
    "phone": "+1-555-0123"
  },
  "message": "Profile updated successfully",
  "timestamp": "2026-07-27T10:30:00Z",
  "path": "/api/v1/users/profile"
}
```

**Error Responses:**

| **Code** | **Scenario**                      | **Response Body**                                           |
| -------- | --------------------------------- | ----------------------------------------------------------- |
| `400`    | Invalid height/weight range       | `{ "code": "VALIDATION_ERROR", "message": "Validation failed", "details": ["heightCm must be between 50 and 300"] }` |
| `400`    | Invalid gender value              | `{ "code": "VALIDATION_ERROR", "message": "gender must be one of: MALE, FEMALE, OTHER" }` |

---

## 5. Module 3: Health Vitals

Base Path: `/api/v1/health`

### 5.1 Get Health Vitals

Retrieves the user's medical vitals and readings.

| **Field**       | **Detail**                                      |
| --------------- | ----------------------------------------------- |
| **Endpoint**    | `GET /api/v1/health/vitals`                     |
| **Purpose**     | Get current user's health vitals                |
| **Auth**        | Authenticated                                   |

**Success Response** — `200 OK`:
```json
{
  "success": true,
  "data": {
    "userId": 1,
    "bloodPressureSystolic": 120,
    "bloodPressureDiastolic": 80,
    "fastingSugar": 92.0,
    "postMealSugar": 135.0,
    "hba1c": 5.4,
    "cholesterolLdl": 100.0,
    "cholesterolHdl": 55.0,
    "triglycerides": 120.0
  },
  "message": "Health vitals retrieved successfully",
  "timestamp": "2026-07-27T10:30:00Z",
  "path": "/api/v1/health/vitals"
}
```

---

### 5.2 Create or Update Health Vitals

Creates or updates the user's medical vitals.

| **Field**       | **Detail**                                      |
| --------------- | ----------------------------------------------- |
| **Endpoint**    | `PUT /api/v1/health/vitals`                     |
| **Purpose**     | Create or update health vitals (upsert pattern) |
| **Auth**        | Authenticated                                   |

**Request Body:**
```json
{
  "bloodPressureSystolic": 120,
  "bloodPressureDiastolic": 80,
  "fastingSugar": 92.0,
  "postMealSugar": 135.0,
  "hba1c": 5.4,
  "cholesterolLdl": 100.0,
  "cholesterolHdl": 55.0,
  "triglycerides": 120.0
}
```

**Validation Rules:**

| **Field**                  | **Rule**                                           |
| -------------------------- | -------------------------------------------------- |
| `bloodPressureSystolic`    | Optional. Range: 60 – 250.                        |
| `bloodPressureDiastolic`   | Optional. Range: 30 – 150.                        |
| `fastingSugar`             | Optional. Range: 20.0 – 500.0.                   |
| `postMealSugar`            | Optional. Range: 20.0 – 600.0.                   |
| `hba1c`                    | Optional. Range: 2.0 – 20.0.                     |
| `cholesterolLdl`           | Optional. Range: 10.0 – 500.0.                   |
| `cholesterolHdl`           | Optional. Range: 5.0 – 150.0.                    |
| `triglycerides`            | Optional. Range: 10.0 – 2000.0.                  |

**Success Response** — `200 OK`:
```json
{
  "success": true,
  "data": {
    "userId": 1,
    "bloodPressureSystolic": 120,
    "bloodPressureDiastolic": 80,
    "fastingSugar": 92.0,
    "postMealSugar": 135.0,
    "hba1c": 5.4,
    "cholesterolLdl": 100.0,
    "cholesterolHdl": 55.0,
    "triglycerides": 120.0
  },
  "message": "Health vitals updated successfully",
  "timestamp": "2026-07-27T10:30:00Z",
  "path": "/api/v1/health/vitals"
}
```

---

### 5.3 Get Medical Conditions

Retrieves all medical conditions for the user.

| **Field**       | **Detail**                                      |
| --------------- | ----------------------------------------------- |
| **Endpoint**    | `GET /api/v1/health/conditions`                 |
| **Purpose**     | Get all medical conditions for the user         |
| **Auth**        | Authenticated                                   |

**Success Response** — `200 OK`:
```json
{
  "success": true,
  "data": [
    {
      "conditionId": 1,
      "condition": "DIABETES",
      "severity": "MODERATE",
      "diagnosedDate": "2024-01-15",
      "notes": "Type 2 diabetes, controlled with diet"
    }
  ],
  "message": "Conditions retrieved successfully",
  "timestamp": "2026-07-27T10:30:00Z",
  "path": "/api/v1/health/conditions"
}
```

---

### 5.4 Add Medical Condition

Adds a medical condition for the user.

| **Field**       | **Detail**                                      |
| --------------- | ----------------------------------------------- |
| **Endpoint**    | `POST /api/v1/health/conditions`                |
| **Purpose**     | Add a new medical condition                     |
| **Auth**        | Authenticated                                   |

**Request Body:**
```json
{
  "condition": "DIABETES",
  "severity": "MODERATE",
  "diagnosedDate": "2024-01-15",
  "notes": "Type 2 diabetes, controlled with diet"
}
```

**Validation Rules:**

| **Field**        | **Rule**                                                     |
| ---------------- | ------------------------------------------------------------ |
| `condition`      | Required. One of: `DIABETES`, `HYPERTENSION`, `PCOD`, `THYROID`, `HIGH_CHOLESTEROL`, `FATTY_LIVER`, `KIDNEY_DISEASE`, `ANEMIA`, `NONE`. |
| `severity`       | Optional. One of: `MILD`, `MODERATE`, `SEVERE`.             |
| `diagnosedDate`  | Optional. Must be a past date.                               |
| `notes`          | Optional. Max 500 chars.                                     |

**Success Response** — `201 Created`:
```json
{
  "success": true,
  "data": {
    "conditionId": 1,
    "condition": "DIABETES",
    "severity": "MODERATE",
    "diagnosedDate": "2024-01-15",
    "notes": "Type 2 diabetes, controlled with diet"
  },
  "message": "Condition added successfully",
  "timestamp": "2026-07-27T10:30:00Z",
  "path": "/api/v1/health/conditions"
}
```

**Error Responses:**

| **Code** | **Scenario**                      | **Response Body**                                           |
| -------- | --------------------------------- | ----------------------------------------------------------- |
| `409`    | Duplicate condition               | `{ "code": "DUPLICATE_CONDITION", "message": "This condition is already recorded for your profile" }` |

---

### 5.5 Remove Medical Condition

Removes a medical condition by ID.

| **Field**       | **Detail**                                      |
| --------------- | ----------------------------------------------- |
| **Endpoint**    | `DELETE /api/v1/health/conditions/{conditionId}`|
| **Purpose**     | Remove a medical condition record               |
| **Auth**        | Authenticated                                   |

**Path Parameters:**

| **Parameter**   | **Type** | **Description**          |
| --------------- | -------- | ------------------------ |
| `conditionId`   | Long     | Condition record ID      |

**Success Response** — `204 No Content`:
```
(empty body)
```

**Error Responses:**

| **Code** | **Scenario**                      | **Response Body**                                           |
| -------- | --------------------------------- | ----------------------------------------------------------- |
| `404`    | Condition not found               | `{ "code": "RESOURCE_NOT_FOUND", "message": "Condition not found with ID: 99" }` |
| `403`    | Condition belongs to another user | `{ "code": "FORBIDDEN", "message": "You can only delete your own conditions" }` |

---

## 6. Module 4: Goals & Lifestyle

Base Path: `/api/v1/goals`

### 6.1 Get Goals

Retrieves the user's health goals and lifestyle data.

| **Field**       | **Detail**                                      |
| --------------- | ----------------------------------------------- |
| **Endpoint**    | `GET /api/v1/goals`                             |
| **Purpose**     | Get user's health goals and lifestyle settings  |
| **Auth**        | Authenticated                                   |

**Success Response** — `200 OK`:
```json
{
  "success": true,
  "data": {
    "userId": 1,
    "primaryGoal": "WEIGHT_LOSS",
    "activityLevel": "MODERATE",
    "targetCalories": 2100,
    "targetProteinG": 105,
    "targetCarbsG": 210,
    "targetFatG": 70,
    "sleepHours": 7.5,
    "waterIntakeMl": 2500
  },
  "message": "Goals retrieved successfully",
  "timestamp": "2026-07-27T10:30:00Z",
  "path": "/api/v1/goals"
}
```

---

### 6.2 Create or Update Goals

Creates or updates the user's health goals and lifestyle settings.

| **Field**       | **Detail**                                      |
| --------------- | ----------------------------------------------- |
| **Endpoint**    | `PUT /api/v1/goals`                             |
| **Purpose**     | Create or update goals (upsert pattern)          |
| **Auth**        | Authenticated                                   |

**Request Body:**
```json
{
  "primaryGoal": "WEIGHT_LOSS",
  "activityLevel": "MODERATE",
  "sleepHours": 7.5,
  "waterIntakeMl": 2500
}
```

**Validation Rules:**

| **Field**        | **Rule**                                                     |
| ---------------- | ------------------------------------------------------------ |
| `primaryGoal`    | Required. One of: `WEIGHT_LOSS`, `WEIGHT_GAIN`, `MUSCLE_GAIN`, `MAINTENANCE`, `HEALTHY_LIFESTYLE`. |
| `activityLevel`  | Required. One of: `SEDENTARY`, `LIGHT`, `MODERATE`, `ACTIVE`, `VERY_ACTIVE`. |
| `sleepHours`     | Optional. Range: 2.0 – 16.0.                                |
| `waterIntakeMl`  | Optional. Range: 200 – 10000.                               |

**Note:** `targetCalories`, `targetProteinG`, `targetCarbsG`, and `targetFatG` are **calculated server-side** by the Recommendation Engine based on the user's profile, vitals, goals, and activity level. They are returned in the response but cannot be set directly by the client.

**Success Response** — `200 OK`:
```json
{
  "success": true,
  "data": {
    "userId": 1,
    "primaryGoal": "WEIGHT_LOSS",
    "activityLevel": "MODERATE",
    "targetCalories": 2100,
    "targetProteinG": 105,
    "targetCarbsG": 210,
    "targetFatG": 70,
    "sleepHours": 7.5,
    "waterIntakeMl": 2500
  },
  "message": "Goals updated successfully",
  "timestamp": "2026-07-27T10:30:00Z",
  "path": "/api/v1/goals"
}
```

---

## 7. Module 5: Food Preferences

Base Path: `/api/v1/preferences`

### 7.1 Get Preferences

Retrieves the user's food preferences.

| **Field**       | **Detail**                                      |
| --------------- | ----------------------------------------------- |
| **Endpoint**    | `GET /api/v1/preferences`                       |
| **Purpose**     | Get user's dietary preferences and restrictions |
| **Auth**        | Authenticated                                   |

**Success Response** — `200 OK`:
```json
{
  "success": true,
  "data": {
    "userId": 1,
    "dietType": "VEGETARIAN",
    "budgetLevel": "MEDIUM",
    "region": "North India",
    "allergies": ["LACTOSE"],
    "excludedFoods": "Mushrooms, Okra"
  },
  "message": "Preferences retrieved successfully",
  "timestamp": "2026-07-27T10:30:00Z",
  "path": "/api/v1/preferences"
}
```

---

### 7.2 Create or Update Preferences

Creates or updates the user's food preferences.

| **Field**       | **Detail**                                      |
| --------------- | ----------------------------------------------- |
| **Endpoint**    | `PUT /api/v1/preferences`                       |
| **Purpose**     | Create or update food preferences (upsert)      |
| **Auth**        | Authenticated                                   |

**Request Body:**
```json
{
  "dietType": "VEGETARIAN",
  "budgetLevel": "MEDIUM",
  "region": "North India",
  "allergies": ["LACTOSE"],
  "excludedFoods": "Mushrooms, Okra"
}
```

**Validation Rules:**

| **Field**        | **Rule**                                                     |
| ---------------- | ------------------------------------------------------------ |
| `dietType`       | Required. One of: `VEGETARIAN`, `NON_VEGETARIAN`, `VEGAN`, `EGGETARIAN`, `FLEXITARIAN`. |
| `budgetLevel`    | Required. One of: `LOW`, `MEDIUM`, `HIGH`.                   |
| `region`         | Optional. Max 100 chars.                                    |
| `allergies`      | Optional. JSON array of strings. Max 10 items. Each max 50 chars. |
| `excludedFoods`  | Optional. Max 1000 chars. Free text.                        |

**Success Response** — `200 OK`:
```json
{
  "success": true,
  "data": {
    "userId": 1,
    "dietType": "VEGETARIAN",
    "budgetLevel": "MEDIUM",
    "region": "North India",
    "allergies": ["LACTOSE"],
    "excludedFoods": "Mushrooms, Okra"
  },
  "message": "Preferences updated successfully",
  "timestamp": "2026-07-27T10:30:00Z",
  "path": "/api/v1/preferences"
}
```

---

## 8. Module 6: Meal Plans & Recommendations

Base Path: `/api/v1/meal-plans`

### 8.1 Generate Meal Plan

Triggers the Recommendation Engine to generate a personalized daily meal plan.

| **Field**       | **Detail**                                      |
| --------------- | ----------------------------------------------- |
| **Endpoint**    | `POST /api/v1/meal-plans/generate`              |
| **Purpose**     | Generate a personalized meal plan for today     |
| **Auth**        | Authenticated                                   |

**Request Body:**
```json
{
  "date": "2026-07-27"
}
```

**Validation Rules:**

| **Field**  | **Rule**                                              |
| ---------- | ----------------------------------------------------- |
| `date`     | Required. Format: `YYYY-MM-DD`. Cannot be in the past more than 7 days. Cannot be more than 7 days in the future. |

**Success Response** — `201 Created`:
```json
{
  "success": true,
  "data": {
    "planId": 1,
    "userId": 1,
    "planDate": "2026-07-27",
    "totalCalories": 1850,
    "totalProteinG": 85.0,
    "totalCarbsG": 220.0,
    "totalFatG": 55.0,
    "dietaryTips": [
      "Drink water 30 minutes before meals for better digestion",
      "Include leafy greens in your lunch for added fiber",
      "Avoid sugary drinks — opt for herbal tea instead"
    ],
    "isGenerated": true,
    "meals": [
      {
        "itemId": 1,
        "mealType": "BREAKFAST",
        "foodId": 42,
        "foodName": "Oats (Rolled)",
        "servingSizeG": 200,
        "servingUnit": "1 bowl",
        "calories": 350,
        "proteinG": 12.5,
        "carbsG": 45.0,
        "fatG": 14.0
      },
      {
        "itemId": 2,
        "mealType": "LUNCH",
        "foodId": 78,
        "foodName": "Brown Rice with Dal",
        "servingSizeG": 300,
        "servingUnit": "1 plate",
        "calories": 550,
        "proteinG": 25.0,
        "carbsG": 80.0,
        "fatG": 12.0
      },
      {
        "itemId": 3,
        "mealType": "SNACK_MORNING",
        "foodId": 15,
        "foodName": "Apple",
        "servingSizeG": 150,
        "servingUnit": "1 medium",
        "calories": 80,
        "proteinG": 0.5,
        "carbsG": 22.0,
        "fatG": 0.3
      },
      {
        "itemId": 4,
        "mealType": "DINNER",
        "foodId": 103,
        "foodName": "Grilled Paneer Salad",
        "servingSizeG": 250,
        "servingUnit": "1 bowl",
        "calories": 450,
        "proteinG": 30.0,
        "carbsG": 25.0,
        "fatG": 22.0
      },
      {
        "itemId": 5,
        "mealType": "SNACK_EVENING",
        "foodId": 56,
        "foodName": "Mixed Nuts",
        "servingSizeG": 30,
        "servingUnit": "1 handful",
        "calories": 170,
        "proteinG": 5.0,
        "carbsG": 6.0,
        "fatG": 15.0
      }
    ]
  },
  "message": "Meal plan generated successfully",
  "timestamp": "2026-07-27T10:30:00Z",
  "path": "/api/v1/meal-plans/generate"
}
```

**Error Responses:**

| **Code** | **Scenario**                      | **Response Body**                                           |
| -------- | --------------------------------- | ----------------------------------------------------------- |
| `400`    | Incomplete profile                | `{ "code": "INCOMPLETE_PROFILE", "message": "Please complete your profile, health vitals, goals, and preferences before generating a meal plan" }` |
| `400`    | Plan already exists for this date | `{ "code": "PLAN_EXISTS", "message": "A meal plan already exists for this date. Use GET to retrieve it." }` |
| `422`    | Recommendation engine failure     | `{ "code": "RECOMMENDATION_FAILED", "message": "Could not generate meal plan. Please check your inputs and try again." }` |

---

### 8.2 Get Meal Plan by Date

Retrieves a previously generated meal plan for a specific date.

| **Field**       | **Detail**                                      |
| --------------- | ----------------------------------------------- |
| **Endpoint**    | `GET /api/v1/meal-plans?date={date}`            |
| **Purpose**     | Retrieve a meal plan for a specific date        |
| **Auth**        | Authenticated                                   |

**Query Parameters:**

| **Parameter** | **Type** | **Required** | **Description**           |
| ------------- | -------- | ------------ | ------------------------- |
| `date`        | String   | No           | Date in `YYYY-MM-DD`. Defaults to today if omitted. |

**Success Response** — `200 OK`:
(Response structure identical to the generate endpoint above)

**Error Responses:**

| **Code** | **Scenario**                      | **Response Body**                                           |
| -------- | --------------------------------- | ----------------------------------------------------------- |
| `404`    | No plan for this date             | `{ "code": "RESOURCE_NOT_FOUND", "message": "No meal plan found for date: 2026-07-27. Generate one first." }` |

---

### 8.3 Get Meal Plan by ID

Retrieves a specific meal plan by its plan ID.

| **Field**       | **Detail**                                      |
| --------------- | ----------------------------------------------- |
| **Endpoint**    | `GET /api/v1/meal-plans/{planId}`               |
| **Purpose**     | Retrieve a specific meal plan by ID             |
| **Auth**        | Authenticated                                   |

**Path Parameters:**

| **Parameter** | **Type** | **Description**    |
| ------------- | -------- | ------------------ |
| `planId`      | Long     | Meal plan ID       |

**Success Response** — `200 OK`:
(Response structure identical to the generate endpoint)

**Error Responses:**

| **Code** | **Scenario**                      | **Response Body**                                           |
| -------- | --------------------------------- | ----------------------------------------------------------- |
| `404`    | Plan not found                    | `{ "code": "RESOURCE_NOT_FOUND", "message": "Meal plan not found with ID: 99" }` |
| `403`    | Plan belongs to another user      | `{ "code": "FORBIDDEN", "message": "You can only view your own meal plans" }` |

---

### 8.4 Get Meal Plan History

Retrieves meal plans for a date range.

| **Field**       | **Detail**                                      |
| --------------- | ----------------------------------------------- |
| **Endpoint**    | `GET /api/v1/meal-plans/history`                |
| **Purpose**     | Get meal plans for a date range (max 7 days)    |
| **Auth**        | Authenticated                                   |

**Query Parameters:**

| **Parameter** | **Type** | **Required** | **Description**             |
| ------------- | -------- | ------------ | --------------------------- |
| `startDate`   | String   | Yes          | Start date `YYYY-MM-DD`    |
| `endDate`     | String   | Yes          | End date `YYYY-MM-DD`      |

**Validation:** Date range cannot exceed 7 days. End date cannot be before start date.

**Success Response** — `200 OK`:
```json
{
  "success": true,
  "data": [
    {
      "planId": 1,
      "planDate": "2026-07-27",
      "totalCalories": 1850,
      "totalProteinG": 85.0,
      "totalCarbsG": 220.0,
      "totalFatG": 55.0,
      "mealCount": 5
    },
    {
      "planId": 2,
      "planDate": "2026-07-26",
      "totalCalories": 1900,
      "totalProteinG": 90.0,
      "totalCarbsG": 210.0,
      "totalFatG": 58.0,
      "mealCount": 4
    }
  ],
  "message": "Meal plan history retrieved successfully",
  "timestamp": "2026-07-27T10:30:00Z",
  "path": "/api/v1/meal-plans/history"
}
```

---

## 9. Module 7: Food Diary

Base Path: `/api/v1/food-diary`

### 9.1 Log Food Entry

Logs a food consumption entry for the user.

| **Field**       | **Detail**                                      |
| --------------- | ----------------------------------------------- |
| **Endpoint**    | `POST /api/v1/food-diary/entries`               |
| **Purpose**     | Log a food consumption entry                    |
| **Auth**        | Authenticated                                   |

**Request Body:**
```json
{
  "foodId": 42,
  "mealType": "BREAKFAST",
  "foodName": "Oats (Rolled)",
  "servingSizeG": 200,
  "loggedDate": "2026-07-27",
  "loggedTime": "08:30:00",
  "notes": "Cooked with water and topped with almonds"
}
```

**Validation Rules:**

| **Field**        | **Rule**                                                     |
| ---------------- | ------------------------------------------------------------ |
| `foodId`         | Optional. Must reference a valid food in catalog if provided. |
| `mealType`       | Required. One of: `BREAKFAST`, `LUNCH`, `DINNER`, `SNACK_MORNING`, `SNACK_EVENING`. |
| `foodName`       | Required. Max 200 chars.                                     |
| `servingSizeG`   | Required. Range: 1 – 5000.                                  |
| `loggedDate`     | Required. Format: `YYYY-MM-DD`. Cannot be in the future.    |
| `loggedTime`     | Optional. Format: `HH:mm:ss`. Defaults to current time.     |
| `notes`          | Optional. Max 1000 chars.                                    |

**Note:** `calories`, `proteinG`, `carbsG`, and `fatG` are **calculated server-side** from the serving size and the food item's per-100g nutritional values.

**Success Response** — `201 Created`:
```json
{
  "success": true,
  "data": {
    "entryId": 1,
    "userId": 1,
    "foodId": 42,
    "mealType": "BREAKFAST",
    "foodName": "Oats (Rolled)",
    "servingSizeG": 200,
    "calories": 350,
    "proteinG": 12.5,
    "carbsG": 45.0,
    "fatG": 14.0,
    "loggedDate": "2026-07-27",
    "loggedTime": "08:30:00",
    "notes": "Cooked with water and topped with almonds"
  },
  "message": "Food entry logged successfully",
  "timestamp": "2026-07-27T10:30:00Z",
  "path": "/api/v1/food-diary/entries"
}
```

**Error Responses:**

| **Code** | **Scenario**                      | **Response Body**                                           |
| -------- | --------------------------------- | ----------------------------------------------------------- |
| `400`    | Future date                       | `{ "code": "VALIDATION_ERROR", "message": "loggedDate cannot be in the future" }` |
| `404`    | Invalid foodId                    | `{ "code": "RESOURCE_NOT_FOUND", "message": "Food item not found with ID: 999" }` |

---

### 9.2 Get Diary Entries for Date

Retrieves all food diary entries for a specific date.

| **Field**       | **Detail**                                      |
| --------------- | ----------------------------------------------- |
| **Endpoint**    | `GET /api/v1/food-diary/entries?date={date}`    |
| **Purpose**     | Get all food diary entries for a date           |
| **Auth**        | Authenticated                                   |

**Query Parameters:**

| **Parameter** | **Type** | **Required** | **Description**                 |
| ------------- | -------- | ------------ | ------------------------------- |
| `date`        | String   | No           | Date `YYYY-MM-DD`. Defaults to today. |

**Success Response** — `200 OK`:
```json
{
  "success": true,
  "data": {
    "date": "2026-07-27",
    "totalCalories": 1850,
    "totalProteinG": 85.0,
    "totalCarbsG": 220.0,
    "totalFatG": 55.0,
    "entries": [
      {
        "entryId": 1,
        "mealType": "BREAKFAST",
        "foodName": "Oats (Rolled)",
        "servingSizeG": 200,
        "calories": 350,
        "proteinG": 12.5,
        "carbsG": 45.0,
        "fatG": 14.0,
        "loggedTime": "08:30:00",
        "notes": "Cooked with water and topped with almonds"
      },
      {
        "entryId": 2,
        "mealType": "LUNCH",
        "foodName": "Brown Rice with Dal",
        "servingSizeG": 300,
        "calories": 550,
        "proteinG": 25.0,
        "carbsG": 80.0,
        "fatG": 12.0,
        "loggedTime": "13:00:00",
        "notes": null
      }
    ]
  },
  "message": "Diary entries retrieved successfully",
  "timestamp": "2026-07-27T10:30:00Z",
  "path": "/api/v1/food-diary/entries"
}
```

---

### 9.3 Delete Diary Entry

Deletes a specific food diary entry.

| **Field**       | **Detail**                                      |
| --------------- | ----------------------------------------------- |
| **Endpoint**    | `DELETE /api/v1/food-diary/entries/{entryId}`   |
| **Purpose**     | Delete a food diary entry                       |
| **Auth**        | Authenticated                                   |

**Path Parameters:**

| **Parameter** | **Type** | **Description**       |
| ------------- | -------- | --------------------- |
| `entryId`     | Long     | Diary entry ID        |

**Success Response** — `204 No Content**:
```
(empty body)
```

**Error Responses:**

| **Code** | **Scenario**                      | **Response Body**                                           |
| -------- | --------------------------------- | ----------------------------------------------------------- |
| `404`    | Entry not found                   | `{ "code": "RESOURCE_NOT_FOUND", "message": "Diary entry not found with ID: 99" }` |
| `403`    | Entry belongs to another user     | `{ "code": "FORBIDDEN", "message": "You can only delete your own diary entries" }` |

---

## 10. Module 8: Progress Dashboard

Base Path: `/api/v1/progress`

### 10.1 Get Dashboard Summary

Retrieves aggregated progress data for the dashboard overview.

| **Field**       | **Detail**                                      |
| --------------- | ----------------------------------------------- |
| **Endpoint**    | `GET /api/v1/progress/summary`                  |
| **Purpose**     | Get today's nutrition summary and latest metrics |
| **Auth**        | Authenticated                                   |

**Success Response** — `200 OK`:
```json
{
  "success": true,
  "data": {
    "currentWeight": 78.0,
    "currentBmi": 25.5,
    "bmiCategory": "OVERWEIGHT",
    "startWeight": 82.0,
    "weightChange": -4.0,
    "todayCalories": 1850,
    "targetCalories": 2100,
    "caloriesRemaining": 250,
    "todayProtein": 85.0,
    "targetProtein": 105.0,
    "todayCarbs": 220.0,
    "targetCarbs": 210.0,
    "todayFat": 55.0,
    "targetFat": 70.0,
    "todayWaterMl": 2500,
    "targetWaterMl": 3000,
    "todaySleepHrs": 7.5,
    "streakDays": 5,
    "lastUpdated": "2026-07-27T22:00:00Z"
  },
  "message": "Dashboard summary retrieved successfully",
  "timestamp": "2026-07-27T10:30:00Z",
  "path": "/api/v1/progress/summary"
}
```

---

### 10.2 Get Weight Trend

Retrieves weight history for the weight trend chart.

| **Field**       | **Detail**                                      |
| --------------- | ----------------------------------------------- |
| **Endpoint**    | `GET /api/v1/progress/weight`                   |
| **Purpose**     | Get weight history data for trend chart         |
| **Auth**        | Authenticated                                   |

**Query Parameters:**

| **Parameter** | **Type** | **Required** | **Description**                  |
| ------------- | -------- | ------------ | -------------------------------- |
| `days`        | Integer  | No           | Number of days of history. Default: 30. Max: 90. |

**Success Response** — `200 OK`:
```json
{
  "success": true,
  "data": {
    "dataPoints": [
      { "date": "2026-06-28", "weight": 82.0 },
      { "date": "2026-07-05", "weight": 81.2 },
      { "date": "2026-07-12", "weight": 80.1 },
      { "date": "2026-07-19", "weight": 79.0 },
      { "date": "2026-07-26", "weight": 78.0 }
    ],
    "startWeight": 82.0,
    "currentWeight": 78.0,
    "totalChange": -4.0,
    "trend": "DOWN"
  },
  "message": "Weight trend retrieved successfully",
  "timestamp": "2026-07-27T10:30:00Z",
  "path": "/api/v1/progress/weight"
}
```

---

### 10.3 Get Calorie Trend

Retrieves calorie consumption vs target for the calorie chart.

| **Field**       | **Detail**                                      |
| --------------- | ----------------------------------------------- |
| **Endpoint**    | `GET /api/v1/progress/calories`                 |
| **Purpose**     | Get calorie trend data for bar/line chart       |
| **Auth**        | Authenticated                                   |

**Query Parameters:**

| **Parameter** | **Type** | **Required** | **Description**                  |
| ------------- | -------- | ------------ | -------------------------------- |
| `days`        | Integer  | No           | Number of days. Default: 7. Max: 30. |

**Success Response** — `200 OK`:
```json
{
  "success": true,
  "data": {
    "dataPoints": [
      { "date": "2026-07-21", "consumed": 1950, "target": 2100 },
      { "date": "2026-07-22", "consumed": 1800, "target": 2100 },
      { "date": "2026-07-23", "consumed": 2100, "target": 2100 },
      { "date": "2026-07-24", "consumed": 1650, "target": 2100 },
      { "date": "2026-07-25", "consumed": 2000, "target": 2100 },
      { "date": "2026-07-26", "consumed": 1900, "target": 2100 },
      { "date": "2026-07-27", "consumed": 1850, "target": 2100 }
    ],
    "averageConsumed": 1893,
    "averageTarget": 2100
  },
  "message": "Calorie trend retrieved successfully",
  "timestamp": "2026-07-27T10:30:00Z",
  "path": "/api/v1/progress/calories"
}
```

---

### 10.4 Get Macro Breakdown

Retrieves macronutrient breakdown for the doughnut chart.

| **Field**       | **Detail**                                      |
| --------------- | ----------------------------------------------- |
| **Endpoint**    | `GET /api/v1/progress/macros`                   |
| **Purpose**     | Get macro split for current day                 |
| **Auth**        | Authenticated                                   |

**Query Parameters:**

| **Parameter** | **Type** | **Required** | **Description**                  |
| ------------- | -------- | ------------ | -------------------------------- |
| `date`        | String   | No           | Date `YYYY-MM-DD`. Defaults to today. |

**Success Response** — `200 OK`:
```json
{
  "success": true,
  "data": {
    "date": "2026-07-27",
    "protein": { "consumedG": 85.0, "targetG": 105.0, "percentage": 81 },
    "carbs": { "consumedG": 220.0, "targetG": 210.0, "percentage": 105 },
    "fat": { "consumedG": 55.0, "targetG": 70.0, "percentage": 79 },
    "calorieSplit": {
      "proteinPercent": 19,
      "carbsPercent": 48,
      "fatPercent": 27
    }
  },
  "message": "Macro breakdown retrieved successfully",
  "timestamp": "2026-07-27T10:30:00Z",
  "path": "/api/v1/progress/macros"
}
```

---

### 10.5 Record Daily Tracking

Records a daily tracking entry (weight, water, sleep).

| **Field**       | **Detail**                                      |
| --------------- | ----------------------------------------------- |
| **Endpoint**    | `POST /api/v1/progress/tracking`                |
| **Purpose**     | Record daily weight, water, and sleep metrics   |
| **Auth**        | Authenticated                                   |

**Request Body:**
```json
{
  "recordedDate": "2026-07-27",
  "weightKg": 78.0,
  "waterIntakeMl": 2500,
  "sleepHours": 7.5,
  "notes": "Felt energetic today"
}
```

**Validation Rules:**

| **Field**        | **Rule**                                           |
| ---------------- | -------------------------------------------------- |
| `recordedDate`   | Required. Format: `YYYY-MM-DD`. Cannot be in future. |
| `weightKg`       | Optional. Range: 10.0 – 500.0.                    |
| `waterIntakeMl`  | Optional. Range: 0 – 10000.                       |
| `sleepHours`     | Optional. Range: 0.0 – 24.0.                      |
| `notes`          | Optional. Max 1000 chars.                         |

**Success Response** — `201 Created`:
```json
{
  "success": true,
  "data": {
    "trackingId": 1,
    "recordedDate": "2026-07-27",
    "weightKg": 78.0,
    "bmi": 25.5,
    "waterIntakeMl": 2500,
    "sleepHours": 7.5,
    "notes": "Felt energetic today"
  },
  "message": "Tracking record created successfully",
  "timestamp": "2026-07-27T10:30:00Z",
  "path": "/api/v1/progress/tracking"
}
```

---

## 11. Module 9: Food Catalog

Base Path: `/api/v1/foods`

### 11.1 Search Foods

Searches the food catalog by name.

| **Field**       | **Detail**                                      |
| --------------- | ----------------------------------------------- |
| **Endpoint**    | `GET /api/v1/foods/search`                      |
| **Purpose**     | Search food items by name                       |
| **Auth**        | Authenticated                                   |

**Query Parameters:**

| **Parameter** | **Type** | **Required** | **Description**               |
| ------------- | -------- | ------------ | ----------------------------- |
| `q`           | String   | Yes          | Search query (min 2 chars)   |
| `category`    | String   | No           | Filter by category            |
| `page`        | Integer  | No           | Page number (default: 0)      |
| `size`        | Integer  | No           | Page size (default: 20, max: 100) |

**Success Response** — `200 OK`:
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "foodId": 42,
        "foodName": "Oats (Rolled)",
        "category": "Grains",
        "calories100g": 389.0,
        "protein100g": 16.9,
        "carbs100g": 66.3,
        "fat100g": 6.9,
        "fiber100g": 10.6,
        "sugar100g": 1.0,
        "dietType": ["VEGETARIAN", "VEGAN"],
        "budgetLevel": "LOW"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1
  },
  "message": "Search results retrieved successfully",
  "timestamp": "2026-07-27T10:30:00Z",
  "path": "/api/v1/foods/search"
}
```

---

### 11.2 Get Food by ID

Retrieves a specific food item's details.

| **Field**       | **Detail**                                      |
| --------------- | ----------------------------------------------- |
| **Endpoint**    | `GET /api/v1/foods/{foodId}`                    |
| **Purpose**     | Get food item details by ID                     |
| **Auth**        | Authenticated                                   |

**Path Parameters:**

| **Parameter** | **Type** | **Description** |
| ------------- | -------- | --------------- |
| `foodId`      | Long     | Food item ID    |

**Success Response** — `200 OK`:
```json
{
  "success": true,
  "data": {
    "foodId": 42,
    "foodName": "Oats (Rolled)",
    "category": "Grains",
    "calories100g": 389.0,
    "protein100g": 16.9,
    "carbs100g": 66.3,
    "fat100g": 6.9,
    "fiber100g": 10.6,
    "sugar100g": 1.0,
    "dietType": ["VEGETARIAN", "VEGAN"],
    "budgetLevel": "LOW",
    "region": "All"
  },
  "message": "Food item retrieved successfully",
  "timestamp": "2026-07-27T10:30:00Z",
  "path": "/api/v1/foods/42"
}
```

---

### 11.3 Get Foods by Category

Retrieves food items filtered by category.

| **Field**       | **Detail**                                      |
| --------------- | ----------------------------------------------- |
| **Endpoint**    | `GET /api/v1/foods/category/{category}`         |
| **Purpose**     | Get food items by category                      |
| **Auth**        | Authenticated                                   |

**Path Parameters:**

| **Parameter** | **Type** | **Description** |
| ------------- | -------- | --------------- |
| `category`    | String   | Category name   |

**Success Response** — `200 OK`:
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "foodId": 42,
        "foodName": "Oats (Rolled)",
        "category": "Grains",
        "calories100g": 389.0,
        "budgetLevel": "LOW"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 15,
    "totalPages": 1
  },
  "message": "Food items retrieved successfully",
  "timestamp": "2026-07-27T10:30:00Z",
  "path": "/api/v1/foods/category/Grains"
}
```

---

## 12. Module 10: Admin

Base Path: `/api/v1/admin`

All endpoints in this module require **ADMIN role**.

### 12.1 Get All Users

Retrieves paginated list of all registered users.

| **Field**       | **Detail**                                      |
| --------------- | ----------------------------------------------- |
| **Endpoint**    | `GET /api/v1/admin/users`                       |
| **Purpose**     | List all registered users (paginated)           |
| **Auth**        | Admin                                           |

**Query Parameters:**

| **Parameter** | **Type** | **Required** | **Description**               |
| ------------- | -------- | ------------ | ----------------------------- |
| `page`        | Integer  | No           | Page number (default: 0)      |
| `size`        | Integer  | No           | Page size (default: 20)       |
| `search`      | String   | No           | Search by email or name       |

**Success Response** — `200 OK`:
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "userId": 1,
        "email": "john.doe@example.com",
        "firstName": "John",
        "lastName": "Doe",
        "role": "USER",
        "isActive": true,
        "lastLoginAt": "2026-07-27T09:15:00Z",
        "createdAt": "2026-07-20T14:30:00Z"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 50,
    "totalPages": 3
  },
  "message": "Users retrieved successfully",
  "timestamp": "2026-07-27T10:30:00Z",
  "path": "/api/v1/admin/users"
}
```

---

### 12.2 Get User Details

Retrieves complete details of a specific user.

| **Field**       | **Detail**                                      |
| --------------- | ----------------------------------------------- |
| **Endpoint**    | `GET /api/v1/admin/users/{userId}`              |
| **Purpose**     | Get complete user details (admin view)          |
| **Auth**        | Admin                                           |

**Path Parameters:**

| **Parameter** | **Type** | **Description** |
| ------------- | -------- | --------------- |
| `userId`      | Long     | User ID to view |

**Success Response** — `200 OK`:
```json
{
  "success": true,
  "data": {
    "user": {
      "userId": 1,
      "email": "john.doe@example.com",
      "firstName": "John",
      "lastName": "Doe",
      "role": "USER",
      "isActive": true,
      "lastLoginAt": "2026-07-27T09:15:00Z",
      "createdAt": "2026-07-20T14:30:00Z"
    },
    "profile": {
      "gender": "MALE",
      "heightCm": 175.0,
      "weightKg": 78.5,
      "bmi": 25.6
    },
    "healthConditions": ["DIABETES"],
    "goal": "WEIGHT_LOSS",
    "dietType": "VEGETARIAN",
    "recentPlans": 12,
    "diaryEntriesLastWeek": 28
  },
  "message": "User details retrieved successfully",
  "timestamp": "2026-07-27T10:30:00Z",
  "path": "/api/v1/admin/users/1"
}
```

---

### 12.3 Deactivate User

Deactivates a user account.

| **Field**       | **Detail**                                      |
| --------------- | ----------------------------------------------- |
| **Endpoint**    | `PATCH /api/v1/admin/users/{userId}/deactivate` |
| **Purpose**     | Deactivate a user account                       |
| **Auth**        | Admin                                           |

**Path Parameters:**

| **Parameter** | **Type** | **Description** |
| ------------- | -------- | --------------- |
| `userId`      | Long     | User ID to deactivate |

**Success Response** — `200 OK`:
```json
{
  "success": true,
  "data": null,
  "message": "User account deactivated successfully",
  "timestamp": "2026-07-27T10:30:00Z",
  "path": "/api/v1/admin/users/1/deactivate"
}
```

---

### 12.4 Get System Health

Retrieves system health status.

| **Field**       | **Detail**                                      |
| --------------- | ----------------------------------------------- |
| **Endpoint**    | `GET /api/v1/admin/system/health`               |
| **Purpose**     | Get system health overview (admin dashboard)    |
| **Auth**        | Admin                                           |

**Success Response** — `200 OK`:
```json
{
  "success": true,
  "data": {
    "totalUsers": 150,
    "activeToday": 45,
    "totalMealPlansGenerated": 3200,
    "totalDiaryEntries": 8500,
    "databaseStatus": "HEALTHY",
    "cacheStatus": "HEALTHY",
    "uptime": "14d 6h 32m",
    "lastBackup": "2026-07-27T02:00:00Z"
  },
  "message": "System health retrieved successfully",
  "timestamp": "2026-07-27T10:30:00Z",
  "path": "/api/v1/admin/system/health"
}
```

---

### 12.5 Add Food Item

Adds a new food item to the catalog.

| **Field**       | **Detail**                                      |
| --------------- | ----------------------------------------------- |
| **Endpoint**    | `POST /api/v1/admin/foods`                      |
| **Purpose**     | Add a new food item to the catalog              |
| **Auth**        | Admin                                           |

**Request Body:**
```json
{
  "foodName": "Quinoa",
  "category": "Grains",
  "calories100g": 120.0,
  "protein100g": 4.4,
  "carbs100g": 21.3,
  "fat100g": 1.9,
  "fiber100g": 2.8,
  "sugar100g": 0.9,
  "dietType": ["VEGETARIAN", "VEGAN"],
  "budgetLevel": "MEDIUM",
  "region": "All"
}
```

**Validation Rules:**

| **Field**        | **Rule**                                           |
| ---------------- | -------------------------------------------------- |
| `foodName`       | Required. Max 200 chars.                           |
| `category`       | Required. Max 100 chars.                           |
| `calories100g`   | Required. Must be >= 0.                            |
| `protein100g`    | Required. Must be >= 0.                            |
| `carbs100g`      | Required. Must be >= 0.                            |
| `fat100g`        | Required. Must be >= 0.                            |
| `dietType`       | Required. Must be non-empty array.                 |
| `budgetLevel`    | Required. One of: `LOW`, `MEDIUM`, `HIGH`.         |

**Success Response** — `201 Created`:
```json
{
  "success": true,
  "data": {
    "foodId": 250,
    "foodName": "Quinoa",
    "category": "Grains",
    "calories100g": 120.0,
    "protein100g": 4.4,
    "carbs100g": 21.3,
    "fat100g": 1.9,
    "fiber100g": 2.8,
    "sugar100g": 0.9,
    "dietType": ["VEGETARIAN", "VEGAN"],
    "budgetLevel": "MEDIUM",
    "region": "All",
    "isActive": true
  },
  "message": "Food item added successfully",
  "timestamp": "2026-07-27T10:30:00Z",
  "path": "/api/v1/admin/foods"
}
```

---

### 12.6 Update Food Item

Updates an existing food item in the catalog.

| **Field**       | **Detail**                                      |
| --------------- | ----------------------------------------------- |
| **Endpoint**    | `PUT /api/v1/admin/foods/{foodId}`              |
| **Purpose**     | Update an existing food item                    |
| **Auth**        | Admin                                           |

**Path Parameters:**

| **Parameter** | **Type** | **Description**     |
| ------------- | -------- | ------------------- |
| `foodId`      | Long     | Food item ID to update |

**Request Body:** (Same structure as Add Food Item)

**Success Response** — `200 OK`:
(Response structure same as Add, with updated values)

**Error Responses:**

| **Code** | **Scenario**                      | **Response Body**                                           |
| -------- | --------------------------------- | ----------------------------------------------------------- |
| `404`    | Food not found                    | `{ "code": "RESOURCE_NOT_FOUND", "message": "Food item not found with ID: 999" }` |

---

### 12.7 Delete Food Item

Soft-deletes a food item from the catalog.

| **Field**       | **Detail**                                      |
| --------------- | ----------------------------------------------- |
| **Endpoint**    | `DELETE /api/v1/admin/foods/{foodId}`           |
| **Purpose**     | Soft-delete a food item (sets isActive = false) |
| **Auth**        | Admin                                           |

**Path Parameters:**

| **Parameter** | **Type** | **Description**     |
| ------------- | -------- | ------------------- |
| `foodId`      | Long     | Food item ID to delete |

**Success Response** — `204 No Content`:
```
(empty body)
```

---

## 13. Common Error Codes

### 13.1 Complete Error Code Reference

| **Error Code**                | **HTTP Status** | **Description**                                      | **When It Occurs**                              |
| ----------------------------- | --------------- | ---------------------------------------------------- | ----------------------------------------------- |
| `VALIDATION_ERROR`            | 400             | One or more fields failed validation                 | Request body fails `@Valid` checks              |
| `MISSING_PARAMETER`           | 400             | Required query parameter is missing                  | GET request without required param              |
| `INVALID_DATE_FORMAT`         | 400             | Date does not match `YYYY-MM-DD` pattern             | Date parameter is malformed                     |
| `INVALID_ENUM_VALUE`          | 400             | Value is not one of the allowed enum values          | Enum field has unacceptable value               |
| `INCOMPLETE_PROFILE`          | 400             | User has not completed required profile data          | Generating meal plan without profile setup       |
| `PLAN_EXISTS`                 | 400             | A meal plan already exists for the specified date     | Duplicate meal plan generation                   |
| `UNAUTHORIZED`                | 401             | Authentication is required                           | Missing or invalid JWT token                     |
| `TOKEN_EXPIRED`               | 401             | JWT token has expired                                | Token older than 24 hours                        |
| `INVALID_CREDENTIALS`         | 401             | Email or password is incorrect                        | Login with wrong credentials                     |
| `ACCOUNT_INACTIVE`            | 401             | User account has been deactivated                    | Login after admin deactivation                   |
| `FORBIDDEN`                   | 403             | User does not have permission for this resource      | Accessing another user's data or admin endpoint   |
| `RESOURCE_NOT_FOUND`          | 404             | Requested resource does not exist                     | Fetching non-existent entity                     |
| `DUPLICATE_EMAIL`             | 409             | Email is already registered                          | Registration with existing email                 |
| `DUPLICATE_CONDITION`         | 409             | Medical condition already recorded for user          | Adding same condition twice                      |
| `DUPLICATE_PLAN`              | 409             | Meal plan already exists for this date               | Multiple plans for same date                     |
| `RECOMMENDATION_FAILED`       | 422             | Recommendation engine could not generate a plan      | Engine rules cannot find matching foods          |
| `RATE_LIMIT_EXCEEDED`         | 429             | Too many requests in a short time                    | Exceeding rate limit for public endpoints        |
| `INTERNAL_ERROR`              | 500             | Unexpected server error                               | Unhandled exception, database connection issue   |

### 13.2 Standard Error Response Format

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "ERROR_CODE_HERE",
    "message": "Human-readable error description",
    "details": ["Optional array of specific error details"],
    "timestamp": "2026-07-27T10:30:00Z",
    "path": "/api/v1/requested/path"
  }
}
```

---

## 14. API Summary

### 14.1 Complete Endpoint Table

| **#** | **Module**      | **Method** | **Endpoint**                                      | **Auth**     | **Response**   |
| ----- | --------------- | ---------- | ------------------------------------------------- | ------------ | -------------- |
| 1     | Authentication  | POST       | `/api/v1/auth/register`                           | Public       | 201 Created    |
| 2     | Authentication  | POST       | `/api/v1/auth/login`                              | Public       | 200 OK         |
| 3     | Authentication  | GET        | `/api/v1/auth/validate`                           | Authenticated| 200 OK         |
| 4     | User Profile    | GET        | `/api/v1/users/profile`                           | Authenticated| 200 OK         |
| 5     | User Profile    | PUT        | `/api/v1/users/profile`                           | Authenticated| 200/201        |
| 6     | Health Vitals   | GET        | `/api/v1/health/vitals`                           | Authenticated| 200 OK         |
| 7     | Health Vitals   | PUT        | `/api/v1/health/vitals`                           | Authenticated| 200 OK         |
| 8     | Health Vitals   | GET        | `/api/v1/health/conditions`                       | Authenticated| 200 OK         |
| 9     | Health Vitals   | POST       | `/api/v1/health/conditions`                       | Authenticated| 201 Created    |
| 10    | Health Vitals   | DELETE     | `/api/v1/health/conditions/{conditionId}`         | Authenticated| 204 No Content |
| 11    | Goals           | GET        | `/api/v1/goals`                                   | Authenticated| 200 OK         |
| 12    | Goals           | PUT        | `/api/v1/goals`                                   | Authenticated| 200 OK         |
| 13    | Preferences     | GET        | `/api/v1/preferences`                             | Authenticated| 200 OK         |
| 14    | Preferences     | PUT        | `/api/v1/preferences`                             | Authenticated| 200 OK         |
| 15    | Meal Plans      | POST       | `/api/v1/meal-plans/generate`                     | Authenticated| 201 Created    |
| 16    | Meal Plans      | GET        | `/api/v1/meal-plans`                              | Authenticated| 200 OK         |
| 17    | Meal Plans      | GET        | `/api/v1/meal-plans/{planId}`                     | Authenticated| 200 OK         |
| 18    | Meal Plans      | GET        | `/api/v1/meal-plans/history`                      | Authenticated| 200 OK         |
| 19    | Food Diary      | POST       | `/api/v1/food-diary/entries`                      | Authenticated| 201 Created    |
| 20    | Food Diary      | GET        | `/api/v1/food-diary/entries`                      | Authenticated| 200 OK         |
| 21    | Food Diary      | DELETE     | `/api/v1/food-diary/entries/{entryId}`            | Authenticated| 204 No Content |
| 22    | Progress        | GET        | `/api/v1/progress/summary`                        | Authenticated| 200 OK         |
| 23    | Progress        | GET        | `/api/v1/progress/weight`                         | Authenticated| 200 OK         |
| 24    | Progress        | GET        | `/api/v1/progress/calories`                       | Authenticated| 200 OK         |
| 25    | Progress        | GET        | `/api/v1/progress/macros`                         | Authenticated| 200 OK         |
| 26    | Progress        | POST       | `/api/v1/progress/tracking`                       | Authenticated| 201 Created    |
| 27    | Food Catalog    | GET        | `/api/v1/foods/search`                            | Authenticated| 200 OK         |
| 28    | Food Catalog    | GET        | `/api/v1/foods/{foodId}`                          | Authenticated| 200 OK         |
| 29    | Food Catalog    | GET        | `/api/v1/foods/category/{category}`               | Authenticated| 200 OK         |
| 30    | Admin           | GET        | `/api/v1/admin/users`                             | Admin        | 200 OK         |
| 31    | Admin           | GET        | `/api/v1/admin/users/{userId}`                    | Admin        | 200 OK         |
| 32    | Admin           | PATCH      | `/api/v1/admin/users/{userId}/deactivate`         | Admin        | 200 OK         |
| 33    | Admin           | GET        | `/api/v1/admin/system/health`                     | Admin        | 200 OK         |
| 34    | Admin           | POST       | `/api/v1/admin/foods`                             | Admin        | 201 Created    |
| 35    | Admin           | PUT        | `/api/v1/admin/foods/{foodId}`                    | Admin        | 200 OK         |
| 36    | Admin           | DELETE     | `/api/v1/admin/foods/{foodId}`                    | Admin        | 204 No Content |

### 14.2 Endpoint Count by Module

| **Module**              | **Endpoints** | **Public** | **Authenticated** | **Admin** |
| ----------------------- | ------------- | ---------- | ----------------- | --------- |
| Authentication          | 3             | 2          | 1                 | 0         |
| User Profile            | 2             | 0          | 2                 | 0         |
| Health Vitals           | 5             | 0          | 5                 | 0         |
| Goals & Lifestyle       | 2             | 0          | 2                 | 0         |
| Food Preferences        | 2             | 0          | 2                 | 0         |
| Meal Plans              | 4             | 0          | 4                 | 0         |
| Food Diary              | 3             | 0          | 3                 | 0         |
| Progress Dashboard      | 5             | 0          | 5                 | 0         |
| Food Catalog            | 3             | 0          | 3                 | 0         |
| Admin                   | 7             | 0          | 0                 | 7         |
| **Total**               | **36**        | **2**      | **27**            | **7**     |

---

## Document Version History

| **Version** | **Date**     | **Author** | **Changes**          |
| ----------- | ------------ | ---------- | -------------------- |
| 1.0         | 2026-07-27   | Architect  | Initial draft        |

---

*End of Document — 05_API_CONTRACT.md*
