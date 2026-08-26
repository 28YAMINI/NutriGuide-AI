package com.nutriguideai.exception;

/** Thrown when a request is refused for business-rule reasons (400). */
public class IllegalOperationException extends RuntimeException {
    public IllegalOperationException(String message) {
        super(message);
    }
}