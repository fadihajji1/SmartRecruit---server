-- Event-driven notifications address users by id (from Kafka events); manual ones by email
ALTER TABLE notifications ALTER COLUMN recipient_email DROP NOT NULL;
ALTER TABLE notifications ADD COLUMN recipient_user_id UUID;
ALTER TABLE notifications ADD COLUMN source_event_id UUID;
ALTER TABLE notifications ADD COLUMN event_type VARCHAR(80);
ALTER TABLE notifications ADD COLUMN link VARCHAR(300);
-- One notification per event and recipient: Kafka redelivery is idempotent
CREATE UNIQUE INDEX uk_notifications_event_recipient
    ON notifications (source_event_id, recipient_user_id) WHERE source_event_id IS NOT NULL;
CREATE INDEX idx_notifications_recipient_user ON notifications (recipient_user_id, is_read, created_at DESC);
