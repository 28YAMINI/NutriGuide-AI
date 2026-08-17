package com.nutriguideai.repository;

import com.nutriguideai.entity.MedicalCondition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MedicalConditionRepository
        extends JpaRepository<MedicalCondition, Long> {

    Optional<MedicalCondition> findByIdAndActiveTrue(Long id);

    Optional<MedicalCondition> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(
            String name,
            Long id
    );

    List<MedicalCondition> findByActiveTrueOrderByNameAsc();
}