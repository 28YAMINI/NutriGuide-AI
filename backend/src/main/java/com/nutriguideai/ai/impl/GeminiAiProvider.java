package com.nutriguideai.ai.impl;

import com.nutriguideai.ai.AiProperties;
import com.nutriguideai.ai.AiProvider;
import com.nutriguideai.ai.GeminiResponse;
import com.nutriguideai.exception.AiProviderException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Gemini REST provider (plain HTTP, no SDK).
 * Endpoint: POST {baseUrl}/models/{model}:generateContent
 */
@Component
@RequiredArgsConstructor
@Primary
@Slf4j
public class GeminiAiProvider implements AiProvider {

    private final RestClient geminiRestClient;
    private final AiProperties aiProperties;



    @Override
    public String generateChat(String systemPrompt, String userPrompt) {
        if (aiProperties.getApiKey() == null || aiProperties.getApiKey().isBlank()) {
            throw new AiProviderException(
                    "AI API key is not configured. Set the NUTRIGUIDE_GEMINI_API_KEY environment variable.");
        }

        Map<String, Object> requestBody = Map.of(
                "systemInstruction", Map.of("parts", List.of(Map.of("text", systemPrompt))),
                "contents", List.of(Map.of("parts", List.of(Map.of("text", userPrompt)))),
                "generationConfig", Map.of(
                        "temperature", aiProperties.getTemperature(),
                        "maxOutputTokens", aiProperties.getMaxOutputTokens())
        );

        GeminiResponse response;
        try {
            response = geminiRestClient.post()
                    .uri("/models/{model}:generateContent", aiProperties.getModel())
                    .body(requestBody)
                    .retrieve()
                    .body(GeminiResponse.class);
        } catch (RestClientException e) {
            log.error("Gemini API call failed", e);
            throw new AiProviderException("AI service is temporarily unavailable", e);
        }

        if (response == null || response.error() != null) {
            String msg = response == null ? "empty response" : response.error().message();
            throw new AiProviderException("AI provider error: " + msg);
        }
        if (response.candidates() == null || response.candidates().isEmpty()
                || response.candidates().get(0).content() == null
                || response.candidates().get(0).content().parts().isEmpty()) {
            throw new AiProviderException("AI provider returned no content (request may have been blocked)");
        }

        String text = response.candidates().get(0).content().parts().get(0).text();
        if (text == null || text.isBlank()) {
            throw new AiProviderException("AI provider returned empty content");
        }
        return text.trim();
    }


    @Override
    public String chat(String prompt) {
        return generateChat("You are NutriGuide AI, an evidence-informed nutrition assistant.", prompt);
    }
}