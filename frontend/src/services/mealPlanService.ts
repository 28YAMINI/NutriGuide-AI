// frontend/src/services/mealPlanService.ts
import api from './api'

export interface MealPlanResponse {
    plan: string
    targets: {
        dailyCalories: number
        proteinGrams: number
        carbsGrams: number
        fatGrams: number
    }
    generatedAt: string
}

export interface MealPlanDetailResponse {
    id: number
    planDate: string
    totalCalories: number
    totalProteinG: number
    totalCarbsG: number
    totalFatG: number
    plan: string
    generatedAt: string
}

export interface MealPlanHistoryResponse {
    plans: MealPlanDetailResponse[]
    total: number
}

export const mealPlanService = {
    /** POST /api/ai/meal-plan — generate a new AI meal plan. */
    generate(request: { days: number; mealsPerDay: number }): Promise<MealPlanResponse> {
        return api.post<MealPlanResponse>('/ai/meal-plan', request).then((r) => r.data)
    },

    /** GET /api/meal-plans?date= — get plan for a specific date. */
    getByDate(date: string): Promise<MealPlanDetailResponse> {
        return api.get<MealPlanDetailResponse>('/meal-plans', { params: { date } }).then((r) => r.data)
    },

    /** GET /api/meal-plans/{id} — get plan by id. */
    getById(id: number): Promise<MealPlanDetailResponse> {
        return api.get<MealPlanDetailResponse>(`/meal-plans/${id}`).then((r) => r.data)
    },

    /** GET /api/meal-plans/history?from=&to= — plan history. */
    getHistory(from: string, to: string): Promise<MealPlanHistoryResponse> {
        return api.get<MealPlanHistoryResponse>('/meal-plans/history', { params: { from, to } }).then((r) => r.data)
    },
}