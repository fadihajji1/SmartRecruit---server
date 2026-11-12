package com.smartrecruit.candidateai.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.minio.MinioClient;

@Configuration
public class MinioConfiguration {
    @Bean
    MinioClient minioClient(@Value("${candidate-ai.storage.endpoint}") String endpoint,
            @Value("${candidate-ai.storage.access-key}") String accessKey,
            @Value("${candidate-ai.storage.secret-key}") String secretKey) {
        return MinioClient.builder().endpoint(endpoint).credentials(accessKey, secretKey).build();
    }
}
