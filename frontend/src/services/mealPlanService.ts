// frontend/src/services/mealPlanService.ts
import api from './api';

export interface MealPlanRequest {
    days: number;
    mealsPerDay: number;
    focus?: string;
}

export const mealPlanService = {
    // Gracefully return null on 404 instead of throwing a loud console error
    getByDate: async (dateStr: string) => {
        try {
            const response = await api.get(`/meal-plans?date=${dateStr}`);
            return response.data;
        } catch (error: any) {
            if (error.response && error.response.status === 404) {
                // No meal plan created yet for this date — completely normal state
                return null;
            }
            throw error;
        }
    },

    generate: async (data: MealPlanRequest) => {
        const response = await api.post('/meal-plans/generate', data);
        return response.data;
    },

    getAll: async () => {
        try {
            const response = await api.get('/meal-plans');
            return response.data || [];
        } catch (error) {
            return [];
        }
    },
};