package com.nutriguideai.repository;

import com.nutriguideai.entity.MealPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MealPlanRepository extends JpaRepository<MealPlan, Long> {

    Optional<MealPlan> findByUserIdAndPlanDate(Long userId, LocalDate planDate);

    List<MealPlan> findByUserIdAndPlanDateBetweenOrderByPlanDateDesc(
            Long userId, LocalDate from, LocalDate to);

    boolean existsByUserIdAndPlanDate(Long id, LocalDate now);
}