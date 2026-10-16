CREATE UNIQUE INDEX uq_notifications_event_recipient
ON notifications(source_event_id, recipient_user_id)
WHERE source_event_id IS NOT NULL;
