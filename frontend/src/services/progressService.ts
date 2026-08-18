// frontend/src/services/progressService.ts
import api from './api'

export interface DashboardSummary {
    currentWeightKg: number | null
    bmi: number | null
    targetCalories: number | null
    caloriesConsumedToday: number | null
    waterIntakeMl: number | null
    sleepHours: number | null
    activeStreakDays: number
    lastTrackedDate: string | null
}

export interface WeightPoint {
    date: string
    weightKg: number
}

export interface WeightTrend {
    dataPoints: WeightPoint[]
    trendDirection: 'UP' | 'DOWN' | 'STABLE'
}

export interface CaloriePoint {
    date: string
    consumed: number
    target: number
}

export interface CalorieTrend {
    dataPoints: CaloriePoint[]
    averageConsumed: number
}

export interface MacroBreakdown {
    proteinConsumedG: number
    proteinTargetG: number
    proteinPercent: number
    carbsConsumedG: number
    carbsTargetG: number
    carbsPercent: number
    fatConsumedG: number
    fatTargetG: number
    fatPercent: number
}

export interface TrackingRequest {
    weightKg?: number
    waterIntakeMl?: number
    sleepHours?: number
    notes?: string
}

export interface TrackingResponse {
    id: number
    recordedDate: string
    weightKg: number | null
    bmi: number | null
    waterIntakeMl: number | null
    sleepHours: number | null
    notes: string | null
}

export const progressService = {
    getSummary(): Promise<DashboardSummary> {
        return api.get<DashboardSummary>('/progress/summary').then((r) => r.data)
    },

    getWeightTrend(days = 30): Promise<WeightTrend> {
        return api.get<WeightTrend>('/progress/weight', { params: { days } }).then((r) => r.data)
    },

    getCalorieTrend(days = 7): Promise<CalorieTrend> {
        return api.get<CalorieTrend>('/progress/calories', { params: { days } }).then((r) => r.data)
    },

    getMacros(): Promise<MacroBreakdown> {
        return api.get<MacroBreakdown>('/progress/macros').then((r) => r.data)
    },

    trackDaily(request: TrackingRequest): Promise<TrackingResponse> {
        return api.post<TrackingResponse>('/progress/tracking', request).then((r) => r.data)
    },
}