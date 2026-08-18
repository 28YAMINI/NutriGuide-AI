package com.nutriguideai.service.impl;

import com.nutriguideai.dto.request.FoodDiaryEntryRequest;
import com.nutriguideai.dto.response.DailyDiaryResponse;
import com.nutriguideai.dto.response.FoodDiaryEntryResponse;
import com.nutriguideai.entity.FoodDiaryEntry;
import com.nutriguideai.entity.FoodItem;
import com.nutriguideai.entity.User;
import com.nutriguideai.exception.BadRequestException;
import com.nutriguideai.exception.ResourceNotFoundException;
import com.nutriguideai.exception.UnauthorizedException;
import com.nutriguideai.repository.FoodDiaryEntryRepository;
import com.nutriguideai.repository.FoodItemRepository;
import com.nutriguideai.repository.UserRepository;
import com.nutriguideai.service.FoodDiaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FoodDiaryServiceImpl implements FoodDiaryService {

    private final FoodDiaryEntryRepository diaryRepository;
    private final FoodItemRepository foodItemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public FoodDiaryEntryResponse logEntry(FoodDiaryEntryRequest request) {
        User user = currentUser();
        LocalDate logDate = request.getLoggedDate() != null
                ? request.getLoggedDate()
                : LocalDate.now();

        if (logDate.isAfter(LocalDate.now())) {
            throw new BadRequestException("Cannot log food for a future date.");
        }

        // Auto-fill nutrition from FoodItem catalog if foodId provided
        Double calories = request.getCalories();
        Double protein = request.getProteinG();
        Double carbs = request.getCarbsG();
        Double fat = request.getFatG();
        String servingSize = request.getServingSize();
        double quantity = request.getQuantity();

        if (request.getFoodId() != null) {
            FoodItem food = foodItemRepository.findById(request.getFoodId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "FoodItem", "id", request.getFoodId()));
            // Scale per serving × quantity (foodItem nutrition is per serving)
            if (calories == null) calories = food.getCalories() * quantity;
            if (protein == null) protein = food.getProtein() * quantity;
            if (carbs == null) carbs = food.getCarbohydrates() * quantity;
            if (fat == null) fat = food.getFat() * quantity;
            if (servingSize == null || servingSize.isBlank()) servingSize = food.getServingSize();
            // Use catalog food name if not overridden
            if (request.getFoodName() == null || request.getFoodName().isBlank()) {
                request.setFoodName(food.getName());
            }
        } else {
            // Custom food — nutrition must be provided
            if (calories == null) calories = 0.0;
            if (protein == null) protein = 0.0;
            if (carbs == null) carbs = 0.0;
            if (fat == null) fat = 0.0;
        }

        FoodDiaryEntry entry = FoodDiaryEntry.builder()
                .userId(user.getId())
                .foodId(request.getFoodId())
                .foodName(request.getFoodName())
                .mealType(request.getMealType())
                .servingSize(servingSize)
                .quantity(quantity)
                .calories(calories)
                .proteinG(protein)
                .carbsG(carbs)
                .fatG(fat)
                .loggedDate(logDate)
                .notes(request.getNotes())
                .build();

        FoodDiaryEntry saved = diaryRepository.save(entry);
        log.info("Diary entry {} logged for user {}", saved.getId(), user.getEmail());
        return toResponse(saved);
    }

    @Override
    public DailyDiaryResponse getEntriesByDate(LocalDate date) {
        User user = currentUser();
        List<FoodDiaryEntry> entries = diaryRepository
                .findByUserIdAndLoggedDateOrderByCreatedAtDesc(user.getId(), date);

        List<FoodDiaryEntryResponse> responses = entries.stream()
                .map(this::toResponse)
                .toList();

        return DailyDiaryResponse.builder()
                .date(date)
                .entries(responses)
                .totalEntries(responses.size())
                .totalCalories(entries.stream().mapToDouble(FoodDiaryEntry::getCalories).sum())
                .totalProteinG(entries.stream().mapToDouble(e -> e.getProteinG() != null ? e.getProteinG() : 0).sum())
                .totalCarbsG(entries.stream().mapToDouble(e -> e.getCarbsG() != null ? e.getCarbsG() : 0).sum())
                .totalFatG(entries.stream().mapToDouble(e -> e.getFatG() != null ? e.getFatG() : 0).sum())
                .build();
    }

    @Override
    @Transactional
    public void deleteEntry(Long entryId) {
        User user = currentUser();
        FoodDiaryEntry entry = diaryRepository.findById(entryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "FoodDiaryEntry", "id", entryId));
        if (!entry.getUserId().equals(user.getId())) {
            throw new ResourceNotFoundException(
                    "FoodDiaryEntry", "id", entryId);
        }
        diaryRepository.deleteById(entryId);
        log.info("Diary entry {} deleted by user {}", entryId, user.getEmail());
    }

    private FoodDiaryEntryResponse toResponse(FoodDiaryEntry e) {
        return FoodDiaryEntryResponse.builder()
                .id(e.getId())
                .foodId(e.getFoodId())
                .foodName(e.getFoodName())
                .mealType(e.getMealType())
                .servingSize(e.getServingSize())
                .quantity(e.getQuantity())
                .calories(e.getCalories())
                .proteinG(e.getProteinG())
                .carbsG(e.getCarbsG())
                .fatG(e.getFatG())
                .loggedDate(e.getLoggedDate())
                .notes(e.getNotes())
                .createdAt(e.getCreatedAt())
                .build();
    }

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new UnauthorizedException("User is not authenticated");
        }
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "email", auth.getName()));
    }
}