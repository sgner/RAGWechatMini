package com.deepRAGForge.ai.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "minio")
@Component
public class MinioProperties {
       private String endpoint;
       private String accessKey;
       private String secretKey;
       private String bucketPrefix;
}
