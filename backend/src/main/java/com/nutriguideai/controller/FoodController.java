package com.nutriguideai.controller;

import com.nutriguideai.dto.request.FoodRequest;
import com.nutriguideai.dto.response.FoodResponse;
import com.nutriguideai.enums.FoodCategory;
import com.nutriguideai.enums.Region;
import com.nutriguideai.service.FoodService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/foods")
@RequiredArgsConstructor
public class FoodController {

    private final FoodService foodService;

    @GetMapping
    public List<FoodResponse> getAll(@RequestParam(required = false) String keyword,
                                     @RequestParam(required = false) FoodCategory category,
                                     @RequestParam(required = false) Region region,
                                     @RequestParam(required = false) Boolean vegetarian) {
        return foodService.getAll(keyword, category, region, vegetarian);
    }

    @GetMapping("/{id}")
    public FoodResponse getById(@PathVariable Long id) {
        return foodService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public FoodResponse create(@Valid @RequestBody FoodRequest request) {
        return foodService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public FoodResponse update(@PathVariable Long id, @Valid @RequestBody FoodRequest request) {
        return foodService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        foodService.delete(id);
        return ResponseEntity.noContent().build();
    }
}