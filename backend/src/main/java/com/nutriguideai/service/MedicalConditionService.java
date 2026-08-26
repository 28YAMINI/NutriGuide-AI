package com.nutriguideai.service;

import com.nutriguideai.dto.request.MedicalConditionRequest;
import com.nutriguideai.dto.response.MedicalConditionResponse;

import java.util.List;

public interface MedicalConditionService  {

    MedicalConditionResponse create(MedicalConditionRequest request);

    MedicalConditionResponse update(Long id, MedicalConditionRequest request);

    void delete(Long id);

    MedicalConditionResponse getById(Long id);

    List<MedicalConditionResponse> getAll();
}