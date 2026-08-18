package com.nutriguideai.repository;

import com.nutriguideai.entity.ProgressTracking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ProgressTrackingRepository extends JpaRepository<ProgressTracking, Long> {

    /** Latest record for weight display on dashboard. */
    Optional<ProgressTracking> findFirstByUserIdOrderByRecordedDateDesc(Long userId);

    /** Weight data points for trend chart. */
    List<ProgressTracking> findByUserIdAndRecordedDateBetweenOrderByRecordedDateAsc(
            Long userId, LocalDate from, LocalDate to);

    /** Check if today's record exists (upsert logic). */
    Optional<ProgressTracking> findByUserIdAndRecordedDate(Long userId, LocalDate date);

    /** All records for calorie/macro aggregation. */
    List<ProgressTracking> findByUserIdAndRecordedDateBetweenOrderByRecordedDateDesc(
            Long userId, LocalDate from, LocalDate to);
}