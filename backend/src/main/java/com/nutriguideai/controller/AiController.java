package com.nutriguideai.controller;

import com.nutriguideai.dto.request.AiChatRequest;
import com.nutriguideai.dto.request.MealPlanRequest;
import com.nutriguideai.dto.response.AiChatResponse;
import com.nutriguideai.dto.response.MealPlanDetailResponse;
import com.nutriguideai.service.AiNutritionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiNutritionService aiNutritionService;

    @PostMapping("/chat")
    public ResponseEntity<AiChatResponse> chat(@Valid @RequestBody AiChatRequest request) {
        return ResponseEntity.ok(aiNutritionService.chat(request));
    }

    @PostMapping("/meal-plan")
    public ResponseEntity<MealPlanDetailResponse> generateMealPlan(
            @Valid @RequestBody MealPlanRequest request) {
        return ResponseEntity.ok(aiNutritionService.generateMealPlan(request));
    }
}