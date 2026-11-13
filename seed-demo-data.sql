-- Seed script for SmartRecruit demo data
-- This creates realistic users and job opportunities

-- First, let's create some candidate users
INSERT INTO users (id, user_type, email, password_hash, full_name, phone, address, city, state, country, postal_code, role, status, created_at, updated_at)
VALUES
-- Candidates
(gen_random_uuid(), 'CANDIDATE', 'sarah.johnson@email.com', '$2a$10$dummy_hash_for_demo', 'Sarah Johnson', '+1-555-0101', '123 Main St', 'San Francisco', 'CA', 'USA', '94102', 'CANDIDATE', 'ACTIVE', NOW(), NOW()),
(gen_random_uuid(), 'CANDIDATE', 'michael.chen@email.com', '$2a$10$dummy_hash_for_demo', 'Michael Chen', '+1-555-0102', '456 Oak Ave', 'New York', 'NY', 'USA', '10001', 'CANDIDATE', 'ACTIVE', NOW(), NOW()),
(gen_random_uuid(), 'CANDIDATE', 'emily.rodriguez@email.com', '$2a$10$dummy_hash_for_demo', 'Emily Rodriguez', '+1-555-0103', '789 Pine Rd', 'Austin', 'TX', 'USA', '78701', 'CANDIDATE', 'ACTIVE', NOW(), NOW()),
(gen_random_uuid(), 'CANDIDATE', 'james.wilson@email.com', '$2a$10$dummy_hash_for_demo', 'James Wilson', '+1-555-0104', '321 Elm St', 'Seattle', 'WA', 'USA', '98101', 'CANDIDATE', 'ACTIVE', NOW(), NOW()),
(gen_random_uuid(), 'CANDIDATE', 'priya.patel@email.com', '$2a$10$dummy_hash_for_demo', 'Priya Patel', '+1-555-0105', '654 Maple Dr', 'Boston', 'MA', 'USA', '02101', 'CANDIDATE', 'ACTIVE', NOW(), NOW()),
(gen_random_uuid(), 'CANDIDATE', 'david.kim@email.com', '$2a$10$dummy_hash_for_demo', 'David Kim', '+1-555-0106', '987 Cedar Ln', 'Chicago', 'IL', 'USA', '60601', 'CANDIDATE', 'ACTIVE', NOW(), NOW()),

-- Recruiters
(gen_random_uuid(), 'RECRUITER', 'jessica.martinez@techcorp.com', '$2a$10$dummy_hash_for_demo', 'Jessica Martinez', '+1-555-0201', 'Tech Corp HQ', 'San Francisco', 'CA', 'USA', '94105', 'RECRUITER', 'ACTIVE', NOW(), NOW()),
(gen_random_uuid(), 'RECRUITER', 'robert.thompson@innovate.io', '$2a$10$dummy_hash_for_demo', 'Robert Thompson', '+1-555-0202', 'Innovate Inc', 'New York', 'NY', 'USA', '10016', 'RECRUITER', 'ACTIVE', NOW(), NOW()),
(gen_random_uuid(), 'RECRUITER', 'lisa.anderson@startup.co', '$2a$10$dummy_hash_for_demo', 'Lisa Anderson', '+1-555-0203', 'Startup HQ', 'Austin', 'TX', 'USA', '78702', 'RECRUITER', 'ACTIVE', NOW(), NOW()),
(gen_random_uuid(), 'RECRUITER', 'mark.davis@enterprise.com', '$2a$10$dummy_hash_for_demo', 'Mark Davis', '+1-555-0204', 'Enterprise Corp', 'Seattle', 'WA', 'USA', '98109', 'RECRUITER', 'ACTIVE', NOW(), NOW()),
(gen_random_uuid(), 'RECRUITER', 'amanda.wright@global.com', '$2a$10$dummy_hash_for_demo', 'Amanda Wright', '+1-555-0205', 'Global Inc', 'Boston', 'MA', 'USA', '02110', 'RECRUITER', 'ACTIVE', NOW(), NOW());

-- Create candidate profiles for candidates
INSERT INTO candidate_profiles (user_id)
SELECT id FROM users WHERE role = 'CANDIDATE';

-- Create recruiter profiles for recruiters
INSERT INTO recruiter_profiles (user_id, employment_proof_object_key, approval_status, approved_at)
SELECT id, 'proof/' || id || '.pdf', 'ACTIVE', NOW() FROM users WHERE role = 'RECRUITER';

-- Now let's create job opportunities in the candidate database
-- First, get some recruiter IDs to use as business_id/recruiter_id
DO $$
DECLARE
    recruiter1 UUID;
    recruiter2 UUID;
    recruiter3 UUID;
    recruiter4 UUID;
    recruiter5 UUID;
BEGIN
    -- Get recruiter IDs
    SELECT id INTO recruiter1 FROM users WHERE email = 'jessica.martinez@techcorp.com';
    SELECT id INTO recruiter2 FROM users WHERE email = 'robert.thompson@innovate.io';
    SELECT id INTO recruiter3 FROM users WHERE email = 'lisa.anderson@startup.co';
    SELECT id INTO recruiter4 FROM users WHERE email = 'mark.davis@enterprise.com';
    SELECT id INTO recruiter5 FROM users WHERE email = 'amanda.wright@global.com';

    -- Insert job projections
    INSERT INTO job_projection (job_id, business_id, recruiter_id, title, description, location, status, updated_at)
    VALUES
    -- Tech Corp jobs
    (gen_random_uuid(), gen_random_uuid(), recruiter1, 'Senior Software Engineer', 'We are looking for a Senior Software Engineer to join our backend team. You will work on scalable microservices architecture using Java, Spring Boot, and cloud technologies. Experience with distributed systems and high-availability applications required.', 'San Francisco, CA', 'PUBLISHED', NOW()),
    (gen_random_uuid(), gen_random_uuid(), recruiter1, 'DevOps Engineer', 'Join our DevOps team to help us scale our infrastructure. You will work with Kubernetes, Docker, AWS, and CI/CD pipelines. Strong experience with infrastructure as code and monitoring tools required.', 'San Francisco, CA', 'PUBLISHED', NOW()),
    (gen_random_uuid(), gen_random_uuid(), recruiter1, 'Product Manager', 'We need a Product Manager to lead our mobile app development. You will work closely with engineering and design teams to deliver user-centric features. Experience with agile methodologies and product strategy required.', 'San Francisco, CA', 'PUBLISHED', NOW()),

    -- Innovate Inc jobs
    (gen_random_uuid(), gen_random_uuid(), recruiter2, 'Frontend Developer', 'We are hiring a Frontend Developer to build modern web applications. You will work with React, TypeScript, and modern CSS frameworks. Experience with responsive design and performance optimization required.', 'New York, NY', 'PUBLISHED', NOW()),
    (gen_random_uuid(), gen_random_uuid(), recruiter2, 'Data Scientist', 'Join our data science team to build ML models and analytics solutions. You will work with Python, TensorFlow, and big data technologies. Strong background in statistics and machine learning required.', 'New York, NY', 'PUBLISHED', NOW()),
    (gen_random_uuid(), gen_random_uuid(), recruiter2, 'UX Designer', 'We need a UX Designer to create intuitive user experiences. You will conduct user research, create wireframes, and design beautiful interfaces. Experience with design systems and accessibility required.', 'New York, NY', 'PUBLISHED', NOW()),

    -- Startup jobs
    (gen_random_uuid(), gen_random_uuid(), recruiter3, 'Full Stack Developer', 'Join our fast-paced startup as a Full Stack Developer. You will work on both frontend and backend using modern technologies. Experience with Node.js, React, and databases required. Startup experience preferred.', 'Austin, TX', 'PUBLISHED', NOW()),
    (gen_random_uuid(), gen_random_uuid(), recruiter3, 'Mobile Developer', 'We are looking for a Mobile Developer to build our iOS and Android apps. You will work with React Native or Flutter. Experience with mobile app development and app store deployment required.', 'Austin, TX', 'PUBLISHED', NOW()),
    (gen_random_uuid(), gen_random_uuid(), recruiter3, 'Growth Hacker', 'Join our growth team to drive user acquisition and retention. You will work with analytics tools, A/B testing, and marketing automation. Experience with growth strategies and data analysis required.', 'Austin, TX', 'PUBLISHED', NOW()),

    -- Enterprise jobs
    (gen_random_uuid(), gen_random_uuid(), recruiter4, 'Cloud Architect', 'We need a Cloud Architect to design and implement our cloud infrastructure. You will work with AWS, Azure, or GCP. Experience with enterprise architecture and security best practices required.', 'Seattle, WA', 'PUBLISHED', NOW()),
    (gen_random_uuid(), gen_random_uuid(), recruiter4, 'Security Engineer', 'Join our security team to protect our systems and data. You will work on security monitoring, incident response, and compliance. Experience with security frameworks and tools required.', 'Seattle, WA', 'PUBLISHED', NOW()),
    (gen_random_uuid(), gen_random_uuid(), recruiter4, 'Technical Lead', 'We are looking for a Technical Lead to guide our engineering team. You will provide technical guidance, code reviews, and mentorship. Experience with team leadership and software architecture required.', 'Seattle, WA', 'PUBLISHED', NOW()),

    -- Global Inc jobs
    (gen_random_uuid(), gen_random_uuid(), recruiter5, 'Business Analyst', 'Join our business analysis team to bridge the gap between business and technology. You will gather requirements, analyze data, and create reports. Experience with SQL, Excel, and business process modeling required.', 'Boston, MA', 'PUBLISHED', NOW()),
    (gen_random_uuid(), gen_random_uuid(), recruiter5, 'QA Engineer', 'We need a QA Engineer to ensure our software quality. You will design test plans, execute tests, and report bugs. Experience with automated testing tools and Agile methodologies required.', 'Boston, MA', 'PUBLISHED', NOW()),
    (gen_random_uuid(), gen_random_uuid(), recruiter5, 'Technical Writer', 'Join our documentation team to create clear and comprehensive technical documentation. You will write user guides, API documentation, and internal wikis. Experience with technical writing and documentation tools required.', 'Boston, MA', 'PUBLISHED', NOW());
END $$;

-- Show the seeded data
SELECT 'Users seeded successfully!' as status;
SELECT COUNT(*) as total_users FROM users;
SELECT COUNT(*) as total_candidates FROM users WHERE role = 'CANDIDATE';
SELECT COUNT(*) as total_recruiters FROM users WHERE role = 'RECRUITER';
