package com.nutriguideai.exception;

public class DuplicateFoodException extends RuntimeException {

    public DuplicateFoodException(String message) {
        super(message);
    }
}