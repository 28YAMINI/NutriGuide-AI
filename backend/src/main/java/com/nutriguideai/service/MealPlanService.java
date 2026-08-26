package com.nutriguideai.service;

import com.nutriguideai.dto.request.MealPlanRequest;
import com.nutriguideai.dto.response.MealPlanDetailResponse;
import com.nutriguideai.dto.response.MealPlanHistoryResponse;

import java.time.LocalDate;
public interface MealPlanService {

    MealPlanDetailResponse generate(MealPlanRequest request);

    MealPlanDetailResponse getByDate(LocalDate date);

    MealPlanDetailResponse getById(Long planId);

    MealPlanHistoryResponse getHistory(LocalDate from, LocalDate to);
}