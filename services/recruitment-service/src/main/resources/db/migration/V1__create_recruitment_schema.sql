CREATE TABLE businesses (
    id UUID PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(2000),
    address VARCHAR(500),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_business_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE TABLE business_members (
    id UUID PRIMARY KEY,
    business_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_business_member_business
        FOREIGN KEY (business_id) REFERENCES businesses (id) ON DELETE CASCADE,
    CONSTRAINT uk_business_member_user UNIQUE (business_id, user_id),
    CONSTRAINT ck_business_member_role CHECK (role IN ('OWNER', 'RECRUITER'))
);

CREATE INDEX idx_business_members_user ON business_members (user_id);

CREATE TABLE job_offers (
    id UUID PRIMARY KEY,
    business_id UUID NOT NULL,
    created_by_user_id UUID NOT NULL,
    title VARCHAR(250) NOT NULL,
    description TEXT NOT NULL,
    location VARCHAR(250),
    contract_type VARCHAR(50),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_job_offer_business
        FOREIGN KEY (business_id) REFERENCES businesses (id),
    CONSTRAINT ck_job_offer_status CHECK (status IN ('OPEN', 'CLOSED', 'DELETED'))
);

CREATE INDEX idx_job_offers_business ON job_offers (business_id);
CREATE INDEX idx_job_offers_public_search ON job_offers (status, created_at DESC);

CREATE TABLE applications (
    id UUID PRIMARY KEY,
    job_id UUID NOT NULL,
    candidate_id UUID NOT NULL,
    candidate_cv_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_application_job
        FOREIGN KEY (job_id) REFERENCES job_offers (id),
    CONSTRAINT uk_application_job_candidate UNIQUE (job_id, candidate_id),
    CONSTRAINT ck_application_status CHECK (status IN ('PENDING', 'HIRED', 'REJECTED', 'UNAVAILABLE'))
);

CREATE INDEX idx_applications_candidate_created ON applications (candidate_id, created_at DESC);
CREATE INDEX idx_applications_job_status ON applications (job_id, status);
