package com.smartrecruit.notification.events;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
@Component public class RecruitmentEventConsumer{
@KafkaListener(topics="smartrecruit.recruitment.events",groupId="notification-service")public void consume(String event){
/* persisted by contract-specific handler in next iteration */}
}

