package com.nutriguideai.ai;

import com.nutriguideai.ai.impl.GeminiAiProvider;
import com.nutriguideai.exception.AiProviderException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GeminiAiProviderTest {

    private MockRestServiceServer server;
    private GeminiAiProvider provider;

    @BeforeEach
    void setUp() {
        AiProperties props = new AiProperties();
        props.setBaseUrl("https://generativelanguage.googleapis.com/v1beta");
        props.setModel("gemini-2.5-flash");
        props.setApiKey("test-key");
        props.setTemperature(0.4);
        props.setMaxOutputTokens(4096);

        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder
                .baseUrl(props.getBaseUrl())
                .defaultHeader("x-goog-api-key", props.getApiKey())
                .build();
        provider = new GeminiAiProvider(restClient, props);
    }

    @Test
    void generateChat_returnsTextFromFirstCandidate() {
        server.expect(requestTo("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("""
                        {"candidates":[{"content":{"parts":[{"text":"Eat more vegetables"}]}}]}
                        """, MediaType.APPLICATION_JSON));

        assertThat(provider.generateChat("sys", "user")).isEqualTo("Eat more vegetables");
        server.verify();
    }

    @Test
    void generateChat_throwsWhenApiErrorReturned() {
        server.expect(requestTo("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"))
                .andRespond(withSuccess("""
                        {"error":{"code":400,"message":"API key not valid","status":"INVALID_ARGUMENT"}}
                        """, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> provider.generateChat("sys", "user"))
                .isInstanceOf(AiProviderException.class)
                .hasMessageContaining("API key not valid");
    }

    @Test
    void generateChat_throwsWhenContentBlocked() {
        server.expect(requestTo("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"))
                .andRespond(withSuccess("{\"candidates\":[]}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> provider.generateChat("sys", "user"))
                .isInstanceOf(AiProviderException.class)
                .hasMessageContaining("no content");
    }

    @Test
    void generateChat_throwsOnHttpError() {
        server.expect(requestTo("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> provider.generateChat("sys", "user"))
                .isInstanceOf(AiProviderException.class);
    }
}