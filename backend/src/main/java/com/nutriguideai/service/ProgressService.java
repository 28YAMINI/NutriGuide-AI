package com.nutriguideai.service;

import com.nutriguideai.dto.request.TrackingRequest;
import com.nutriguideai.dto.response.*;

public interface ProgressService {

    /** Dashboard overview: weight, BMI, streak, today's intake. */
    DashboardSummaryResponse getSummary();

    /** Weight line chart: data points for the last N days. */
    WeightTrendResponse getWeightTrend(Integer days);

    /** Calorie bar chart: consumed vs target for the last N days. */
    CalorieTrendResponse getCalorieTrend(Integer days);

    /** Macro doughnut: protein/carbs/fat consumed vs target. */
    MacroBreakdownResponse getMacros();

    /** Log daily tracking: weight, water, sleep, notes. */
    TrackingResponse trackDaily(TrackingRequest request);
}