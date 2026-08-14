package com.nutriguideai.repository;

import com.nutriguideai.entity.FoodPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data JPA repository for the FoodPreference entity (1:1 with users).
 */
public interface FoodPreferenceRepository extends JpaRepository<FoodPreference, Long> {

    Optional<FoodPreference> findByUserId(Long userId);
}