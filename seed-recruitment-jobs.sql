-- Seed for the RECRUITMENT database (recruitment_db, port 5434) so the public job board has published offers.
-- Reuses the demo offers from seed-jobs-fixed.sql / seed-jobs-more-fixed.sql (which only fill candidate-ai's job_projection).
-- Run AFTER recruitment-service has started once (Flyway creates the tables). Safe to re-run.
--   docker exec -i smartrecruit-recruitment-postgres psql -U smartrecruit -d recruitment_db < seed-recruitment-jobs.sql
BEGIN;
CREATE TEMP TABLE job_projection (job_id UUID, business_id UUID, recruiter_id UUID, title TEXT, description TEXT,
  location TEXT, status TEXT, updated_at TIMESTAMPTZ) ON COMMIT DROP;
INSERT INTO job_projection (job_id, business_id, recruiter_id, title, description, location, status, updated_at)
VALUES
-- Tech Corp jobs (Jessica Martinez)
(gen_random_uuid(), gen_random_uuid(), 'cfe10550-4a12-4e86-aa5d-51748327a23d', 'Senior Software Engineer', 'We are looking for a Senior Software Engineer to join our backend team. You will work on scalable microservices architecture using Java, Spring Boot, and cloud technologies. Experience with distributed systems and high-availability applications required.', 'San Francisco, CA', 'PUBLISHED', NOW()),
(gen_random_uuid(), gen_random_uuid(), 'cfe10550-4a12-4e86-aa5d-51748327a23d', 'DevOps Engineer', 'Join our DevOps team to help us scale our infrastructure. You will work with Kubernetes, Docker, AWS, and CI/CD pipelines. Strong experience with infrastructure as code and monitoring tools required.', 'San Francisco, CA', 'PUBLISHED', NOW()),
(gen_random_uuid(), gen_random_uuid(), 'cfe10550-4a12-4e86-aa5d-51748327a23d', 'Product Manager', 'We need a Product Manager to lead our mobile app development. You will work closely with engineering and design teams to deliver user-centric features. Experience with agile methodologies and product strategy required.', 'San Francisco, CA', 'PUBLISHED', NOW()),

-- Innovate Inc jobs (Robert Thompson)
(gen_random_uuid(), gen_random_uuid(), 'e66fb875-006b-4919-b1b8-680a04f2a32c', 'Frontend Developer', 'We are hiring a Frontend Developer to build modern web applications. You will work with React, TypeScript, and modern CSS frameworks. Experience with responsive design and performance optimization required.', 'New York, NY', 'PUBLISHED', NOW()),
(gen_random_uuid(), gen_random_uuid(), 'e66fb875-006b-4919-b1b8-680a04f2a32c', 'Data Scientist', 'Join our data science team to build ML models and analytics solutions. You will work with Python, TensorFlow, and big data technologies. Strong background in statistics and machine learning required.', 'New York, NY', 'PUBLISHED', NOW()),
(gen_random_uuid(), gen_random_uuid(), 'e66fb875-006b-4919-b1b8-680a04f2a32c', 'UX Designer', 'We need a UX Designer to create intuitive user experiences. You will conduct user research, create wireframes, and design beautiful interfaces. Experience with design systems and accessibility required.', 'New York, NY', 'PUBLISHED', NOW()),

-- Startup jobs (Lisa Anderson)
(gen_random_uuid(), gen_random_uuid(), 'd341d60a-8067-4b3d-87ca-e8e81abfd367', 'Full Stack Developer', 'Join our fast-paced startup as a Full Stack Developer. You will work on both frontend and backend using modern technologies. Experience with Node.js, React, and databases required. Startup experience preferred.', 'Austin, TX', 'PUBLISHED', NOW()),
(gen_random_uuid(), gen_random_uuid(), 'd341d60a-8067-4b3d-87ca-e8e81abfd367', 'Mobile Developer', 'We are looking for a Mobile Developer to build our iOS and Android apps. You will work with React Native or Flutter. Experience with mobile app development and app store deployment required.', 'Austin, TX', 'PUBLISHED', NOW()),
(gen_random_uuid(), gen_random_uuid(), 'd341d60a-8067-4b3d-87ca-e8e81abfd367', 'Growth Hacker', 'Join our growth team to drive user acquisition and retention. You will work with analytics tools, A/B testing, and marketing automation. Experience with growth strategies and data analysis required.', 'Austin, TX', 'PUBLISHED', NOW()),

-- Enterprise jobs (Mark Davis)
(gen_random_uuid(), gen_random_uuid(), '92f29ffe-dacc-4d52-95c4-fa303ae479fb', 'Cloud Architect', 'We need a Cloud Architect to design and implement our cloud infrastructure. You will work with AWS, Azure, or GCP. Experience with enterprise architecture and security best practices required.', 'Seattle, WA', 'PUBLISHED', NOW()),
(gen_random_uuid(), gen_random_uuid(), '92f29ffe-dacc-4d52-95c4-fa303ae479fb', 'Security Engineer', 'Join our security team to protect our systems and data. You will work on security monitoring, incident response, and compliance. Experience with security frameworks and tools required.', 'Seattle, WA', 'PUBLISHED', NOW()),
(gen_random_uuid(), gen_random_uuid(), '92f29ffe-dacc-4d52-95c4-fa303ae479fb', 'Technical Lead', 'We are looking for a Technical Lead to guide our engineering team. You will provide technical guidance, code reviews, and mentorship. Experience with team leadership and software architecture required.', 'Seattle, WA', 'PUBLISHED', NOW()),

-- Global Inc jobs (Amanda Wright)
(gen_random_uuid(), gen_random_uuid(), '3ff3e788-e324-4af3-a6c2-99e0e635ae61', 'Business Analyst', 'Join our business analysis team to bridge the gap between business and technology. You will gather requirements, analyze data, and create reports. Experience with SQL, Excel, and business process modeling required.', 'Boston, MA', 'PUBLISHED', NOW()),
(gen_random_uuid(), gen_random_uuid(), '3ff3e788-e324-4af3-a6c2-99e0e635ae61', 'QA Engineer', 'We need a QA Engineer to ensure our software quality. You will design test plans, execute tests, and report bugs. Experience with automated testing tools and Agile methodologies required.', 'Boston, MA', 'PUBLISHED', NOW()),
(gen_random_uuid(), gen_random_uuid(), '3ff3e788-e324-4af3-a6c2-99e0e635ae61', 'Technical Writer', 'Join our documentation team to create clear and comprehensive technical documentation. You will write user guides, API documentation, and internal wikis. Experience with technical writing and documentation tools required.', 'Boston, MA', 'PUBLISHED', NOW());
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

-- one demo business per recruiter (deterministic id)
INSERT INTO businesses (id, owner_user_id, name, description, status, created_at, updated_at)
SELECT DISTINCT md5(recruiter_id::text || ':business')::uuid, recruiter_id, 'Demo company', 'Seeded demo business', 'ACTIVE', NOW(), NOW()
FROM job_projection
ON CONFLICT (id) DO NOTHING;

INSERT INTO jobs (id, business_id, created_by_user_id, title, location, contract_type, description, status, created_at, updated_at, published_at)
SELECT gen_random_uuid(), md5(p.recruiter_id::text || ':business')::uuid, p.recruiter_id, p.title, p.location, 'Full-time',
       p.description, 'PUBLISHED', NOW(), NOW(), NOW()
FROM job_projection p
WHERE NOT EXISTS (SELECT 1 FROM jobs j WHERE j.created_by_user_id = p.recruiter_id AND j.title = p.title);
COMMIT;

SELECT status, COUNT(*) FROM jobs GROUP BY status;
