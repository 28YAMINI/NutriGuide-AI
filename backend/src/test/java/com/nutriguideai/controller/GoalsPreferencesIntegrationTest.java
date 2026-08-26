package com.nutriguideai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutriguideai.repository.FoodItemRepository;
import com.nutriguideai.repository.FoodPreferenceRepository;
import com.nutriguideai.repository.HealthVitalsRepository;
import com.nutriguideai.repository.UserConditionRepository;
import com.nutriguideai.repository.UserGoalRepository;
import com.nutriguideai.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end tests for the Goals and Food Preferences modules, run against
 * the full Spring context and the configured database. Standalone — no
 * shared base class.
 */
@SpringBootTest
@AutoConfigureMockMvc
class GoalsPreferencesIntegrationTest {

    private static final String USER_EMAIL = "goalsuser@test.com";
    private static final String USER_PASSWORD = "Password@123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FoodItemRepository foodItemRepository;

    @Autowired
    private HealthVitalsRepository healthVitalsRepository;

    @Autowired
    private UserConditionRepository userConditionRepository;

    @Autowired
    private UserGoalRepository userGoalRepository;

    @Autowired
    private FoodPreferenceRepository foodPreferenceRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanDatabase() {
        // All of these reference users — clear them before users.
        userGoalRepository.deleteAll();
        foodPreferenceRepository.deleteAll();
        userConditionRepository.deleteAll();
        healthVitalsRepository.deleteAll();
        foodItemRepository.deleteAll();
        userRepository.deleteAll();
    }

    // ── GOALS ──────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/goals before any save → 404")
    void getGoalsWithoutRecordReturns404() throws Exception {
        String userToken = registerAndLogin(USER_EMAIL, USER_PASSWORD);

        mockMvc.perform(get("/api/goals")
                        .header("Authorization", bearer(userToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("PUT /api/goals → 200 with server-calculated targets")
    void upsertGoalsCalculatesTargets() throws Exception {
        String userToken = registerAndLogin(USER_EMAIL, USER_PASSWORD);

        mockMvc.perform(put("/api/goals")
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "primaryGoal": "WEIGHT_LOSS",
                                  "activityLevel": "MODERATE",
                                  "sleepHours": 7.5,
                                  "waterIntakeMl": 2500
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").isNumber())
                .andExpect(jsonPath("$.primaryGoal").value("WEIGHT_LOSS"))
                .andExpect(jsonPath("$.activityLevel").value("MODERATE"))
                .andExpect(jsonPath("$.targetCalories").value(2044))
                .andExpect(jsonPath("$.targetProteinG").value(153))
                .andExpect(jsonPath("$.targetCarbsG").value(204))
                .andExpect(jsonPath("$.targetFatG").value(68))
                .andExpect(jsonPath("$.sleepHours").value(7.5))
                .andExpect(jsonPath("$.waterIntakeMl").value(2500));
    }

    @Test
    @DisplayName("PUT /api/goals out-of-range sleepHours → 400")
    void upsertGoalsOutOfRangeReturns400() throws Exception {
        String userToken = registerAndLogin(USER_EMAIL, USER_PASSWORD);

        mockMvc.perform(put("/api/goals")
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "primaryGoal": "WEIGHT_LOSS",
                                  "activityLevel": "MODERATE",
                                  "sleepHours": 20.0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("sleepHours")));
    }

    @Test
    @DisplayName("PUT /api/goals invalid primaryGoal → 400")
    void upsertGoalsInvalidEnumReturns400() throws Exception {
        String userToken = registerAndLogin(USER_EMAIL, USER_PASSWORD);

        mockMvc.perform(put("/api/goals")
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "primaryGoal": "NOT_A_GOAL",
                                  "activityLevel": "MODERATE"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("GET /api/goals without token → 401")
    void getGoalsWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/goals"))
                .andExpect(status().isUnauthorized());
    }

    // ── PREFERENCES ────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/preferences before any save → 404")
    void getPreferencesWithoutRecordReturns404() throws Exception {
        String userToken = registerAndLogin(USER_EMAIL, USER_PASSWORD);

        mockMvc.perform(get("/api/preferences")
                        .header("Authorization", bearer(userToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("PUT /api/preferences → 200 with allergies array round-trip")
    void upsertPreferencesSavesAllergies() throws Exception {
        String userToken = registerAndLogin(USER_EMAIL, USER_PASSWORD);

        mockMvc.perform(put("/api/preferences")
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "dietType": "VEGETARIAN",
                                  "budgetLevel": "MEDIUM",
                                  "region": "North India",
                                  "allergies": ["LACTOSE", "PEANUTS"],
                                  "excludedFoods": "Mushrooms, Okra"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").isNumber())
                .andExpect(jsonPath("$.dietType").value("VEGETARIAN"))
                .andExpect(jsonPath("$.budgetLevel").value("MEDIUM"))
                .andExpect(jsonPath("$.region").value("North India"))
                .andExpect(jsonPath("$.allergies", hasSize(2)))
                .andExpect(jsonPath("$.allergies[0]").value("LACTOSE"))
                .andExpect(jsonPath("$.allergies[1]").value("PEANUTS"))
                .andExpect(jsonPath("$.excludedFoods").value("Mushrooms, Okra"));

        // Round-trip: GET returns the same values.
        mockMvc.perform(get("/api/preferences")
                        .header("Authorization", bearer(userToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allergies", hasSize(2)));
    }

    @Test
    @DisplayName("PUT /api/preferences invalid dietType → 400")
    void upsertPreferencesInvalidEnumReturns400() throws Exception {
        String userToken = registerAndLogin(USER_EMAIL, USER_PASSWORD);

        mockMvc.perform(put("/api/preferences")
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "dietType": "NOT_A_DIET",
                                  "budgetLevel": "MEDIUM"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("PUT /api/preferences without token → 401")
    void upsertPreferencesWithoutTokenReturns401() throws Exception {
        mockMvc.perform(put("/api/preferences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "dietType": "VEGETARIAN",
                                  "budgetLevel": "LOW"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    // ── HELPERS ─────────────────────────────────────────────────

    private String registerAndLogin(String email, String password) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody(email, password)))
                .andExpect(status().isCreated());
        return login(email, password);
    }

    private String login(String email, String password) throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(body).get("token").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String registerBody(String email, String password) {
        return """
                {
                  "firstName": "Test",
                  "lastName": "User",
                  "email": "%s",
                  "password": "%s",
                  "age": 30,
                  "gender": "MALE",
                  "height": 175.0,
                  "weight": 70.0,
                  "activityLevel": "MODERATE",
                  "goal": "LOSE_WEIGHT"
                }
                """.formatted(email, password);
    }
}