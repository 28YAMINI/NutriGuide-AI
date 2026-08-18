package com.nutriguideai.controller;

import com.nutriguideai.config.StandardErrorResponses;
import com.nutriguideai.dto.request.FoodDiaryEntryRequest;
import com.nutriguideai.dto.response.DailyDiaryResponse;
import com.nutriguideai.dto.response.FoodDiaryEntryResponse;
import com.nutriguideai.service.FoodDiaryService;
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
@RequestMapping("/api/food-diary")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Food Diary", description = "Log, view, and delete food diary entries.")
public class FoodDiaryController {

    private final FoodDiaryService foodDiaryService;

    @PostMapping("/entries")
    @Operation(summary = "Log a food entry", description = "Creates a new food diary entry for the current user.")
    @StandardErrorResponses
    public ResponseEntity<FoodDiaryEntryResponse> logEntry(
            @Valid @RequestBody FoodDiaryEntryRequest request) {
        log.info("Logging food entry: {}", request.getFoodName());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(foodDiaryService.logEntry(request));
    }

    @GetMapping("/entries")
    @Operation(summary = "Get entries by date", description = "Returns all entries for a date with daily totals.")
    @StandardErrorResponses
    public ResponseEntity<DailyDiaryResponse> getEntriesByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.debug("Fetching diary for {}", date);
        return ResponseEntity.ok(foodDiaryService.getEntriesByDate(date));
    }

    @DeleteMapping("/entries/{id}")
    @Operation(summary = "Delete a food entry", description = "Removes a food diary entry by id.")
    @StandardErrorResponses
    public ResponseEntity<Void> deleteEntry(@PathVariable Long id) {
        log.info("Deleting diary entry {}", id);
        foodDiaryService.deleteEntry(id);
        return ResponseEntity.noContent().build();
    }
}