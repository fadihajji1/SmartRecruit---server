-- Seed script for SmartRecruit demo data - Users only
-- This creates realistic users for candidates and recruiters

-- Create candidate users
INSERT INTO users (id, user_type, email, password_hash, full_name, phone, address, city, state, country, postal_code, role, status, created_at, updated_at)
VALUES
-- Candidates
(gen_random_uuid(), 'CANDIDATE', 'sarah.johnson@email.com', '$2a$10$dummy_hash_for_demo', 'Sarah Johnson', '+1-555-0101', '123 Main St', 'San Francisco', 'CA', 'USA', '94102', 'CANDIDATE', 'ACTIVE', NOW(), NOW()),
(gen_random_uuid(), 'CANDIDATE', 'michael.chen@email.com', '$2a$10$dummy_hash_for_demo', 'Michael Chen', '+1-555-0102', '456 Oak Ave', 'New York', 'NY', 'USA', '10001', 'CANDIDATE', 'ACTIVE', NOW(), NOW()),
(gen_random_uuid(), 'CANDIDATE', 'emily.rodriguez@email.com', '$2a$10$dummy_hash_for_demo', 'Emily Rodriguez', '+1-555-0103', '789 Pine Rd', 'Austin', 'TX', 'USA', '78701', 'CANDIDATE', 'ACTIVE', NOW(), NOW()),
(gen_random_uuid(), 'CANDIDATE', 'james.wilson@email.com', '$2a$10$dummy_hash_for_demo', 'James Wilson', '+1-555-0104', '321 Elm St', 'Seattle', 'WA', 'USA', '98101', 'CANDIDATE', 'ACTIVE', NOW(), NOW()),
(gen_random_uuid(), 'CANDIDATE', 'priya.patel@email.com', '$2a$10$dummy_hash_for_demo', 'Priya Patel', '+1-555-0105', '654 Maple Dr', 'Boston', 'MA', 'USA', '02101', 'CANDIDATE', 'ACTIVE', NOW(), NOW()),
(gen_random_uuid(), 'CANDIDATE', 'david.kim@email.com', '$2a$10$dummy_hash_for_demo', 'David Kim', '+1-555-0106', '987 Cedar Ln', 'Chicago', 'IL', 'USA', '60601', 'CANDIDATE', 'ACTIVE', NOW(), NOW());

-- Create recruiter users
INSERT INTO users (id, user_type, email, password_hash, full_name, phone, address, city, state, country, postal_code, role, status, created_at, updated_at)
VALUES
(gen_random_uuid(), 'RECRUITER', 'jessica.martinez@techcorp.com', '$2a$10$dummy_hash_for_demo', 'Jessica Martinez', '+1-555-0201', 'Tech Corp HQ', 'San Francisco', 'CA', 'USA', '94105', 'RECRUITER', 'ACTIVE', NOW(), NOW()),
(gen_random_uuid(), 'RECRUITER', 'robert.thompson@innovate.io', '$2a$10$dummy_hash_for_demo', 'Robert Thompson', '+1-555-0202', 'Innovate Inc', 'New York', 'NY', 'USA', '10016', 'RECRUITER', 'ACTIVE', NOW(), NOW()),
(gen_random_uuid(), 'RECRUITER', 'lisa.anderson@startup.co', '$2a$10$dummy_hash_for_demo', 'Lisa Anderson', '+1-555-0203', 'Startup HQ', 'Austin', 'TX', 'USA', '78702', 'RECRUITER', 'ACTIVE', NOW(), NOW()),
(gen_random_uuid(), 'RECRUITER', 'mark.davis@enterprise.com', '$2a$10$dummy_hash_for_demo', 'Mark Davis', '+1-555-0204', 'Enterprise Corp', 'Seattle', 'WA', 'USA', '98109', 'RECRUITER', 'ACTIVE', NOW(), NOW()),
(gen_random_uuid(), 'RECRUITER', 'amanda.wright@global.com', '$2a$10$dummy_hash_for_demo', 'Amanda Wright', '+1-555-0205', 'Global Inc', 'Boston', 'MA', 'USA', '02110', 'RECRUITER', 'ACTIVE', NOW(), NOW());

-- Create candidate profiles for new candidates
INSERT INTO candidate_profiles (user_id)
SELECT id FROM users 
WHERE role = 'CANDIDATE' 
AND email IN ('sarah.johnson@email.com', 'michael.chen@email.com', 'emily.rodriguez@email.com', 'james.wilson@email.com', 'priya.patel@email.com', 'david.kim@email.com')
AND id NOT IN (SELECT user_id FROM candidate_profiles);

-- Create recruiter profiles for new recruiters
INSERT INTO recruiter_profiles (user_id, employment_proof_object_key, approval_status, approved_at)
SELECT id, 'proof/' || id || '.pdf', 'ACTIVE', NOW() 
FROM users 
WHERE role = 'RECRUITER' 
AND email IN ('jessica.martinez@techcorp.com', 'robert.thompson@innovate.io', 'lisa.anderson@startup.co', 'mark.davis@enterprise.com', 'amanda.wright@global.com')
AND id NOT IN (SELECT user_id FROM recruiter_profiles);

-- Show the seeded data
SELECT 'Users seeded successfully!' as status;
SELECT COUNT(*) as total_users FROM users;
SELECT COUNT(*) as total_candidates FROM users WHERE role = 'CANDIDATE';
SELECT COUNT(*) as total_recruiters FROM users WHERE role = 'RECRUITER';
