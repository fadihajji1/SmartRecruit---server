ALTER TABLE applications ADD COLUMN job_title VARCHAR(200);
ALTER TABLE applications ADD COLUMN candidate_name VARCHAR(200);
ALTER TABLE applications ADD COLUMN candidate_email VARCHAR(320);
UPDATE applications a SET job_title = j.title FROM jobs j WHERE j.id = a.job_id AND a.job_title IS NULL;
