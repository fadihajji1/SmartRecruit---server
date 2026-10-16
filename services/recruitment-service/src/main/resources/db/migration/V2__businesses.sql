CREATE TABLE businesses (
 id UUID PRIMARY KEY,
 owner_user_id UUID NOT NULL,
 name VARCHAR(200) NOT NULL,
 description TEXT,
 website VARCHAR(500),
 industry VARCHAR(150),
 address VARCHAR(255),
 city VARCHAR(100),
 country VARCHAR(100),
 status VARCHAR(30) NOT NULL,
 created_at TIMESTAMPTZ NOT NULL,
 updated_at TIMESTAMPTZ NOT NULL
);
ALTER TABLE jobs ADD CONSTRAINT fk_jobs_business FOREIGN KEY (business_id) REFERENCES businesses(id);
CREATE INDEX idx_jobs_status_created ON jobs(status, created_at DESC);
CREATE INDEX idx_jobs_business ON jobs(business_id);
CREATE INDEX idx_applications_candidate ON applications(candidate_id);
