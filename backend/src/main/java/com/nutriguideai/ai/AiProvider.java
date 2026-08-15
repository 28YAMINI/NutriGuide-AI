package com.nutriguideai.ai;

/**
 * Abstraction over an LLM provider. Implementations call the vendor REST API.
 * Business code only ever talks to this interface.
 */
public interface AiProvider {

    /**
     * @param systemPrompt the assistant persona + user context (never user-supplied raw)
     * @param userPrompt   the user's question or generation instruction
     * @return the model's text reply
     * @throws com.nutriguideai.exception.AiProviderException on any provider failure
     */
    String generateChat(String systemPrompt, String userPrompt);
}