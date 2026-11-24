package com.smartrecruit.candidateai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class CandidateAiServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CandidateAiServiceApplication.class, args);
    }
}
