# 02 - SOFTWARE REQUIREMENTS SPECIFICATION (SRS)

# NutriGuide AI
### AI-Powered Personalized Nutrition & Meal Planning System

---

# 1. Introduction

## 1.1 Purpose

This Software Requirements Specification (SRS) defines the functional and non-functional requirements for NutriGuide AI. It serves as a reference for developers, testers, project stakeholders, and future contributors throughout the software development lifecycle.

---

## 1.2 Project Objective

The primary objective of NutriGuide AI is to provide personalized nutrition and meal recommendations using user health information, dietary preferences, medical conditions, and wellness goals.

The system aims to:

- Simplify healthy meal planning.
- Encourage healthier eating habits.
- Recommend affordable and personalized meals.
- Improve user awareness of nutritional intake.

---

## 1.3 Intended Audience

- Developers
- Project Guide
- Test Engineers
- Future Contributors
- End Users

---

# 2. User Roles

## 2.1 User

A registered individual who uses the application.

Responsibilities:

- Register
- Login
- Complete health profile
- View recommendations
- Track nutrition
- Update preferences

---

## 2.2 Administrator (Future)

Responsibilities:

- Manage food database
- Manage users
- View reports
- Monitor system health

---

# 3. Functional Requirements

## FR-001 User Registration

Description:

Users should be able to create an account.

Inputs

- Name
- Email
- Password

Expected Output

- Account created successfully.

Validation

- Email must be unique.
- Password should satisfy security rules.

Priority

High

---

## FR-002 User Login

Description

Users should log in using registered credentials.

Inputs

- Email
- Password

Expected Output

- JWT Token
- Successful authentication

Priority

High

---

## FR-003 User Profile

The system shall allow users to store:

- Name
- Age
- Gender
- Height
- Weight
- Activity Level
- Lifestyle

Priority

High

---

## FR-004 BMI Calculation

The system shall automatically calculate BMI whenever height or weight changes.

Formula

BMI = Weight (kg) / Height² (m)

Priority

High

---

## FR-005 Health Goals

Users shall select one or more goals:

- Weight Loss
- Weight Gain
- Muscle Gain
- Weight Maintenance

Priority

High

---

## FR-006 Dietary Preferences

Supported preferences:

- Vegetarian
- Vegan
- Non-Vegetarian
- Eggetarian

Priority

High

---

## FR-007 Medical Conditions

Users may optionally specify:

- Diabetes
- Thyroid
- PCOS
- Hypertension
- High Cholesterol

Priority

Medium

---

## FR-008 Food Allergies

Users may specify allergies such as:

- Milk
- Eggs
- Peanuts
- Seafood
- Gluten

Priority

Medium

---

## FR-009 Budget Selection

Users shall choose:

- Low
- Medium
- High

The recommendation engine should consider the selected budget.

Priority

Medium

---

## FR-010 AI Meal Recommendation

The system shall generate:

- Breakfast
- Lunch
- Dinner
- Snacks

Each recommendation shall include:

- Food Name
- Calories
- Protein
- Carbohydrates
- Fat
- Estimated Cost
- Reason for Recommendation

Priority

High

---

## FR-011 Daily Nutrition Summary

Display:

- Total Calories
- Protein
- Carbohydrates
- Fat

Priority

Medium

---

## FR-012 Meal History

Users shall view previously generated meal plans.

Priority

Medium

---

## FR-013 Dashboard

Dashboard shall display:

- BMI
- Daily Calories
- Goal Progress
- Recent Meal Plan

Priority

Medium

---

## FR-014 AI Nutrition Assistant

Users may ask nutrition-related questions.

Examples:

- What should I eat after a workout?
- Suggest a high-protein breakfast.
- Recommend meals under my budget.

Priority

Low (Phase 1 basic implementation)

---

# 4. Non-Functional Requirements

## Performance

- API response < 2 seconds
- Meal generation < 5 seconds

---

## Availability

System availability should be at least 99%.

---

## Scalability

Support future integration of AI models, wearable devices, and mobile applications.

---

## Reliability

The system should handle invalid input without crashing.

---

## Security

- JWT Authentication
- BCrypt Password Encryption
- Input Validation
- Secure REST APIs

---

## Usability

- Responsive UI
- Easy navigation
- Beginner-friendly interface

---

## Maintainability

- Modular architecture
- Clean code
- Layered design
- Reusable components

---

## Compatibility

Supported browsers:

- Chrome
- Edge
- Firefox

---

# 5. User Stories

### US-001

As a new user,

I want to register,

so that I can access personalized meal recommendations.

---

### US-002

As a registered user,

I want to log in securely,

so that my information remains private.

---

### US-003

As a user,

I want to update my health profile,

so that recommendations remain accurate.

---

### US-004

As a user,

I want personalized meal recommendations,

so that I can achieve my health goals.

---

### US-005

As a user,

I want to track my nutrition,

so that I know whether I am meeting my goals.

---

### US-006

As a user,

I want my meal plan to respect my budget,

so that I can follow it consistently.

---

# 6. Business Rules

- Every user must register before accessing personalized features.
- Email addresses must be unique.
- Passwords must be encrypted before storage.
- Meal recommendations must respect dietary preferences.
- Meal recommendations should avoid known allergies.
- Budget should influence meal selection.
- Medical conditions should be considered during recommendation generation.

---

# 7. Validation Rules

## Registration

- Name cannot be empty.
- Email must be valid.
- Password must contain at least 8 characters.

---

## Profile

- Age: 10–100 years
- Height: Positive value
- Weight: Positive value

---

## Health Goals

At least one goal must be selected.

---

## Preferences

Only supported dietary options may be selected.

---

# 8. Assumptions

- Users provide correct health information.
- Internet connection is available.
- Food nutritional information is accurate.
- Phase 1 uses rule-based recommendations.

---

# 9. Constraints

- Backend: Java 21
- Framework: Spring Boot
- Database: MySQL
- Frontend: React
- Authentication: JWT
- Build Tool: Maven

---

# 10. Out of Scope (Phase 1)

The following features are excluded from the first release:

- Online food ordering
- Grocery delivery
- Barcode scanning
- Wearable device integration
- AI image recognition
- Voice assistant
- Multi-language support
- Dietitian consultation
- Payment gateway

---

# 11. Acceptance Criteria

The project shall be considered complete when:

- Users can register and log in successfully.
- User profiles can be created and updated.
- BMI is calculated correctly.
- Personalized meal recommendations are generated.
- Nutrition summaries are displayed.
- Dashboard information is accurate.
- APIs return appropriate status codes.
- Authentication is secure.
- All major functional requirements are tested.

---

# 12. Future Scope

Future versions may include:

- Machine Learning recommendation engine
- Medical report analysis
- Recipe generation
- Grocery integration
- Mobile application
- Smartwatch integration
- Personalized notifications
- Doctor/Dietitian portal
- Voice-based nutrition assistant

---

# 13. Conclusion

This Software Requirements Specification establishes the complete set of functional and non-functional requirements for NutriGuide AI. It provides a structured foundation for architecture design, database modeling, API development, frontend implementation, testing, and deployment while ensuring the project remains scalable, secure, and maintainable.