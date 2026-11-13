-- Fixes rows already written with approval_status='APPROVED'
-- which cannot be mapped to Java AccountStatus enum (valid: ACTIVE, PENDING, REJECTED, INACTIVE)
-- Run against identity_db on port 15433:
--   docker exec -i smartrecruit-identity-postgres psql -U smartrecruit -d identity_db < repair_approval_status.sql

UPDATE recruiter_profiles
SET approval_status = 'ACTIVE'
WHERE approval_status = 'APPROVED';

-- Also fix any users.status rows with the same bad value, just in case:
UPDATE users
SET status = 'ACTIVE'
WHERE status = 'APPROVED';

SELECT 'approval_status repair done' AS result;
