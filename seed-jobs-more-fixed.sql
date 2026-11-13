-- Additional job opportunities seed script with actual recruiter IDs

INSERT INTO job_projection (job_id, business_id, recruiter_id, title, description, location, status, updated_at)
VALUES
-- FinTech jobs (Sarah Connor)
(gen_random_uuid(), gen_random_uuid(), 'a9737722-27b0-4f15-b153-2d2f45b0f56d', 'Blockchain Developer', 'We are looking for a Blockchain Developer to build our decentralized finance platform. You will work with Ethereum, Solidity, and smart contracts. Experience with DeFi protocols and cryptocurrency required.', 'New York, NY', 'PUBLISHED', NOW()),
(gen_random_uuid(), gen_random_uuid(), 'a9737722-27b0-4f15-b153-2d2f45b0f56d', 'Financial Analyst', 'Join our financial analysis team to drive investment decisions. You will analyze market trends, build financial models, and prepare reports. Experience with Excel, Bloomberg, and financial modeling required.', 'New York, NY', 'PUBLISHED', NOW()),
(gen_random_uuid(), gen_random_uuid(), 'a9737722-27b0-4f15-b153-2d2f45b0f56d', 'Compliance Officer', 'We need a Compliance Officer to ensure our platform meets regulatory requirements. You will monitor compliance, conduct audits, and implement policies. Experience with financial regulations and compliance frameworks required.', 'New York, NY', 'PUBLISHED', NOW()),

-- HealthTech jobs (John Smith)
(gen_random_uuid(), gen_random_uuid(), 'e8965fcd-3f55-45a6-a7df-8e386f3a7c2c', 'Healthcare Data Scientist', 'Join our healthcare data science team to improve patient outcomes. You will work with medical data, build predictive models, and analyze health trends. Experience with healthcare data and HIPAA compliance required.', 'Boston, MA', 'PUBLISHED', NOW()),
(gen_random_uuid(), gen_random_uuid(), 'e8965fcd-3f55-45a6-a7df-8e386f3a7c2c', 'Telemedicine Developer', 'We are hiring a Telemedicine Developer to build our virtual care platform. You will work with video conferencing, EHR integration, and secure messaging. Experience with healthcare IT and telemedicine required.', 'Boston, MA', 'PUBLISHED', NOW()),
(gen_random_uuid(), gen_random_uuid(), 'e8965fcd-3f55-45a6-a7df-8e386f3a7c2c', 'Medical Device Engineer', 'Join our medical device team to develop life-saving technology. You will work on hardware and software for medical devices. Experience with FDA regulations and medical device development required.', 'Boston, MA', 'PUBLISHED', NOW()),

-- EduTech jobs (Emma Wilson)
(gen_random_uuid(), gen_random_uuid(), '518006f9-e0a2-4c99-a6c2-0f3277eed87f', 'Learning Experience Designer', 'We need a Learning Experience Designer to create engaging educational content. You will design courses, create assessments, and optimize learning paths. Experience with instructional design and educational technology required.', 'Austin, TX', 'PUBLISHED', NOW()),
(gen_random_uuid(), gen_random_uuid(), '518006f9-e0a2-4c99-a6c2-0f3277eed87f', 'EdTech Product Manager', 'Join our product team to drive educational innovation. You will manage our learning platform roadmap, work with educators, and analyze user feedback. Experience with edtech and product management required.', 'Austin, TX', 'PUBLISHED', NOW()),
(gen_random_uuid(), gen_random_uuid(), '518006f9-e0a2-4c99-a6c2-0f3277eed87f', 'Virtual Reality Developer', 'We are looking for a VR Developer to create immersive learning experiences. You will work with Unity, VR headsets, and educational content. Experience with VR development and educational applications required.', 'Austin, TX', 'PUBLISHED', NOW()),

-- RetailTech jobs (Mike Johnson)
(gen_random_uuid(), gen_random_uuid(), 'd059b631-dd8f-41fa-9ad9-ab0163fbe872', 'E-commerce Developer', 'Join our e-commerce team to build our online marketplace. You will work with Shopify, payment systems, and inventory management. Experience with e-commerce platforms and web development required.', 'Chicago, IL', 'PUBLISHED', NOW()),
(gen_random_uuid(), gen_random_uuid(), 'd059b631-dd8f-41fa-9ad9-ab0163fbe872', 'Supply Chain Analyst', 'We need a Supply Chain Analyst to optimize our logistics operations. You will analyze supply chain data, forecast demand, and improve efficiency. Experience with supply chain management and data analysis required.', 'Chicago, IL', 'PUBLISHED', NOW()),
(gen_random_uuid(), gen_random_uuid(), 'd059b631-dd8f-41fa-9ad9-ab0163fbe872', 'Retail Systems Engineer', 'Join our retail systems team to maintain our POS and inventory systems. You will work with retail hardware, software integrations, and database management. Experience with retail technology and systems integration required.', 'Chicago, IL', 'PUBLISHED', NOW()),

-- LogiTech jobs (Olivia Brown)
(gen_random_uuid(), gen_random_uuid(), '0faacac3-64bc-4623-97bf-a17237bf30b8', 'IoT Engineer', 'We are hiring an IoT Engineer to build our connected devices platform. You will work with sensors, MQTT, and cloud platforms. Experience with IoT protocols and embedded systems required.', 'Seattle, WA', 'PUBLISHED', NOW()),
(gen_random_uuid(), gen_random_uuid(), '0faacac3-64bc-4623-97bf-a17237bf30b8', 'Firmware Developer', 'Join our firmware team to develop low-level software for our devices. You will work with C/C++, embedded systems, and hardware interfaces. Experience with firmware development and embedded Linux required.', 'Seattle, WA', 'PUBLISHED', NOW()),
(gen_random_uuid(), gen_random_uuid(), '0faacac3-64bc-4623-97bf-a17237bf30b8', 'Logistics Optimization Specialist', 'We need a Logistics Optimization Specialist to improve our supply chain efficiency. You will analyze logistics data, optimize routes, and reduce costs. Experience with logistics software and optimization algorithms required.', 'Seattle, WA', 'PUBLISHED', NOW()),

-- AI Startup jobs (Liam Davis)
(gen_random_uuid(), gen_random_uuid(), '2f43eacf-f39e-4eea-9030-6189f555df61', 'Machine Learning Engineer', 'Join our AI team to build cutting-edge ML models. You will work with TensorFlow, PyTorch, and large-scale data processing. Experience with deep learning and model deployment required.', 'San Francisco, CA', 'PUBLISHED', NOW()),
(gen_random_uuid(), gen_random_uuid(), '2f43eacf-f39e-4eea-9030-6189f555df61', 'AI Research Scientist', 'We are looking for an AI Research Scientist to push the boundaries of artificial intelligence. You will conduct research, publish papers, and develop new algorithms. PhD in Computer Science or related field required.', 'San Francisco, CA', 'PUBLISHED', NOW()),
(gen_random_uuid(), gen_random_uuid(), '2f43eacf-f39e-4eea-9030-6189f555df61', 'Natural Language Processing Engineer', 'Join our NLP team to build language understanding systems. You will work with transformers, text classification, and sentiment analysis. Experience with NLP libraries and large language models required.', 'San Francisco, CA', 'PUBLISHED', NOW()),

-- Cybersecurity jobs (Ava Miller)
(gen_random_uuid(), gen_random_uuid(), '03c5594a-0de5-48cf-9562-c12bf17c7c7b', 'Penetration Tester', 'We need a Penetration Tester to identify security vulnerabilities. You will conduct ethical hacking, write reports, and recommend security improvements. Experience with penetration testing tools and security frameworks required.', 'Atlanta, GA', 'PUBLISHED', NOW()),
(gen_random_uuid(), gen_random_uuid(), '03c5594a-0de5-48cf-9562-c12bf17c7c7b', 'Security Analyst', 'Join our security operations center to monitor and respond to threats. You will analyze security logs, investigate incidents, and implement security controls. Experience with SIEM tools and incident response required.', 'Atlanta, GA', 'PUBLISHED', NOW()),
(gen_random_uuid(), gen_random_uuid(), '03c5594a-0de5-48cf-9562-c12bf17c7c7b', 'Cryptography Engineer', 'We are looking for a Cryptography Engineer to develop secure encryption systems. You will work with cryptographic algorithms, key management, and secure protocols. Experience with cryptography and security engineering required.', 'Atlanta, GA', 'PUBLISHED', NOW()),

-- Cloud Services jobs (Noah Garcia)
(gen_random_uuid(), gen_random_uuid(), '49fdcdfd-5e60-4f96-8882-59dca8085aa8', 'AWS Solutions Architect', 'Join our cloud team to design scalable AWS solutions. You will work with EC2, Lambda, S3, and other AWS services. AWS certification and experience with cloud architecture required.', 'Denver, CO', 'PUBLISHED', NOW()),
(gen_random_uuid(), gen_random_uuid(), '49fdcdfd-5e60-4f96-8882-59dca8085aa8', 'Kubernetes Administrator', 'We need a Kubernetes Administrator to manage our container infrastructure. You will work with Kubernetes, Helm, and container orchestration. Experience with Kubernetes administration and DevOps practices required.', 'Denver, CO', 'PUBLISHED', NOW()),
(gen_random_uuid(), gen_random_uuid(), '49fdcdfd-5e60-4f96-8882-59dca8085aa8', 'Cloud Security Engineer', 'Join our cloud security team to protect our cloud infrastructure. You will work with cloud security tools, compliance frameworks, and security best practices. Experience with cloud security and compliance required.', 'Denver, CO', 'PUBLISHED', NOW());

-- Show the results
SELECT 'Additional job opportunities seeded successfully!' as status;
SELECT COUNT(*) as total_jobs FROM job_projection;
