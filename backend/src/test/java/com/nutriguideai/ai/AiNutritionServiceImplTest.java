package com.nutriguideai.ai;

import com.nutriguideai.ai.AiProvider;
import com.nutriguideai.dto.request.MealPlanRequest;
import com.nutriguideai.dto.response.MealPlanResponse;
import com.nutriguideai.entity.FoodPreference;
import com.nutriguideai.entity.User;
import com.nutriguideai.entity.UserGoal;
import com.nutriguideai.enums.ActivityLevel;
import com.nutriguideai.enums.Gender;
import com.nutriguideai.enums.PrimaryGoal;
import com.nutriguideai.exception.UnauthorizedException;
import com.nutriguideai.repository.FoodPreferenceRepository;
import com.nutriguideai.repository.UserGoalRepository;
import com.nutriguideai.repository.UserRepository;
import com.nutriguideai.service.impl.AiNutritionServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AiNutritionServiceImplTest {

    private static final String EMAIL = "alice@nutriguide.com";

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

        UserRepository userRepository =
                createUserRepository(user);

        UserGoalRepository userGoalRepository =
                createUserGoalRepository(goal);

        FoodPreferenceRepository foodPreferenceRepository =
                createFoodPreferenceRepository();

        AiProvider aiProvider = new AiProvider() {

            @Override
            public String generateChat(
                    String systemPrompt,
                    String userPrompt
            ) {
                return "Breakfast: oats with milk and fruit";
            }

            @Override
            public String chat(String prompt) {
                return "AI response";
            }
        };

        AiNutritionServiceImpl service =
                new AiNutritionServiceImpl(
                        userRepository,
                        userGoalRepository,
                        foodPreferenceRepository,
                        aiProvider
                );

        MealPlanResponse response =
                service.generateMealPlan(
                        MealPlanRequest.builder()
                                .days(1)
                                .mealsPerDay(4)
                                .build()
                );

        assertThat(response)
                .isNotNull();

        assertThat(response.getPlan())
                .contains("Breakfast");

        assertThat(response.getTargets())
                .isNotNull();

        assertThat(response.getGeneratedAt())
                .isNotNull();
    }

    @Test
    void generateMealPlan_throwsUnauthorized_whenNotAuthenticated() {

        // No SecurityContext set on purpose.

        UserRepository userRepository =
                createUserRepository(null);

        UserGoalRepository userGoalRepository =
                createUserGoalRepository(null);

        FoodPreferenceRepository foodPreferenceRepository =
                createFoodPreferenceRepository();

        AiProvider aiProvider = new AiProvider() {

            @Override
            public String generateChat(
                    String systemPrompt,
                    String userPrompt
            ) {
                return "AI response";
            }

            @Override
            public String chat(String prompt) {
                return "AI response";
            }
        };

        AiNutritionServiceImpl service =
                new AiNutritionServiceImpl(
                        userRepository,
                        userGoalRepository,
                        foodPreferenceRepository,
                        aiProvider
                );

        assertThatThrownBy(() ->
                service.generateMealPlan(
                        MealPlanRequest.builder()
                                .days(1)
                                .mealsPerDay(4)
                                .build()
                )
        )
                .isInstanceOf(UnauthorizedException.class);
    }

    private void authenticate(String email) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        email,
                        null
                )
        );
    }

    // ================================================================
    // USER REPOSITORY TEST DOUBLE
    // ================================================================

    private UserRepository createUserRepository(User user) {

        InvocationHandler handler =
                (proxy, method, args) -> {

                    if ("findByEmail".equals(method.getName())) {
                        return Optional.ofNullable(user);
                    }

                    if ("toString".equals(method.getName())) {
                        return "TestUserRepository";
                    }

                    if ("hashCode".equals(method.getName())) {
                        return System.identityHashCode(proxy);
                    }

                    if ("equals".equals(method.getName())) {
                        return proxy == args[0];
                    }

                    throw new UnsupportedOperationException(
                            "UserRepository method not implemented in test: "
                                    + method.getName()
                    );
                };

        return (UserRepository) Proxy.newProxyInstance(
                UserRepository.class.getClassLoader(),
                new Class<?>[]{UserRepository.class},
                handler
        );
    }

    // ================================================================
    // USER GOAL REPOSITORY TEST DOUBLE
    // ================================================================

    private UserGoalRepository createUserGoalRepository(UserGoal goal) {

        InvocationHandler handler =
                (proxy, method, args) -> {

                    if ("findByUserId".equals(method.getName())) {
                        return Optional.ofNullable(goal);
                    }

                    if ("toString".equals(method.getName())) {
                        return "TestUserGoalRepository";
                    }

                    if ("hashCode".equals(method.getName())) {
                        return System.identityHashCode(proxy);
                    }

                    if ("equals".equals(method.getName())) {
                        return proxy == args[0];
                    }

                    throw new UnsupportedOperationException(
                            "UserGoalRepository method not implemented in test: "
                                    + method.getName()
                    );
                };

        return (UserGoalRepository) Proxy.newProxyInstance(
                UserGoalRepository.class.getClassLoader(),
                new Class<?>[]{UserGoalRepository.class},
                handler
        );
    }

    // ================================================================
    // FOOD PREFERENCE REPOSITORY TEST DOUBLE
    // ================================================================

    private FoodPreferenceRepository createFoodPreferenceRepository() {

        InvocationHandler handler =
                (proxy, method, args) -> {

                    if ("findByUserId".equals(method.getName())) {
                        return Optional.<FoodPreference>empty();
                    }

                    if ("toString".equals(method.getName())) {
                        return "TestFoodPreferenceRepository";
                    }

                    if ("hashCode".equals(method.getName())) {
                        return System.identityHashCode(proxy);
                    }

                    if ("equals".equals(method.getName())) {
                        return proxy == args[0];
                    }

                    throw new UnsupportedOperationException(
                            "FoodPreferenceRepository method not implemented in test: "
                                    + method.getName()
                    );
                };

        return (FoodPreferenceRepository) Proxy.newProxyInstance(
                FoodPreferenceRepository.class.getClassLoader(),
                new Class<?>[]{FoodPreferenceRepository.class},
                handler
        );
    }
}