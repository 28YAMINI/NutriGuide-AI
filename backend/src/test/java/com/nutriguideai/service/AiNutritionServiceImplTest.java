package com.nutriguideai.service;

import com.nutriguideai.ai.AiPromptBuilder;
import com.nutriguideai.ai.AiProvider;
import com.nutriguideai.ai.MealPlanFocus;
import com.nutriguideai.ai.UserHealthContext;
import com.nutriguideai.dto.request.AiChatRequest;
import com.nutriguideai.dto.request.MealPlanRequest;
import com.nutriguideai.entity.FoodPreference;
import com.nutriguideai.entity.Goal;
import com.nutriguideai.entity.HealthProfile;
import com.nutriguideai.entity.User;
import com.nutriguideai.exception.ResourceNotFoundException;
import com.nutriguideai.repository.FoodPreferenceRepository;
import com.nutriguideai.repository.GoalRepository;
import com.nutriguideai.repository.HealthProfileRepository;
import com.nutriguideai.repository.UserRepository;
import com.nutriguideai.service.impl.AiNutritionServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiNutritionServiceImplTest {

    @Mock UserRepository userRepository;
    @Mock HealthProfileRepository healthProfileRepository;
    @Mock GoalRepository goalRepository;
    @Mock FoodPreferenceRepository foodPreferenceRepository;
    @Mock AiPromptBuilder promptBuilder;
    @Mock AiProvider aiProvider;
    @Mock TargetCalculator targetCalculator;

    private AiNutritionServiceImpl service;
    private User user;

    @BeforeEach
    void setUp() {
        service = new AiNutritionServiceImpl(userRepository, healthProfileRepository,
                goalRepository, foodPreferenceRepository, promptBuilder, aiProvider, targetCalculator);
        user = User.builder().id(1L).email("alice@nutriguide.com").firstName("Alice").build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("alice@nutriguide.com", null, List.of()));
        when(userRepository.findByEmail("alice@nutriguide.com")).thenReturn(Optional.of(user));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void chat_returnsReplyFromProvider() {
        stubContext();
        when(promptBuilder.buildSystemPrompt(any(UserHealthContext.class))).thenReturn("sys");
        when(aiProvider.generateChat("sys", "What should I eat for dinner?")).thenReturn("Try dal and rice.");

        var response = service.chat(AiChatRequest.builder().message("What should I eat for dinner?").build());

        assertThat(response.getReply()).isEqualTo("Try dal and rice.");
        assertThat(response.getGeneratedAt()).isNotNull();
    }

    @Test
    void generateMealPlan_usesFocusAndDays() {
        stubContext();
        when(promptBuilder.buildSystemPrompt(any(UserHealthContext.class))).thenReturn("sys");
        when(promptBuilder.buildMealPlanUserPrompt(any(UserHealthContext.class), any(), any(), any())).thenReturn("prompt");
        when(aiProvider.generateChat("sys", "prompt")).thenReturn("# Day 1 ...");

        var response = service.generateMealPlan(
                MealPlanRequest.builder().days(7).mealsPerDay(4).focus(MealPlanFocus.WEEKLY).build());

        assertThat(response.getPlan()).startsWith("# Day 1");
        verify(promptBuilder).buildMealPlanUserPrompt(any(UserHealthContext.class), any(), any(), any());
    }

    @Test
    void chat_throwsWhenProfileMissing() {
        when(healthProfileRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.chat(AiChatRequest.builder().message("hi").build()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void chat_throwsWhenGoalMissing() {
        when(healthProfileRepository.findByUserId(1L)).thenReturn(Optional.of(healthProfile()));
        when(goalRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.chat(AiChatRequest.builder().message("hi").build()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ⚠️ ADAPT getters to your entities.
    private void stubContext() {
        when(healthProfileRepository.findByUserId(1L)).thenReturn(Optional.of(healthProfile()));
        when(goalRepository.findByUserId(1L)).thenReturn(Optional.of(Goal.builder().goalType("WEIGHT_LOSS").build()));
        when(foodPreferenceRepository.findByUserId(1L)).thenReturn(Optional.of(
                FoodPreference.builder().dietType("VEGETARIAN").budgetLevel("LOW").region("South India")
                        .allergies(List.of("Peanuts")).excludedFoods(List.of()).build()));
        when(targetCalculator.calculate(any(), any())).thenReturn(new TargetCalculator.Targets(2050, 1600, 120, 160, 50));
    }

    private HealthProfile healthProfile() {
        return HealthProfile.builder().age(28).gender("FEMALE").heightCm(165.0).weightKg(62.0)
                .activityLevel("MODERATE").sleepHours(7).waterIntakeLiters(2.0)
                .medicalConditions(List.of("Diabetes")).build();
    }
}