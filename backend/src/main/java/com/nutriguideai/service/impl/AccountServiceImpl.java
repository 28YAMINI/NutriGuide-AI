package com.nutriguideai.service.impl;

import com.nutriguideai.entity.EmailVerificationToken;
import com.nutriguideai.entity.PasswordResetToken;
import com.nutriguideai.entity.User;
import com.nutriguideai.exception.InvalidTokenException;
import com.nutriguideai.exception.TokenExpiredException;
import com.nutriguideai.repository.EmailVerificationTokenRepository;
import com.nutriguideai.repository.PasswordResetTokenRepository;
import com.nutriguideai.repository.RefreshTokenRepository;
import com.nutriguideai.repository.UserRepository;
import com.nutriguideai.service.AccountService;
import com.nutriguideai.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.verification.expiration-ms:86400000}")
    private long verificationExpirationMs;

    @Value("${app.password-reset.expiration-ms:1800000}")
    private long passwordResetExpirationMs;

    @Value("${app.base-url:http://localhost:8081}")
    private String baseUrl;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @Override
    @Transactional
    public void sendVerificationEmail(String email) {

        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            log.debug("Verification email requested for unknown email");
            return;
        }

        User user = userOpt.get();

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            return;
        }

        // Remove any previous verification token.
        emailVerificationTokenRepository.deleteByUser(user);

        String rawToken = generateSecureToken();
        String tokenHash = hashToken(rawToken);

        EmailVerificationToken verificationToken =
                new EmailVerificationToken();

        verificationToken.setUser(user);
        verificationToken.setTokenHash(tokenHash);
        verificationToken.setExpiresAt(
                LocalDateTime.now()
                        .plus(Duration.ofMillis(verificationExpirationMs))
        );

        emailVerificationTokenRepository.save(verificationToken);

        String link =
                baseUrl
                        + "/api/auth/verify-email?token="
                        + rawToken;

        emailService.send(
                user.getEmail(),
                "Verify your email - NutriGuide AI",
                "Hello,\n\n"
                        + "Please verify your email address by opening this link:\n"
                        + link
                        + "\n\n"
                        + "This link expires in "
                        + formatDuration(verificationExpirationMs)
                        + ".\n"
                        + "If you did not register on NutriGuide AI, "
                        + "you can safely ignore this email."
        );
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {

        if (token == null || token.isBlank()) {
            throw new InvalidTokenException();
        }

        EmailVerificationToken verificationToken =
                emailVerificationTokenRepository
                        .findByTokenHash(hashToken(token))
                        .orElseThrow(InvalidTokenException::new);

        if (verificationToken.getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            emailVerificationTokenRepository.delete(verificationToken);
            throw new TokenExpiredException();
        }

        User user = verificationToken.getUser();

        user.setEmailVerified(true);
        userRepository.save(user);

        // Token is single-use.
        emailVerificationTokenRepository.delete(verificationToken);

        log.info("Email verified for user {}", user.getEmail());
    }

    @Override
    @Transactional
    public void requestPasswordReset(String email) {

        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            log.debug("Password reset requested for unknown email");
            return;
        }

        User user = userOpt.get();

        // Remove previous reset tokens.
        passwordResetTokenRepository.deleteByUser(user);

        String rawToken = generateSecureToken();
        String tokenHash = hashToken(rawToken);

        PasswordResetToken resetToken =
                new PasswordResetToken();

        resetToken.setUser(user);
        resetToken.setTokenHash(tokenHash);
        resetToken.setExpiresAt(
                LocalDateTime.now()
                        .plus(Duration.ofMillis(passwordResetExpirationMs))
        );

        passwordResetTokenRepository.save(resetToken);

        String link =
                frontendUrl
                        + "/reset-password?token="
                        + rawToken;

        emailService.send(
                user.getEmail(),
                "Reset your password - NutriGuide AI",
                "Hello,\n\n"
                        + "You requested a password reset. "
                        + "Open this link to choose a new password:\n"
                        + link
                        + "\n\n"
                        + "This link expires in "
                        + formatDuration(passwordResetExpirationMs)
                        + ".\n"
                        + "If you did not request this, "
                        + "you can safely ignore this email."
        );
    }

    @Override
    @Transactional
    public void resetPassword(
            String token,
            String newPassword) {

        if (token == null || token.isBlank()) {
            throw new InvalidTokenException();
        }

        PasswordResetToken resetToken =
                passwordResetTokenRepository
                        .findByTokenHash(hashToken(token))
                        .orElseThrow(InvalidTokenException::new);

        if (resetToken.getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            passwordResetTokenRepository.delete(resetToken);
            throw new TokenExpiredException();
        }

        User user = resetToken.getUser();

        user.setPassword(
                passwordEncoder.encode(newPassword)
        );

        userRepository.save(user);

        // Reset token is single-use.
        passwordResetTokenRepository.delete(resetToken);

        // Revoke all existing refresh tokens after password reset.
        refreshTokenRepository.deleteByUser(user);

        log.info(
                "Password reset completed for user {}",
                user.getEmail()
        );
    }

    private String generateSecureToken() {

        byte[] bytes = new byte[32];

        SECURE_RANDOM.nextBytes(bytes);

        return HexFormat.of().formatHex(bytes);
    }

    private String hashToken(String token) {

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash =
                    digest.digest(
                            token.getBytes(StandardCharsets.UTF_8)
                    );

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "SHA-256 algorithm not available",
                    e
            );
        }
    }

    private String formatDuration(long millis) {

        Duration duration =
                Duration.ofMillis(millis);

        if (duration.toMinutes() < 60) {
            return duration.toMinutes() + " minutes";
        }

        return duration.toHours() + " hours";
    }
}