package com.nutriguideai.service;

import com.nutriguideai.dto.request.PreferenceRequest;
import com.nutriguideai.dto.response.PreferenceResponse;
import com.nutriguideai.entity.FoodPreference;
import com.nutriguideai.entity.User;
import com.nutriguideai.exception.ResourceNotFoundException;
import com.nutriguideai.exception.UnauthorizedException;
import com.nutriguideai.repository.FoodPreferenceRepository;
import com.nutriguideai.repository.UserRepository;
import com.nutriguideai.service.PreferenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the food preferences contract.
 *
 * <p>Business rules: identity always comes from the JWT principal, never
 * from client input; preferences are a 1:1 record per user with upsert
 * semantics — fields omitted from a partial update keep their existing
 * values; allergies are stored as a JSON array.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PreferenceServiceImpl implements PreferenceService {

    private final FoodPreferenceRepository foodPreferenceRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public PreferenceResponse getPreferences() {
        User user = currentUser();
        FoodPreference preference = foodPreferenceRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "FoodPreference", "userId", user.getId()));
        return PreferenceResponse.fromEntity(preference);
    }

    @Override
    @Transactional
    public PreferenceResponse upsertPreferences(PreferenceRequest request) {
        User user = currentUser();

        FoodPreference preference = foodPreferenceRepository.findByUserId(user.getId())
                .orElseGet(() -> FoodPreference.builder().user(user).build());

        if (request.getDietType() != null) {
            preference.setDietType(request.getDietType());
        }
        if (request.getBudgetLevel() != null) {
            preference.setBudgetLevel(request.getBudgetLevel());
        }
        if (request.getRegion() != null) {
            preference.setRegion(request.getRegion());
        }
        if (request.getAllergies() != null) {
            preference.setAllergies(request.getAllergies());
        }
        if (request.getExcludedFoods() != null) {
            preference.setExcludedFoods(request.getExcludedFoods());
        }

        FoodPreference saved = foodPreferenceRepository.save(preference);
        log.info("Food preferences upserted for {}", user.getEmail());
        return PreferenceResponse.fromEntity(saved);
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