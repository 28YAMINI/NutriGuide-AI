package com.nutriguideai.service;


import com.nutriguideai.dto.request.CreateFoodItemRequest;
import com.nutriguideai.dto.request.UpdateFoodItemRequest;
import com.nutriguideai.dto.response.FoodItemResponse;
import com.nutriguideai.enums.FoodCategory;

import java.util.List;

/**
 * Contract for food catalog operations.
 *
 * <p>Rule: this interface speaks only in DTOs — request DTOs in, response DTOs
 * out. The {@code FoodItem} entity is an implementation detail of the service
 * layer and never appears in signatures. Implementations must not be called by
 * controllers directly; the controller depends on this interface.</p>
 */
public interface FoodItemService {

    /** Creates a food item after validating uniqueness of the (normalized) name. */
    FoodItemResponse createFoodItem(CreateFoodItemRequest request);

    /** Returns the full food catalog, unmapped to response DTOs. */
    List<FoodItemResponse> getAllFoodItems();

    /** Returns one food item, throwing ResourceNotFoundException if absent. */
    FoodItemResponse getFoodItemById(Long id);

    /** Replaces a food item's fields entirely (PUT semantics), 404 if absent. */
    FoodItemResponse updateFoodItem(Long id, UpdateFoodItemRequest request);

    /** Deletes a food item, 404 if absent. No-op guard on unknown id. */
    void deleteFoodItem(Long id);

    /** All foods in a given category. */
    List<FoodItemResponse> getFoodItemsByCategory(FoodCategory category);

    /** Case-insensitive substring search on the food name. */
    List<FoodItemResponse> searchFoodItems(String name);
}