package com.smartrecruit.subscription.service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.smartrecruit.subscription.domain.Subscription;

import tools.jackson.databind.json.JsonMapper;

/** Publishes subscription lifecycle events after commit; best effort, never fails the request. */
@Component
public class SubscriptionEvents implements DisposableBean {
    private static final Logger log = LoggerFactory.getLogger(SubscriptionEvents.class);
    private final DefaultKafkaProducerFactory<String, String> producerFactory;
    private final KafkaTemplate<String, String> kafka;
    private final JsonMapper mapper = JsonMapper.builder().build();
    private final String topic;

    public SubscriptionEvents(@Value("${spring.kafka.bootstrap-servers:localhost:9093}") String servers,
            @Value("${subscription.events.topic:smartrecruit.domain-events}") String topic) {
        this.topic = topic;
        this.producerFactory = new DefaultKafkaProducerFactory<>(Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, servers,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                ProducerConfig.MAX_BLOCK_MS_CONFIG, 1500));
        this.kafka = new KafkaTemplate<>(producerFactory);
    }

    public void publish(String type, Subscription subscription, String planCode, String details) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("subscriptionId", str(subscription.getId()));
        payload.put("ownerId", str(subscription.getOwnerId()));
        payload.put("recipientUserId", str(subscription.getOwnerId()));
        payload.put("audience", subscription.getAudience() == null ? null : subscription.getAudience().name());
        payload.put("planCode", planCode);
        payload.put("status", subscription.getStatus() == null ? null : subscription.getStatus().name());
        payload.put("details", details);
        UUID eventId = UUID.randomUUID();
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("eventId", eventId.toString());
        event.put("eventType", type);
        event.put("aggregateId", str(subscription.getId()));
        event.put("occurredAt", Instant.now().toString());
        event.put("payload", payload);
        String json = mapper.writeValueAsString(event);
        Runnable send = () -> {
            try {
                kafka.send(topic, eventId.toString(), json).whenComplete((result, error) -> {
                    if (error != null) log.warn("Subscription event {} not published: {}", type, error.getMessage());
                });
            } catch (RuntimeException ex) {
                log.warn("Subscription event {} not published: {}", type, ex.getMessage());
            }
        };
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    send.run();
                }
            });
        } else {
            send.run();
        }
    }

    @Override
    public void destroy() {
        producerFactory.destroy();
    }

    private static String str(UUID id) {
        return id == null ? null : id.toString();
    }
}
