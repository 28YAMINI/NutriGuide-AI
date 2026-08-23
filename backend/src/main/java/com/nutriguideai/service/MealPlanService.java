package com.nutriguideai.service;

import com.nutriguideai.dto.request.MealPlanRequest;
import com.nutriguideai.dto.response.MealPlanDetailResponse;
import com.nutriguideai.dto.response.MealPlanHistoryResponse;
import com.nutriguideai.dto.response.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface MealPlanService {
    MealPlanDetailResponse generate(MealPlanRequest request);
    MealPlanDetailResponse getByDate(LocalDate date);
    MealPlanDetailResponse getById(Long id);
    MealPlanHistoryResponse getHistory(LocalDate from, LocalDate to);
    PagedResponse<MealPlanDetailResponse> getPagedMealPlans(Pageable pageable);
}