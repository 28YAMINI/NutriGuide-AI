package com.nutriguideai.controller;

import com.nutriguideai.dto.request.MedicalConditionRequest;
import com.nutriguideai.dto.response.MedicalConditionResponse;
import com.nutriguideai.service.MedicalConditionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/medical-conditions")
@RequiredArgsConstructor
public class MedicalConditionController {

    private final MedicalConditionService medicalConditionService;

    @GetMapping
    public List<MedicalConditionResponse> getAll() {
        return medicalConditionService.getAll();
    }

    @GetMapping("/{id}")
    public MedicalConditionResponse getById(@PathVariable Long id) {
        return medicalConditionService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public MedicalConditionResponse create(@Valid @RequestBody MedicalConditionRequest request) {
        return medicalConditionService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public MedicalConditionResponse update(@PathVariable Long id, @Valid @RequestBody MedicalConditionRequest request) {
        return medicalConditionService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        medicalConditionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}