package com.nutriguideai.service;

import com.nutriguideai.dto.request.AiChatRequest;
import com.nutriguideai.dto.request.MealPlanRequest;
import com.nutriguideai.dto.response.AiChatResponse;
import com.nutriguideai.dto.response.MealPlanDetailResponse;

public interface AiNutritionService {

    AiChatResponse chat(AiChatRequest request);

    MealPlanDetailResponse generateMealPlan(MealPlanRequest request);
}
