package com.nutriguideai.service;

import com.nutriguideai.dto.request.FoodRequest;
import com.nutriguideai.dto.response.FoodResponse;
import com.nutriguideai.enums.FoodCategory;
import com.nutriguideai.enums.Region;

import java.util.List;

public interface FoodService {

    FoodResponse create(FoodRequest request);

    FoodResponse update(Long id, FoodRequest request);

    void delete(Long id);

    FoodResponse getById(Long id);

    List<FoodResponse> getAll(String keyword, FoodCategory category, Region region, Boolean vegetarian);
}