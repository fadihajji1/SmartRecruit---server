package com.smartrecruit.candidateai.events;

import java.time.Instant;
import java.util.UUID;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartrecruit.candidateai.service.CandidateAiService;

@Component
public class JobEventConsumer {
    private final ObjectMapper mapper;
    private final CandidateAiService service;
    public JobEventConsumer(ObjectMapper mapper, CandidateAiService service) { this.mapper = mapper; this.service = service; }

    @KafkaListener(topics = "${candidate-ai.events.topic}")
    public void consume(String message) throws Exception {
        JsonNode root = mapper.readTree(message);
        String eventType = text(root, "eventType", "type", "name");
        if (eventType == null || !eventType.toLowerCase(java.util.Locale.ROOT).contains("job")) return;
        JsonNode data = root.path("payload");
        if (data.has("job") && data.get("job").isObject()) data = data.get("job");
        String id = text(data, "jobId", "id");
        if (id == null) id = text(root, "aggregateId", "jobId");
        UUID jobId;
        try { jobId = id == null ? null : UUID.fromString(id); } catch (IllegalArgumentException ex) { return; }
        Instant occurredAt = null;
        String timestamp = text(root, "occurredAt", "timestamp");
        if (timestamp != null) try { occurredAt = Instant.parse(timestamp); } catch (Exception ignored) { }
        service.applyJobEvent(eventType, jobId, data, occurredAt);
    }

    private static String text(JsonNode node, String... names) {
        for (String name : names) if (node.hasNonNull(name) && !node.get(name).asText().isBlank()) return node.get(name).asText();
        return null;
    }
}
