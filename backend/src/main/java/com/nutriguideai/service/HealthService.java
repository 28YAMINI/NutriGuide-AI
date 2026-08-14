package com.nutriguideai.service;

import com.nutriguideai.dto.request.AddConditionRequest;
import com.nutriguideai.dto.request.HealthVitalsRequest;
import com.nutriguideai.dto.response.ConditionResponse;
import com.nutriguideai.dto.response.HealthVitalsResponse;

import java.util.List;

public interface HealthService {

    HealthVitalsResponse getMyVitals();

    HealthVitalsResponse upsertVitals(HealthVitalsRequest request);

    List<ConditionResponse> getMyConditions();

    ConditionResponse addCondition(AddConditionRequest request);

    void deleteCondition(Long conditionId);
}