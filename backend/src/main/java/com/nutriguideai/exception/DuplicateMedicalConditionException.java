package com.nutriguideai.exception;

public class DuplicateMedicalConditionException extends RuntimeException {

    public DuplicateMedicalConditionException(String message) {
        super(message);
    }
}