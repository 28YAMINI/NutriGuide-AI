package com.nutriguideai.dto.response;

import com.nutriguideai.enums.FoodCategory;
import com.nutriguideai.enums.Region;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodResponse {

    private Long id;
    private String name;
    private FoodCategory category;
    private Region region;
    private Double calories;
    private Double protein;
    private Double carbs;
    private Double fat;
    private Double fiber;
    private Boolean vegetarian;
    private Boolean vegan;
    private String allergens;
    private Boolean active;
}