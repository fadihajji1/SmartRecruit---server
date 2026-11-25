-- Additional job opportunities seed script
-- Adding more jobs from different industries

-- First, get the new recruiter IDs
DO $$
DECLARE
    fintech_recruiter UUID;
    healthtech_recruiter UUID;
    edtech_recruiter UUID;
    retailtech_recruiter UUID;
    logitech_recruiter UUID;
    ai_recruiter UUID;
    cybersecurity_recruiter UUID;
    cloudservices_recruiter UUID;
BEGIN
    -- Get recruiter IDs
    SELECT id INTO fintech_recruiter FROM users WHERE email = 'sarah.connor@fintech.com';
    SELECT id INTO healthtech_recruiter FROM users WHERE email = 'john.smith@healthtech.io';
    SELECT id INTO edtech_recruiter FROM users WHERE email = 'emma.wilson@edtech.edu';
    SELECT id INTO retailtech_recruiter FROM users WHERE email = 'mike.johnson@retailtech.com';
    SELECT id INTO logitech_recruiter FROM users WHERE email = 'olivia.brown@logitech.com';
    SELECT id INTO ai_recruiter FROM users WHERE email = 'liam.davis@ai-startup.ai';
    SELECT id INTO cybersecurity_recruiter FROM users WHERE email = 'ava.miller@cybersecurity.io';
    SELECT id INTO cloudservices_recruiter FROM users WHERE email = 'noah.garcia@cloudservices.com';

    -- FinTech jobs
    INSERT INTO job_projection (job_id, business_id, recruiter_id, title, description, location, status, updated_at)
    VALUES
    (gen_random_uuid(), gen_random_uuid(), fintech_recruiter, 'Blockchain Developer', 'We are looking for a Blockchain Developer to build our decentralized finance platform. You will work with Ethereum, Solidity, and smart contracts. Experience with DeFi protocols and cryptocurrency required.', 'New York, NY', 'PUBLISHED', NOW()),
    (gen_random_uuid(), gen_random_uuid(), fintech_recruiter, 'Financial Analyst', 'Join our financial analysis team to drive investment decisions. You will analyze market trends, build financial models, and prepare reports. Experience with Excel, Bloomberg, and financial modeling required.', 'New York, NY', 'PUBLISHED', NOW()),
    (gen_random_uuid(), gen_random_uuid(), fintech_recruiter, 'Compliance Officer', 'We need a Compliance Officer to ensure our platform meets regulatory requirements. You will monitor compliance, conduct audits, and implement policies. Experience with financial regulations and compliance frameworks required.', 'New York, NY', 'PUBLISHED', NOW());

    -- HealthTech jobs
    INSERT INTO job_projection (job_id, business_id, recruiter_id, title, description, location, status, updated_at)
    VALUES
    (gen_random_uuid(), gen_random_uuid(), healthtech_recruiter, 'Healthcare Data Scientist', 'Join our healthcare data science team to improve patient outcomes. You will work with medical data, build predictive models, and analyze health trends. Experience with healthcare data and HIPAA compliance required.', 'Boston, MA', 'PUBLISHED', NOW()),
    (gen_random_uuid(), gen_random_uuid(), healthtech_recruiter, 'Telemedicine Developer', 'We are hiring a Telemedicine Developer to build our virtual care platform. You will work with video conferencing, EHR integration, and secure messaging. Experience with healthcare IT and telemedicine required.', 'Boston, MA', 'PUBLISHED', NOW()),
    (gen_random_uuid(), gen_random_uuid(), healthtech_recruiter, 'Medical Device Engineer', 'Join our medical device team to develop life-saving technology. You will work on hardware and software for medical devices. Experience with FDA regulations and medical device development required.', 'Boston, MA', 'PUBLISHED', NOW());

    -- EduTech jobs
    INSERT INTO job_projection (job_id, business_id, recruiter_id, title, description, location, status, updated_at)
    VALUES
    (gen_random_uuid(), gen_random_uuid(), edtech_recruiter, 'Learning Experience Designer', 'We need a Learning Experience Designer to create engaging educational content. You will design courses, create assessments, and optimize learning paths. Experience with instructional design and educational technology required.', 'Austin, TX', 'PUBLISHED', NOW()),
    (gen_random_uuid(), gen_random_uuid(), edtech_recruiter, 'EdTech Product Manager', 'Join our product team to drive educational innovation. You will manage our learning platform roadmap, work with educators, and analyze user feedback. Experience with edtech and product management required.', 'Austin, TX', 'PUBLISHED', NOW()),
    (gen_random_uuid(), gen_random_uuid(), edtech_recruiter, 'Virtual Reality Developer', 'We are looking for a VR Developer to create immersive learning experiences. You will work with Unity, VR headsets, and educational content. Experience with VR development and educational applications required.', 'Austin, TX', 'PUBLISHED', NOW());

    -- RetailTech jobs
    INSERT INTO job_projection (job_id, business_id, recruiter_id, title, description, location, status, updated_at)
    VALUES
    (gen_random_uuid(), gen_random_uuid(), retailtech_recruiter, 'E-commerce Developer', 'Join our e-commerce team to build our online marketplace. You will work with Shopify, payment systems, and inventory management. Experience with e-commerce platforms and web development required.', 'Chicago, IL', 'PUBLISHED', NOW()),
    (gen_random_uuid(), gen_random_uuid(), retailtech_recruiter, 'Supply Chain Analyst', 'We need a Supply Chain Analyst to optimize our logistics operations. You will analyze supply chain data, forecast demand, and improve efficiency. Experience with supply chain management and data analysis required.', 'Chicago, IL', 'PUBLISHED', NOW()),
    (gen_random_uuid(), gen_random_uuid(), retailtech_recruiter, 'Retail Systems Engineer', 'Join our retail systems team to maintain our POS and inventory systems. You will work with retail hardware, software integrations, and database management. Experience with retail technology and systems integration required.', 'Chicago, IL', 'PUBLISHED', NOW());

    -- LogiTech jobs
    INSERT INTO job_projection (job_id, business_id, recruiter_id, title, description, location, status, updated_at)
    VALUES
    (gen_random_uuid(), gen_random_uuid(), logitech_recruiter, 'IoT Engineer', 'We are hiring an IoT Engineer to build our connected devices platform. You will work with sensors, MQTT, and cloud platforms. Experience with IoT protocols and embedded systems required.', 'Seattle, WA', 'PUBLISHED', NOW()),
    (gen_random_uuid(), gen_random_uuid(), logitech_recruiter, 'Firmware Developer', 'Join our firmware team to develop low-level software for our devices. You will work with C/C++, embedded systems, and hardware interfaces. Experience with firmware development and embedded Linux required.', 'Seattle, WA', 'PUBLISHED', NOW()),
    (gen_random_uuid(), gen_random_uuid(), logitech_recruiter, 'Logistics Optimization Specialist', 'We need a Logistics Optimization Specialist to improve our supply chain efficiency. You will analyze logistics data, optimize routes, and reduce costs. Experience with logistics software and optimization algorithms required.', 'Seattle, WA', 'PUBLISHED', NOW());

    -- AI Startup jobs
    INSERT INTO job_projection (job_id, business_id, recruiter_id, title, description, location, status, updated_at)
    VALUES
    (gen_random_uuid(), gen_random_uuid(), ai_recruiter, 'Machine Learning Engineer', 'Join our AI team to build cutting-edge ML models. You will work with TensorFlow, PyTorch, and large-scale data processing. Experience with deep learning and model deployment required.', 'San Francisco, CA', 'PUBLISHED', NOW()),
    (gen_random_uuid(), gen_random_uuid(), ai_recruiter, 'AI Research Scientist', 'We are looking for an AI Research Scientist to push the boundaries of artificial intelligence. You will conduct research, publish papers, and develop new algorithms. PhD in Computer Science or related field required.', 'San Francisco, CA', 'PUBLISHED', NOW()),
    (gen_random_uuid(), gen_random_uuid(), ai_recruiter, 'Natural Language Processing Engineer', 'Join our NLP team to build language understanding systems. You will work with transformers, text classification, and sentiment analysis. Experience with NLP libraries and large language models required.', 'San Francisco, CA', 'PUBLISHED', NOW());

    -- Cybersecurity jobs
    INSERT INTO job_projection (job_id, business_id, recruiter_id, title, description, location, status, updated_at)
    VALUES
    (gen_random_uuid(), gen_random_uuid(), cybersecurity_recruiter, 'Penetration Tester', 'We need a Penetration Tester to identify security vulnerabilities. You will conduct ethical hacking, write reports, and recommend security improvements. Experience with penetration testing tools and security frameworks required.', 'Atlanta, GA', 'PUBLISHED', NOW()),
    (gen_random_uuid(), gen_random_uuid(), cybersecurity_recruiter, 'Security Analyst', 'Join our security operations center to monitor and respond to threats. You will analyze security logs, investigate incidents, and implement security controls. Experience with SIEM tools and incident response required.', 'Atlanta, GA', 'PUBLISHED', NOW()),
    (gen_random_uuid(), gen_random_uuid(), cybersecurity_recruiter, 'Cryptography Engineer', 'We are looking for a Cryptography Engineer to develop secure encryption systems. You will work with cryptographic algorithms, key management, and secure protocols. Experience with cryptography and security engineering required.', 'Atlanta, GA', 'PUBLISHED', NOW());

    -- Cloud Services jobs
    INSERT INTO job_projection (job_id, business_id, recruiter_id, title, description, location, status, updated_at)
    VALUES
    (gen_random_uuid(), gen_random_uuid(), cloudservices_recruiter, 'AWS Solutions Architect', 'Join our cloud team to design scalable AWS solutions. You will work with EC2, Lambda, S3, and other AWS services. AWS certification and experience with cloud architecture required.', 'Denver, CO', 'PUBLISHED', NOW()),
    (gen_random_uuid(), gen_random_uuid(), cloudservices_recruiter, 'Kubernetes Administrator', 'We need a Kubernetes Administrator to manage our container infrastructure. You will work with Kubernetes, Helm, and container orchestration. Experience with Kubernetes administration and DevOps practices required.', 'Denver, CO', 'PUBLISHED', NOW()),
    (gen_random_uuid(), gen_random_uuid(), cloudservices_recruiter, 'Cloud Security Engineer', 'Join our cloud security team to protect our cloud infrastructure. You will work with cloud security tools, compliance frameworks, and security best practices. Experience with cloud security and compliance required.', 'Denver, CO', 'PUBLISHED', NOW());
END $$;

-- Show the results
SELECT 'Additional job opportunities seeded successfully!' as status;
SELECT COUNT(*) as total_jobs FROM job_projection;
