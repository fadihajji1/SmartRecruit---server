CREATE TABLE cvs (
    id UUID PRIMARY KEY,
    candidate_id UUID NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    object_key VARCHAR(180) NOT NULL UNIQUE,
    content_type VARCHAR(120) NOT NULL,
    size_bytes BIGINT NOT NULL CHECK (size_bytes BETWEEN 1 AND 10485760),
    sha256 VARCHAR(64) NOT NULL,
    status VARCHAR(16) NOT NULL,
    uploaded_at TIMESTAMPTZ NOT NULL,
    deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX ix_cvs_candidate_status_upload ON cvs(candidate_id, status, uploaded_at DESC);
CREATE INDEX ix_cvs_candidate_hash ON cvs(candidate_id, sha256, status);

CREATE TABLE cv_analysis (
    id UUID PRIMARY KEY,
    cv_id UUID NOT NULL REFERENCES cvs(id),
    analysis_type VARCHAR(32) NOT NULL,
    provider VARCHAR(32) NOT NULL,
    model VARCHAR(80),
    status VARCHAR(16) NOT NULL,
    score NUMERIC(5,2),
    skills_json TEXT NOT NULL,
    matched_keywords_json TEXT NOT NULL,
    missing_keywords_json TEXT NOT NULL,
    suggestions_json TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX ix_cv_analysis_latest ON cv_analysis(cv_id, created_at DESC);

CREATE TABLE job_projection (
    job_id UUID PRIMARY KEY,
    business_id UUID,
    recruiter_id UUID,
    title VARCHAR(160) NOT NULL,
    description TEXT NOT NULL,
    location VARCHAR(160),
    status VARCHAR(24) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX ix_job_projection_published ON job_projection(status, updated_at DESC);
