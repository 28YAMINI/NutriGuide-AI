package com.nutriguideai.service;

import com.nutriguideai.dto.request.AiChatRequest;
import com.nutriguideai.dto.request.MealPlanRequest;
import com.nutriguideai.dto.response.AiChatResponse;
import com.nutriguideai.dto.response.MealPlanResponse;

/** Contract for AI-powered nutrition assistance. Identity comes from the JWT principal. */
public interface AiNutritionService {

    AiChatResponse chat(AiChatRequest request);

    MealPlanResponse generateMealPlan(MealPlanRequest request);
}