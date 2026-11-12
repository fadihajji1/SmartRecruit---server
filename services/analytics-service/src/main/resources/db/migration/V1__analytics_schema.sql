CREATE TABLE analytics_events (
    id UUID PRIMARY KEY,
    event_id UUID NOT NULL UNIQUE,
    event_type VARCHAR(80) NOT NULL,
    aggregate_id UUID,
    business_id UUID,
    recruiter_id UUID,
    candidate_id UUID,
    job_id UUID,
    application_id UUID,
    interview_id UUID,
    status VARCHAR(24),
    title VARCHAR(160),
    occurred_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX ix_analytics_recruiter_time ON analytics_events(recruiter_id, occurred_at DESC);
CREATE INDEX ix_analytics_job_recruiter ON analytics_events(job_id, recruiter_id, occurred_at DESC);
CREATE INDEX ix_analytics_application_recruiter ON analytics_events(application_id, recruiter_id, occurred_at DESC);
