package com.nutriguideai.controller;

import com.nutriguideai.config.StandardErrorResponses;
import com.nutriguideai.dto.request.CreateFoodItemRequest;
import com.nutriguideai.dto.request.UpdateFoodItemRequest;
import com.nutriguideai.dto.response.FoodItemResponse;
import com.nutriguideai.enums.FoodCategory;
import com.nutriguideai.service.FoodItemService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
 * <p>
 * GET endpoints are public. POST / PUT / DELETE are ADMIN-only.
 * Authorization is enforced both by SecurityConfig and @PreAuthorize.
 * Business logic is delegated to FoodItemService.
 * </p>
 */
@RestController
@RequestMapping("/api/foods")
@RequiredArgsConstructor
@Slf4j
@Tag(
        name = "Foods",
        description = "Food catalog. Reads are public; writes require the ADMIN role."
)
public class FoodItemController {

    private final FoodItemService foodItemService;

    // ──────────────────────────────────────────────
    // POST /api/foods
    // ──────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Add a food",
            description = "Creates a new food item. ADMIN role required."
    )
    @StandardErrorResponses
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Food item created",
                    content = @Content(
                            schema = @Schema(implementation = FoodItemResponse.class)
                    )
            )
    })
    public ResponseEntity<FoodItemResponse> createFoodItem(
            @Valid @RequestBody CreateFoodItemRequest request) {

        log.info("Creating food item: {}", request.getName());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(foodItemService.createFoodItem(request));
    }

    // ──────────────────────────────────────────────
    // GET /api/foods
    // ──────────────────────────────────────────────

    @GetMapping
    @Operation(
            summary = "List all foods",
            description = "Returns the complete food catalog. Public endpoint."
    )
    @StandardErrorResponses
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "All foods",
                    content = @Content(
                            array = @ArraySchema(
                                    schema = @Schema(implementation = FoodItemResponse.class)
                            )
                    )
            )
    })
    public ResponseEntity<List<FoodItemResponse>> getAllFoodItems() {

        log.debug("Fetching all food items");

        return ResponseEntity.ok(foodItemService.getAllFoodItems());
    }

    // ──────────────────────────────────────────────
    // GET /api/foods/{id}
    // ──────────────────────────────────────────────

    @GetMapping("/{id}")
    @Operation(
            summary = "Get a food by id",
            description = "Returns a single food item by its id. Public endpoint."
    )
    @Parameter(
            name = "id",
            description = "Food id",
            required = true,
            example = "1"
    )
    @StandardErrorResponses
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The food",
                    content = @Content(
                            schema = @Schema(implementation = FoodItemResponse.class)
                    )
            )
    })
    public ResponseEntity<FoodItemResponse> getFoodItemById(
            @PathVariable Long id) {

        log.debug("Fetching food item: id={}", id);

        return ResponseEntity.ok(
                foodItemService.getFoodItemById(id)
        );
    }

    // ──────────────────────────────────────────────
    // PUT /api/foods/{id}
    // ──────────────────────────────────────────────

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Update a food",
            description = "Updates an existing food item. ADMIN role required."
    )
    @Parameter(
            name = "id",
            description = "Food id",
            required = true,
            example = "1"
    )
    @StandardErrorResponses
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Updated food",
                    content = @Content(
                            schema = @Schema(implementation = FoodItemResponse.class)
                    )
            )
    })
    public ResponseEntity<FoodItemResponse> updateFoodItem(
            @PathVariable Long id,
            @Valid @RequestBody UpdateFoodItemRequest request) {

        log.info("Updating food item: id={}", id);

        return ResponseEntity.ok(
                foodItemService.updateFoodItem(id, request)
        );
    }

    // ──────────────────────────────────────────────
    // DELETE /api/foods/{id}
    // ──────────────────────────────────────────────

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete a food",
            description = "Deletes a food item. ADMIN role required."
    )
    @Parameter(
            name = "id",
            description = "Food id",
            required = true,
            example = "1"
    )
    @StandardErrorResponses
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Deleted"
            )
    })
    public ResponseEntity<Void> deleteFoodItem(
            @PathVariable Long id) {

        log.info("Deleting food item: id={}", id);

        foodItemService.deleteFoodItem(id);

        return ResponseEntity.noContent().build();
    }

    // ──────────────────────────────────────────────
    // GET /api/foods/category/{category}
    // ──────────────────────────────────────────────

    @GetMapping("/category/{category}")
    @Operation(
            summary = "List foods by category",
            description = "Returns all food items belonging to the specified category."
    )
    @Parameter(
            name = "category",
            description = "Food category",
            required = true,
            schema = @Schema(implementation = FoodCategory.class),
            example = "FRUITS"
    )
    @StandardErrorResponses
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Foods in the category",
                    content = @Content(
                            array = @ArraySchema(
                                    schema = @Schema(implementation = FoodItemResponse.class)
                            )
                    )
            )
    })
    public ResponseEntity<List<FoodItemResponse>> getFoodItemsByCategory(
            @PathVariable FoodCategory category) {

        log.debug("Fetching food items by category: {}", category);

        return ResponseEntity.ok(
                foodItemService.getFoodItemsByCategory(category)
        );
    }

    // ──────────────────────────────────────────────
    // GET /api/foods/search
    // ──────────────────────────────────────────────

    @GetMapping("/search")
    @Operation(
            summary = "Search foods",
            description = "Searches food items by name. Search is case-insensitive."
    )
    @Parameter(
            name = "name",
            description = "Search term matched against the food name",
            required = false,
            example = "apple"
    )
    @StandardErrorResponses
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Matching foods",
                    content = @Content(
                            array = @ArraySchema(
                                    schema = @Schema(implementation = FoodItemResponse.class)
                            )
                    )
            )
    })
    public ResponseEntity<List<FoodItemResponse>> searchFoodItems(
            @RequestParam(name = "name", required = false) String name) {

        log.debug("Searching food items: name={}", name);

        return ResponseEntity.ok(
                foodItemService.searchFoodItems(name)
        );
    }
}