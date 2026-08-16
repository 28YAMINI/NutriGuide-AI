package com.nutriguideai.exception;

/** Thrown when an account is temporarily locked after repeated failed logins (423). */
public class AccountLockedException extends RuntimeException {
    public AccountLockedException(String message) {
        super(message);
    }
}