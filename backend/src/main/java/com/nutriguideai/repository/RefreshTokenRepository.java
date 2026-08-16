package com.nutriguideai.repository;

import com.nutriguideai.entity.RefreshToken;
import com.nutriguideai.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    long deleteByExpiresAtBefore(LocalDateTime cutoff);

    void deleteByUser(User user);
}