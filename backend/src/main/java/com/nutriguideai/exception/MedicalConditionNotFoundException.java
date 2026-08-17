package com.nutriguideai.exception;

public class MedicalConditionNotFoundException extends RuntimeException {

    public MedicalConditionNotFoundException(String message) {
        super(message);
    }
}