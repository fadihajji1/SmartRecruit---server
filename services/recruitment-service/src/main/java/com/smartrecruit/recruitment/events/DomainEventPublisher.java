package com.smartrecruit.recruitment.events;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.smartrecruit.recruitment.model.OutboxMessage;
import com.smartrecruit.recruitment.repository.OutboxRepository;

import tools.jackson.databind.json.JsonMapper;

/**
 * Transactional outbox for recruitment domain events.
 * {@link #enqueue} stores the event in the same transaction as the business change; a scheduler then pushes
 * pending rows to the shared domain-events topic. Envelope (consumed by notification, analytics and
 * candidate-ai): eventId, eventType, aggregateId, occurredAt, payload.
 */
@Component
public class DomainEventPublisher implements DisposableBean {
	private static final Logger log = LoggerFactory.getLogger(DomainEventPublisher.class);

	private final OutboxRepository outbox;
	private final KafkaTemplate<String, String> kafka;
	private final DefaultKafkaProducerFactory<String, String> producerFactory;
	private final JsonMapper mapper = JsonMapper.builder().build();
	private final String topic;

	public DomainEventPublisher(OutboxRepository outbox,
			@Value("${spring.kafka.bootstrap-servers:localhost:9093}") String bootstrapServers,
			@Value("${recruitment.events.topic:smartrecruit.domain-events}") String topic) {
		this.outbox = outbox;
		this.topic = topic;
		// Spring Boot 4 no longer auto-configures a KafkaTemplate from plain spring-kafka, so build one here.
		this.producerFactory = new DefaultKafkaProducerFactory<>(Map.of(
				ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
				ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
				ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
				ProducerConfig.ACKS_CONFIG, "all",
				ProducerConfig.MAX_BLOCK_MS_CONFIG, 5000));
		this.kafka = new KafkaTemplate<>(producerFactory);
	}

	/** Stores an event; must be called inside the transaction that performs the business change. */
	@Transactional(propagation = Propagation.MANDATORY)
	public void enqueue(String eventType, UUID aggregateId, Map<String, Object> payload) {
		UUID eventId = UUID.randomUUID();
		Map<String, Object> event = new LinkedHashMap<>();
		event.put("eventId", eventId.toString());
		event.put("eventType", eventType);
		event.put("aggregateId", aggregateId == null ? null : aggregateId.toString());
		event.put("occurredAt", Instant.now().toString());
		event.put("payload", payload);
		outbox.save(new OutboxMessage(eventId, eventType, mapper.writeValueAsString(event)));
	}

	@Scheduled(fixedDelayString = "${recruitment.events.publish-delay-ms:3000}")
	@Transactional
	public void publishPending() {
		for (OutboxMessage message : outbox.findTop100ByPublishedAtIsNullOrderByCreatedAtAsc()) {
			try {
				kafka.send(topic, message.getId().toString(), message.getPayload()).get(10, TimeUnit.SECONDS);
				message.markPublished();
			} catch (Exception e) {
				if (e instanceof InterruptedException) Thread.currentThread().interrupt();
				message.markFailed(e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
				log.warn("Kafka unavailable, {} will be retried: {}", message.getEventType(), message.getLastError());
				break; // keep ordering; retry on the next tick
			}
		}
	}

	@Override
	public void destroy() {
		producerFactory.destroy();
	}
}
