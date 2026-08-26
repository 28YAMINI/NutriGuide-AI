package com.nutriguideai.controller;

import com.nutriguideai.dto.request.MedicalConditionSelectionRequest;
import com.nutriguideai.dto.response.MedicalConditionResponse;
import com.nutriguideai.service.UserMedicalConditionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/my/medical-conditions")
@RequiredArgsConstructor
public class UserMedicalConditionController {

    private final UserMedicalConditionService userMedicalConditionService;

    @GetMapping
    public List<MedicalConditionResponse> getMyConditions(Authentication authentication) {
        return userMedicalConditionService.getConditionsForEmail(authentication.getName());
    }

    @PostMapping
    public MedicalConditionResponse addCondition(@Valid @RequestBody MedicalConditionSelectionRequest request,
                                                 Authentication authentication) {
        return userMedicalConditionService.addConditionForEmail(authentication.getName(), request.getConditionId());
    }

    @DeleteMapping("/{conditionId}")
    public ResponseEntity<Void> removeCondition(@PathVariable Long conditionId,
                                                Authentication authentication) {
        userMedicalConditionService.removeConditionForEmail(authentication.getName(), conditionId);
        return ResponseEntity.noContent().build();
    }
}