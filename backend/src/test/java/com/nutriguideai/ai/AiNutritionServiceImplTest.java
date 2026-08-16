package com.nutriguideai.ai;

import com.nutriguideai.dto.request.MealPlanRequest;
import com.nutriguideai.dto.response.MealPlanResponse;
import com.nutriguideai.entity.User;
import com.nutriguideai.entity.UserGoal;
import com.nutriguideai.enums.ActivityLevel;
import com.nutriguideai.enums.Gender;
import com.nutriguideai.enums.PrimaryGoal;
import com.nutriguideai.exception.UnauthorizedException;
import com.nutriguideai.repository.UserGoalRepository;
import com.nutriguideai.repository.UserRepository;
import com.nutriguideai.service.impl.AiNutritionServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiNutritionServiceImplTest {

    private static final String EMAIL = "alice@nutriguide.com";

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserGoalRepository userGoalRepository;

    @Mock
    private AiProvider aiProvider;

    @InjectMocks
    private AiNutritionServiceImpl service;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void generateMealPlan_returnsPlanFromProvider() {
        authenticate(EMAIL);

        User user = User.builder()
                .id(1L)
                .email(EMAIL)
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

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(userGoalRepository.findByUserId(1L)).thenReturn(Optional.of(goal));
        when(aiProvider.generateChat(anyString(), anyString()))
                .thenReturn("Breakfast: oats with milk and fruit");

        MealPlanResponse response = service.generateMealPlan(MealPlanRequest.builder().build());

        assertThat(response.getPlan()).contains("Breakfast");
        assertThat(response.getTargets()).isNotNull();
        assertThat(response.getGeneratedAt()).isNotNull();
        verify(aiProvider).generateChat(anyString(), anyString());
    }

    @Test
    void generateMealPlan_throwsUnauthorized_whenNotAuthenticated() {
        // No SecurityContext set on purpose
        assertThatThrownBy(() -> service.generateMealPlan(MealPlanRequest.builder().build()))
                .isInstanceOf(UnauthorizedException.class);
    }

    private void authenticate(String email) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(email, null));
    }
}