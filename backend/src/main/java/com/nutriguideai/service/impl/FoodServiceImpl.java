package com.nutriguideai.service.impl;

import com.nutriguideai.dto.request.FoodRequest;
import com.nutriguideai.dto.response.FoodResponse;
import com.nutriguideai.entity.Food;
import com.nutriguideai.enums.FoodCategory;
import com.nutriguideai.enums.Region;
import com.nutriguideai.exception.DuplicateFoodException;
import com.nutriguideai.exception.FoodNotFoundException;
import com.nutriguideai.repository.FoodRepository;
import com.nutriguideai.service.FoodService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FoodServiceImpl implements FoodService {

    private final FoodRepository foodRepository;

    @Override
    @Transactional
    public FoodResponse create(FoodRequest request) {
        if (foodRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateFoodException("Food '" + request.getName() + "' already exists");
        }

        Food food = Food.builder()
                .name(request.getName().trim())
                .category(request.getCategory())
                .region(request.getRegion())
                .calories(request.getCalories())
                .protein(request.getProtein())
                .carbs(request.getCarbs())
                .fat(request.getFat())
                .fiber(request.getFiber())
                .vegetarian(request.getVegetarian())
                .vegan(request.getVegan())
                .allergens(request.getAllergens())
                .active(request.getActive() == null || request.getActive())
                .build();

        return toResponse(foodRepository.save(food));
    }

    @Override
    @Transactional
    public FoodResponse update(Long id, FoodRequest request) {
        Food food = foodRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new FoodNotFoundException("Food not found with id " + id));

        if (foodRepository.existsByNameIgnoreCaseAndIdNot(request.getName(), id)) {
            throw new DuplicateFoodException("Food '" + request.getName() + "' already exists");
        }

        food.setName(request.getName().trim());
        food.setCategory(request.getCategory());
        food.setRegion(request.getRegion());
        food.setCalories(request.getCalories());
        food.setProtein(request.getProtein());
        food.setCarbs(request.getCarbs());
        food.setFat(request.getFat());
        food.setFiber(request.getFiber());
        food.setVegetarian(request.getVegetarian());
        food.setVegan(request.getVegan());
        food.setAllergens(request.getAllergens());
        food.setActive(request.getActive() == null || request.getActive());

        return toResponse(food);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Food food = foodRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new FoodNotFoundException("Food not found with id " + id));
        food.setActive(false);
    }

    @Override
    public FoodResponse getById(Long id) {
        return toResponse(foodRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new FoodNotFoundException("Food not found with id " + id)));
    }

    @Override
    public List<FoodResponse> getAll(String keyword, FoodCategory category, Region region, Boolean vegetarian) {
        return foodRepository.search(keyword, category, region, vegetarian)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private FoodResponse toResponse(Food food) {
        return FoodResponse.builder()
                .id(food.getId())
                .name(food.getName())
                .category(food.getCategory())
                .region(food.getRegion())
                .calories(food.getCalories())
                .protein(food.getProtein())
                .carbs(food.getCarbs())
                .fat(food.getFat())
                .fiber(food.getFiber())
                .vegetarian(food.getVegetarian())
                .vegan(food.getVegan())
                .allergens(food.getAllergens())
                .active(food.getActive())
                .build();
    }
}