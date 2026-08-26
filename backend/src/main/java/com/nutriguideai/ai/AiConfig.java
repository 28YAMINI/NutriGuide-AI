package com.nutriguideai.ai;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class AiConfig {

    @Bean
    public RestClient geminiRestClient(AiProperties props) {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl(props.getBaseUrl())
                .requestFactory(geminiRequestFactory());
        // Only attach the key if configured, so tests/startup stay green without one.
        if (props.getApiKey() != null && !props.getApiKey().isBlank()) {
            builder.defaultHeader("x-goog-api-key", props.getApiKey());
        }
        return builder.build();
    }

    private ClientHttpRequestFactory geminiRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofSeconds(90));
        return factory;
    }
}