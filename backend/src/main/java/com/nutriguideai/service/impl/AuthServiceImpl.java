package com.nutriguide.service.impl;

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
import com.nutriguideai.service.impl.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.nutriguideai.service.impl.JwtTokenProvider jwtTokenProvider;

    // ──────────────────────────────────────────────
    // REGISTER
    // ──────────────────────────────────────────────

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        log.info("Attempting to register user with email: {}", email);

        if (userRepository.existsByEmail(email)) {
            log.warn("Registration failed – email already exists: {}", email);
            throw new UserAlreadyExistsException(
                    "Email '" + email + "' is already registered"
            );
        }

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .role(Role.USER)
                .age(request.getAge())
                .gender(request.getGender())
                .height(request.getHeight())
                .weight(request.getWeight())
                .activityLevel(request.getActivityLevel())
                .goal(request.getGoal())
                .build();

        User savedUser = userRepository.save(user);

        log.info("User registered successfully with ID: {}", savedUser.getId());

        // NOTE: No JWT is generated at registration.
        //       The user must log in to receive a token.
        return RegisterResponse.builder()
                .userId(savedUser.getId())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .message("Registration successful. Please login.")
                .build();
    }

    // ──────────────────────────────────────────────
    // LOGIN
    // ──────────────────────────────────────────────

    @Override
    public LoginResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        log.info("Login attempt for email: {}", email);

        // Single lookup — we don't differentiate "email not found"
        // from "wrong password" to prevent user enumeration.
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null
                || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {

            log.warn("Login failed for email: {}", email);
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtTokenProvider.generateToken(user.getEmail());

        log.info("Login successful for user ID: {}", user.getId());

        return LoginResponse.builder()
                .userId(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .token(token)
                .build();
    }
}