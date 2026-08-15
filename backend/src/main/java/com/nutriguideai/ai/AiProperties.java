package com.nutriguideai.ai;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ai.gemini")
public class AiProperties {

    private String baseUrl = "https://generativelanguage.googleapis.com/v1beta";
    private String model = "gemini-2.5-flash";
    /** Injected from the NUTRIGUIDE_GEMINI_API_KEY env var. Never commit a real key. */
    private String apiKey;
    private double temperature = 0.4;
    private int maxOutputTokens = 4096;
}