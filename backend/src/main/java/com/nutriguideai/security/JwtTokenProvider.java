package com.nutriguideai.security;


import com.nutriguideai.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long expirationMs;

    /**
     * Constructor injection of config values.
     * The secret is converted into an HMAC key once, at startup — not on every request.
     */
    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    /**
     * Builds and signs a JWT.
     * - subject  = email (the user's stable identity)
     * - claim    = role (drives hasRole() authorization)
     * - issuedAt = now, expiration = now + configured TTL
     */
    public String generateToken(String email, Role role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(email)
                .claim("role", role.name())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    /** Extracts the subject (email) — used by the filter to find the user. */
    public String getEmailFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    /** Extracts the expiration — useful for debugging and tests. */
    public Date getExpirationFromToken(String token) {
        return parseClaims(token).getExpiration();
    }

    /**
     * Validates signature + expiration without throwing.
     * Returns false for tampered, malformed, or expired tokens.
     */
    public boolean isValidToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * The single parsing path — every public method funnels through here.
     * verifyWith() checks the HMAC signature; parseSignedClaims() throws
     * JwtException if the token is invalid or expired.
     */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
