package com.nutriguideai.controller;

import com.nutriguideai.dto.request.MealPlanRequest;
import com.nutriguideai.dto.response.MealPlanDetailResponse;
import com.nutriguideai.dto.response.MealPlanHistoryResponse;
import com.nutriguideai.service.MealPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/meal-plans")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Meal Plans", description = "Generate and retrieve daily meal plans")
public class MealPlanController {

    private final MealPlanService mealPlanService;

    @PostMapping("/generate")
    @Operation(summary = "Generate today's meal plan",
            description = "Calls the AI service, persists the plan, enforces one plan per user per day.")
    public ResponseEntity<MealPlanDetailResponse> generate(@Valid @RequestBody MealPlanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mealPlanService.generate(request));
    }

    @GetMapping
    @Operation(summary = "Get the plan for a date",
            description = "Returns 404 if no plan exists for that date.")
    public ResponseEntity<MealPlanDetailResponse> getByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(mealPlanService.getByDate(date));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a plan by id",
            description = "Ownership enforced; returns 404 if not found or not owned.")
    public ResponseEntity<MealPlanDetailResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(mealPlanService.getById(id));
    }

    @GetMapping("/history")
    @Operation(summary = "Plan history",
            description = "Date range required, max 7 days, newest first.")
    public ResponseEntity<MealPlanHistoryResponse> getHistory(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(mealPlanService.getHistory(from, to));
    }
}