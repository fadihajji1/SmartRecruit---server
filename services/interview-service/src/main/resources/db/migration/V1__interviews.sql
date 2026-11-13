CREATE TABLE interviews (
  id UUID PRIMARY KEY,
  application_id UUID NOT NULL,
  candidate_id UUID NOT NULL,
  recruiter_id UUID NOT NULL,
  job_id UUID,
  job_title VARCHAR(200),
  candidate_name VARCHAR(200),
  scheduled_at TIMESTAMPTZ NOT NULL,
  duration_minutes INTEGER NOT NULL DEFAULT 45,
  external_meeting_url TEXT,
  notes TEXT,
  status VARCHAR(30) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_interviews_recruiter ON interviews(recruiter_id, scheduled_at);
CREATE INDEX idx_interviews_candidate ON interviews(candidate_id, scheduled_at);
