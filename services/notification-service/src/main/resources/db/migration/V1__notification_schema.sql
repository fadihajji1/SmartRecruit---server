CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    recipient_user_id UUID NOT NULL,
    source_event_id UUID,
    event_type VARCHAR(80) NOT NULL,
    type VARCHAR(40) NOT NULL,
    title VARCHAR(160) NOT NULL,
    content TEXT NOT NULL,
    read_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    deleted_at TIMESTAMPTZ,
    CONSTRAINT uk_notification_event_recipient UNIQUE (source_event_id, recipient_user_id)
);
CREATE INDEX ix_notifications_recipient_created ON notifications(recipient_user_id, created_at DESC) WHERE deleted_at IS NULL;
