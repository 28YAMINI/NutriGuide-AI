package com.nutriguideai.repository;

import com.nutriguideai.entity.MedicalCondition;
import com.nutriguideai.entity.User;
import com.nutriguideai.entity.UserMedicalCondition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserMedicalConditionRepository
        extends JpaRepository<UserMedicalCondition, Long> {

    List<UserMedicalCondition> findByUser(User user);

    Optional<UserMedicalCondition> findByUserAndMedicalCondition(
            User user,
            MedicalCondition medicalCondition
    );

    boolean existsByUserAndMedicalCondition(
            User user,
            MedicalCondition medicalCondition
    );
}