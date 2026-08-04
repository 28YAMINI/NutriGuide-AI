package com.nutriguideai.controller;


import com.nutriguideai.dto.request.CreateFoodItemRequest;
import com.nutriguideai.dto.request.UpdateFoodItemRequest;
import com.nutriguideai.dto.response.FoodItemResponse;
import com.nutriguideai.enums.FoodCategory;
import com.nutriguideai.service.FoodItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for the food catalog.
 *
 * <p>GET endpoints are public (anyone can browse foods). POST / PUT / DELETE
 * are ADMIN-only — enforced by URL rules in SecurityConfig AND by
 * {@code @PreAuthorize} here (defense in depth). No business logic lives
 * in this class; it delegates entirely to FoodItemService.</p>
 */
@RestController
@RequestMapping("/api/foods")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Food Item",
        description = "Food catalog — reads are public, writes require ADMIN")
public class FoodItemController {

    private final FoodItemService foodItemService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a food item",
            description = "Admin only. Rejects duplicate (case-insensitive) names.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Food item created"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token"),
            @ApiResponse(responseCode = "403", description = "Authenticated user is not an admin"),
            @ApiResponse(responseCode = "409", description = "Food name already exists")
    })
    public ResponseEntity<FoodItemResponse> createFoodItem(
            @Valid @RequestBody CreateFoodItemRequest request) {
        log.info("Creating food item: {}", request.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(foodItemService.createFoodItem(request));
    }

    @GetMapping
    @Operation(summary = "Get all food items",
            description = "Public. Returns the full catalog.")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Catalog retrieved"))
    public ResponseEntity<List<FoodItemResponse>> getAllFoodItems() {
        log.debug("Fetching all food items");
        return ResponseEntity.ok(foodItemService.getAllFoodItems());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a food item by id", description = "Public.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Food item found"),
            @ApiResponse(responseCode = "404", description = "Food item not found")
    })
    public ResponseEntity<FoodItemResponse> getFoodItemById(@PathVariable Long id) {
        log.debug("Fetching food item: id={}", id);
        return ResponseEntity.ok(foodItemService.getFoodItemById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a food item",
            description = "Admin only. Full replacement (PUT) — send the complete item.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Food item updated"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token"),
            @ApiResponse(responseCode = "403", description = "Authenticated user is not an admin"),
            @ApiResponse(responseCode = "404", description = "Food item not found"),
            @ApiResponse(responseCode = "409", description = "Food name already exists")
    })
    public ResponseEntity<FoodItemResponse> updateFoodItem(
            @PathVariable Long id,
            @Valid @RequestBody UpdateFoodItemRequest request) {
        log.info("Updating food item: id={}", id);
        return ResponseEntity.ok(foodItemService.updateFoodItem(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a food item", description = "Admin only.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Food item deleted"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token"),
            @ApiResponse(responseCode = "403", description = "Authenticated user is not an admin"),
            @ApiResponse(responseCode = "404", description = "Food item not found")
    })
    public ResponseEntity<Void> deleteFoodItem(@PathVariable Long id) {
        log.info("Deleting food item: id={}", id);
        foodItemService.deleteFoodItem(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Get food items by category",
            description = "Public. Category must be a valid FoodCategory value.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Food items found"),
            @ApiResponse(responseCode = "400", description = "Invalid category value")
    })
    public ResponseEntity<List<FoodItemResponse>> getFoodItemsByCategory(
            @PathVariable FoodCategory category) {
        log.debug("Fetching food items by category: {}", category);
        return ResponseEntity.ok(foodItemService.getFoodItemsByCategory(category));
    }

    @GetMapping("/search")
    @Operation(summary = "Search food items by name",
            description = "Public. Case-insensitive substring search; blank name returns an empty list.")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Search results"))
    public ResponseEntity<List<FoodItemResponse>> searchFoodItems(
            @RequestParam(name = "name", required = false) String name) {
        log.debug("Searching food items: name={}", name);
        return ResponseEntity.ok(foodItemService.searchFoodItems(name));
    }
}
