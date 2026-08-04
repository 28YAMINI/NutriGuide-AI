package com.nutriguideai.exception;

public class DuplicateFoodItemException extends RuntimeException {
    public DuplicateFoodItemException(String message) {
        super(message);
    }
}
