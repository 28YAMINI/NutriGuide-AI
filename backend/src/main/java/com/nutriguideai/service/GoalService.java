package com.nutriguideai.service;

import com.nutriguideai.dto.request.GoalRequest;
import com.nutriguideai.dto.response.GoalResponse;
import org.springframework.transaction.annotation.Transactional;

public interface GoalService {

    @Transactional(readOnly = true)
    GoalResponse getGoals();

    GoalResponse getMyGoals();

    GoalResponse upsertGoals(GoalRequest request);
}