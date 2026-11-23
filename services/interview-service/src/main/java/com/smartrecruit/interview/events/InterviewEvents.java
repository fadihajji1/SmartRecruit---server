package com.smartrecruit.interview.events;

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

import com.smartrecruit.interview.model.Interview;

import tools.jackson.databind.json.JsonMapper;

/**
 * Publishes interview events (notifications + analytics) after the transaction commits.
 * Publication is best effort: a Kafka outage never fails the HTTP request.
 */
@Component
public class InterviewEvents implements DisposableBean {
    private static final Logger log = LoggerFactory.getLogger(InterviewEvents.class);
    private final DefaultKafkaProducerFactory<String, String> producerFactory;
    private final KafkaTemplate<String, String> kafka;
    private final JsonMapper mapper = JsonMapper.builder().build();
    private final String topic;

    public InterviewEvents(@Value("${spring.kafka.bootstrap-servers:localhost:9093}") String servers,
            @Value("${interview.events.topic:smartrecruit.domain-events}") String topic) {
        this.topic = topic;
        this.producerFactory = new DefaultKafkaProducerFactory<>(Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, servers,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                ProducerConfig.MAX_BLOCK_MS_CONFIG, 1500));
        this.kafka = new KafkaTemplate<>(producerFactory);
    }

    /**
     * @param recipientUserId who should be notified (null lets the consumer derive candidate/recruiter)
     */
    public void publish(String type, Interview interview, UUID recipientUserId, Instant proposedScheduledAt) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("interviewId", str(interview.getId()));
        payload.put("applicationId", str(interview.getApplicationId()));
        payload.put("jobId", str(interview.getJobId()));
        payload.put("jobTitle", interview.getJobTitle());
        payload.put("candidateId", str(interview.getCandidateId()));
        payload.put("candidateName", interview.getCandidateName());
        payload.put("recruiterId", str(interview.getRecruiterId()));
        payload.put("scheduledAt", interview.getScheduledAt() == null ? null : interview.getScheduledAt().toString());
        payload.put("status", interview.getStatus().name());
        if (recipientUserId != null) payload.put("recipientUserId", str(recipientUserId));
        if (proposedScheduledAt != null) payload.put("proposedScheduledAt", proposedScheduledAt.toString());
        UUID eventId = UUID.randomUUID();
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("eventId", eventId.toString());
        event.put("eventType", type);
        event.put("aggregateId", str(interview.getId()));
        event.put("occurredAt", Instant.now().toString());
        event.put("payload", payload);
        String json = mapper.writeValueAsString(event);
        Runnable send = () -> {
            try {
                kafka.send(topic, eventId.toString(), json).whenComplete((result, error) -> {
                    if (error != null) log.warn("Interview event {} not published: {}", type, error.getMessage());
                });
            } catch (RuntimeException ex) {
                log.warn("Interview event {} not published: {}", type, ex.getMessage());
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
