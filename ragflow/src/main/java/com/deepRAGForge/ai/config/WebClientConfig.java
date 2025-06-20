package com.deepRAGForge.ai.config;

import com.deepRAGForge.ai.properties.RAGFlowDeepSeekProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class WebClientConfig {
    private final RAGFlowDeepSeekProperties properties;
    @Bean
    @Qualifier("deepseekOpenAiWebClient")
    public WebClient deepseekOpenAiWebClient(){
        return WebClient.builder()
                .baseUrl(properties.getOpenaiBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getApiKey())
                .filter(logRequest())
                .build();
    }
    @Bean
    @Qualifier("deepseekWebClient")
    public WebClient deepseekWebClient(){
         return WebClient.builder()
                 .baseUrl(properties.getBaseUrl())
                 .defaultHeader(HttpHeaders.CONTENT_TYPE,MediaType.APPLICATION_JSON_VALUE)
                 .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getApiKey())
                 .filter(logRequest())
                 .build();
    }
    private ExchangeFilterFunction logRequest() {
        return (request, next) -> {
            System.out.println("Request: " + request.method() + " " + request.url());
            return next.exchange(request);
        };
    }
}
