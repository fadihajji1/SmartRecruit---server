package com.smartrecruit.notification.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartrecruit.notification.domain.Notification;
import com.smartrecruit.notification.repo.NotificationRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RecruitmentEventConsumer {
    public record RecruitmentEvent(UUID eventId,String type,List<UUID> recipients,String title,String message,Instant occurredAt) {}
    private final ObjectMapper mapper;
    private final NotificationRepository notifications;
    public RecruitmentEventConsumer(ObjectMapper mapper,NotificationRepository notifications) { this.mapper=mapper;this.notifications=notifications; }

    @KafkaListener(topics="${recruitment.events.topic:smartrecruit.recruitment.events}",groupId="notification-service")
    @Transactional
    public void consume(String payload) {
        try {
            RecruitmentEvent event=mapper.readValue(payload,RecruitmentEvent.class);
            if(event.eventId()==null||event.type()==null||event.recipients()==null)throw new IllegalArgumentException("Invalid recruitment event");
            for(UUID recipient:event.recipients()) {
                if(recipient==null||notifications.existsBySourceEventIdAndRecipientUserId(event.eventId(),recipient))continue;
                Notification n=new Notification();n.sourceEventId=event.eventId();n.recipientUserId=recipient;
                n.type=event.type();n.title=event.title();n.content=event.message();n.createdAt=event.occurredAt()==null?Instant.now():event.occurredAt();
                notifications.save(n);
            }
        } catch(Exception e) { throw new IllegalStateException("Could not process recruitment event",e); }
    }
}
