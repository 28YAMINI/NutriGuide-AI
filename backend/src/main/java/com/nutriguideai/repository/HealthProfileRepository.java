package com.nutriguideai.repository;

import com.nutriguideai.entity.HealthProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for health profiles.
 *
 * <p>Business rules: a health profile is a 1:1 record per user with upsert
 * semantics. All lookups are scoped by userId — identity/ownership is
 * enforced in the service layer (JWT principal), never from client input.</p>
 */
@Repository
public interface HealthProfileRepository extends JpaRepository<HealthProfile, Long> {

    /**
     * Fetch the profile for a user (1:1 — one row per user).
     * Property path {@code user.id} resolves through the {@code User} relation,
     * same pattern as {@code FoodPreferenceRepository.findByUserId}.
     */
    Optional<HealthProfile> findByUserId(Long userId);

    /** Quick existence check used before insert-vs-update decisions. */
    boolean existsByUserId(Long userId);

    /**
     * Delete a user's profile by user id (derived delete query).
     * Use only from service code that has already verified ownership.
     */
    @Modifying
    @Query("DELETE FROM HealthProfile hp WHERE hp.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}