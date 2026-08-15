package com.nutriguideai.ai;

import java.util.List;

/** Minimal mapping of the Gemini generateContent REST response. */
public record GeminiResponse(List<Candidate> candidates, Error error) {

    public record Candidate(Content content) {}

    public record Content(List<Part> parts) {}

    public record Part(String text) {}

    public record Error(int code, String message, String status) {}
}