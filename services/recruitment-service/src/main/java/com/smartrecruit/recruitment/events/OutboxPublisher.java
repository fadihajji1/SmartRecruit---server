package com.smartrecruit.recruitment.events;

import com.smartrecruit.recruitment.repo.OutboxRepository;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

/**
 * Transactional outbox: events are stored with the business change and pushed to Kafka by a scheduler,
 * so the public API keeps working even when Kafka is down.
 * Envelope matches the consumers in notification/analytics/candidate-ai: eventId, eventType, aggregateId, occurredAt, payload.
 */
@Component
public class OutboxPublisher {
    private final OutboxRepository repository;
    private final KafkaTemplate<String, String> kafka;
    private final JsonMapper mapper = JsonMapper.builder().build();
    @Value("${recruitment.events.topic:smartrecruit.domain-events}") private String topic;

    public OutboxPublisher(OutboxRepository repository, KafkaTemplate<String, String> kafka) {
        this.repository = repository; this.kafka = kafka;
    }

    @Transactional
    public void enqueue(String eventType, UUID aggregateId, Map<String, Object> payload) {
        UUID eventId = UUID.randomUUID();
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("eventId", eventId.toString());
        event.put("eventType", eventType);
        event.put("aggregateId", aggregateId == null ? null : aggregateId.toString());
        event.put("occurredAt", Instant.now().toString());
        event.put("payload", payload);
        OutboxMessage row = new OutboxMessage();
        row.id = eventId;
        row.payload = mapper.writeValueAsString(event);
        repository.save(row);
    }

    @Scheduled(fixedDelayString = "${recruitment.events.publish-delay-ms:5000}")
    @Transactional
    public void publishPending() {
        for (OutboxMessage message : repository.findTop100ByPublishedAtIsNullOrderByCreatedAtAsc()) {
            try {
                kafka.send(topic, message.id.toString(), message.payload).get(10, TimeUnit.SECONDS);
                message.publishedAt = Instant.now(); message.lastError = null;
            } catch (Exception e) {
                message.attempts++;
                String error = e.getMessage();
                message.lastError = error == null ? e.getClass().getSimpleName() : error.substring(0, Math.min(error.length(), 2000));
                if (e instanceof InterruptedException) Thread.currentThread().interrupt();
                break; // broker unavailable: retry on next tick instead of blocking 10s per row
            }
        }
    }
}
