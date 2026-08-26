package com.nutriguideai.repository;

import com.nutriguideai.entity.HealthVitals;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data JPA repository for the HealthVitals entity (1:1 with users).
 */
public interface HealthVitalsRepository extends JpaRepository<HealthVitals, Long> {

    Optional<HealthVitals> findByUserId(Long userId);
}