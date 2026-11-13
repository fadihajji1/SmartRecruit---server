package com.smartrecruit.interview.events;

import com.smartrecruit.interview.domain.Interview;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/** Best-effort publication of interview events (notifications + analytics). Never blocks or fails the HTTP request. */
@Component
public class InterviewEvents {
    private static final Logger log = LoggerFactory.getLogger(InterviewEvents.class);
    private final KafkaTemplate<String, String> kafka;
    private final JsonMapper mapper = JsonMapper.builder().build();
    private final String topic;

    public InterviewEvents(@Value("${spring.kafka.bootstrap-servers:localhost:9093}") String servers,
                           @Value("${interview.events.topic:smartrecruit.domain-events}") String topic) {
        this.topic = topic;
        this.kafka = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(Map.of(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, servers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
            ProducerConfig.MAX_BLOCK_MS_CONFIG, 1500)));
    }

    public void publish(String type, Interview interview) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("interviewId", str(interview.id));
            payload.put("applicationId", str(interview.applicationId));
            payload.put("jobId", str(interview.jobId));
            payload.put("jobTitle", interview.jobTitle);
            payload.put("candidateId", str(interview.candidateId));
            payload.put("recruiterId", str(interview.recruiterId));
            payload.put("scheduledAt", interview.scheduledAt == null ? null : interview.scheduledAt.toString());
            payload.put("status", interview.status == null ? null : interview.status.name());
            Map<String, Object> event = new LinkedHashMap<>();
            UUID eventId = UUID.randomUUID();
            event.put("eventId", eventId.toString());
            event.put("eventType", type);
            event.put("aggregateId", str(interview.id));
            event.put("occurredAt", Instant.now().toString());
            event.put("payload", payload);
            kafka.send(topic, eventId.toString(), mapper.writeValueAsString(event))
                .whenComplete((r, e) -> { if (e != null) log.warn("Interview event {} not published: {}", type, e.getMessage()); });
        } catch (Exception e) {
            log.warn("Interview event {} not published: {}", type, e.getMessage());
        }
    }

    private static String str(UUID id) { return id == null ? null : id.toString(); }
}
