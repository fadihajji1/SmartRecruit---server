ALTER TABLE jobs ADD COLUMN published_at TIMESTAMPTZ;
ALTER TABLE jobs ADD COLUMN expires_at TIMESTAMPTZ;
CREATE INDEX idx_jobs_expiration ON jobs(expires_at) WHERE status = 'PUBLISHED';
