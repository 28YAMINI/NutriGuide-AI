package com.nutriguideai.dto.request;

import com.nutriguideai.enums.BudgetLevel;
import com.nutriguideai.enums.DietType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreferenceRequest {

    @NotNull(message = "Diet type is required")
    private DietType dietType;

    @NotNull(message = "Budget level is required")
    private BudgetLevel budgetLevel;

    @Size(max = 100, message = "Region must not exceed 100 characters")
    private String region;

    @Valid
    @Size(max = 10, message = "Maximum 10 allergies allowed")
    private List<
            @Size(max = 50, message = "Each allergy must not exceed 50 characters")
                    String
            > allergies;

    @Size(max = 1000, message = "Excluded foods must not exceed 1000 characters")
    private String excludedFoods;
}