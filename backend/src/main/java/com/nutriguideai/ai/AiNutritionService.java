package com.nutriguideai.ai;

import com.nutriguideai.dto.request.MealPlanRequest;
import com.nutriguideai.dto.response.MealPlanResponse;

/**
 * Service contract for the AI-powered nutrition assistant.
 */
public interface AiNutritionService {

    MealPlanResponse generateMealPlan(MealPlanRequest request);
}