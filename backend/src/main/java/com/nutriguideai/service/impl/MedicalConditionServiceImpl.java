package com.nutriguideai.service.impl;

import com.nutriguideai.dto.request.MedicalConditionRequest;
import com.nutriguideai.dto.response.MedicalConditionResponse;
import com.nutriguideai.entity.MedicalCondition;
import com.nutriguideai.exception.DuplicateMedicalConditionException;
import com.nutriguideai.exception.MedicalConditionNotFoundException;
import com.nutriguideai.repository.MedicalConditionRepository;
import com.nutriguideai.service.MedicalConditionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicalConditionServiceImpl implements MedicalConditionService {

    private final MedicalConditionRepository medicalConditionRepository;

    @Override
    @Transactional
    public MedicalConditionResponse create(MedicalConditionRequest request) {
        if (medicalConditionRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateMedicalConditionException("Medical condition '" + request.getName() + "' already exists");
        }

        MedicalCondition condition = MedicalCondition.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .active(request.getActive() == null || request.getActive())
                .build();

        return toResponse(medicalConditionRepository.save(condition));
    }

    @Override
    @Transactional
    public MedicalConditionResponse update(Long id, MedicalConditionRequest request) {
        MedicalCondition condition = medicalConditionRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new MedicalConditionNotFoundException("Medical condition not found with id " + id));

        if (medicalConditionRepository.existsByNameIgnoreCaseAndIdNot(request.getName(), id)) {
            throw new DuplicateMedicalConditionException("Medical condition '" + request.getName() + "' already exists");
        }

        condition.setName(request.getName().trim());
        condition.setDescription(request.getDescription());
        condition.setActive(request.getActive() == null || request.getActive());

        return toResponse(condition);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        MedicalCondition condition = medicalConditionRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new MedicalConditionNotFoundException("Medical condition not found with id " + id));
        condition.setActive(false);
    }

    @Override
    public MedicalConditionResponse getById(Long id) {
        return toResponse(medicalConditionRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new MedicalConditionNotFoundException("Medical condition not found with id " + id)));
    }

    @Override
    public List<MedicalConditionResponse> getAll() {
        return medicalConditionRepository.findByActiveTrueOrderByNameAsc()
                .stream()
                .map(this::toResponse)
                .toList();
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