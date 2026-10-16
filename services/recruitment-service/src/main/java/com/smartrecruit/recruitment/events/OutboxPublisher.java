package com.smartrecruit.recruitment.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartrecruit.recruitment.repo.OutboxRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OutboxPublisher {
    private final OutboxRepository repository;
    private final ObjectMapper mapper;
    private final KafkaTemplate<String,String> kafka;
    @Value("${recruitment.events.topic:smartrecruit.recruitment.events}") private String topic;
    public OutboxPublisher(OutboxRepository repository,ObjectMapper mapper,KafkaTemplate<String,String> kafka) {
        this.repository=repository;this.mapper=mapper;this.kafka=kafka;
    }
    @Transactional public void enqueue(String type,List<UUID> recipients,String title,String message) {
        try {
            OutboxEvent event=new OutboxEvent(UUID.randomUUID(),type,recipients.stream().distinct().toList(),title,message,Instant.now());
            OutboxMessage row=new OutboxMessage();row.id=event.eventId();row.payload=mapper.writeValueAsString(event);repository.save(row);
        } catch(Exception e) { throw new IllegalStateException("Could not persist recruitment event",e); }
    }
    @Scheduled(fixedDelayString="${recruitment.events.publish-delay-ms:1000}")
    @Transactional
    public void publishPending() {
        for(OutboxMessage message:repository.findTop100ByPublishedAtIsNullOrderByCreatedAtAsc()) {
            try {
                kafka.send(topic,message.id.toString(),message.payload).get(10,TimeUnit.SECONDS);
                message.publishedAt=Instant.now();message.lastError=null;
            } catch(Exception e) {
                message.attempts++;
                String error=e.getMessage();message.lastError=error==null?e.getClass().getSimpleName():error.substring(0,Math.min(error.length(),2000));
            }
        }
    }
}
