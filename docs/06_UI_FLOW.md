# 06 — UI FLOW

| **Field**             | **Value**                                    |
| --------------------- | -------------------------------------------- |
| **Project Name**      | NutriGuide AI                                |
| **Document Title**    | UI Flow and Screen Design Document           |
| **Version**           | 1.0                                          |
| **Author**            | Software Architecture Team                   |
| **Created On**        | 2026-07-27                                   |
| **Last Updated**      | 2026-07-27                                   |
| **Framework**         | React 18, Tailwind CSS, React Router 6       |
| **Status**            | Draft                                        |
| **References**        | `05_API_CONTRACT.md`, `03_SYSTEM_ARCHITECTURE.md` |

---

## Table of Contents

1. [Introduction](#1-introduction)
2. [Navigation Flow](#2-navigation-flow)
3. [User Journeys](#3-user-journeys)
4. [Screen Inventory](#4-screen-inventory)
5. [Dashboard Layout](#5-dashboard-layout)
6. [Wireframe Descriptions](#6-wireframe-descriptions)
7. [Component Hierarchy](#7-component-hierarchy)
8. [Reusable Components](#8-reusable-components)
9. [Responsive Design Strategy](#9-responsive-design-strategy)
10. [Accessibility Guidelines](#10-accessibility-guidelines)
11. [Error Pages](#11-error-pages)
12. [Loading States](#12-loading-states)
13. [Empty States](#13-empty-states)
14. [Design System Tokens](#14-design-system-tokens)

---

## 1. Introduction

### 1.1 Purpose

This document defines the complete UI flow for NutriGuide AI. It describes navigation between screens, user journeys through the application, the layout structure, wireframe descriptions for every screen, component hierarchy, reusable UI components, and state management for loading, error, and empty states. It also covers responsive design strategy and accessibility guidelines.

### 1.2 Design Philosophy

| **Principle**           | **Application**                                              |
| ----------------------- | ------------------------------------------------------------ |
| **Clarity**             | Each screen has one primary action. No clutter.              |
| **Progressive Disclosure** | Health profile setup is a multi-step wizard, not one overwhelming form. |
| **Data-Driven**         | Charts and numbers always visible. No decorative graphics.   |
| **Consistency**         | Same layout pattern on every authenticated screen.           |
| **Feedback First**      | Every user action shows loading → success/error immediately. |

### 1.3 Target Screen Sizes

| **Breakpoint** | **Size**      | **Layout**      |
| -------------- | ------------- | --------------- |
| Mobile         | < 640px       | Single column   |
| Tablet         | 640px – 1024px| 2-column grid   |
| Desktop        | > 1024px      | Sidebar + main  |

---

## 2. Navigation Flow

### 2.1 Complete Navigation Map

```
                           ┌─────────────────────┐
                           │     Landing Page     │
                           │    (Public /)        │
                           └──────┬──────────────┘
                                  │ "Get Started" / "Login"
                                  ▼
              ┌─────────────────────────────────────┐
              │              Auth Pages              │
              │                                     │
              │   ┌────────────┐  ┌──────────────┐  │
              │   │   Login    │  │  Register    │  │
              │   │   /login   │  │  /register   │  │
              │   └─────┬──────┘  └──────┬───────┘  │
              │         │                 │          │
              │         └──────┬──────────┘          │
              │                │ Auth success         │
              └────────────────┼────────────────────┘
                              │
                ┌─────────────▼─────────────┐
                │    Onboarding Wizard      │
                │   (first login only)      │
                │                           │
                │  Step 1: Profile Form     │
                │  Step 2: Health Vitals    │
                │  Step 3: Goals & Lifestyle│
                │  Step 4: Food Preferences │
                └─────────────┬─────────────┘
                              │ Complete
                              ▼
    ┌─────────────────────────────────────────────────────────────────┐
    │                    AUTHENTICATED SHELL                          │
    │  ┌──────────┐  ┌────────────────────────────────────────────┐  │
    │  │          │  │                                            │  │
    │  │ Sidebar  │  │                 Main Content               │  │
    │  │          │  │                                            │  │
    │  │ ──────── │  │  ┌──────────┐  ┌─────────┐  ┌──────────┐  │  │
    │  │ Dashboard│  │  │Dashboard │  │ Meal    │  │ Food     │  │  │
    │  │          │  │  │  /       │  │ Plan    │  │ Diary    │  │  │
    │  │ ──────── │  │  │          │  │  /meal- │  │  /food-  │  │  │
    │  │ Meal Plan│  │  │ -Summary │  │  plan   │  │  diary   │  │  │
    │  │          │  │  │ -Charts  │  │          │  │          │  │  │
    │  │ ──────── │  │  │ -Streak  │  │ -Today's │  │ -Add Meal│  │  │
    │  │ Food     │  │  └──────────┘  │  Plan    │  │ -History │  │  │
    │  │ Diary    │  │                │ -Generate │  └──────────┘  │  │
    │  │          │  │                └─────────┘                  │  │
    │  │ ──────── │  │  ┌──────────┐  ┌──────────────────────────┐ │  │
    │  │ Progress │  │  │ Progress │  │        Profile           │ │  │
    │  │          │  │  │  /progress│  │  /profile                │ │  │
    │  │ ──────── │  │  │          │  │                          │ │  │
    │  │ Profile  │  │  │ -Weight  │  │ -Personal Info           │ │  │
    │  │          │  │  │ -Calorie │  │ -Health Vitals           │ │  │
    │  │ ──────── │  │  │ -Macro   │  │ -Goals & Lifestyle      │ │  │
    │  │ (Admin)  │  │  └──────────┘  │ -Food Preferences       │ │  │
    │  │          │  │                └──────────────────────────┘ │  │
    │  └──────────┘  └────────────────────────────────────────────┘  │
    └─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
                    ┌──────────────────┐
                    │   Profile Incomplete?  │
                    │   ──► Redirect to     │
                    │   Profile Page        │
                    └──────────────────────┘
```

### 2.2 Navigation Rules

| **Rule**                                      | **Implementation**                                   |
| --------------------------------------------- | ---------------------------------------------------- |
| Unauthenticated users → Landing or Auth       | `ProtectedRoute` component redirects to `/login`     |
| First login → Onboarding Wizard               | Check if profile exists; if not, redirect to wizard  |
| Logged in → Dashboard by default              | `/` route renders Dashboard for authenticated users  |
| Admin users → See "Admin" link in sidebar      | Role-based sidebar menu rendering                    |
| 404 → Custom Not Found page                   | Catch-all route in React Router                      |

---

## 3. User Journeys

### 3.1 Journey 1: New User Onboarding

```
Step 1: Landing Page
┌─────────────────────────────────────────────────────┐
│  [Logo] NutriGuide AI                               │
│                                                     │
│  "Your Personalized AI Nutrition Assistant"         │
│                                                     │
│  ┌───────────────────┐  ┌───────────────────┐      │
│  │  Get Started Free │  │  Sign In           │      │
│  └───────────────────┘  └───────────────────┘      │
└─────────────────────────┬───────────────────────────┘
                          │ Click "Get Started Free"
                          ▼
Step 2: Registration
┌─────────────────────────────────────────────────────┐
│  Create Your Account                                │
│                                                     │
│  Email        [_____________________________]       │
│  Password     [_____________________________]       │
│  First Name   [_____________________________]       │
│  Last Name    [_____________________________]       │
│                                                     │
│  ┌───────────────────┐                              │
│  │  Create Account   │                              │
│  └───────────────────┘                              │
│                                                     │
│  Already have an account? [Sign In]                 │
└─────────────────────────┬───────────────────────────┘
                          │ Account created → Auto-login
                          ▼
Step 3: Onboarding Wizard (Step 1 of 4)
┌─────────────────────────────────────────────────────┐
│  Let's Get to Know You          Step 1 of 4         │
│                                                     │
│  First Name   [John                      ]          │
│  Last Name    [Doe                       ]          │
│  Date of Birth[1995-03-15      ] [📅]              │
│  Gender       ○ Male  ● Female  ○ Other             │
│  Height (cm)  [175.0                        ]       │
│  Weight (kg)  [78.5                         ]       │
│                                                     │
│  ┌──────────┐  ┌───────────────────┐               │
│  │  Skip    │  │  Next →           │               │
│  └──────────┘  └───────────────────┘               │
└─────────────────────────┬───────────────────────────┘
                          │ Click "Next"
                          ▼
Step 4: Onboarding Wizard (Step 2 of 4) — Health Vitals
┌─────────────────────────────────────────────────────┐
│  Health Information (Optional)     Step 2 of 4      │
│                                                     │
│  Medical Conditions (select all that apply):        │
│  ☐ Diabetes     ☐ Hypertension     ☐ PCOD/PCOS     │
│  ☐ Thyroid      ☐ High Cholesterol ☐ Fatty Liver   │
│  ☐ Kidney Disease  ☐ Anemia        ☐ None           │
│                                                     │
│  Blood Pressure    [120 ] / [80  ] mmHg             │
│  Fasting Sugar     [92.0                    ] mg/dL │
│  HbA1c             [5.4                     ] %     │
│                                                     │
│  ┌──────────┐  ┌───────────────────┐               │
│  │  ← Back  │  │  Next →           │               │
│  └──────────┘  └───────────────────┘               │
└─────────────────────────┬───────────────────────────┘
                          │ Click "Next"
                          ▼
Step 5: Onboarding Wizard (Step 3 of 4) — Goals
┌─────────────────────────────────────────────────────┐
│  Your Goals & Lifestyle          Step 3 of 4        │
│                                                     │
│  What's your primary goal?                          │
│  ○ Weight Loss   ● Weight Gain   ○ Muscle Gain     │
│  ○ Maintenance   ○ Healthy Lifestyle               │
│                                                     │
│  Activity Level:  [Moderate Exercise (3-5x/week) ▼] │
│                                                     │
│  Average Sleep:   [7.5                      ] hrs   │
│  Daily Water:     [2500                    ] mL     │
│                                                     │
│  ┌──────────┐  ┌───────────────────┐               │
│  │  ← Back  │  │  Next →           │               │
│  └──────────┘  └───────────────────┘               │
└─────────────────────────┬───────────────────────────┘
                          │ Click "Next"
                          ▼
Step 6: Onboarding Wizard (Step 4 of 4) — Preferences
┌─────────────────────────────────────────────────────┐
│  Food Preferences               Step 4 of 4        │
│                                                     │
│  Diet Type:     [Vegetarian                    ▼]  │
│  Budget:        [Medium                       ▼]  │
│  Region:        [North India                ]       │
│                                                     │
│  Allergies:     [lactose] [Add +]                   │
│                 [peanuts]                            │
│                                                     │
│  Foods to exclude:                                  │
│  [Mushrooms, Okra                         ]         │
│                                                     │
│  ┌──────────┐  ┌───────────────────┐               │
│  │  ← Back  │  │  Complete Setup   │               │
│  └──────────┘  └───────────────────┘               │
└─────────────────────────┬───────────────────────────┘
                          │ Click "Complete Setup"
                          ▼
Step 7: Dashboard (First Visit)
┌─────────────────────────────────────────────────────┐
│  🎉 Welcome to NutriGuide AI!                       │
│                                                     │
│  Your personalized meal plan is ready!              │
│                                                     │
│  ┌───────────────────┐                              │
│  │  View My Meal Plan│                              │
│  └───────────────────┘                              │
│                                                     │
│  ┌───────┐  ┌───────────┐  ┌────────┐               │
│  │ 78.5  │  │  1,850    │  │  85g   │               │
│  │ Weight│  │  Calories │  │Protein │               │
│  └───────┘  └───────────┘  └────────┘               │
└─────────────────────────────────────────────────────┘
```

### 3.2 Journey 2: Daily Meal Plan

```
User opens app → Dashboard → Clicks "Today's Meal Plan"
    │
    ▼
┌───────────────────────────────────────────────────────┐
│  Today's Meal Plan        [Regenerate] [Customize]   │
│                                                       │
│  1850 / 2100 cal  ● Protein 85/105g ● Carbs 220/210g│
│  ● Fat 55/70g                                         │
│                                                       │
│  ┌─ BREAKFAST ──────────────────────────────────────┐ │
│  │  Oats (Rolled) — 1 bowl          350 cal         │ │
│  │  P: 12g  C: 45g  F: 14g                          │ │
│  └──────────────────────────────────────────────────┘ │
│                                                       │
│  ┌─ MORNING SNACK ──────────────────────────────────┐ │
│  │  Apple — 1 medium                  80 cal         │ │
│  │  P: 0.5g  C: 22g  F: 0.3g                        │ │
│  └──────────────────────────────────────────────────┘ │
│                                                       │
│  ┌─ LUNCH ──────────────────────────────────────────┐ │
│  │  Brown Rice with Dal — 1 plate    550 cal         │ │
│  │  P: 25g  C: 80g  F: 12g                           │ │
│  └──────────────────────────────────────────────────┘ │
│                                                       │
│  ┌─ EVENING SNACK ──────────────────────────────────┐ │
│  │  Mixed Nuts — 1 handful          170 cal          │ │
│  │  P: 5g  C: 6g  F: 15g                             │ │
│  └──────────────────────────────────────────────────┘ │
│                                                       │
│  ┌─ DINNER ─────────────────────────────────────────┐ │
│  │  Grilled Paneer Salad — 1 bowl   450 cal          │ │
│  │  P: 30g  C: 25g  F: 22g                            │ │
│  └──────────────────────────────────────────────────┘ │
│                                                       │
│  💡 Tips:                                             │
│  • Drink water 30 minutes before meals                │
│  • Include leafy greens in lunch                      │
│  • Avoid sugary drinks                                │
└───────────────────────────────────────────────────────┘
```

### 3.3 Journey 3: Logging Food in Diary

```
User opens app → Food Diary → Clicks "Add Entry"
    │
    ▼
┌───────────────────────────────────────────────────────┐
│  Log Meal Entry                                       │
│                                                       │
│  Meal: [Breakfast                    ▼]              │
│  Date: [2026-07-27       ] [📅]                       │
│  Time: [08:30                              ]          │
│                                                       │
│  Search Food: [oats________________________] 🔍       │
│                                                       │
│  ┌─ Search Results ────────────────────────────────┐  │
│  │  ● Oats (Rolled)   389 cal/100g  [Select]       │  │
│  │  ○ Oat Milk        47 cal/100g   [Select]       │  │
│  │  ○ Oatmeal Cookie  450 cal/100g  [Select]       │  │
│  └──────────────────────────────────────────────────┘  │
│                                                       │
│  Serving Size: [200         ] grams                   │
│                                                       │
│  ═══ Calculated Nutrition ═══                         │
│  Calories:     350                                     │
│  Protein:      12.5g   Carbs:  45.0g   Fat:  14.0g   │
│                                                       │
│  Notes: [Cooked with water and topped with almonds]   │
│                                                       │
│  ┌──────────────┐  ┌────────────────────┐             │
│  │  Cancel      │  │  ✓ Log Entry       │             │
│  └──────────────┘  └────────────────────┘             │
└───────────────────────────────────────────────────────┘
    │
    ▼
┌───────────────────────────────────────────────────────┐
│  Food Diary — July 27, 2026                           │
│                                                       │
│  Total: 1,850 cal  ● Protein 85g ● Carbs 220g ● Fat 55g│
│                                                       │
│  ┌─ BREAKFAST ──────────────────────────────────────┐ │
│  │  🥣 Oats (Rolled)      350 cal    08:30  [🗑]    │ │
│  └──────────────────────────────────────────────────┘ │
│                                                       │
│  ┌─ LUNCH ──────────────────────────────────────────┐ │
│  │  🍚 Brown Rice with Dal  550 cal    13:00  [🗑]  │ │
│  └──────────────────────────────────────────────────┘ │
│                                                       │
│  [+ Add Another Meal]                                 │
└───────────────────────────────────────────────────────┘
```

### 3.4 Journey 4: Progress Dashboard

```
User clicks "Progress" in sidebar
    │
    ▼
┌───────────────────────────────────────────────────────┐
│  Your Progress                    📅 [Last 30 Days ▼] │
│                                                       │
│  ┌── Weight Trend ────────────────────────────────┐   │
│  │                                                  │   │
│  │  82 ────○────○────○────○────○────○────○─── 78   │   │
│  │        Week 1  2   3   4   5   6   7   8         │   │
│  │                                                  │   │
│  │  ↓ 4.0 kg  •  Started: 82 kg  •  Current: 78 kg │   │
│  └──────────────────────────────────────────────────┘   │
│                                                       │
│  ┌── Calorie Trend (Last 7 Days) ──────────────────┐ │
│  │                                                    │ │
│  │  2000 ████  ██████  ████  ██████  █████          │ │
│  │  1500 ████  ██████  ████  ██████  █████  ████    │ │
│  │  1000 ████  ██████  ████  ██████  █████  ████    │ │
│  │       21st    22nd   23rd   24th   25th   26th    │ │
│  │       ▒ Target (2100)  █ Consumed                │ │
│  └──────────────────────────────────────────────────┘ │
│                                                       │
│  ┌── Macronutrient Split ──────────────────────────┐ │
│  │   [DOUGHNUT CHART]                                │ │
│  │   ● Protein 19%    85/105g (81%)                 │ │
│  │   ● Carbs   48%   220/210g (105%)                │ │
│  │   ● Fat     27%    55/70g  (79%)                 │ │
│  └──────────────────────────────────────────────────┘ │
│                                                       │
│  🔥 Streak: 5 days logging!                           │
└───────────────────────────────────────────────────────┘
```

---

## 4. Screen Inventory

### 4.1 Screen List

| **#** | **Screen**           | **Route**              | **Auth**     | **Purpose**                           |
| ----- | -------------------- | ---------------------- | ------------ | ------------------------------------- |
| 1     | Landing Page         | `/`                    | Public       | Marketing, CTA to sign up             |
| 2     | Login Page           | `/login`               | Public       | Email/password login                  |
| 3     | Register Page        | `/register`            | Public       | New user registration                 |
| 4     | Onboarding Wizard    | `/onboarding`          | Authenticated| Multi-step profile setup (first login)|
| 5     | Dashboard            | `/dashboard`           | Authenticated| Summary cards + charts                |
| 6     | Meal Plan Page       | `/meal-plan`           | Authenticated| Daily meal plan display + generate    |
| 7     | Food Diary Page      | `/food-diary`          | Authenticated| Log meals, view daily intake          |
| 8     | Progress Page        | `/progress`            | Authenticated| Weight, calorie, macro charts         |
| 9     | Profile Page         | `/profile`             | Authenticated| View/edit all profile data            |
| 10    | Admin Page           | `/admin`               | Admin        | User list, system health, food catalog|
| 11    | 404 Not Found        | `*`                    | Public       | Catch-all for unknown routes          |
| 12    | 403 Forbidden        | `/forbidden`           | Public       | Insufficient permissions              |

### 4.2 Screen Dependencies

| **Screen**          | **Prerequisites**                          | **Fallback If Missing**           |
| ------------------- | ------------------------------------------ | --------------------------------- |
| Landing             | None                                       | —                                 |
| Login/Register      | None                                       | —                                 |
| Onboarding          | Authenticated user, no profile             | Redirect to dashboard if done     |
| Dashboard           | Authenticated user                         | Redirect to login                 |
| Meal Plan           | Authenticated user, profile                | Redirect to onboarding wizard     |
| Food Diary          | Authenticated user                         | Redirect to login                 |
| Progress            | Authenticated user, at least 1 tracking    | Show empty state with CTA         |
| Profile             | Authenticated user                         | Redirect to login                 |
| Admin               | Authenticated user + ADMIN role            | Show 403 page                     |

---

## 5. Dashboard Layout

### 5.1 Authenticated Shell (Desktop)

```
┌─────────────────────────────────────────────────────────────────────┐
│  [Logo] NutriGuide AI                                        [👤]  │
│                                                           John Doe │
│  ┌──────┬────────────────────────────────────────────────────────┐ │
│  │      │                                                        │ │
│  │      │  ┌─── Summary Cards Row ────────────────────────────┐  │ │
│  │      │  │                                                    │  │ │
│  │      │  │  ┌────────┐  ┌────────┐  ┌────────┐  ┌────────┐ │  │ │
│  │      │  │  │ Weight │  │Calories│  │ Protein│  │ Water  │ │  │ │
│  │      │  │  │78.0 kg │  │1,850   │  │85g/105g│  │2.5/3.0L│ │  │ │
│  │      │  │  │▼ 4 kg  │  │of 2,100│  │  (81%) │  │ (83%) │ │  │ │
│  │      │  │  └────────┘  └────────┘  └────────┘  └────────┘ │  │ │
│  │      │  └────────────────────────────────────────────────────┘  │ │
│  │      │                                                        │ │
│  │      │  ┌─── Charts Row ────────────────────────────────────┐  │ │
│  │      │  │                                                    │  │ │
│  │ Dash │  │  ┌──────────────┐      ┌────────────────────┐    │  │ │
│  │ board│  │  │ Weight Trend │      │  Calorie Trend     │    │  │ │
│  │      │  │  │ [Line Chart] │      │  [Bar Chart]       │    │  │ │
│  │ ──── │  │  │ 82 → 78 kg  │      │  Consumed/Target   │    │  │ │
│  │ Meal │  │  └──────────────┘      └────────────────────┘    │  │ │
│  │ Plan │  │                                                    │  │ │
│  │      │  │  ┌──────────────┐      ┌────────────────────┐    │  │ │
│  │ ──── │  │  │ Macro Split  │      │  Today's Meals     │    │  │ │
│  │ Food │  │  │ [Doughnut]   │      │  Quick View        │    │  │ │
│  │ Diary│  │  │ P:19%C:48%F  │      │  🥣 Oats - 350    │    │  │ │
│  │      │  │  │              │      │  🍚 Rice - 550    │    │  │ │
│  │ ──── │  │  └──────────────┘      │  🥗 Salad - 450   │    │  │ │
│  │ Pro- │  │                        └────────────────────┘    │  │ │
│  │ gress│  └────────────────────────────────────────────────────┘  │ │
│  │      │                                                        │ │
│  │ ──── │  ┌─── Activity Row ──────────────────────────────────┐  │ │
│  │ Prof │  │                                                    │  │ │
│  │ ile  │  │  🔥 5-day streak!   📅 Next meal in 2 hours       │  │ │
│  │      │  │  [Generate Today's Plan]  [Log Today's Meals]     │  │ │
│  │ ──── │  └────────────────────────────────────────────────────┘  │ │
│  │Admin │                                                        │ │
│  │      │                                                        │ │
│  └──────┴────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────┘
```

### 5.2 Layout Structure

| **Region**           | **Component**            | **Width**        | **Description**                     |
| -------------------- | ------------------------ | ---------------- | ----------------------------------- |
| **Top Navigation**   | Navbar                   | 100%             | Logo, page title, user avatar       |
| **Left Sidebar**     | Sidebar                  | 240px (desktop)  | Navigation links (collapsible)      |
| **Main Content**     | Page content area        | Remaining width  | Rendered page component             |
| **Footer**           | Footer                   | 100%             | Links, copyright                    |

### 5.3 Sidebar Navigation Items

| **Icon** | **Label**      | **Route**         | **Role**      | **Show When**           |
| -------- | -------------- | ----------------- | ------------- | ----------------------- |
| 📊       | Dashboard      | `/dashboard`      | All           | Always                  |
| 🍽️       | Meal Plan      | `/meal-plan`      | All           | Always                  |
| 📝       | Food Diary     | `/food-diary`     | All           | Always                  |
| 📈       | Progress       | `/progress`       | All           | Always                  |
| 👤       | Profile        | `/profile`        | All           | Always                  |
| ⚙️       | Admin Panel    | `/admin`          | ADMIN only    | If user.role === 'ADMIN'|

---

## 6. Wireframe Descriptions

### 6.1 Landing Page

| **Element**            | **Description**                                              |
| ---------------------- | ------------------------------------------------------------ |
| **Hero Section**       | Full-width section with headline "Your Personal AI Nutritionist", subtitle about personalized meal plans, and two CTAs: "Get Started Free" (primary) and "Sign In" (secondary). Background: subtle gradient or illustration of healthy food. |
| **Features Section**   | 3-column grid showing key features: Personalized Plans, Track Progress, Budget-Friendly. Each with icon, title, short description. |
| **How It Works**       | 4-step horizontal process: Register → Enter Health Data → Get Meal Plan → Track Progress. Simple numbered steps with icons. |
| **Testimonials**       | (Optional) 2-3 user quotes about the app. Can be static placeholders for v1. |
| **CTA Section**        | "Ready to transform your eating habits?" with primary button "Start Your Journey". |
| **Footer**             | Logo, quick links, copyright. Minimal.                      |

### 6.2 Login Page

| **Element**            | **Description**                                              |
| ---------------------- | ------------------------------------------------------------ |
| **Header**             | App logo + "Welcome Back" heading + "Sign in to your account" subtitle. |
| **Form**               | Email input, Password input, "Remember me" checkbox, "Sign In" button (full width). |
| **Links**              | "Don't have an account? Sign Up" below form.                 |
| **Validation**         | Inline validation: red border + error text below invalid fields. |
| **Error State**        | Banner above form: "Invalid email or password. Please try again." with dismiss button. |
| **Loading State**      | Button shows spinner, all inputs disabled during submission. |

### 6.3 Register Page

| **Element**            | **Description**                                              |
| ---------------------- | ------------------------------------------------------------ |
| **Header**             | App logo + "Create Account" heading.                         |
| **Form**               | Email, Password (with strength indicator), Confirm Password, First Name, Last Name. |
| **Password Strength**  | Visual bar below password input: Weak / Medium / Strong with color coding. |
| **Password Rules**     | Small text listing: "At least 8 characters, 1 uppercase, 1 lowercase, 1 number, 1 special character." |
| **Button**             | "Create Account" — full width, disabled until form valid.    |
| **Loading State**      | Button shows spinner. Text changes to "Creating your account...". |
| **Success**            | Auto-redirect to onboarding after 1.5s.                     |

### 6.4 Onboarding Wizard

| **Element**            | **Description**                                              |
| ---------------------- | ------------------------------------------------------------ |
| **Progress Bar**       | Horizontal stepper: Profile → Health → Goals → Preferences. Current step highlighted. |
| **Step 1: Profile**    | First name, Last name, Date of birth (date picker), Gender (radio group), Height (cm), Weight (kg). |
| **Step 2: Health**     | Medical conditions (checkbox grid), Blood pressure (2 fields), Fasting sugar, HbA1c, Cholesterol. All fields optional. |
| **Step 3: Goals**      | Goal type (radio cards with icons), Activity level (dropdown), Sleep hours, Water intake (slider or number). |
| **Step 4: Preferences**| Diet type (dropdown), Budget (radio), Region (text), Allergies (tag input), Excluded foods (textarea). |
| **Navigation**         | "Back" (except step 1), "Next" (steps 1-3), "Complete" (step 4). "Skip" link on optional sections. |
| **Completion**         | Confirmation screen with celebration animation and "Go to Dashboard" button. |

### 6.5 Dashboard Page

| **Element**            | **Description**                                              |
| ---------------------- | ------------------------------------------------------------ |
| **Summary Cards Row** | 4 cards: Current Weight (with trend arrow), Calories Today (vs target), Protein (progress bar), Water Intake (progress bar). |
| **Weight Trend Chart** | Line chart showing weight over last 30 days. X-axis: dates. Y-axis: weight (kg). Tooltip on hover. |
| **Calorie Trend Chart**| Bar chart showing consumed vs target for last 7 days. Dual color: green (under), red (over). |
| **Macro Doughnut**    | Doughnut chart: Protein (blue), Carbs (green), Fat (orange). Shows percentage + grams below. |
| **Today's Meals**     | Quick list of today's meals from the plan. Each shows food name, calories, meal type. |
| **Streak Badge**      | "🔥 5-day streak!" badge with motivational message.         |
| **CTA Buttons**       | "📋 View Today's Meal Plan", "📝 Log Today's Meals".       |

### 6.6 Meal Plan Page

| **Element**            | **Description**                                              |
| ---------------------- | ------------------------------------------------------------ |
| **Header**             | "Today's Meal Plan" with date selector (previous day / today / next day arrows). |
| **Nutrition Summary**  | Compact bar showing total calories (1850/2100) + macro breakdown. Color-coded progress bars. |
| **Meal Cards**         | 5 collapsible cards (Breakfast, Morning Snack, Lunch, Evening Snack, Dinner). Each card shows: food name, serving size, calories, macro breakdown (P/C/F). |
| **Action Buttons**     | "🔄 Regenerate Plan" (generates new plan), "✏️ Customize" (edit individual meals). |
| **Dietary Tips**       | Expandable section at bottom showing 2-3 condition-specific tips. |
| **Empty State**        | If no plan for today: "No meal plan for today. Generate one!" with large "Generate Plan" button. |

### 6.7 Food Diary Page

| **Element**            | **Description**                                              |
| ---------------------- | ------------------------------------------------------------ |
| **Date Selector**      | Date picker with left/right arrows to navigate days.        |
| **Daily Summary**      | Compact cards showing total calories, protein, carbs, fat for the selected date. |
| **Meal Sections**      | Meals grouped by type (Breakfast, Lunch, Dinner, Snacks). Each entry: food name, calories, time, delete button. |
| **Add Entry Button**   | Floating "✚" button or "Add Meal" at bottom of each section. |
| **Add Entry Modal**    | Modal overlay: meal type dropdown, food search (typeahead with results), serving size input, auto-calculated nutrition, notes. |
| **Empty State**        | "No meals logged for this date. Start tracking!" with "Log Your First Meal" button. |

### 6.8 Progress Page

| **Element**            | **Description**                                              |
| ---------------------- | ------------------------------------------------------------ |
| **Time Range Selector**| Tabs: "7 Days", "30 Days", "90 Days".                        |
| **Weight Chart**       | Full-width line chart. X-axis: dates. Y-axis: weight (kg). Shows trend line. Annotations for min/max. |
| **Calorie Chart**      | Bar chart. Consumed vs target. Shows average line.          |
| **Macro Breakdown**    | Side-by-side: doughnut chart (split %) + table (consumed/target/percentage for P/C/F). |
| **BMI History**        | Small chart showing BMI over time with healthy range highlighted. |
| **Stats Cards**        | Row: Total weight change, Average daily calories, Best streak. |
| **Empty State**        | "Start tracking to see your progress! Log your first meal or weight entry." |

### 6.9 Profile Page

| **Element**            | **Description**                                              |
| ---------------------- | ------------------------------------------------------------ |
| **Tab Navigation**     | Tabs: "Personal Info", "Health Vitals", "Goals & Lifestyle", "Food Preferences". |
| **Tab 1: Personal**    | Editable form: first name, last name, date of birth, gender, height, weight. Save button. |
| **Tab 2: Health**      | Editable vitals form + condition list with add/remove.       |
| **Tab 3: Goals**       | Goal selector, activity level, sleep, water intake. Save button. |
| **Tab 4: Preferences** | Diet type, budget, region, allergies (tag input), excluded foods. Save button. |
| **Save Behavior**      | Each tab has its own "Save Changes" button. Success toast on save. |

### 6.10 Admin Page

| **Element**            | **Description**                                              |
| ---------------------- | ------------------------------------------------------------ |
| **Tab Navigation**     | Tabs: "Users", "Food Catalog", "System Health".              |
| **Users Tab**          | Paginated table: ID, Name, Email, Role, Status, Last Login, Actions (View/Deactivate). Search bar. |
| **Food Catalog Tab**   | Paginated table with search + add/edit/delete. Add/Edit opens modal form. |
| **System Health Tab**  | Cards: Total users, Active today, Plans generated, Diary entries, DB status, Cache status, Uptime, Last backup. |

---

## 7. Component Hierarchy

### 7.1 Component Tree

```
<App>
  <AuthProvider>
    <Router>
      <Routes>
        {/* Public Routes */}
        <Route path="/" element={<LandingPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/forbidden" element={<ForbiddenPage />} />
        <Route path="*" element={<NotFoundPage />} />

        {/* Authenticated Routes */}
        <Route element={<ProtectedRoute />}>
          <Route element={<AppShell />}>
            <Route path="/onboarding" element={<OnboardingWizard />} />
            <Route path="/dashboard" element={<DashboardPage />} />
            <Route path="/meal-plan" element={<MealPlanPage />} />
            <Route path="/food-diary" element={<FoodDiaryPage />} />
            <Route path="/progress" element={<ProgressPage />} />
            <Route path="/profile" element={<ProfilePage />} />

            {/* Admin Routes */}
            <Route element={<AdminRoute />}>
              <Route path="/admin" element={<AdminPage />} />
            </Route>
          </Route>
        </Route>
      </Routes>
    </Router>
  </AuthProvider>
</App>
```

### 7.2 Page → Component Mapping

| **Page**            | **Composed Of**                                              |
| ------------------- | ------------------------------------------------------------ |
| LandingPage         | Navbar, HeroSection, FeaturesGrid, HowItWorks, Testimonials, CtaSection, Footer |
| LoginPage           | AuthLayout, LoginForm, ValidationFeedback                    |
| RegisterPage        | AuthLayout, RegisterForm, PasswordStrengthBar, ValidationFeedback |
| OnboardingWizard    | StepperProgress, ProfileStep, HealthStep, GoalsStep, PreferencesStep, CompletionScreen |
| DashboardPage       | SummaryCardsRow, WeightTrendChart, CalorieBarChart, MacroDoughnutChart, TodayMealSummary, StreakBadge, ActionButtons |
| MealPlanPage        | DateSelector, NutritionSummaryBar, MealCard (×5), DietaryTipsSection, ActionButtons |
| FoodDiaryPage       | DateSelector, DailySummaryBar, MealSection (×4), EntryCard, AddEntryModal, FoodSearchInput |
| ProgressPage        | TimeRangeTabs, WeightLineChart, CalorieBarChart, MacroDoughnutChart, BmiMiniChart, StatsCardRow |
| ProfilePage         | ProfileTabs, PersonalInfoForm, HealthVitalsForm, ConditionList, GoalsForm, PreferencesForm |
| AdminPage           | AdminTabs, UsersTable, FoodCatalogTable, FoodFormModal, SystemHealthCards |

---

## 8. Reusable Components

### 8.1 Common Component Library

| **Component**          | **Props**                                    | **Purpose**                        |
| ---------------------- | -------------------------------------------- | ---------------------------------- |
| **Navbar**             | `userName`, `avatarUrl`, `onLogout`          | Top navigation bar                 |
| **Sidebar**            | `activeRoute`, `userRole`, `onNavigate`      | Left navigation menu               |
| **Footer**             | —                                            | Page footer                        |
| **AppShell**           | —                                            | Layout wrapper (sidebar + content) |
| **ProtectedRoute**     | `requiredRole`                               | Redirects unauthenticated users    |
| **LoadingSpinner**     | `size`, `color`, `label`                     | Generic loading indicator          |
| **SkeletonLoader**     | `variant` (card/table/chart/text), `count`   | Content skeleton placeholder       |
| **ErrorState**         | `message`, `onRetry`, `errorCode`            | Error display with retry button    |
| **EmptyState**         | `icon`, `title`, `description`, `actionLabel`, `onAction` | Empty data state       |
| **PageHeader**         | `title`, `subtitle`, `actions`               | Consistent page heading            |
| **Card**               | `title`, `children`, `className`, `onClick`  | Container card                     |
| **SummaryCard**        | `label`, `value`, `subtitle`, `trend`, `icon`| Dashboard metric card              |
| **Modal**              | `isOpen`, `onClose`, `title`, `children`     | Overlay modal                      |
| **Toast**              | `type` (success/error/info), `message`       | Notification toast                 |
| **Button**             | `variant` (primary/secondary/ghost/danger), `loading`, `disabled`, `onClick` | Action button |
| **Input**              | `label`, `error`, `type`, `placeholder`      | Form input                         |
| **Select**             | `label`, `options`, `value`, `onChange`      | Dropdown selector                  |
| **DatePicker**         | `value`, `onChange`, `min`, `max`            | Date selection                     |
| **TagInput**           | `tags`, `onAdd`, `onRemove`, `suggestions`   | Tag-based input (e.g., allergies)  |
| **ProgressBar**        | `value`, `max`, `color`, `showLabel`         | Horizontal progress bar            |
| **Badge**              | `variant`, `text`                            | Status badge                       |
| **Tooltip**            | `content`, `children`                        | Hover tooltip                      |
| **Tabs**               | `tabs[]`, `activeTab`, `onChange`            | Tab navigation                     |
| **Pagination**         | `page`, `totalPages`, `onPageChange`         | Page navigation                    |
| **ChartContainer**     | `title`, `height`, `children`                | Chart wrapper with consistent style|

### 8.2 Domain-Specific Components

| **Component**           | **Props**                                    | **Purpose**                        |
| ----------------------- | -------------------------------------------- | ---------------------------------- |
| **SummaryCardsRow**     | `data: { weight, calories, protein, water }` | Dashboard summary row              |
| **WeightTrendChart**    | `dataPoints[]`, `days`                       | Weight line chart                  |
| **CalorieBarChart**     | `dataPoints[]`, `days`                       | Calorie vs target bar chart        |
| **MacroDoughnutChart**  | `protein`, `carbs`, `fat`                    | Macro split doughnut chart         |
| **MealCard**            | `mealType`, `foodName`, `nutrition`, `serving` | Individual meal display card      |
| **NutritionSummaryBar** | `calories`, `protein`, `carbs`, `fat`, `targets` | Nutrition progress bar           |
| **MealSection**         | `mealType`, `entries[]`                      | Grouped meal entries in diary      |
| **EntryCard**           | `entry`, `onDelete`                          | Individual diary entry             |
| **AddEntryModal**       | `isOpen`, `onClose`, `onSave`               | Food logging modal                 |
| **FoodSearchInput**     | `onSelect`, `placeholder`                    | Typeahead food search              |
| **DateSelector**        | `date`, `onChange`, `arrows`                 | Date navigation with arrows        |
| **StepperProgress**     | `currentStep`, `totalSteps`, `labels[]`      | Multi-step progress indicator      |
| **PasswordStrengthBar** | `password`                                   | Password strength visual indicator |
| **StreakBadge**         | `streakDays`                                 | Gamification streak display        |
| **ConditionCheckboxGrid** | `selected[]`, `onChange`                  | Medical condition selector grid    |

---

## 9. Responsive Design Strategy

### 9.1 Breakpoint Strategy

| **Breakpoint** | **Width**      | **Layout**         | **Sidebar**       | **Cards**      | **Charts**       |
| -------------- | -------------- | ------------------ | ----------------- | -------------- | ---------------- |
| Mobile         | < 640px        | Single column      | Hidden (hamburger)| Stacked        | Full width       |
| Tablet         | 640–1024px     | 2-column grid      | Collapsed (icons) | 2 per row      | 50% width        |
| Desktop        | > 1024px       | Sidebar + content  | Full (240px)      | 4 per row      | Side-by-side     |

### 9.2 Mobile Adaptations

| **Desktop Feature**        | **Mobile Adaptation**                         |
| -------------------------- | --------------------------------------------- |
| Sidebar navigation         | Hamburger menu → slide-out drawer            |
| 4 summary cards in a row   | 2 cards per row, stacked                      |
| Charts side-by-side        | Full-width charts stacked vertically          |
| Table with many columns    | Horizontal scroll + column priority hiding    |
| Multi-column forms         | Single column, full-width inputs             |
| Modals                     | Full-screen bottom sheets                     |
| Date picker                | Native `<input type="date">`                  |

### 9.3 Responsive Breakpoint Rules

```
/* Tailwind breakpoints used throughout */

/* Mobile first — single column by default */

/* sm: 640px — tablet */
@media (min-width: 640px) {
  .card-grid { grid-template-columns: repeat(2, 1fr); }
}

/* md: 768px — small desktop */
@media (min-width: 768px) {
  .sidebar { display: block; width: 64px; }
  .card-grid { grid-template-columns: repeat(3, 1fr); }
}

/* lg: 1024px — desktop */
@media (min-width: 1024px) {
  .sidebar { width: 240px; }
  .card-grid { grid-template-columns: repeat(4, 1fr); }
  .charts-grid { grid-template-columns: repeat(2, 1fr); }
}

/* xl: 1280px — large desktop */
@media (min-width: 1280px) {
  .container { max-width: 1280px; margin: 0 auto; }
}
```

---

## 10. Accessibility Guidelines

### 10.1 WCAG 2.1 Compliance Targets

| **Principle**       | **Guideline**            | **Target Level** | **Implementation**                                   |
| ------------------- | ------------------------ | ---------------- | ---------------------------------------------------- |
| Perceivable         | Text alternatives        | A                | All images have `alt` text                           |
| Perceivable         | Adaptable                | AA               | Content retains meaning when linearized              |
| Perceivable         | Distinguishable          | AA               | Color is not the only way to convey information      |
| Operable            | Keyboard accessible      | AA               | All interactive elements reachable via Tab           |
| Operable            | Enough time              | AA               | No time limits on forms                              |
| Operable            | Navigable                | AA               | Skip to content link, logical tab order              |
| Understandable      | Readable                 | AA               | `lang` attribute on HTML element                     |
| Understandable      | Predictable              | AA               | Consistent navigation across pages                   |
| Understandable      | Input assistance         | AA               | Error messages identify and describe issues          |
| Robust              | Compatible               | AA               | Valid HTML, ARIA landmarks                           |

### 10.2 Specific Accessibility Requirements

| **Requirement**                          | **Implementation**                                   |
| ---------------------------------------- | ---------------------------------------------------- |
| **Color contrast**                       | Minimum 4.5:1 for normal text, 3:1 for large text.  |
| **Focus indicators**                     | Visible focus ring (2px offset) on all interactive elements. |
| **Form labels**                          | Every `<input>` has an associated `<label>`.         |
| **Error announcements**                  | Errors are announced via `aria-live="polite"`.       |
| **Chart accessibility**                  | Charts have `aria-label` describing the data. Data also available in table format. |
| **Skip navigation**                      | "Skip to main content" link at top of every page.    |
| **Screen reader support**                | ARIA landmarks: `banner`, `navigation`, `main`, `contentinfo`. |
| **Touch targets**                        | Minimum 44×44px for mobile tap targets.              |
| **Reduced motion**                       | `prefers-reduced-motion` media query disables animations. |

---

## 11. Error Pages

### 11.1 404 Not Found Page

```
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│                                                             │
│                    ┌─────────────────────┐                   │
│                    │   404               │                   │
│                    │   ┌─┐ ┌─┐ ┌─┐       │                   │
│                    │   │ │ │ │ │ │       │                   │
│                    │   └─┘ └─┘ └─┘       │                   │
│                    └─────────────────────┘                   │
│                                                             │
│            Oops! Page not found                              │
│                                                             │
│     The page you're looking for doesn't exist               │
│     or has been moved.                                      │
│                                                             │
│          ┌────────────────────────┐                         │
│          │  🏠 Go to Dashboard    │                         │
│          └────────────────────────┘                         │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 11.2 403 Forbidden Page

```
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│                    ┌─────────────────────┐                   │
│                    │   🔒                │                   │
│                    │   Access Denied     │                   │
│                    └─────────────────────┘                   │
│                                                             │
│            You don't have permission                         │
│            to access this page.                              │
│                                                             │
│     If you believe this is a mistake, please contact         │
│     your administrator.                                     │
│                                                             │
│          ┌────────────────────────┐                         │
│          │  🏠 Go to Dashboard    │                         │
│          └────────────────────────┘                         │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 11.3 Generic Error Page (500)

```
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│                    ┌─────────────────────┐                   │
│                    │   ⚠️                │                   │
│                    │   Something went     │                   │
│                    │   wrong             │                   │
│                    └─────────────────────┘                   │
│                                                             │
│            An unexpected error occurred.                     │
│            Our team has been notified.                       │
│                                                             │
│          ┌────────────────────────┐                         │
│          │  🔄 Try Again          │                         │
│          └────────────────────────┘                         │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 12. Loading States

### 12.1 Loading State Implementation

Every data-fetching component follows the same state machine:

```
┌──────────┐
│  IDLE    │  ──► Component rendered, no action yet
└────┬─────┘
     │ Data fetch initiated
     ▼
┌──────────┐
│ LOADING  │  ──► Show skeleton or spinner
└────┬─────┘
     │
     ├──► Success → ┌─────────┐
     │              │  DATA   │  ──► Render content
     │              └─────────┘
     │
     ├──► Error   → ┌─────────┐
     │              │  ERROR  │  ──► Error component
     │              └─────────┘
     │
     └──► Empty   → ┌─────────┐
                    │  EMPTY  │  ──► Empty state component
                    └─────────┘
```

### 12.2 Loading Patterns

| **Component**            | **Loading Pattern**      | **Details**                                              |
| ------------------------ | ------------------------ | -------------------------------------------------------- |
| **Dashboard Page**       | Skeleton grid            | 4 shimmer rectangles for summary cards, 2 shimmer chart areas |
| **Meal Plan Page**       | Skeleton cards           | 5 shimmer meal cards with placeholder text lines         |
| **Food Diary Page**      | Skeleton list            | 3-4 shimmer entry rows with shimmer summary bar          |
| **Progress Page**        | Skeleton charts          | 3 shimmer chart containers with animated gradient        |
| **Profile Page**         | Skeleton form             | Shimmer input fields matching the form layout             |
| **Admin Table**          | Skeleton table rows      | 5 shimmer rows with varying column widths                |
| **Button Click**         | Button spinner           | Button text replaced with spinner, inputs disabled       |
| **Onboarding Save**      | Full page loader         | Subtle overlay with spinner while processing             |

### 12.3 Skeleton Variants

```jsx
// Card Skeleton
<SkeletonLoader variant="card" count={4} />
// Output: 4 rectangular shimmer blocks

// Table Skeleton
<SkeletonLoader variant="table" count={5} />
// Output: 5 shimmer rows matching table column widths

// Chart Skeleton
<SkeletonLoader variant="chart" />
// Output: Rectangular shimmer area matching chart size

// Text Skeleton
<SkeletonLoader variant="text" count={3} />
// Output: 3 shimmer lines of text
```

---

## 13. Empty States

### 13.1 Empty States Inventory

| **Screen**          | **Condition**                    | **Empty State**                                              |
| ------------------- | -------------------------------- | ------------------------------------------------------------ |
| **Dashboard**       | No profile data                  | "Set up your profile to see your personalized dashboard!" with "Go to Profile" button |
| **Meal Plan**       | No plan generated for today      | "No meal plan for today. Generate one!" with large "Generate Plan" button |
| **Food Diary**      | No entries for selected date     | "No meals logged for this date. Start tracking!" with "Log Your First Meal" button |
| **Food Diary**      | No entries ever                  | "Welcome! Start tracking your meals to see your nutrition summary" with "Log a Meal" button |
| **Progress**        | No tracking data                 | "Start tracking to see your progress! Log your first meal or weight entry" with "Go to Diary" button |
| **Progress**        | No weight entries                | "Record your first weight to start tracking your progress" with "Add Weight" button |
| **Health Vitals**   | No vitals entered                | "No health data yet. Add your medical information for better meal recommendations." with "Add Health Vitals" button |
| **Search Results**  | No matching foods                | "No foods found for '[query]'. Try a different search term." |
| **Admin: Users**    | No users match search            | "No users found matching your search criteria."              |
| **Admin: Catalog**  | No foods in category             | "No food items in this category. Add one!" with "Add Food" button |

### 13.2 Empty State Component Design

Every empty state follows this consistent pattern:

```
┌──────────────────────────────────────────────┐
│                                              │
│           ┌──────────────────┐               │
│           │                  │               │
│           │   Large Icon     │               │
│           │   (Illustration) │               │
│           │                  │               │
│           └──────────────────┘               │
│                                              │
│           Title Text                         │
│           (e.g., "No meal plan yet")         │
│                                              │
│           Description text explaining        │
│           what the user should do next.      │
│                                              │
│           ┌──────────────────────┐           │
│           │  Call to Action     │           │
│           │  Button             │           │
│           └──────────────────────┘           │
│                                              │
└──────────────────────────────────────────────┘
```

---

## 14. Design System Tokens

### 14.1 Color Palette

| **Token**              | **Hex**     | **Usage**                                    |
| ---------------------- | ----------- | -------------------------------------------- |
| `color-primary`        | `#2D6A4F`   | Primary buttons, links, active states        |
| `color-primary-light`  | `#40916C`   | Hover states                                 |
| `color-primary-dark`   | `#1B4332`   | Active/pressed states                        |
| `color-secondary`      | `#52B788`   | Accent elements, badges                      |
| `color-background`     | `#FFFFFF`   | Page background                              |
| `color-surface`        | `#F8F9FA`   | Card and container backgrounds               |
| `color-text-primary`   | `#1A1A2E`   | Primary text                                 |
| `color-text-secondary` | `#6C757D`   | Secondary text, labels                       |
| `color-text-muted`     | `#ADB5BD`   | Placeholder text, disabled                   |
| `color-border`         | `#DEE2E6`   | Borders, dividers                            |
| `color-success`        | `#2D6A4F`   | Success indicators, progress complete         |
| `color-warning`        | `#E9C46A`   | Warning states, near-limit indicators         |
| `color-danger`         | `#E63946`   | Error states, delete buttons, danger zones    |
| `color-info`           | `#457B9D`   | Information banners, tips                     |

### 14.2 Typography

| **Token**              | **Value**      | **Usage**                                    |
| ---------------------- | -------------- | -------------------------------------------- |
| `font-family`          | `Inter, system-ui, sans-serif` | Body text, headings          |
| `font-size-xs`         | `0.75rem`      | Small labels, captions                       |
| `font-size-sm`         | `0.875rem`     | Body text, form inputs                       |
| `font-size-base`       | `1rem`         | Default body text                            |
| `font-size-lg`         | `1.125rem`     | Card titles, section headers                 |
| `font-size-xl`         | `1.25rem`      | Page subtitles                               |
| `font-size-2xl`        | `1.5rem`       | Page headings                                |
| `font-size-3xl`        | `1.875rem`     | Hero headings                                |
| `font-weight-normal`   | `400`          | Body text                                    |
| `font-weight-medium`   | `500`          | Labels, buttons                              |
| `font-weight-semibold` | `600`          | Card titles, section headers                 |
| `font-weight-bold`     | `700`          | Page headings                                |
| `line-height-normal`   | `1.5`          | Body text                                    |
| `line-height-tight`    | `1.25`         | Headings                                     |

### 14.3 Spacing

| **Token**  | **Value** | **Usage**                                    |
| ---------- | --------- | -------------------------------------------- |
| `space-1`  | `0.25rem` | Tiny gaps (4px)                              |
| `space-2`  | `0.5rem`  | Small gaps between related elements          |
| `space-3`  | `0.75rem` | Internal card padding                        |
| `space-4`  | `1rem`    | Standard padding, gap between sections       |
| `space-6`  | `1.5rem`  | Between form fields                          |
| `space-8`  | `2rem`    | Between major sections                       |
| `space-12` | `3rem`    | Page top/bottom padding                      |
| `space-16` | `4rem`    | Hero section padding                         |

### 14.4 Shadows

| **Token**          | **Value**                                  | **Usage**                     |
| ------------------ | ------------------------------------------ | ----------------------------- |
| `shadow-sm`        | `0 1px 2px rgba(0,0,0,0.05)`              | Subtle card shadow            |
| `shadow-md`        | `0 4px 6px rgba(0,0,0,0.07)`              | Elevated cards, modals        |
| `shadow-lg`        | `0 10px 15px rgba(0,0,0,0.1)`             | Dropdowns, floating elements  |
| `shadow-focus`     | `0 0 0 3px rgba(45,106,79,0.3)`          | Focus ring for inputs         |

### 14.5 Border Radius

| **Token**    | **Value**  | **Usage**                                    |
| ------------ | ---------- | -------------------------------------------- |
| `radius-sm`  | `0.25rem`  | Input fields, small elements                 |
| `radius-md`  | `0.5rem`   | Cards, buttons, modals                       |
| `radius-lg`  | `0.75rem`  | Larger containers                            |
| `radius-full`| `9999px`   | Badges, avatars, pills                       |

---

## Document Version History

| **Version** | **Date**     | **Author** | **Changes**          |
| ----------- | ------------ | ---------- | -------------------- |
| 1.0         | 2026-07-27   | Architect  | Initial draft        |

---

*End of Document — 06_UI_FLOW.md*
