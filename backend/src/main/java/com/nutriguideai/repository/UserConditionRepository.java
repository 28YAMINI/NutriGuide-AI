package com.nutriguideai.repository;

import com.nutriguideai.entity.UserCondition;
import com.nutriguideai.enums.MedicalCondition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data JPA repository for the UserCondition entity.
 *
 * <p>The (user_id, condition) unique constraint is declared on the entity,
 * and {@code existsByUserIdAndCondition} lets the service reject duplicate
 * entries with a clear 409 before the database enforces it.</p>
 */
public interface UserConditionRepository extends JpaRepository<UserCondition, Long> {

    List<UserCondition> findByUserIdOrderByCreatedAtAsc(Long userId);

    boolean existsByUserIdAndCondition(Long userId, MedicalCondition condition);
}