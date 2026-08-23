package com.nutriguideai.controller;

import com.nutriguideai.dto.request.MealPlanRequest;
import com.nutriguideai.dto.response.MealPlanDetailResponse;
import com.nutriguideai.dto.response.MealPlanHistoryResponse;
import com.nutriguideai.dto.response.PagedResponse;
import com.nutriguideai.service.MealPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/meal-plans")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Meal Plans", description = "Meal planning and AI generation endpoints")
public class MealPlanController {

    private final MealPlanService mealPlanService;

    @PostMapping("/generate")
    @Operation(summary = "Generate meal plan", description = "Calls the AI service, persists/updates the plan for the user.")
    public ResponseEntity<MealPlanDetailResponse> generate(@Valid @RequestBody(required = false) MealPlanRequest request) {
        if (request == null) {
            request = MealPlanRequest.builder().days(1).mealsPerDay(3).build();
        }
        return ResponseEntity.ok(mealPlanService.generate(request));
    }

    @GetMapping("/today")
    @Operation(summary = "Get today's meal plan")
    public ResponseEntity<MealPlanDetailResponse> getToday() {
        return ResponseEntity.ok(mealPlanService.getByDate(LocalDate.now()));
    }

    @GetMapping("/date/{date}")
    @Operation(summary = "Get meal plan for a specific date (YYYY-MM-DD)")
    public ResponseEntity<MealPlanDetailResponse> getByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(mealPlanService.getByDate(date));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get meal plan by ID")
    public ResponseEntity<MealPlanDetailResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(mealPlanService.getById(id));
    }

    @GetMapping("/history")
    @Operation(summary = "Get meal plan history within a date range (max 7 days)")
    public ResponseEntity<MealPlanHistoryResponse> getHistory(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(mealPlanService.getHistory(from, to));
    }
    @GetMapping("/paged")
    @Operation(summary = "Get paginated meal plans")
    public ResponseEntity<PagedResponse<MealPlanDetailResponse>> getPagedPlans(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "planDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(mealPlanService.getPagedMealPlans(pageable));
    }
}
