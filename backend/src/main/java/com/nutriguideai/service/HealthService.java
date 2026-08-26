package com.nutriguideai.service;

import com.nutriguideai.dto.request.AddConditionRequest;
import com.nutriguideai.dto.request.HealthVitalsRequest;
import com.nutriguideai.dto.response.ConditionResponse;
import com.nutriguideai.dto.response.HealthVitalsResponse;

import java.util.List;

/**
 * Service contract for health vitals and medical conditions.
 * Every operation is scoped to the authenticated user (JWT principal).
 */
public interface HealthService {

    HealthVitalsResponse getMyVitals();

    HealthVitalsResponse upsertVitals(HealthVitalsRequest request);

    List<ConditionResponse> getMyConditions();

    ConditionResponse addCondition(AddConditionRequest request);

    void deleteCondition(Long conditionId);
}