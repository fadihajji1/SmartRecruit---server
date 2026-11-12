package com.smartrecruit.notification.events;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartrecruit.notification.service.NotificationService;

@Component
public class DomainEventConsumer {
    private final ObjectMapper mapper;
    private final NotificationService service;
    public DomainEventConsumer(ObjectMapper mapper, NotificationService service) { this.mapper = mapper; this.service = service; }

    @KafkaListener(topics = "${notifications.events.topic}")
    public void consume(String raw) throws Exception {
        JsonNode root = mapper.readTree(raw);
        String eventType = value(root, "eventType", "type", "name");
        String eventIdText = value(root, "eventId", "id");
        UUID eventId = parseUuid(eventIdText);
        if (eventId == null || eventType == null) return;
        JsonNode payload = root.path("payload");
        Set<UUID> recipients = new LinkedHashSet<>();
        add(recipients, value(payload, "recipientUserId", "recipientId"));
        JsonNode recipientList = payload.path("recipientUserIds");
        if (recipientList.isArray()) recipientList.forEach(id -> add(recipients, id.asText()));
        String normalized = eventType.replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase(Locale.ROOT);
        String type = "SYSTEM";
        String title = "SmartRecruit update";
        String content = "There is a new update in your SmartRecruit workspace.";

        if (normalized.contains("APPLICATION_SUBMITTED")) {
            add(recipients, value(payload, "recruiterId", "ownerUserId")); type = "APPLICATION"; title = "New application received";
            content = "A candidate submitted an application" + suffix(value(payload, "jobTitle", "title")) + ".";
        } else if (normalized.contains("APPLICATION_STATUS")) {
            add(recipients, value(payload, "candidateId")); type = "APPLICATION"; title = "Application status updated";
            content = "Your application status changed to " + safe(value(payload, "status", "newStatus"), "updated") + suffix(value(payload, "jobTitle")) + ".";
        } else if (normalized.contains("INTERVIEW")) {
            add(recipients, value(payload, "candidateId")); add(recipients, value(payload, "recruiterId")); type = "INTERVIEW";
            title = normalized.contains("CANCEL") ? "Interview cancelled" : "Interview updated";
            content = "An interview" + suffix(value(payload, "jobTitle", "title")) + " was updated.";
        } else if (normalized.contains("SUBSCRIPTION")) {
            add(recipients, value(payload, "ownerId", "userId")); type = "SUBSCRIPTION"; title = "Subscription updated";
            content = "Your subscription" + suffix(value(payload, "planCode")) + " was updated.";
        } else if (recipients.isEmpty()) {
            return;
        }
        service.createFromEvent(eventId, eventType, recipients, title, content, type);
    }

    private static void add(Set<UUID> values, String value) { UUID id = parseUuid(value); if (id != null) values.add(id); }
    private static UUID parseUuid(String value) { try { return value == null || value.isBlank() ? null : UUID.fromString(value); } catch (IllegalArgumentException ex) { return null; } }
    private static String value(JsonNode node, String... names) { for (String name:names) if (node.hasNonNull(name) && !node.get(name).asText().isBlank()) return node.get(name).asText(); return null; }
    private static String suffix(String value) { return value == null ? "" : " for " + value; }
    private static String safe(String value, String fallback) { return value == null ? fallback : value; }
}
