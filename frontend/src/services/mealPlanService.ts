import api from './api'

export type MealPlanFocus = 'DAILY' | 'WEEKLY' | 'GROCERY'

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
    generate(request: {
        days: number
        mealsPerDay: number
        focus: MealPlanFocus
    }): Promise<MealPlanDetailResponse> {
        return api
            .post<MealPlanDetailResponse>('/ai/meal-plan', request)
            .then((r) => r.data)
    },

    getByDate(date: string): Promise<MealPlanDetailResponse> {
        return api
            .get<MealPlanDetailResponse>('/meal-plans', {
                params: { date },
            })
            .then((r) => r.data)
    },

    getById(id: number): Promise<MealPlanDetailResponse> {
        return api
            .get<MealPlanDetailResponse>(`/meal-plans/${id}`)
            .then((r) => r.data)
    },

    getHistory(
        from: string,
        to: string,
    ): Promise<MealPlanHistoryResponse> {
        return api
            .get<MealPlanHistoryResponse>('/meal-plans/history', {
                params: { from, to },
            })
            .then((r) => r.data)
    },
}