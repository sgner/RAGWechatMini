package com.deepRAGForge.ai.config;

import com.deepRAGForge.ai.properties.MinioProperties;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class MinioConfig {
    private final MinioProperties minioProperties;
    @Bean
    public MinioClient minioClient(){
         return MinioClient.builder()
                 .endpoint(minioProperties.getEndpoint())
                 .credentials(minioProperties.getAccessKey(),minioProperties.getSecretKey())
                 .build();
    }
}
