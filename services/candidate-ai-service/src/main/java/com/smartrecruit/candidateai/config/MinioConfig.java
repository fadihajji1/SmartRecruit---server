package com.smartrecruit.candidateai.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {
    @Bean MinioClient minioClient(@Value("${minio.url:http://localhost:9000}") String url,
                                  @Value("${minio.access-key:minioadmin}") String access,
                                  @Value("${minio.secret-key:minioadmin}") String secret) {
        return MinioClient.builder().endpoint(url).credentials(access,secret).build();
    }
}
