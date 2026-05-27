package com.selvendran.smarthome;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class OllamaClientConfig {

    @Bean
    public WebClient ollamaWebClient(@Value("${ollama.base.url:http://localhost:11434}") String baseUrl) {
        String root = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        return WebClient.builder()
                .baseUrl(root)
                .build();
    }
}
