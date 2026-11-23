-- Business profile fields shown on job pages and in the admin console
ALTER TABLE businesses ADD COLUMN website VARCHAR(500);
ALTER TABLE businesses ADD COLUMN industry VARCHAR(120);
ALTER TABLE businesses ADD COLUMN city VARCHAR(120);
ALTER TABLE businesses ADD COLUMN country VARCHAR(120);

-- Applicant details captured from the candidate's token when applying, so recruiters
-- can review applications without a cross-service lookup
ALTER TABLE applications ADD COLUMN message VARCHAR(5000);
ALTER TABLE applications ADD COLUMN candidate_name VARCHAR(200);
ALTER TABLE applications ADD COLUMN candidate_email VARCHAR(320);

-- Hiring pipeline: PENDING -> REVIEWING -> INTERVIEW -> HIRED / REJECTED
ALTER TABLE applications DROP CONSTRAINT ck_application_status;
ALTER TABLE applications ADD CONSTRAINT ck_application_status
    CHECK (status IN ('PENDING', 'REVIEWING', 'INTERVIEW', 'HIRED', 'REJECTED', 'UNAVAILABLE'));

-- Transactional outbox: domain events are stored with the business change and
-- published to Kafka by a scheduler, so the API keeps working while Kafka is down
CREATE TABLE event_outbox (
    id UUID PRIMARY KEY,
    event_type VARCHAR(80) NOT NULL,
    payload TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    published_at TIMESTAMPTZ,
    attempts INTEGER NOT NULL DEFAULT 0,
    last_error TEXT
);
CREATE INDEX idx_event_outbox_pending ON event_outbox (created_at) WHERE published_at IS NULL;
