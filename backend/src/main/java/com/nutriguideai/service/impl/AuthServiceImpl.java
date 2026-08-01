package com.nutriguideai.service.impl;

import com.nutriguideai.dto.request.LoginRequest;
import com.nutriguideai.dto.request.RegisterRequest;
import com.nutriguideai.dto.response.LoginResponse;
import com.nutriguideai.dto.response.RegisterResponse;
import com.nutriguideai.entity.User;
import com.nutriguideai.enums.Role;
import com.nutriguideai.exception.InvalidCredentialsException;
import com.nutriguideai.exception.UserAlreadyExistsException;
import com.nutriguideai.repository.UserRepository;
import com.nutriguideai.security.JwtTokenProvider;
import com.nutriguideai.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private static final String REGISTRATION_SUCCESS_MESSAGE = "Registration successful. Please login.";

    /**
     * A structurally-valid BCrypt hash of a random string.
     * Used only to keep timing constant when the email is unknown —
     * BCrypt hashing is deliberately slow (~100ms), so an attacker
     * could otherwise measure the difference between "user exists"
     * and "user missing" and enumerate valid emails.
     */
    private static final String DUMMY_PASSWORD_HASH =
            "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // ──────────────────────────────────────────────
    // REGISTER
    // ──────────────────────────────────────────────

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.getEmail());
        log.info("Attempting to register user with email: {}", email);

        // Step 1: duplicate check (on the normalized email)
        if (userRepository.existsByEmail(email)) {
            log.warn("Registration failed — email already exists: {}", email);
            throw new UserAlreadyExistsException("Email '" + email + "' is already registered");
        }

        // Step 2: build the entity — password hashed immediately, never stored in plain text
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(Role.USER)                    // role is always USER — never client-supplied
                .age(request.getAge())
                .gender(request.getGender())
                .height(request.getHeight())
                .weight(request.getWeight())
                .activityLevel(request.getActivityLevel())
                .goal(request.getGoal())
                .build();

        // Step 3: persist
        User savedUser = userRepository.save(user);
        log.info("User registered successfully with id: {}", savedUser.getId());

        // Step 4: NO JWT here — the user must login to get a token
        return RegisterResponse.of(savedUser, REGISTRATION_SUCCESS_MESSAGE);
    }

    // ──────────────────────────────────────────────
    // LOGIN
    // ──────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String email = normalizeEmail(request.getEmail());
        log.info("Login attempt for email: {}", email);

        // Step 1: find the user; null-safe (no exception yet — same path as wrong password)
        User user = userRepository.findByEmail(email).orElse(null);

        // Step 2: verify password — ALWAYS runs exactly one BCrypt match,
        //         even when the user doesn't exist (dummy hash), to keep timing constant.
        String storedHash = (user != null) ? user.getPassword() : DUMMY_PASSWORD_HASH;
        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), storedHash);

        // Step 3: identical error for both failure cases — no user enumeration
        if (user == null || !passwordMatches) {
            log.warn("Login failed — invalid credentials for email: {}", email);
            throw new InvalidCredentialsException("Invalid email or password");
        }

        // Step 4: token only after successful authentication
        String token = jwtTokenProvider.generateToken(user.getEmail(), user.getRole());
        log.info("User logged in successfully: {}", user.getEmail());

        return LoginResponse.of(token, user);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}