package com.nutriguideai.exception;

public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException() {
        super("Invalid or already used token.");
    }

    public InvalidTokenException(String message) {
        super(message);
    }
}