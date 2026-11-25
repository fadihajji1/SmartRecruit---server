-- Details shown in candidate/recruiter calendars without cross-service lookups
ALTER TABLE interviews ADD COLUMN job_title VARCHAR(250);
ALTER TABLE interviews ADD COLUMN candidate_name VARCHAR(200);
ALTER TABLE interviews ADD COLUMN duration_minutes INTEGER NOT NULL DEFAULT 45;
ALTER TABLE interviews ADD COLUMN notes TEXT;
