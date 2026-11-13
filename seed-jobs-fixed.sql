-- Seed script for SmartRecruit demo data - Job Opportunities
-- This creates realistic job opportunities using actual recruiter IDs

-- Insert job projections with actual recruiter IDs
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

-- Show the seeded data
SELECT 'Job opportunities seeded successfully!' as status;
SELECT COUNT(*) as total_jobs FROM job_projection;
SELECT title, location, status FROM job_projection ORDER BY updated_at DESC LIMIT 5;
