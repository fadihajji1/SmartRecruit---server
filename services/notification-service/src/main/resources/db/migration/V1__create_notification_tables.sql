CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    recipient_email VARCHAR(320) NOT NULL,
    recipient_role VARCHAR(32),
    title VARCHAR(200) NOT NULL,
    message VARCHAR(2000) NOT NULL,
    type VARCHAR(50) NOT NULL DEFAULT 'GENERAL',
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    read_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_notifications_recipient_email ON notifications(recipient_email);
CREATE INDEX idx_notifications_recipient_read ON notifications(recipient_email, is_read);
