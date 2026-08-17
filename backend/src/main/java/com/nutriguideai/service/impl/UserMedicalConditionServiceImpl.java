package com.nutriguideai.service.impl;

import com.nutriguideai.dto.response.MedicalConditionResponse;
import com.nutriguideai.entity.MedicalCondition;
import com.nutriguideai.entity.User;
import com.nutriguideai.entity.UserMedicalCondition;
import com.nutriguideai.exception.MedicalConditionNotFoundException;
import com.nutriguideai.exception.UnauthorizedException;
import com.nutriguideai.repository.MedicalConditionRepository;
import com.nutriguideai.repository.UserMedicalConditionRepository;
import com.nutriguideai.repository.UserRepository;
import com.nutriguideai.service.UserMedicalConditionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserMedicalConditionServiceImpl implements UserMedicalConditionService {

    private final UserRepository userRepository;
    private final MedicalConditionRepository medicalConditionRepository;
    private final UserMedicalConditionRepository userMedicalConditionRepository;

    @Override
    public List<MedicalConditionResponse> getConditionsForEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        return userMedicalConditionRepository.findByUser(user)
                .stream()
                .map(umc -> toResponse(umc.getMedicalCondition()))
                .toList();
    }

    @Override
    @Transactional
    public MedicalConditionResponse addConditionForEmail(String email, Long conditionId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        MedicalCondition condition = medicalConditionRepository.findByIdAndActiveTrue(conditionId)
                .orElseThrow(() -> new MedicalConditionNotFoundException("Medical condition not found with id " + conditionId));

        // Idempotent: selecting an already-selected condition is a no-op.
        if (userMedicalConditionRepository.findByUserAndMedicalCondition(user, condition).isEmpty()) {
            userMedicalConditionRepository.save(UserMedicalCondition.builder()
                    .user(user)
                    .medicalCondition(condition)
                    .build());
        }

        return toResponse(condition);
    }

    @Override
    @Transactional
    public void removeConditionForEmail(String email, Long conditionId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        MedicalCondition condition = medicalConditionRepository.findByIdAndActiveTrue(conditionId)
                .orElseThrow(() -> new MedicalConditionNotFoundException("Medical condition not found with id " + conditionId));

        UserMedicalCondition userCondition = userMedicalConditionRepository
                .findByUserAndMedicalCondition(user, condition)
                .orElseThrow(() -> new MedicalConditionNotFoundException("Condition is not linked to this user"));

        userMedicalConditionRepository.delete(userCondition);
    }

    private MedicalConditionResponse toResponse(MedicalCondition condition) {
        return MedicalConditionResponse.builder()
                .id(condition.getId())
                .name(condition.getName())
                .description(condition.getDescription())
                .active(condition.getActive())
                .build();
    }
}