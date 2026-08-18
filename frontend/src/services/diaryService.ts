// frontend/src/services/diaryService.ts
import api from './api'

export type MealType = 'BREAKFAST' | 'LUNCH' | 'DINNER' | 'SNACKS'

export interface FoodDiaryEntryRequest {
    foodId?: number
    foodName: string
    mealType: MealType
    servingSize: string
    quantity: number
    calories?: number
    proteinG?: number
    carbsG?: number
    fatG?: number
    loggedDate?: string
    notes?: string
}

export interface FoodDiaryEntryResponse {
    id: number
    foodId: number | null
    foodName: string
    mealType: MealType
    servingSize: string
    quantity: number
    calories: number
    proteinG: number
    carbsG: number
    fatG: number
    loggedDate: string
    notes: string | null
    createdAt: string
}

export interface DailyDiaryResponse {
    date: string
    entries: FoodDiaryEntryResponse[]
    totalEntries: number
    totalCalories: number
    totalProteinG: number
    totalCarbsG: number
    totalFatG: number
}

export const diaryService = {
    /** POST /api/food-diary/entries — log a food entry. */
    logEntry(request: FoodDiaryEntryRequest): Promise<FoodDiaryEntryResponse> {
        return api.post<FoodDiaryEntryResponse>('/food-diary/entries', request).then((r) => r.data)
    },

    /** GET /api/food-diary/entries?date= — entries for a date. */
    getEntriesByDate(date: string): Promise<DailyDiaryResponse> {
        return api.get<DailyDiaryResponse>('/food-diary/entries', { params: { date } }).then((r) => r.data)
    },

    /** DELETE /api/food-diary/entries/{id}. */
    deleteEntry(id: number): Promise<void> {
        return api.delete(`/food-diary/entries/${id}`).then(() => undefined)
    },
}