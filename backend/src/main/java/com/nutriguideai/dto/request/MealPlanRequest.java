// backend/src/main/java/com/nutriguideai/dto/request/MealPlanRequest.java
package com.nutriguideai.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nutriguideai.enums.MealPlanFocus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealPlanRequest {

    @Min(1) @Max(7)
    @JsonProperty("days")
    private Integer days;

    @Min(2) @Max(6)
    @JsonProperty("mealsPerDay")
    private Integer mealsPerDay;

    @JsonProperty("focus")
    private MealPlanFocus focus;
}