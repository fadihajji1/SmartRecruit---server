CREATE TABLE event_outbox (
 id UUID PRIMARY KEY,
 payload TEXT NOT NULL,
 created_at TIMESTAMPTZ NOT NULL,
 published_at TIMESTAMPTZ,
 attempts INTEGER NOT NULL DEFAULT 0,
 last_error TEXT
);
CREATE INDEX idx_event_outbox_pending ON event_outbox(created_at) WHERE published_at IS NULL;
