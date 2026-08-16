package com.nutriguideai.service.impl;

import com.nutriguideai.dto.request.GoalRequest;
import com.nutriguideai.dto.response.GoalResponse;
import com.nutriguideai.entity.Goal;
import com.nutriguideai.entity.User;
import com.nutriguideai.entity.UserGoal;
import com.nutriguideai.enums.ActivityLevel;
import com.nutriguideai.enums.Gender;
import com.nutriguideai.enums.PrimaryGoal;
import com.nutriguideai.enums.Role;
import com.nutriguideai.exception.ResourceNotFoundException;
import com.nutriguideai.exception.UnauthorizedException;
import com.nutriguideai.repository.UserGoalRepository;
import com.nutriguideai.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GoalServiceImplTest {

    private static final Long USER_ID = 1L;
    private static final String USER_EMAIL = "alice@nutriguide.com";

    private UserGoalRepository userGoalRepository;
    private UserRepository userRepository;
    private GoalServiceImpl goalService;
    private Goal.GoalType parseGoalType(String value) {
        try {
            return Goal.GoalType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown goalType: " + value);
        }
    }

    @BeforeEach
    void setUp() {
        userGoalRepository = mock(UserGoalRepository.class);
        userRepository = mock(UserRepository.class);
        goalService = new GoalServiceImpl(userGoalRepository, userRepository);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void upsertGoalsCreatesNewRecordWithCalculatedTargets() {
        authenticate(fullProfile());

        when(userGoalRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
        when(userGoalRepository.save(any(UserGoal.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        GoalResponse response = goalService.upsertGoals(
                GoalRequest.builder() != null ? GoalRequest.builder()
                        .primaryGoal(PrimaryGoal.WEIGHT_LOSS)
                        .activityLevel(ActivityLevel.MODERATE)
                        .sleepHours(7.5)
                        .waterIntakeMl(2500)
                        .build() : null);

        assertThat(response.getUserId()).isEqualTo(USER_ID);
        assertThat(response.getPrimaryGoal()).isEqualTo(PrimaryGoal.WEIGHT_LOSS);
        assertThat(response.getActivityLevel()).isEqualTo(ActivityLevel.MODERATE);
        assertThat(response.getSleepHours()).isEqualTo(7.5);
        assertThat(response.getWaterIntakeMl()).isEqualTo(2500);
        // 30y male / 175cm / 70kg, moderate, weight loss -> 2044 kcal, 30/40/30 split
        assertThat(response.getTargetCalories()).isEqualTo(2044);
        assertThat(response.getTargetProteinG()).isEqualTo(153);
        assertThat(response.getTargetCarbsG()).isEqualTo(204);
        assertThat(response.getTargetFatG()).isEqualTo(68);
        verify(userGoalRepository).save(any(UserGoal.class));
    }

    @Test
    void upsertGoalsUpdatesExistingRecord() {
        authenticate(fullProfile());

        when(userGoalRepository.findByUserId(USER_ID))
                .thenReturn(Optional.of(goal(10L, fullProfile(),
                        PrimaryGoal.MAINTENANCE, ActivityLevel.SEDENTARY,
                        null, null, null, null, null, null)));
        when(userGoalRepository.save(any(UserGoal.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        GoalResponse response = goalService.upsertGoals(
                GoalRequest.builder()
                        .primaryGoal(PrimaryGoal.WEIGHT_LOSS)
                        .activityLevel(ActivityLevel.MODERATE)
                        .build());

        assertThat(response.getPrimaryGoal()).isEqualTo(PrimaryGoal.WEIGHT_LOSS);
        assertThat(response.getActivityLevel()).isEqualTo(ActivityLevel.MODERATE);
        assertThat(response.getTargetCalories()).isEqualTo(2044);
    }

    @Test
    void upsertGoalsKeepsExistingTargetsWhenProfileIncomplete() {
        authenticate(user(USER_ID, USER_EMAIL, Role.USER, 30, Gender.MALE, 175.0, null));

        when(userGoalRepository.findByUserId(USER_ID))
                .thenReturn(Optional.of(goal(10L, fullProfile(),
                        PrimaryGoal.MAINTENANCE, ActivityLevel.SEDENTARY,
                        2000, 150, 200, 67, 7.0, 2000)));
        when(userGoalRepository.save(any(UserGoal.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        GoalResponse response = goalService.upsertGoals(
                GoalRequest.builder()
                        .primaryGoal(PrimaryGoal.WEIGHT_LOSS)
                        .activityLevel(ActivityLevel.MODERATE)
                        .build());

        // Profile incomplete -> targets are not recalculated, previous values kept.
        assertThat(response.getPrimaryGoal()).isEqualTo(PrimaryGoal.WEIGHT_LOSS);
        assertThat(response.getTargetCalories()).isEqualTo(2000);
        assertThat(response.getTargetProteinG()).isEqualTo(150);
    }

    @Test
    void getGoalsReturnsRecord() {
        authenticate(fullProfile());

        when(userGoalRepository.findByUserId(USER_ID))
                .thenReturn(Optional.of(goal(10L, fullProfile(),
                        PrimaryGoal.WEIGHT_LOSS, ActivityLevel.MODERATE,
                        2044, 153, 204, 68, 7.5, 2500)));

        GoalResponse response = goalService.getGoals();

        assertThat(response.getUserId()).isEqualTo(USER_ID);
        assertThat(response.getPrimaryGoal()).isEqualTo(PrimaryGoal.WEIGHT_LOSS);
        assertThat(response.getTargetCalories()).isEqualTo(2044);
        assertThat(response.getSleepHours()).isEqualTo(7.5);
    }

    @Test
    void getGoalsThrowsNotFoundWhenAbsent() {
        authenticate(fullProfile());

        when(userGoalRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> goalService.getGoals())
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("UserGoal");
    }

    @Test
    void operationsWithoutAuthenticationThrowUnauthorized() {
        assertThatThrownBy(() -> goalService.getGoals())
                .isInstanceOf(UnauthorizedException.class);
    }

    // ── HELPERS ─────────────────────────────────────────────────

    private void authenticate(User user) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        user.getEmail(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    }

    private User fullProfile() {
        return user(USER_ID, USER_EMAIL, Role.USER, 30, Gender.MALE, 175.0, 70.0);
    }

    private User user(Long id, String email, Role role, Integer age, Gender gender,
                      Double height, Double weight) {
        return User.builder()
                .id(id)
                .email(email)
                .firstName("Test")
                .lastName("User")
                .role(role)
                .age(age)
                .gender(gender)
                .height(height)
                .weight(weight)
                .build();
    }

    private UserGoal goal(Long id, User owner, PrimaryGoal primaryGoal,
                          ActivityLevel activityLevel, Integer targetCalories,
                          Integer targetProteinG, Integer targetCarbsG, Integer targetFatG,
                          Double sleepHours, Integer waterIntakeMl) {
        return UserGoal.builder()
                .id(id)
                .user(owner)
                .primaryGoal(primaryGoal)
                .activityLevel(activityLevel)
                .targetCalories(targetCalories)
                .targetProteinG(targetProteinG)
                .targetCarbsG(targetCarbsG)
                .targetFatG(targetFatG)
                .sleepHours(sleepHours)
                .waterIntakeMl(waterIntakeMl)
                .build();
    }
}