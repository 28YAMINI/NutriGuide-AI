package com.nutriguideai.service.impl;

import com.nutriguideai.dto.request.AddConditionRequest;
import com.nutriguideai.dto.request.HealthVitalsRequest;
import com.nutriguideai.dto.response.ConditionResponse;
import com.nutriguideai.dto.response.HealthVitalsResponse;
import com.nutriguideai.entity.User;
import com.nutriguideai.entity.UserMedicalCondition;
import com.nutriguideai.exception.ResourceNotFoundException;
import com.nutriguideai.exception.UnauthorizedException;
import com.nutriguideai.repository.UserMedicalConditionRepository;
import com.nutriguideai.repository.UserRepository;
import com.nutriguideai.service.HealthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Implementation of the health contract: vitals (stored on {@link User})
 * and medical conditions (1:N records owned by the user).
 *
 * <p>Identity always comes from the JWT principal, never from client input;
 * a user can only read/delete their own conditions (403 otherwise).</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HealthProfileServiceImpl implements HealthService {

    private final UserRepository userRepository;
    private final UserMedicalConditionRepository conditionRepository;

    @Override
    @Transactional(readOnly = true)
    public HealthVitalsResponse getMyVitals() {
        User user = currentUser();
        return HealthVitalsResponse.fromEntity(user);
    }

    @Override
    @Transactional
    public HealthVitalsResponse upsertVitals(HealthVitalsRequest request) {
        User user = currentUser();

        if (request.getAge() != null) {
            user.setAge(request.getAge());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getHeight() != null) {
            user.setHeight(request.getHeight());
        }
        if (request.getWeight() != null) {
            user.setWeight(request.getWeight());
        }

        userRepository.save(user);
        log.info("Health vitals upserted for {}", user.getEmail());
        return HealthVitalsResponse.fromEntity(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConditionResponse> getMyConditions() {
        User user = currentUser();
        return conditionRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(c -> ConditionResponse.builder()
                        .condition(c.getCondition())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public ConditionResponse addCondition(AddConditionRequest request) {
        User user = currentUser();

        if (conditionRepository.existsByUserIdAndCondition(user.getId(), request.getCondition())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Condition already recorded: " + request.getCondition());
        }

        UserMedicalCondition saved = conditionRepository.save(
                UserMedicalCondition.builder()
                        .user(user)
                        .condition(request.getCondition())
                        .build());
        log.info("Condition {} added for {}", request.getCondition(), user.getEmail());
        return ConditionResponse.builder()
                .condition(saved.getCondition())
                .build();
    }

    @Override
    @Transactional
    public void deleteCondition(Long conditionId) {
        User user = currentUser();

        UserMedicalCondition condition = conditionRepository.findById(conditionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "UserMedicalCondition", "id", conditionId));

        if (!condition.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "This condition does not belong to you");
        }

        conditionRepository.delete(condition);
        log.info("Condition {} deleted for {}", conditionId, user.getEmail());
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