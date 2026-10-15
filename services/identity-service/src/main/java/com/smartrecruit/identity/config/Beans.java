package com.smartrecruit.identity.config;

import io.minio.MinioClient;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.context.annotation.Bean;

import org.springframework.context.annotation.Configuration;

@Configuration public class Beans {
 @Bean MinioClient minio(@Value("${minio.url:http://localhost:9000}") String url,@Value("${minio.access-key:minioadmin}") String access,@Value("${minio.secret-key:minioadmin}") String secret){
 return MinioClient.builder().endpoint(url).credentials(access,secret).build();
 }
 }

