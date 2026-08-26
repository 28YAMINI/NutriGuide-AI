package com.nutriguideai.service;

import com.nutriguideai.dto.request.FoodDiaryEntryRequest;
import com.nutriguideai.dto.response.DailyDiaryResponse;
import com.nutriguideai.dto.response.FoodDiaryEntryResponse;

import java.time.LocalDate;

public interface FoodDiaryService {

    FoodDiaryEntryResponse logEntry(FoodDiaryEntryRequest request);

    DailyDiaryResponse getEntriesByDate(LocalDate date);

    void deleteEntry(Long entryId);
}