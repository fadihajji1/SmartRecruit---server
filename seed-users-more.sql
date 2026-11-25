-- Additional users seed script for SmartRecruit demo data
-- Adding more candidates and recruiters from different industries

-- Additional Candidates
INSERT INTO users (id, user_type, email, password_hash, full_name, phone, address, city, state, country, postal_code, role, status, created_at, updated_at)
VALUES
(gen_random_uuid(), 'CANDIDATE', 'alex.martinez@email.com', '$2a$10$dummy_hash_for_demo', 'Alex Martinez', '+1-555-0111', '123 Tech St', 'Denver', 'CO', 'USA', '80202', 'CANDIDATE', 'ACTIVE', NOW(), NOW()),
(gen_random_uuid(), 'CANDIDATE', 'sophia.liu@email.com', '$2a$10$dummy_hash_for_demo', 'Sophia Liu', '+1-555-0112', '456 Innovation Ave', 'Portland', 'OR', 'USA', '97201', 'CANDIDATE', 'ACTIVE', NOW(), NOW()),
(gen_random_uuid(), 'CANDIDATE', 'jordan.taylor@email.com', '$2a$10$dummy_hash_for_demo', 'Jordan Taylor', '+1-555-0113', '789 Creative Blvd', 'Miami', 'FL', 'USA', '33101', 'CANDIDATE', 'ACTIVE', NOW(), NOW()),
(gen_random_uuid(), 'CANDIDATE', 'riley.anderson@email.com', '$2a$10$dummy_hash_for_demo', 'Riley Anderson', '+1-555-0114', '321 Design Dr', 'Atlanta', 'GA', 'USA', '30301', 'CANDIDATE', 'ACTIVE', NOW(), NOW()),
(gen_random_uuid(), 'CANDIDATE', 'casey.morgan@email.com', '$2a$10$dummy_hash_for_demo', 'Casey Morgan', '+1-555-0115', '654 Analytics Ln', 'Phoenix', 'AZ', 'USA', '85001', 'CANDIDATE', 'ACTIVE', NOW(), NOW()),
(gen_random_uuid(), 'CANDIDATE', 'quinn.brown@email.com', '$2a$10$dummy_hash_for_demo', 'Quinn Brown', '+1-555-0116', '987 Security Way', 'Nashville', 'TN', 'USA', '37201', 'CANDIDATE', 'ACTIVE', NOW(), NOW()),
(gen_random_uuid(), 'CANDIDATE', 'avery.garcia@email.com', '$2a$10$dummy_hash_for_demo', 'Avery Garcia', '+1-555-0117', '147 Cloud Ct', 'Dallas', 'TX', 'USA', '75201', 'CANDIDATE', 'ACTIVE', NOW(), NOW()),
(gen_random_uuid(), 'CANDIDATE', 'harper.lee@email.com', '$2a$10$dummy_hash_for_demo', 'Harper Lee', '+1-555-0118', '258 Mobile Rd', 'Minneapolis', 'MN', 'USA', '55401', 'CANDIDATE', 'ACTIVE', NOW(), NOW()),
(gen_random_uuid(), 'CANDIDATE', 'skyler.white@email.com', '$2a$10$dummy_hash_for_demo', 'Skyler White', '+1-555-0119', '369 Startup St', 'San Diego', 'CA', 'USA', '92101', 'CANDIDATE', 'ACTIVE', NOW(), NOW()),
(gen_random_uuid(), 'CANDIDATE', 'rowan.harris@email.com', '$2a$10$dummy_hash_for_demo', 'Rowan Harris', '+1-555-0120', '741 Enterprise Blvd', 'Philadelphia', 'PA', 'USA', '19101', 'CANDIDATE', 'ACTIVE', NOW(), NOW());

-- Additional Recruiters from different industries
INSERT INTO users (id, user_type, email, password_hash, full_name, phone, address, city, state, country, postal_code, role, status, created_at, updated_at)
VALUES
(gen_random_uuid(), 'RECRUITER', 'sarah.connor@fintech.com', '$2a$10$dummy_hash_for_demo', 'Sarah Connor', '+1-555-0301', 'FinTech Corp', 'New York', 'NY', 'USA', '10017', 'RECRUITER', 'ACTIVE', NOW(), NOW()),
(gen_random_uuid(), 'RECRUITER', 'john.smith@healthtech.io', '$2a$10$dummy_hash_for_demo', 'John Smith', '+1-555-0302', 'HealthTech Inc', 'Boston', 'MA', 'USA', '02111', 'RECRUITER', 'ACTIVE', NOW(), NOW()),
(gen_random_uuid(), 'RECRUITER', 'emma.wilson@edtech.edu', '$2a$10$dummy_hash_for_demo', 'Emma Wilson', '+1-555-0303', 'EduTech Solutions', 'Austin', 'TX', 'USA', '78703', 'RECRUITER', 'ACTIVE', NOW(), NOW()),
(gen_random_uuid(), 'RECRUITER', 'mike.johnson@retailtech.com', '$2a$10$dummy_hash_for_demo', 'Mike Johnson', '+1-555-0304', 'RetailTech Corp', 'Chicago', 'IL', 'USA', '60602', 'RECRUITER', 'ACTIVE', NOW(), NOW()),
(gen_random_uuid(), 'RECRUITER', 'olivia.brown@logitech.com', '$2a$10$dummy_hash_for_demo', 'Olivia Brown', '+1-555-0305', 'LogiTech Systems', 'Seattle', 'WA', 'USA', '98110', 'RECRUITER', 'ACTIVE', NOW(), NOW()),
(gen_random_uuid(), 'RECRUITER', 'liam.davis@ai-startup.ai', '$2a$10$dummy_hash_for_demo', 'Liam Davis', '+1-555-0306', 'AI Startup Inc', 'San Francisco', 'CA', 'USA', '94106', 'RECRUITER', 'ACTIVE', NOW(), NOW()),
(gen_random_uuid(), 'RECRUITER', 'ava.miller@cybersecurity.io', '$2a$10$dummy_hash_for_demo', 'Ava Miller', '+1-555-0307', 'CyberSec Corp', 'Atlanta', 'GA', 'USA', '30302', 'RECRUITER', 'ACTIVE', NOW(), NOW()),
(gen_random_uuid(), 'RECRUITER', 'noah.garcia@cloudservices.com', '$2a$10$dummy_hash_for_demo', 'Noah Garcia', '+1-555-0308', 'CloudServices Inc', 'Denver', 'CO', 'USA', '80203', 'RECRUITER', 'ACTIVE', NOW(), NOW());

-- Create candidate profiles for new candidates
INSERT INTO candidate_profiles (user_id)
SELECT id FROM users 
WHERE role = 'CANDIDATE' 
AND email IN ('alex.martinez@email.com', 'sophia.liu@email.com', 'jordan.taylor@email.com', 'riley.anderson@email.com', 'casey.morgan@email.com', 'quinn.brown@email.com', 'avery.garcia@email.com', 'harper.lee@email.com', 'skyler.white@email.com', 'rowan.harris@email.com')
AND id NOT IN (SELECT user_id FROM candidate_profiles);

-- Create recruiter profiles for new recruiters
INSERT INTO recruiter_profiles (user_id, employment_proof_object_key, approval_status, approved_at)
SELECT id, 'proof/' || id || '.pdf', 'ACTIVE', NOW() 
FROM users 
WHERE role = 'RECRUITER' 
AND email IN ('sarah.connor@fintech.com', 'john.smith@healthtech.io', 'emma.wilson@edtech.edu', 'mike.johnson@retailtech.com', 'olivia.brown@logitech.com', 'liam.davis@ai-startup.ai', 'ava.miller@cybersecurity.io', 'noah.garcia@cloudservices.com')
AND id NOT IN (SELECT user_id FROM recruiter_profiles);

-- Show the results
SELECT 'Additional users seeded successfully!' as status;
SELECT COUNT(*) as total_users FROM users;
SELECT COUNT(*) as total_candidates FROM users WHERE role = 'CANDIDATE';
SELECT COUNT(*) as total_recruiters FROM users WHERE role = 'RECRUITER';
