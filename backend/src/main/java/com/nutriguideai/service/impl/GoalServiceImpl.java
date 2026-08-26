package com.nutriguideai.service.impl;

import com.nutriguideai.dto.request.GoalRequest;
import com.nutriguideai.dto.response.GoalResponse;
import com.nutriguideai.entity.User;
import com.nutriguideai.entity.UserGoal;
import com.nutriguideai.exception.ResourceNotFoundException;
import com.nutriguideai.exception.UnauthorizedException;
import com.nutriguideai.repository.UserGoalRepository;
import com.nutriguideai.repository.UserRepository;
import com.nutriguideai.service.GoalService;
import com.nutriguideai.service.TargetCalculator;
import com.nutriguideai.service.TargetCalculator.TargetMacros;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the goals contract.
 *
 * <p>Business rules: identity always comes from the JWT principal, never
 * from client input; goals are a 1:1 record per user with upsert
 * semantics — fields omitted from a partial update keep their existing
 * values; calorie and macro targets are calculated server-side by
 * {@link TargetCalculator} and are never accepted from the client.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GoalServiceImpl implements GoalService {

    private final UserGoalRepository userGoalRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    @Override
    public GoalResponse getGoals() {
        User user = currentUser();
        UserGoal goal = userGoalRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "UserGoal", "userId", user.getId()));
        return GoalResponse.fromEntity(goal);
    }

    @Override
    public GoalResponse getMyGoals() {
        return null;
    }

    @Override
    @Transactional
    public GoalResponse upsertGoals(GoalRequest request) {
        User user = currentUser();

        UserGoal goal = userGoalRepository.findByUserId(user.getId())
                .orElseGet(() -> UserGoal.builder().user(user).build());

        if (request.getPrimaryGoal() != null) {
            goal.setPrimaryGoal(request.getPrimaryGoal());
        }
        if (request.getActivityLevel() != null) {
            goal.setActivityLevel(request.getActivityLevel());
        }
        if (request.getSleepHours() != null) {
            goal.setSleepHours(request.getSleepHours());
        }
        if (request.getWaterIntakeMl() != null) {
            goal.setWaterIntakeMl(request.getWaterIntakeMl());
        }

        // Targets are recomputed on every save so a change to the goal or
        // activity level is reflected immediately; a null result (incomplete
        // profile data) keeps the previously stored values.
        TargetMacros macros = TargetCalculator.calculate(
                user, goal.getPrimaryGoal(), goal.getActivityLevel());
        if (macros != null) {
            goal.setTargetCalories(macros.calories());
            goal.setTargetProteinG(macros.proteinG());
            goal.setTargetCarbsG(macros.carbsG());
            goal.setTargetFatG(macros.fatG());
        }

        UserGoal saved = userGoalRepository.save(goal);
        log.info("Goals upserted for {}", user.getEmail());
        return GoalResponse.fromEntity(saved);
    }

    /** Identity ALWAYS comes from the JWT principal, never from client input. */
    private User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new UnauthorizedException("User is not authenticated");
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "email", authentication.getName()));
    }
}