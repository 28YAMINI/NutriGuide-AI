package com.nutriguideai.repository;

import com.nutriguideai.entity.UserMedicalCondition;
import com.nutriguideai.enums.MedicalCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserMedicalConditionRepository extends JpaRepository<UserMedicalCondition, Long> {

    List<UserMedicalCondition> findByUserIdOrderByCreatedAtDesc(Long userId);

    boolean existsByUserIdAndCondition(Long userId, MedicalCondition condition);
}