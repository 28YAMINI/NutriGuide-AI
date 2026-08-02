package com.nutriguideai.service.impl;



import com.nutriguideai.dto.request.CreateFoodItemRequest;
import com.nutriguideai.dto.request.UpdateFoodItemRequest;
import com.nutriguideai.dto.response.FoodItemResponse;
import com.nutriguideai.entity.FoodItem;
import com.nutriguideai.enums.FoodCategory;
import com.nutriguideai.exception.DuplicateFoodItemException;
import com.nutriguideai.exception.ResourceNotFoundException;
import com.nutriguideai.repository.FoodItemRepository;
import com.nutriguideai.service.FoodItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of the food catalog contract.
 *
 * <p>Business rules: names are trimmed and internally collapsed before any
 * comparison or persistence; duplicates are rejected case-insensitively on
 * both write paths (update excludes the item itself); optional text fields
 * are trimmed; reads are read-only transactions.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FoodItemServiceImpl implements FoodItemService {

    private final FoodItemRepository foodItemRepository;

    @Override
    @Transactional
    public FoodItemResponse createFoodItem(CreateFoodItemRequest request) {
        String normalizedName = normalizeFoodName(request.getName());

        if (foodItemRepository.existsByNameIgnoreCase(normalizedName)) {
            log.warn("Duplicate food name rejected on create: {}", normalizedName);
            throw new DuplicateFoodItemException(
                    "Food item '" + normalizedName + "' already exists");
        }

        FoodItem saved = foodItemRepository.save(mapToEntity(request, normalizedName));
        log.info("Food item created: id={}, name={}", saved.getId(), saved.getName());
        return FoodItemResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FoodItemResponse> getAllFoodItems() {
        return foodItemRepository.findAll().stream()
                .map(FoodItemResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FoodItemResponse getFoodItemById(Long id) {
        return FoodItemResponse.fromEntity(findFoodItemOrThrow(id));
    }

    @Override
    @Transactional
    public FoodItemResponse updateFoodItem(Long id, UpdateFoodItemRequest request) {
        FoodItem foodItem = findFoodItemOrThrow(id);
        String normalizedName = normalizeFoodName(request.getName());

        if (foodItemRepository.existsByNameIgnoreCaseAndIdNot(normalizedName, id)) {
            log.warn("Duplicate food name rejected on update: {}", normalizedName);
            throw new DuplicateFoodItemException(
                    "Food item '" + normalizedName + "' already exists");
        }

        applyFields(foodItem, request, normalizedName);

        // managed entity → Hibernate dirty checking flushes on commit; no save() needed
        log.info("Food item updated: id={}, name={}", id, normalizedName);
        return FoodItemResponse.fromEntity(foodItem);
    }

    @Override
    @Transactional
    public void deleteFoodItem(Long id) {
        FoodItem foodItem = findFoodItemOrThrow(id);
        foodItemRepository.delete(foodItem);
        log.info("Food item deleted: id={}, name={}", id, foodItem.getName());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FoodItemResponse> getFoodItemsByCategory(FoodCategory category) {
        return foodItemRepository.findByCategory(category).stream()
                .map(FoodItemResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FoodItemResponse> searchFoodItems(String name) {
        String normalized = normalizeFoodName(name);
        if (normalized == null || normalized.isEmpty()) {
            log.debug("Empty search term, returning empty result");
            return List.of();
        }
        return foodItemRepository.findByNameContainingIgnoreCase(normalized).stream()
                .map(FoodItemResponse::fromEntity)
                .toList();
    }

    // ── mapping helpers ──────────────────────────────────────────────

    /** Builds a new entity from a create request; name arrives already normalized. */
    private FoodItem mapToEntity(CreateFoodItemRequest request, String normalizedName) {
        return FoodItem.builder()
                .name(normalizedName)
                .description(normalize(request.getDescription()))
                .category(request.getCategory())
                .calories(request.getCalories())
                .protein(request.getProtein())
                .carbohydrates(request.getCarbohydrates())
                .fat(request.getFat())
                .fiber(request.getFiber())
                .servingSize(normalize(request.getServingSize()))
                .imageUrl(normalize(request.getImageUrl()))
                .vegetarian(request.getVegetarian())
                .build();
    }

    /** Copies update-request fields onto a managed entity; name arrives already normalized. */
    private void applyFields(FoodItem foodItem, UpdateFoodItemRequest request,
                             String normalizedName) {
        foodItem.setName(normalizedName);
        foodItem.setDescription(normalize(request.getDescription()));
        foodItem.setCategory(request.getCategory());
        foodItem.setCalories(request.getCalories());
        foodItem.setProtein(request.getProtein());
        foodItem.setCarbohydrates(request.getCarbohydrates());
        foodItem.setFat(request.getFat());
        foodItem.setFiber(request.getFiber());
        foodItem.setServingSize(normalize(request.getServingSize()));
        foodItem.setImageUrl(normalize(request.getImageUrl()));
        foodItem.setVegetarian(request.getVegetarian());
    }

    /** 404 lookup — every "not found" case flows through this single helper. */
    private FoodItem findFoodItemOrThrow(Long id) {
        return foodItemRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Food item not found: id={}", id);
                    return new ResourceNotFoundException("FoodItem", "id", id);
                });
    }

    /**
     * Name normalization: trims AND collapses internal whitespace runs,
     * so " Green   Apple " becomes "Green Apple". Collapsing before the
     * duplicate check means spacing variants can never create near-duplicates.
     */
    private String normalizeFoodName(String name) {
        if (name == null) {
            return null;
        }
        return name.trim().replaceAll("\\s+", " ");
    }

    /** Trim-only normalization for optional text fields (description, servingSize, imageUrl). */
    private String normalize(String value) {
        return value == null ? null : value.trim();
    }
}
