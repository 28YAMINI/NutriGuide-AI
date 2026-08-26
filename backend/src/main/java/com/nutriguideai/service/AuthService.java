package com.nutriguideai.service;

import com.nutriguideai.dto.request.LoginRequest;
import com.nutriguideai.dto.request.LogoutRequest;
import com.nutriguideai.dto.request.RefreshTokenRequest;
import com.nutriguideai.dto.request.RegisterRequest;
import com.nutriguideai.dto.response.LoginResponse;
import com.nutriguideai.dto.response.RefreshTokenResponse;
import com.nutriguideai.dto.response.RegisterResponse;

/**
 * Contract for authentication operations.
 * Implementations are free to change internals (hashing, token format)
 * without affecting controllers or tests.
 */
public interface AuthService {

    RegisterResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    /** Validates a refresh token, revokes it (rotation), and mints a new pair. */
    RefreshTokenResponse refresh(RefreshTokenRequest request);

    /** Revokes the presented refresh token. Idempotent — unknown tokens are ignored. */
    void logout(LogoutRequest request);
}