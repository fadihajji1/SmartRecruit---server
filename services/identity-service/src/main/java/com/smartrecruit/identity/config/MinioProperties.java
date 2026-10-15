package com.smartrecruit.identity.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("identity.storage.minio")
public record MinioProperties(String endpoint, String accessKey, String secretKey, String bucket) {
}
