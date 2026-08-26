package com.nutriguideai.exception;

public class TokenExpiredException extends RuntimeException {

    public TokenExpiredException() {
        super("Token has expired. Please request a new one.");
    }

    public TokenExpiredException(String message) {
        super(message);
    }
}