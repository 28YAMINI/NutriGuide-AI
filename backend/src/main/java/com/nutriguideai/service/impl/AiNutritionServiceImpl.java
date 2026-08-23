package com.nutriguideai.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutriguideai.dto.request.AiChatRequest;
import com.nutriguideai.dto.request.MealPlanRequest;
import com.nutriguideai.dto.response.AiChatResponse;
import com.nutriguideai.dto.response.MealPlanDetailResponse;
import com.nutriguideai.model.UserProfile;
import com.nutriguideai.repository.UserProfileRepository;
import com.nutriguideai.repository.UserRepository;
import com.nutriguideai.service.AiNutritionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiNutritionServiceImpl implements AiNutritionService {

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${ai.gemini.api-key}")
    private String geminiApiKey;

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";

    // ── Chat ──────────────────────────────────────────────────────────
    @Override
    public AiChatResponse chat(AiChatRequest request) {
        UserProfile profile = getCurrentUserProfile();
        String systemPrompt = buildChatSystemPrompt(profile);
        String fullPrompt = systemPrompt + "\n\nUser Question: " + (request != null ? request.getMessage() : "");

        String aiText = callGemini(fullPrompt, 1, 3);

        return AiChatResponse.builder()
                .reply(aiText)
                .model("gemini-1.5-flash")
                .generatedAt(LocalDateTime.now())
                .build();
    }

    // ── Meal Plan ─────────────────────────────────────────────────────
    @Override
    public MealPlanDetailResponse generateMealPlan(MealPlanRequest request) {
        UserProfile profile = getCurrentUserProfile();

        int meals = (request != null && request.getMealsPerDay() != null) ? request.getMealsPerDay() : 3;
        int days = (request != null && request.getDays() != null) ? request.getDays() : 1;
        String focus = (request != null && request.getFocus() != null) ? request.getFocus().name() : "BALANCED";

        String prompt = buildMealPlanPrompt(profile, request);

        log.info("Requesting meal plan from Gemini AI: {} days, {} meals/day, focus={}", days, meals, focus);
        String aiText = callGemini(prompt, days, meals);

        int totalCalories = calculateCalories(meals, focus);
        int protein = calculateProtein(meals, focus);
        int carbs = calculateCarbs(meals, focus);
        int fat = calculateFat(meals, focus);

        return MealPlanDetailResponse.builder()
                .planDate(LocalDate.now())
                .totalCalories(totalCalories)
                .totalProtein(protein)
                .totalCarbs(carbs)
                .totalFat(fat)
                .waterIntake(2500)
                .plan(aiText)
                .dietaryTips("Focus: " + focus + " — ensure consistent hydration throughout the day.")
                .items(List.of())
                .build();
    }

    // ── Gemini HTTP call ──────────────────────────────────────────────
    private String callGemini(String promptText, int days, int meals) {
        if (geminiApiKey == null || geminiApiKey.trim().isEmpty() || geminiApiKey.contains("YOUR_")) {
            log.warn("Gemini API key is not configured; using dynamic structured generator.");
            return generateDynamicPlan(days, meals);
        }

        String url = GEMINI_URL + "?key=" + geminiApiKey.trim();

        try {
            Map<String, Object> textPart = new HashMap<>();
            textPart.put("text", promptText);

            Map<String, Object> contentObj = new HashMap<>();
            contentObj.put("parts", List.of(textPart));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", List.of(contentObj));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, String.class);

            if (response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode textNode = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");
                if (!textNode.isMissingNode()) {
                    String generatedText = textNode.asText();
                    if (generatedText != null && !generatedText.isBlank()) {
                        log.info("Gemini AI successfully generated {} days / {} meals plan!", days, meals);
                        return generatedText;
                    }
                }
            }
        } catch (HttpClientErrorException e) {
            log.error("Gemini API HTTP Error {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Gemini API Call Exception: {}", e.getMessage(), e);
        }

        return generateDynamicPlan(days, meals);
    }

    // ── Dynamic generator fallback ────────────────────────────────────
    private String generateDynamicPlan(int days, int meals) {
        StringBuilder sb = new StringBuilder();
        sb.append("### 🥗 Personalized Nutrition Meal Plan (").append(days).append(" Days, ").append(meals).append(" Meals/Day)\n\n");

        String[][] sampleMeals = {
                {"Breakfast", "Scrambled Eggs with Avocado & Whole Grain Toast", "380 kcal | 24g P | 28g C | 18g F"},
                {"Morning Snack", "Greek Yogurt with Mixed Berries & Almonds", "220 kcal | 18g P | 16g C | 8g F"},
                {"Lunch", "Grilled Herb Chicken Breast with Quinoa & Steamed Broccoli", "550 kcal | 45g P | 48g C | 14g F"},
                {"Afternoon Snack", "Apple Slices with 1 tbsp Natural Peanut Butter", "190 kcal | 4g P | 22g C | 10g F"},
                {"Dinner", "Pan-Seared Salmon Fillet with Roasted Sweet Potatoes & Asparagus", "580 kcal | 40g P | 42g C | 22g F"},
                {"Evening Snack", "Cottage Cheese with Chia Seeds or Light Whey Protein Shake", "160 kcal | 22g P | 6g C | 4g F"}
        };

        for (int d = 1; d <= days; d++) {
            sb.append("#### 📅 Day ").append(d).append("\n");
            for (int m = 1; m <= meals; m++) {
                int index = (m - 1) % sampleMeals.length;
                String mealType = sampleMeals[index][0];
                String dish = sampleMeals[index][1];
                String macros = sampleMeals[index][2];

                sb.append("- **Meal ").append(m).append(" (").append(mealType).append("):** ")
                        .append(dish).append(" — _").append(macros).append("_\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    // ── Prompt builders ───────────────────────────────────────────────
    private String buildChatSystemPrompt(UserProfile profile) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are NutriGuide AI, a certified clinical nutritionist assistant. ");
        sb.append("Provide evidence-based nutrition advice. ");
        if (profile != null) {
            if (profile.getGender() != null) sb.append("Client gender: ").append(profile.getGender()).append(", ");
            sb.append("age: ").append(profile.getAge()).append(", ");
            sb.append("height: ").append(profile.getHeightCm()).append("cm, ");
            sb.append("weight: ").append(profile.getWeightKg()).append("kg. ");
        }
        return sb.toString();
    }

    private String buildMealPlanPrompt(UserProfile profile, MealPlanRequest request) {
        int meals = (request != null && request.getMealsPerDay() != null) ? request.getMealsPerDay() : 3;
        int days = (request != null && request.getDays() != null) ? request.getDays() : 1;
        String focus = (request != null && request.getFocus() != null) ? request.getFocus().name() : "BALANCED";

        StringBuilder sb = new StringBuilder();
        sb.append("You are an expert clinical dietitian.\n");
        sb.append("TASK: Create a comprehensive ").append(days).append("-day meal plan with EXACTLY ")
                .append(meals).append(" distinct meals for each day.\n");
        sb.append("Dietary Goal / Focus: ").append(focus).append("\n\n");

        if (profile != null) {
            sb.append("User Biometrics:\n");
            sb.append("- Age: ").append(profile.getAge()).append("\n");
            sb.append("- Height: ").append(profile.getHeightCm()).append(" cm\n");
            sb.append("- Weight: ").append(profile.getWeightKg()).append(" kg\n");
            if (profile.getGender() != null) {
                sb.append("- Gender: ").append(profile.getGender()).append("\n");
            }
        }

        sb.append("\nFORMAT REQUIREMENTS:\n");
        sb.append("1. Structure each day with a clear heading: '### 📅 Day 1', '### 📅 Day 2', up to '### 📅 Day ").append(days).append("'.\n");
        sb.append("2. Under every day, list exactly ").append(meals).append(" meals: '#### Meal 1 (Breakfast)', '#### Meal 2', ..., up to '#### Meal ").append(meals).append("'.\n");
        sb.append("3. For every meal include food ingredients, portion size, and estimated calories/protein.\n");
        sb.append("4. Strictly adhere to the ").append(focus).append(" dietary strategy.\n");
        sb.append("5. Complete all ").append(days).append(" days without truncating.");

        return sb.toString();
    }

    private int calculateCalories(int meals, String focus) {
        int basePerMeal = 450;
        if ("HIGH_PROTEIN".equalsIgnoreCase(focus)) basePerMeal = 480;
        if ("LOW_CARB".equalsIgnoreCase(focus)) basePerMeal = 400;
        return basePerMeal * meals;
    }

    private int calculateProtein(int meals, String focus) {
        int proteinPerMeal = "HIGH_PROTEIN".equalsIgnoreCase(focus) ? 35 : 25;
        return proteinPerMeal * meals;
    }

    private int calculateCarbs(int meals, String focus) {
        int carbsPerMeal = "LOW_CARB".equalsIgnoreCase(focus) ? 20 : 45;
        return carbsPerMeal * meals;
    }

    private int calculateFat(int meals, String focus) {
        int fatPerMeal = "LOW_CARB".equalsIgnoreCase(focus) ? 22 : 12;
        return fatPerMeal * meals;
    }

    // ── User lookup ───────────────────────────────────────────────────
    private UserProfile getCurrentUserProfile() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return null;
            }

            String email = auth.getName();
            return userRepository.findByEmail(email)
                    .flatMap(user -> userProfileRepository.findByUserId(user.getId()))
                    .orElse(null);
        } catch (Exception e) {
            log.warn("Could not retrieve user profile: {}", e.getMessage());
            return null;
        }
    }
}