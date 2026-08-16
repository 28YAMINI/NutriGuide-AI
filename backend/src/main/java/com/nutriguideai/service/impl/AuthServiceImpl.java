package com.nutriguideai.service.impl;

import com.nutriguideai.dto.request.LoginRequest;
import com.nutriguideai.dto.request.LogoutRequest;
import com.nutriguideai.dto.request.RefreshTokenRequest;
import com.nutriguideai.dto.request.RegisterRequest;
import com.nutriguideai.dto.response.LoginResponse;
import com.nutriguideai.dto.response.RefreshTokenResponse;
import com.nutriguideai.dto.response.RegisterResponse;
import com.nutriguideai.entity.RefreshToken;
import com.nutriguideai.entity.User;
import com.nutriguideai.enums.Role;
import com.nutriguideai.exception.AccountLockedException;
import com.nutriguideai.exception.InvalidCredentialsException;
import com.nutriguideai.exception.UnauthorizedException;
import com.nutriguideai.exception.UserAlreadyExistsException;
import com.nutriguideai.repository.RefreshTokenRepository;
import com.nutriguideai.repository.UserRepository;
import com.nutriguideai.security.JwtTokenProvider;
import com.nutriguideai.security.LoginRateLimiter;
import com.nutriguideai.service.AuthService;
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
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private static final String REGISTRATION_SUCCESS_MESSAGE =
            "Registration successful. Please login.";

    private static final String DUMMY_PASSWORD_HASH =
            "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

    private static final int MAX_FAILED_ATTEMPTS = 5;

    private static final Duration LOCKOUT_DURATION =
            Duration.ofMinutes(15);

    private static final DateTimeFormatter LOCK_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final LoginRateLimiter loginRateLimiter;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    // ------------------------------------------------------------------
    // REGISTER
    // ------------------------------------------------------------------

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {

        String email = normalizeEmail(request.getEmail());

        log.info("Attempting to register user with email: {}", email);

        if (userRepository.existsByEmail(email)) {
            log.warn(
                    "Registration failed - email already exists: {}",
                    email
            );

            throw new UserAlreadyExistsException(
                    "Email '" + email + "' is already registered"
            );
        }

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(Role.USER)
                .age(request.getAge())
                .gender(request.getGender())
                .height(request.getHeight())
                .weight(request.getWeight())
                .activityLevel(request.getActivityLevel())
                .goal(request.getGoal())
                .build();

        User savedUser = userRepository.save(user);

        log.info(
                "User registered successfully with id: {}",
                savedUser.getId()
        );

        return RegisterResponse.of(
                savedUser,
                REGISTRATION_SUCCESS_MESSAGE
        );
    }

    // ------------------------------------------------------------------
    // LOGIN
    // ------------------------------------------------------------------

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {

        String email = normalizeEmail(request.getEmail());

        log.info("Login attempt for email: {}", email);

        // Rate-limit before accessing the database.
        loginRateLimiter.checkAllowed(email);

        User user = userRepository
                .findByEmail(email)
                .orElse(null);

        if (user != null && isLocked(user)) {
            log.warn(
                    "Login blocked for locked account: {}",
                    email
            );

            throw new AccountLockedException(
                    lockMessage(user.getLockedUntil())
            );
        }

        String storedHash =
                user != null
                        ? user.getPassword()
                        : DUMMY_PASSWORD_HASH;

        boolean passwordMatches =
                passwordEncoder.matches(
                        request.getPassword(),
                        storedHash
                );

        if (user == null || !passwordMatches) {

            registerFailedAttempt(user);

            throw new InvalidCredentialsException(
                    "Invalid email or password"
            );
        }

        // Reset lockout counters after successful authentication.
        if (user.getFailedLoginAttempts() != null
                || user.getLockedUntil() != null) {

            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);

            userRepository.save(user);
        }

        String accessToken =
                jwtTokenProvider.generateToken(
                        user.getEmail(),
                        user.getRole()
                );

        String refreshToken =
                issueRefreshToken(user);

        log.info(
                "User logged in successfully: {}",
                user.getEmail()
        );

        return LoginResponse.of(
                accessToken,
                refreshToken,
                user
        );
    }

    // ------------------------------------------------------------------
    // REFRESH TOKEN
    // ------------------------------------------------------------------

    @Override
    @Transactional
    public RefreshTokenResponse refresh(
            RefreshTokenRequest request) {

        RefreshToken stored =
                refreshTokenRepository
                        .findByTokenHash(
                                hash(request.getRefreshToken())
                        )
                        .orElseThrow(() ->
                                new UnauthorizedException(
                                        "Invalid refresh token"
                                )
                        );

        if (Boolean.TRUE.equals(stored.getRevoked())
                || stored.getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            throw new UnauthorizedException(
                    "Invalid refresh token"
            );
        }

        User user = stored.getUser();

        // Rotate old refresh token.
        stored.setRevoked(true);

        String accessToken =
                jwtTokenProvider.generateToken(
                        user.getEmail(),
                        user.getRole()
                );

        String newRefreshToken =
                issueRefreshToken(user);

        log.info(
                "Refresh token rotated for {}",
                user.getEmail()
        );

        return RefreshTokenResponse.of(
                accessToken,
                newRefreshToken
        );
    }

    // ------------------------------------------------------------------
    // LOGOUT
    // ------------------------------------------------------------------

    @Override
    @Transactional
    public void logout(LogoutRequest request) {

        refreshTokenRepository
                .findByTokenHash(
                        hash(request.getRefreshToken())
                )
                .ifPresent(stored -> {

                    stored.setRevoked(true);

                    log.info(
                            "Refresh token revoked for user id {}",
                            stored.getUser().getId()
                    );
                });
    }

    // ------------------------------------------------------------------
    // REFRESH TOKEN HELPER
    // ------------------------------------------------------------------

    private String issueRefreshToken(User user) {

        String rawToken = generateRandomToken();

        LocalDateTime expiresAt =
                LocalDateTime.now()
                        .plus(Duration.ofMillis(refreshExpirationMs));

        RefreshToken token =
                RefreshToken.builder()
                        .user(user)
                        .tokenHash(hash(rawToken))
                        .expiresAt(expiresAt)
                        .revoked(false)
                        .build();

        refreshTokenRepository.save(token);

        return rawToken;
    }

    // ------------------------------------------------------------------
    // LOGIN FAILURE / ACCOUNT LOCKOUT
    // ------------------------------------------------------------------

    private void registerFailedAttempt(User user) {

        // Unknown users do not have an account to lock.
        if (user == null) {
            return;
        }

        int attempts =
                (user.getFailedLoginAttempts() == null
                        ? 0
                        : user.getFailedLoginAttempts()) + 1;

        if (attempts >= MAX_FAILED_ATTEMPTS) {

            user.setFailedLoginAttempts(0);

            user.setLockedUntil(
                    LocalDateTime.now()
                            .plus(LOCKOUT_DURATION)
            );

            userRepository.save(user);

            log.warn(
                    "Account locked for {} until {}",
                    user.getEmail(),
                    user.getLockedUntil()
            );

            throw new AccountLockedException(
                    lockMessage(user.getLockedUntil())
            );
        }

        user.setFailedLoginAttempts(attempts);

        userRepository.save(user);
    }

    private boolean isLocked(User user) {

        return user.getLockedUntil() != null
                && user.getLockedUntil()
                .isAfter(LocalDateTime.now());
    }

    private String lockMessage(LocalDateTime until) {

        return "Account temporarily locked due to too many "
                + "failed attempts. Try again after "
                + (until == null
                ? "a few minutes"
                : until.format(LOCK_TIME_FORMAT))
                + ".";
    }

    // ------------------------------------------------------------------
    // TOKEN HELPERS
    // ------------------------------------------------------------------

    private String generateRandomToken() {

        byte[] bytes = new byte[64];

        new SecureRandom().nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private String hash(String rawToken) {

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            return HexFormat.of().formatHex(
                    digest.digest(
                            rawToken.getBytes(StandardCharsets.UTF_8)
                    )
            );

        } catch (NoSuchAlgorithmException e) {

            throw new IllegalStateException(
                    "SHA-256 unavailable",
                    e
            );
        }
    }

    private String normalizeEmail(String email) {

        return email
                .trim()
                .toLowerCase();
    }
}