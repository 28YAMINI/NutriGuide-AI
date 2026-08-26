package com.nutriguideai.service;

import com.nutriguideai.dto.response.MedicalConditionResponse;

import java.util.List;

public interface UserMedicalConditionService {

    List<MedicalConditionResponse> getConditionsForEmail(String email);

    MedicalConditionResponse addConditionForEmail(String email, Long conditionId);

    void removeConditionForEmail(String email, Long conditionId);
}