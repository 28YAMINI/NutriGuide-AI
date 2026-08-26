package com.nutriguideai.ai;

public interface AiProvider {

    /** Two-part chat: system prompt + user prompt (used by the meal-plan flow). */
    String generateChat(String systemPrompt, String userPrompt);

    /** One-shot chat with a single prompt. */
    String chat(String prompt);
}