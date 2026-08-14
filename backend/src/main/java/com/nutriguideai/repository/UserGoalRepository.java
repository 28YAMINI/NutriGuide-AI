package com.nutriguideai.repository;

import com.nutriguideai.entity.UserGoal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data JPA repository for the UserGoal entity (1:1 with users).
 */
public interface UserGoalRepository extends JpaRepository<UserGoal, Long> {

    Optional<UserGoal> findByUserId(Long userId);
}