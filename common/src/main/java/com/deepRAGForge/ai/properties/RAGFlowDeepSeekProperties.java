package com.deepRAGForge.ai.properties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix= "ragflow.deepseek")
@Component
public class    RAGFlowDeepSeekProperties {
    private String openaiBaseUrl;
    private String baseUrl;
    private String datasetUrl;
    private String apiKey;
    private String assistantUrl;
    private String conversationUrl;
}
