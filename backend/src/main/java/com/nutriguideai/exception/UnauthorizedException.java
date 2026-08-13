package com.nutriguideai.exception;

import org.springframework.web.bind.annotation.ExceptionHandler;


public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);

    }
}
