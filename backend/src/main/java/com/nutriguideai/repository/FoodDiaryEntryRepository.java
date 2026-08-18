package com.nutriguideai.repository;

import com.nutriguideai.entity.FoodDiaryEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface FoodDiaryEntryRepository extends JpaRepository<FoodDiaryEntry, Long> {

    List<FoodDiaryEntry> findByUserIdAndLoggedDateOrderByCreatedAtDesc(Long userId, LocalDate loggedDate);

    List<FoodDiaryEntry> findByUserIdAndLoggedDateBetweenOrderByLoggedDateDesc(
            Long userId, LocalDate from, LocalDate to);

    void deleteByUserIdAndId(Long userId, Long entryId);
}