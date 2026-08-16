package com.nutriguideai.security;

import com.nutriguideai.exception.RateLimitExceededException;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory fixed-window rate limiter for the login endpoint.
 *
 * <p>Keyed by normalized email: more than {@link #MAX_ATTEMPTS} login
 * attempts in {@link #WINDOW} trips the limiter (429). This guards
 * against brute-force bursts; the account lockout in AuthServiceImpl
 * provides the durable per-account protection.</p>
 */
@Component
public class LoginRateLimiter {

    static final int MAX_ATTEMPTS = 10;
    static final Duration WINDOW = Duration.ofMinutes(1);
    private static final int MAX_KEYS = 10_000;

    private final ConcurrentHashMap<String, Window> windows =
            new ConcurrentHashMap<>();

    /**
     * Throws RateLimitExceededException when the key exceeded
     * its window budget.
     */
    public void checkAllowed(String key) {
        long now = System.currentTimeMillis();

        Window window = windows.computeIfAbsent(
                key,
                k -> new Window(now, 0)
        );

        synchronized (window) {
            if (now - window.startedAtMillis >= WINDOW.toMillis()) {
                window.startedAtMillis = now;
                window.count = 0;
            }

            if (window.count >= MAX_ATTEMPTS) {
                throw new RateLimitExceededException(
                        "Too many login attempts. Please try again shortly."
                );
            }

            window.count++;
        }

        // Crude memory bound: drop the whole map occasionally instead of
        // tracking per-key timestamps for eviction.
        if (windows.size() > MAX_KEYS) {
            windows.clear();
        }
    }

    /**
     * Mutable fixed-window slot.
     */
    private static final class Window {

        private long startedAtMillis;
        private int count;

        private Window(long startedAtMillis, int count) {
            this.startedAtMillis = startedAtMillis;
            this.count = count;
        }
    }
}