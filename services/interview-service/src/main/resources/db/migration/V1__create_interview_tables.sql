CREATE TABLE interviews (
    id UUID PRIMARY KEY,
    application_id UUID NOT NULL,
    candidate_id UUID NOT NULL,
    recruiter_id UUID NOT NULL,
    job_id UUID NOT NULL,
    scheduled_at TIMESTAMP WITH TIME ZONE NOT NULL,
    external_meeting_url TEXT NOT NULL,
    status VARCHAR(24) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_interviews_candidate ON interviews(candidate_id, scheduled_at);
CREATE INDEX idx_interviews_recruiter ON interviews(recruiter_id, scheduled_at);

CREATE TABLE delay_requests (
    id UUID PRIMARY KEY,
    interview_id UUID NOT NULL REFERENCES interviews(id) ON DELETE CASCADE,
    requested_by_user_id UUID NOT NULL,
    proposed_scheduled_at TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(24) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    resolved_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_delay_requests_interview ON delay_requests(interview_id, status);
