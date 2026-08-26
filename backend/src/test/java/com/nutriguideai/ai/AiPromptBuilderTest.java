package com.nutriguideai.ai;

import com.nutriguideai.dto.request.MealPlanRequest;
import com.nutriguideai.entity.User;
import com.nutriguideai.entity.UserGoal;
import com.nutriguideai.enums.ActivityLevel;
import com.nutriguideai.enums.Gender;
import com.nutriguideai.enums.PrimaryGoal;
import com.nutriguideai.service.TargetCalculator.Targets;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AiPromptBuilderTest {

    private final AiPromptBuilder promptBuilder = new AiPromptBuilder();

    @Test
    void buildSystemPrompt_coversRoleAndSafety() {
        String prompt = promptBuilder.buildSystemPrompt();

        assertThat(prompt).contains("nutrition", "medical advice");
    }

    @Test
    void buildMealPlanUserPrompt_containsProfileAndTargets() {
        User user = User.builder()
                .age(30)
                .gender(Gender.MALE)
                .height(175.0)
                .weight(70.0)
                .build();
        UserGoal goal = UserGoal.builder()
                .primaryGoal(PrimaryGoal.WEIGHT_LOSS)
                .activityLevel(ActivityLevel.MODERATE)
                .sleepHours(7.5)
                .waterIntakeMl(2500)
                .build();
        Targets targets = new Targets(24.5, 1648.8, 2555.6, 2044.0, 153.0, 204.0, 68.0);

        String prompt = promptBuilder.buildMealPlanUserPrompt(
                user, goal, targets, MealPlanRequest.builder().build());

        assertThat(prompt).contains("WEIGHT_LOSS", "2044", "175");
    }
}