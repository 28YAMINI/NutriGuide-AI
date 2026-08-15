package com.nutriguideai.ai;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AiPromptBuilderTest {

    private final AiPromptBuilder builder = new AiPromptBuilder();

    private UserHealthContext context() {
        return new UserHealthContext(
                "Alice", 28, "FEMALE", 165.0, 62.0,
                "MODERATE", "7", "2.0",
                List.of("Diabetes"),
                "WEIGHT_LOSS",
                2050.0, 1600.0, 120.0, 160.0, 50.0,
                "VEGETARIAN", "LOW", "South India",
                List.of("Peanuts"), List.of("Brinjal"));
    }

    @Test
    void buildSystemPrompt_embedsUserContext() {
        String prompt = builder.buildSystemPrompt(context());

        assertThat(prompt).contains("Alice", "165.0", "62.0", "WEIGHT_LOSS");
        assertThat(prompt).contains("Diabetes", "Peanuts", "South India", "LOW");
    }

    @Test
    void buildSystemPrompt_handlesEmptyLists() {
        UserHealthContext noConditions = new UserHealthContext(
                "Bob", 30, "MALE", 175.0, 80.0,
                "SEDENTARY", "6", "1.5",
                List.of(), "HEALTHY_LIFESTYLE",
                2000.0, 2000.0, 100.0, 250.0, 55.0,
                "NON_VEGETARIAN", "MEDIUM", "North India",
                List.of(), List.of());

        String prompt = builder.buildSystemPrompt(noConditions);

        assertThat(prompt).contains("none");
        assertThat(prompt).doesNotContain("null");
    }

    @Test
    void buildMealPlanUserPrompt_reflectsFocusAndDays() {
        assertThat(builder.buildMealPlanUserPrompt(context(), 7, 4, MealPlanFocus.WEEKLY))
                .contains("7-day", "4 meals");
        assertThat(builder.buildMealPlanUserPrompt(context(), 1, 3, MealPlanFocus.GROCERY))
                .contains("grocery list");
        assertThat(builder.buildMealPlanUserPrompt(context(), 1, 3, MealPlanFocus.DAILY))
                .contains("1-day");
    }
}