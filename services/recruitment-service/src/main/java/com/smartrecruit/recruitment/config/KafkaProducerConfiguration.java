package com.smartrecruit.recruitment.config;

import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

/** Explicit producer setup: with Spring Boot 4 the plain spring-kafka dependency no longer brings Kafka auto-configuration. */
@Configuration
public class KafkaProducerConfiguration {
    @Bean
    ProducerFactory<String, String> recruitmentProducerFactory(
            @Value("${spring.kafka.bootstrap-servers:localhost:9093}") String bootstrapServers) {
        return new DefaultKafkaProducerFactory<>(Map.of(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
            ProducerConfig.MAX_BLOCK_MS_CONFIG, 5000,
            ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 5000,
            ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 10000));
    }

    @Bean
    KafkaTemplate<String, String> recruitmentKafkaTemplate(ProducerFactory<String, String> recruitmentProducerFactory) {
        return new KafkaTemplate<>(recruitmentProducerFactory);
    }
}
