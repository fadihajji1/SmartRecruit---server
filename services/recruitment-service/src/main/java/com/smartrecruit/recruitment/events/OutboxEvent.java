package com.smartrecruit.recruitment.events;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OutboxEvent(UUID eventId,String type,List<UUID> recipients,String title,String message,Instant occurredAt) {}
