package com.nutriguideai.ai;

import com.nutriguideai.dto.request.MealPlanRequest;
import com.nutriguideai.dto.response.MealPlanDetailResponse;

/**
 * Service contract for the AI-powered nutrition assistant.
 */
public interface AiNutritionService {

    MealPlanDetailResponse generateMealPlan(MealPlanRequest request);
}