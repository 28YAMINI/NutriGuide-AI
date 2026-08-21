package com.nutriguideai.service.impl;

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
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiNutritionServiceImpl implements AiNutritionService {

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    @Value("${ai.gemini.api-key}")
    private String geminiApiKey;

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

    // ── Chat ──────────────────────────────────────────────────────────
    @Override
    public AiChatResponse chat(AiChatRequest request) {
        UserProfile profile = getCurrentUserProfile();
        String systemPrompt = buildChatSystemPrompt(profile);

        String aiText = callGemini(systemPrompt, request.getMessage());

        return AiChatResponse.builder()
                .reply(aiText)                          // ← field is "reply"
                .model("gemini-2.0-flash")
                .generatedAt(java.time.LocalDateTime.now())
                .build();
    }

    // ── Meal Plan ─────────────────────────────────────────────────────
    @Override
    public MealPlanDetailResponse generateMealPlan(MealPlanRequest request) {
        UserProfile profile = getCurrentUserProfile();
        if (profile == null) {
            throw new RuntimeException("Please complete your profile first");
        }

        String systemPrompt = buildMealPlanPrompt(profile, request);
        String userMessage = "Generate my meal plan for today.";
        String aiText = callGemini(systemPrompt, userMessage);

        // TODO: parse aiText into structured meals once Gemini returns
        //       consistent JSON.  For now, store the raw AI response as
        //       dietaryTips and use default macro targets.
        return MealPlanDetailResponse.builder()
                .planDate(LocalDate.now())
                .totalCalories(2000)
                .totalProtein(150)
                .totalCarbs(250)
                .totalFat(65)
                .waterIntake(2500)
                .dietaryTips(aiText)
                .items(List.of())                       // empty until parsing is added
                .build();
    }

    // ── Gemini HTTP call ──────────────────────────────────────────────
    private String callGemini(String systemInstruction, String userMessage) {
        String url = GEMINI_URL + "?key=" + geminiApiKey;

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", userMessage)
                        ))
                ),
                "systemInstruction", Map.of(
                        "parts", List.of(
                                Map.of("text", systemInstruction)
                        )
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, Map.class);

            // Extract text from Gemini response structure
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> candidates =
                    (List<Map<String, Object>>) response.getBody().get("candidates");
            @SuppressWarnings("unchecked")
            Map<String, Object> content =
                    (Map<String, Object>) candidates.get(0).get("content");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> parts =
                    (List<Map<String, Object>>) content.get("parts");

            return (String) parts.get(0).get("text");
        } catch (Exception e) {
            log.error("Gemini API call failed", e);
            return "Unable to generate response at this time. Please try again later.";
        }
    }

    // ── Prompt builders ───────────────────────────────────────────────
    private String buildChatSystemPrompt(UserProfile profile) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are NutriGuide AI, a personalized nutrition assistant. ");
        sb.append("Provide helpful, evidence-based nutrition advice. ");
        if (profile != null) {
            sb.append("The user is a ").append(profile.getGender()).append(", ");
            sb.append(profile.getAge()).append(" years old, ");
            sb.append(profile.getHeightCm()).append(" cm tall, ");
            sb.append(profile.getWeightKg()).append(" kg. ");
        }
        sb.append("Keep responses concise and practical.");
        return sb.toString();
    }

    private String buildMealPlanPrompt(UserProfile profile, MealPlanRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are NutriGuide AI, a personalized nutrition assistant.\n");
        sb.append("Generate a detailed daily meal plan with exactly ");
        sb.append(request.getMealsPerDay()).append(" meals spread across ");
        sb.append(request.getDays()).append(" day(s).\n\n");

        if (profile != null) {
            sb.append("User Profile:\n");
            sb.append("- Age: ").append(profile.getAge()).append("\n");
            sb.append("- Gender: ").append(profile.getGender()).append("\n");
            sb.append("- Height: ").append(profile.getHeightCm()).append(" cm\n");
            sb.append("- Weight: ").append(profile.getWeightKg()).append(" kg\n");
        }

        if (request.getFocus() != null) {
            sb.append("- Focus: ").append(request.getFocus()).append("\n");
        }

        sb.append("\nProvide meals with: food name, serving size, and approximate calories.\n");
        sb.append("Format as structured text with clear meal labels.");
        return sb.toString();
    }

    // ── User lookup ───────────────────────────────────────────────────
    private UserProfile getCurrentUserProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }

        // Adjust this based on how your JWT filter stores the user identifier.
        // Option A — if your JWT subject stores the user ID as a string:
        //     Long userId = Long.parseLong(auth.getName());
        //     return userProfileRepository.findByUserId(userId).orElse(null);
        //
        // Option B — if your JWT subject stores the email:
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .map(user -> userProfileRepository.findByUserId(user.getId()).orElse(null))
                .orElse(null);
    }
}