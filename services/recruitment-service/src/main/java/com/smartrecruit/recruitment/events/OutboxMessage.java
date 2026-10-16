package com.smartrecruit.recruitment.events;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="event_outbox")
public class OutboxMessage {
    @Id public UUID id;
    @Column(nullable=false,columnDefinition="text") public String payload;
    @Column(nullable=false) public Instant createdAt;
    public Instant publishedAt;
    @Column(nullable=false) public int attempts;
    @Column(columnDefinition="text") public String lastError;
    @PrePersist void create() { if(id==null)id=UUID.randomUUID();if(createdAt==null)createdAt=Instant.now(); }
}
