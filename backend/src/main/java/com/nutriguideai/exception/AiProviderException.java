package com.nutriguideai.exception;

/** Thrown when the AI provider is unreachable, misconfigured, or returns garbage. */
public class AiProviderException extends RuntimeException {

    public AiProviderException(String message) {
        super(message);
    }

    public AiProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}