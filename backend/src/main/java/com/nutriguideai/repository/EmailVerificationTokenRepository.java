package com.nutriguideai.repository;

import com.nutriguideai.entity.EmailVerificationToken;
import com.nutriguideai.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken> findByTokenHash(String tokenHash);

    void deleteByUser(User user);
}