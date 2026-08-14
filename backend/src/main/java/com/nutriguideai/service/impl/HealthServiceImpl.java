package com.nutriguideai.service.impl;

import com.nutriguideai.dto.request.AddConditionRequest;
import com.nutriguideai.dto.request.HealthVitalsRequest;
import com.nutriguideai.dto.response.ConditionResponse;
import com.nutriguideai.dto.response.HealthVitalsResponse;
import com.nutriguideai.entity.HealthVitals;
import com.nutriguideai.entity.User;
import com.nutriguideai.entity.UserCondition;
import com.nutriguideai.exception.DuplicateConditionException;
import com.nutriguideai.exception.ResourceNotFoundException;
import com.nutriguideai.exception.UnauthorizedException;
import com.nutriguideai.repository.HealthVitalsRepository;
import com.nutriguideai.repository.UserConditionRepository;
import com.nutriguideai.repository.UserRepository;
import com.nutriguideai.service.HealthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of the health vitals and conditions contract.
 *
 * <p>Business rules: identity always comes from the JWT principal, never
 * from client input; vitals are a 1:1 record per user with upsert
 * semantics — fields omitted from a partial update keep their existing
 * values; a user can never have the same condition recorded twice (409);
 * every operation is scoped to the authenticated user — deleting a
 * condition owned by another user returns 403 for any role.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HealthServiceImpl implements HealthService {

    private final HealthVitalsRepository healthVitalsRepository;
    private final UserConditionRepository userConditionRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public HealthVitalsResponse getMyVitals() {
        User user = currentUser();
        HealthVitals vitals = healthVitalsRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "HealthVitals", "userId", user.getId()));
        return HealthVitalsResponse.fromEntity(vitals);
    }

    @Override
    @Transactional
    public HealthVitalsResponse upsertVitals(HealthVitalsRequest request) {
        User user = currentUser();

        HealthVitals vitals = healthVitalsRepository.findByUserId(user.getId())
                .orElseGet(() -> HealthVitals.builder().user(user).build());

        // Partial update: only non-null fields are applied, so a client
        // that sends a subset of the vitals never wipes the rest.
        if (request.getBloodPressureSystolic() != null) {
            vitals.setBloodPressureSystolic(request.getBloodPressureSystolic());
        }
        if (request.getBloodPressureDiastolic() != null) {
            vitals.setBloodPressureDiastolic(request.getBloodPressureDiastolic());
        }
        if (request.getFastingSugar() != null) {
            vitals.setFastingSugar(request.getFastingSugar());
        }
        if (request.getPostMealSugar() != null) {
            vitals.setPostMealSugar(request.getPostMealSugar());
        }
        if (request.getHba1c() != null) {
            vitals.setHba1c(request.getHba1c());
        }
        if (request.getCholesterolLdl() != null) {
            vitals.setCholesterolLdl(request.getCholesterolLdl());
        }
        if (request.getCholesterolHdl() != null) {
            vitals.setCholesterolHdl(request.getCholesterolHdl());
        }
        if (request.getTriglycerides() != null) {
            vitals.setTriglycerides(request.getTriglycerides());
        }

        HealthVitals saved = healthVitalsRepository.save(vitals);
        log.info("Health vitals upserted for {}", user.getEmail());
        return HealthVitalsResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConditionResponse> getMyConditions() {
        User user = currentUser();
        return userConditionRepository.findByUserIdOrderByCreatedAtAsc(user.getId())
                .stream()
                .map(ConditionResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public ConditionResponse addCondition(AddConditionRequest request) {
        User user = currentUser();

        if (userConditionRepository.existsByUserIdAndCondition(
                user.getId(), request.getCondition())) {
            log.warn("User {} already has condition {}",
                    user.getEmail(), request.getCondition());
            throw new DuplicateConditionException(
                    "This condition is already recorded for your profile");
        }

        UserCondition condition = UserCondition.builder()
                .user(user)
                .condition(request.getCondition())
                .severity(request.getSeverity())
                .diagnosedDate(request.getDiagnosedDate())
                .notes(request.getNotes())
                .build();

        UserCondition saved = userConditionRepository.save(condition);
        log.info("Condition {} added for {}", saved.getCondition(), user.getEmail());
        return ConditionResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public void deleteCondition(Long conditionId) {
        UserCondition condition = userConditionRepository.findById(conditionId)
                .orElseThrow(() -> {
                    log.warn("Condition not found: id={}", conditionId);
                    return new ResourceNotFoundException(
                            "UserCondition", "id", conditionId);
                });

        User current = currentUser();
        // The module is user-scoped: any role may delete only the
        // authenticated user's own conditions.
        if (!condition.getUser().getId().equals(current.getId())) {
            log.warn("User {} attempted to delete condition {} owned by {}",
                    current.getEmail(), conditionId, condition.getUser().getEmail());
            throw new AccessDeniedException("You can only delete your own conditions");
        }

        userConditionRepository.delete(condition);
        log.info("Condition deleted: id={}", conditionId);
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