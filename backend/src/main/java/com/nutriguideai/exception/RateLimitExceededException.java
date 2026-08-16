package com.nutriguideai.exception;

/** Thrown when a client exceeds the login rate limit (429). */
public class RateLimitExceededException extends RuntimeException {
    public RateLimitExceededException(String message) {
        super(message);
    }
}