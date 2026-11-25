package com.smartrecruit.notification.events;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartrecruit.notification.model.NotificationType;
import com.smartrecruit.notification.service.NotificationService;

/**
 * Turns domain events from the shared topic into in-app notifications.
 * Recipients come from payload.recipientUserId(s); otherwise they are derived from the event type.
 * Failures are retried and then sent to the dead-letter topic by the container error handler.
 */
@Component
public class DomainEventConsumer {
	private final ObjectMapper mapper;
	private final NotificationService service;
	private final DateTimeFormatter dateFormat;

	public DomainEventConsumer(ObjectMapper mapper, NotificationService service,
			@Value("${notifications.display-zone:Africa/Tunis}") String zone) {
		this.mapper = mapper;
		this.service = service;
		this.dateFormat = DateTimeFormatter.ofPattern("EEE d MMM, HH:mm", Locale.ENGLISH).withZone(ZoneId.of(zone));
	}

	private record Message(String title, String content, NotificationType type, String link) {
	}

	@KafkaListener(topics = "${notifications.events.topic}")
	public void consume(String raw) throws Exception {
		JsonNode root = mapper.readTree(raw);
		String eventType = value(root, "eventType", "type");
		UUID eventId = parseUuid(value(root, "eventId", "id"));
		if (eventId == null || eventType == null) return;
		JsonNode p = root.path("payload");
		String type = eventType.replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase(Locale.ROOT);

		Set<UUID> recipients = new LinkedHashSet<>();
		add(recipients, value(p, "recipientUserId"));
		if (p.path("recipientUserIds").isArray()) p.path("recipientUserIds").forEach(id -> add(recipients, id.asText()));

		Message message = describe(type, p, recipients);
		if (message == null || recipients.isEmpty()) return;
		service.createFromEvent(eventId, type, recipients, message.title(), message.content(), message.type(),
				message.link());
	}

	private Message describe(String type, JsonNode p, Set<UUID> recipients) {
		String job = or(value(p, "jobTitle", "title"), "a role");
		String candidate = or(value(p, "candidateName"), "A candidate");
		String when = date(value(p, "scheduledAt"));
		switch (type) {
			case "APPLICATION_SUBMITTED":
				if (recipients.isEmpty()) add(recipients, value(p, "recruiterId"));
				return new Message("New application", candidate + " applied for " + job + ".",
						NotificationType.APPLICATION_UPDATE, "/recruiter");
			case "APPLICATION_STATUS_CHANGED":
				if (recipients.isEmpty()) add(recipients, value(p, "candidateId"));
				return statusMessage(job, value(p, "status"));
			case "INTERVIEW_SCHEDULED":
				if (recipients.isEmpty()) add(recipients, value(p, "candidateId"));
				return new Message("Interview scheduled", "Your interview for " + job + " is on " + when + ".",
						NotificationType.INTERVIEW_SCHEDULED, "/interviews");
			case "INTERVIEW_RESCHEDULED":
				if (recipients.isEmpty()) add(recipients, value(p, "candidateId"));
				return new Message("Interview moved", "Your interview for " + job + " now takes place on " + when + ".",
						NotificationType.INTERVIEW_SCHEDULED, "/interviews");
			case "INTERVIEW_UPDATED":
				if (recipients.isEmpty()) add(recipients, value(p, "candidateId"));
				return new Message("Interview details updated", "The meeting details for your " + job
						+ " interview changed.", NotificationType.INTERVIEW_SCHEDULED, "/interviews");
			case "INTERVIEW_CANCELLED":
				if (recipients.isEmpty()) {
					add(recipients, value(p, "candidateId"));
					add(recipients, value(p, "recruiterId"));
				}
				return new Message("Interview cancelled", "The " + job + " interview on " + when + " was cancelled.",
						NotificationType.INTERVIEW_SCHEDULED, "/interviews");
			case "INTERVIEW_COMPLETED":
				if (recipients.isEmpty()) add(recipients, value(p, "candidateId"));
				return new Message("Interview completed", "Thanks for interviewing for " + job
						+ ". The recruiter will follow up.", NotificationType.INTERVIEW_SCHEDULED, "/applications");
			case "INTERVIEW_DELAY_REQUESTED":
				if (recipients.isEmpty()) add(recipients, value(p, "recruiterId"));
				return new Message("New time requested", candidate + " asked to move the " + job + " interview to "
						+ date(value(p, "proposedScheduledAt")) + ".", NotificationType.INTERVIEW_SCHEDULED, "/interviews");
			case "INTERVIEW_DELAY_APPROVED":
				if (recipients.isEmpty()) add(recipients, value(p, "candidateId"));
				return new Message("New time accepted", "Your " + job + " interview moved to "
						+ date(value(p, "proposedScheduledAt")) + ".", NotificationType.INTERVIEW_SCHEDULED, "/interviews");
			case "INTERVIEW_DELAY_REJECTED":
				if (recipients.isEmpty()) add(recipients, value(p, "candidateId"));
				return new Message("New time declined", "The recruiter kept the original time for your " + job
						+ " interview: " + when + ".", NotificationType.INTERVIEW_SCHEDULED, "/interviews");
			default:
				break;
		}
		if (type.startsWith("SUBSCRIPTION_")) {
			if (recipients.isEmpty()) add(recipients, value(p, "ownerId", "userId"));
			String plan = or(value(p, "planCode"), "your plan");
			String action = type.substring("SUBSCRIPTION_".length());
			String content = switch (action) {
				case "CREATED" -> "Your " + plan + " plan is active.";
				case "DEFAULT_ASSIGNED", "DEFAULT" -> "You are on the " + plan + " plan.";
				case "UPGRADED" -> "You upgraded to " + plan + ". New features are available now.";
				case "DOWNGRADE_SCHEDULED" -> "Your plan changes to " + plan + " at the end of the current period.";
				case "DOWNGRADED" -> "Your plan changed to " + plan + ".";
				case "RENEWED" -> "Your " + plan + " plan was renewed.";
				case "ADMIN_UPDATED" -> "An administrator updated your " + plan + " subscription.";
				case "CANCELLED" -> "Your " + plan + " plan was cancelled. You keep access until the period ends.";
				default -> "Your subscription changed (" + label(action) + ").";
			};
			return new Message("Subscription update", content, NotificationType.SUBSCRIPTION_UPDATE, "/subscriptions");
		}
		// Job events feed analytics and recommendations, not inboxes; unknown events only reach explicit recipients
		if (type.startsWith("JOB_") || recipients.isEmpty()) return null;
		return new Message("SmartRecruit update", "There is a new update in your workspace.", NotificationType.GENERAL,
				null);
	}

	private String date(String iso) {
		if (iso == null) return "the planned date";
		try {
			return dateFormat.format(Instant.parse(iso));
		} catch (RuntimeException ex) {
			return iso;
		}
	}

	/** What the candidate reads when a recruiter moves their application. Kept in step with the recruiter's preview. */
	private static Message statusMessage(String job, String status) {
		String value = status == null ? "" : status.trim().toUpperCase(Locale.ROOT);
		return switch (value) {
			case "HIRED" -> new Message("Congratulations!", "You have been selected for " + job
					+ ". The recruiter will contact you about the next steps.", NotificationType.APPLICATION_UPDATE, "/applications");
			case "REJECTED" -> new Message("Application update", "The company decided not to move forward with your application for "
					+ job + ". Thank you for applying, and good luck with your search.", NotificationType.APPLICATION_UPDATE, "/applications");
			case "INTERVIEW" -> new Message("Application update", "Your application for " + job
					+ " moved to the interview stage.", NotificationType.APPLICATION_UPDATE, "/applications");
			case "REVIEWING" -> new Message("Application update", "Your application for " + job
					+ " is now under review.", NotificationType.APPLICATION_UPDATE, "/applications");
			case "PENDING" -> new Message("Application update", "Your application for " + job
					+ " is back in the Applied stage.", NotificationType.APPLICATION_UPDATE, "/applications");
			default -> new Message("Application update", "Your application for " + job + " is now "
					+ label(status) + ".", NotificationType.APPLICATION_UPDATE, "/applications");
		};
	}

	private static String label(String value) {
		if (value == null) return "updated";
		String text = value.replace('_', ' ').toLowerCase(Locale.ROOT);
		return switch (text) {
			case "interview" -> "at interview stage";
			case "reviewing" -> "under review";
			default -> text;
		};
	}

	private static void add(Set<UUID> values, String value) {
		UUID id = parseUuid(value);
		if (id != null) values.add(id);
	}

	private static UUID parseUuid(String value) {
		try {
			return value == null || value.isBlank() ? null : UUID.fromString(value);
		} catch (IllegalArgumentException ex) {
			return null;
		}
	}

	private static String value(JsonNode node, String... names) {
		for (String name : names) {
			if (node.hasNonNull(name) && !node.get(name).asText().isBlank()) return node.get(name).asText();
		}
		return null;
	}

	private static String or(String value, String fallback) {
		return value == null ? fallback : value;
	}
}
