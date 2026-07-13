package com.luontd.etlworkerservice.infrastructure.llm;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class GeminiLlmConfig {

    @Value("${app.llm.base-url}")
    private String baseUrl;

    @Value("${app.llm.api-key}")
    private String apiKey;

    /**
     * RestClient được cấu hình sẵn base-url trỏ tới Gemini API.
     * API key được đính kèm qua query param khi build request trong Adapter.
     */
    @Bean(name = "geminiRestClient")
    public RestClient geminiRestClient() {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
