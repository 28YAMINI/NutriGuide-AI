package com.nutriguideai.controller;

import com.nutriguideai.config.StandardErrorResponses;
import com.nutriguideai.dto.request.TrackingRequest;
import com.nutriguideai.dto.response.*;
import com.nutriguideai.service.ProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Progress", description = "Dashboard data: weight trends, calorie tracking, macro breakdown.")
public class ProgressController {

    private final ProgressService progressService;

    @GetMapping("/summary")
    @Operation(summary = "Dashboard summary", description = "Current weight, BMI, calorie progress, streak.")
    @StandardErrorResponses
    public ResponseEntity<DashboardSummaryResponse> getSummary() {
        log.debug("Fetching dashboard summary");
        return ResponseEntity.ok(progressService.getSummary());
    }

    @GetMapping("/weight")
    @Operation(summary = "Weight trend", description = "Weight data points for line chart (default 30 days).")
    @StandardErrorResponses
    public ResponseEntity<WeightTrendResponse> getWeightTrend(
            @RequestParam(defaultValue = "30") Integer days) {
        log.debug("Fetching weight trend for {} days", days);
        return ResponseEntity.ok(progressService.getWeightTrend(days));
    }

    @GetMapping("/calories")
    @Operation(summary = "Calorie trend", description = "Consumed vs target for bar chart (default 7 days).")
    @StandardErrorResponses
    public ResponseEntity<CalorieTrendResponse> getCalorieTrend(
            @RequestParam(defaultValue = "7") Integer days) {
        log.debug("Fetching calorie trend for {} days", days);
        return ResponseEntity.ok(progressService.getCalorieTrend(days));
    }

    @GetMapping("/macros")
    @Operation(summary = "Macro breakdown", description = "Protein/carbs/fat consumed vs target.")
    @StandardErrorResponses
    public ResponseEntity<MacroBreakdownResponse> getMacros() {
        log.debug("Fetching macro breakdown");
        return ResponseEntity.ok(progressService.getMacros());
    }

    @PostMapping("/tracking")
    @Operation(summary = "Log daily tracking", description = "Save weight, water, sleep, notes for today.")
    @StandardErrorResponses
    public ResponseEntity<TrackingResponse> trackDaily(
            @Valid @RequestBody TrackingRequest request) {
        log.info("Tracking daily progress");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(progressService.trackDaily(request));
    }
}