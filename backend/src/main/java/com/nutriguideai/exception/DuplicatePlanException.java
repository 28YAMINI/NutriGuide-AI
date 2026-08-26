package com.nutriguideai.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicatePlanException extends RuntimeException {

    public DuplicatePlanException(String message) {
        super(message);
    }

}