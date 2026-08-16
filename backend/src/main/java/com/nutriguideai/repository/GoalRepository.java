package com.nutriguideai.repository;

import com.nutriguideai.entity.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for health goals.
 *
 * <p>Business rules: a user keeps a goal history, but at most one goal is
 * {@code active} at a time. Ownership is enforced in the service layer
 * (JWT principal), never from client input.</p>
 */
@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {

    /** The user's current active goal (1:1 with User while active). */
    Optional<Goal> findByUserIdAndActiveTrue(Long userId);

    /** Full goal history, newest first. */
    List<Goal> findByUserIdOrderByCreatedAtDesc(Long userId);

    /** Existence check used by services before create-vs-upsert decisions. */
    boolean existsByUserIdAndActiveTrue(Long userId);

    /** Most recently updated active goal — used as the tie-breaker. */
    Optional<Goal> findTopByUserIdAndActiveTrueOrderByUpdatedAtDesc(Long userId);

    Object findByUserId(long l);
}