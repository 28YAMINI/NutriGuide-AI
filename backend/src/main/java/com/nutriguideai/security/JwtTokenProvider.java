package com.nutriguideai.security;

import com.nutriguideai.enums.Role;

/**
 * Contract for JWT access-token operations.
 *
 * <p>Kept as an interface so consumers and tests depend on the contract,
 * not on the concrete signing implementation.</p>
 */
public interface JwtTokenProvider {

    /** Generates a signed JWT for the given email and role. */
    String generateToken(String email, Role role);

    /** Extracts the subject (email) from a token. */
    String getEmailFromToken(String token);

    /** True when the token is well-formed, signed by us and not expired. */
    boolean isValidToken(String token);
}