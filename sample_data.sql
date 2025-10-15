-- Sample Data for Job Notifier Application
-- Run this script after the application creates the tables

-- Sample Jobs (these would typically be inserted by the external Telegram module)
INSERT INTO jobs (timestamp, job, processed, created_at) VALUES
('2025-10-15 09:00:00', '{
  "company": "Google",
  "experience": "5-8 years",
  "location": "Mountain View, CA",
  "salary": "$150k-$200k",
  "description": "We are looking for a Senior Java Developer to join our Cloud Platform team. You will work on building scalable microservices using Spring Boot and Kubernetes. Strong background in distributed systems required. Excellent benefits and work-life balance. Must have experience with Docker, CI/CD, and cloud infrastructure."
}', false, '2025-10-15 09:00:00'),

('2025-10-15 09:30:00', '{
  "company": "Amazon",
  "experience": "6+ years",
  "location": "Seattle, WA",
  "salary": "$140k-$180k",
  "description": "Amazon Web Services is hiring Senior Backend Engineers. Work on high-performance systems serving millions of customers. Expertise in Java, Spring Boot, and AWS services required. Remote options available. We offer competitive compensation, stock options, and comprehensive benefits."
}', false, '2025-10-15 09:30:00'),

('2025-10-15 10:00:00', '{
  "company": "Netflix",
  "experience": "7+ years",
  "location": "Los Gatos, CA",
  "salary": "$170k-$230k",
  "description": "Netflix is seeking experienced Java developers to build the next generation of streaming technology. Must have strong experience with microservices, reactive programming, and distributed systems. Work on systems that serve 200M+ subscribers worldwide. Full remote work available."
}', false, '2025-10-15 10:00:00'),

('2025-10-15 10:30:00', '{
  "company": "Meta",
  "experience": "4-6 years",
  "location": "Menlo Park, CA",
  "salary": "$130k-$190k",
  "description": "Meta is hiring Backend Engineers for our Infrastructure team. Work on systems that support billions of users. Java, Spring, and large-scale systems experience required. Contribute to products used by billions. Competitive salary, RSUs, and excellent benefits."
}', false, '2025-10-15 10:30:00'),

('2025-10-15 11:00:00', '{
  "company": "Microsoft",
  "experience": "5-7 years",
  "location": "Redmond, WA",
  "salary": "$135k-$185k",
  "description": "Join Microsoft Azure team as a Senior Software Engineer. Build cloud services that power the world. Strong Java and distributed systems experience needed. Work on cutting-edge technology with global impact. Hybrid work model available."
}', false, '2025-10-15 11:00:00'),

('2025-10-15 11:30:00', '{
  "company": "Apple",
  "experience": "5-10 years",
  "location": "Cupertino, CA",
  "salary": "$160k-$220k",
  "description": "Apple Inc. is looking for a Senior Software Engineer to join our Services team. Design and implement scalable backend services. Work with cross-functional teams. Mentor junior engineers. Requires 5+ years experience with Java/Spring Boot and strong understanding of distributed systems."
}', false, '2025-10-15 11:30:00'),

('2025-10-15 12:00:00', '{
  "company": "Stripe",
  "experience": "4-7 years",
  "location": "San Francisco, CA",
  "salary": "$145k-$195k",
  "description": "Stripe is hiring Backend Engineers to build payment infrastructure for the internet. Work with Java, Go, and modern technologies. Help millions of businesses accept payments. Remote-friendly culture with competitive compensation and equity."
}', false, '2025-10-15 12:00:00'),

('2025-10-15 12:30:00', '{
  "company": "Startup XYZ",
  "experience": "2-3 years",
  "location": "Austin, TX",
  "salary": "$80k-$100k",
  "description": "Early-stage startup looking for junior backend developers. Learn and grow with us! Small team, big impact. Stock options available."
}', false, '2025-10-15 12:30:00'),

('2025-10-15 13:00:00', '{
  "company": "Airbnb",
  "experience": "6+ years",
  "location": "San Francisco, CA",
  "salary": "$155k-$205k",
  "description": "Airbnb is seeking Senior Backend Engineers to work on our platform that connects millions of travelers with unique accommodations. Java, Spring Boot, microservices architecture. Help us build the future of travel. Remote work supported."
}', false, '2025-10-15 13:00:00'),

('2025-10-15 13:30:00', '{
  "company": "Uber",
  "experience": "5-8 years",
  "location": "San Francisco, CA",
  "salary": "$140k-$190k",
  "description": "Join Uber Engineering to work on real-time systems at massive scale. Backend engineer position for rides/eats platform. Java, distributed systems, high-throughput services. Shape the future of mobility. Hybrid work arrangement."
}', false, '2025-10-15 13:30:00');

-- Plain text format examples
INSERT INTO jobs (timestamp, job, processed, created_at) VALUES
('2025-10-15 14:00:00', 
'Company: Salesforce
Experience: 5-7 years
Location: San Francisco, CA
Salary: $145k-$195k

Salesforce is hiring Senior Backend Engineers for our Platform team.

Responsibilities:
- Design and implement scalable APIs
- Build microservices using Java and Spring Boot
- Collaborate with product and design teams
- Optimize system performance

Requirements:
- 5+ years of backend development experience
- Strong Java and Spring framework knowledge
- Experience with cloud platforms (AWS/GCP)
- Excellent problem-solving skills

Benefits:
- Competitive salary and equity
- Comprehensive health coverage
- 401k matching
- Flexible PTO
- Remote work options', 
false, '2025-10-15 14:00:00'),

('2025-10-15 14:30:00',
'Company: LinkedIn
Experience: 6+ years
Location: Sunnyvale, CA
Salary: $150k-$200k

LinkedIn is looking for experienced Backend Engineers to join our Infrastructure team.

About the Role:
Build systems that power the world\'s largest professional network. Work on high-scale distributed systems serving 800M+ members.

Technical Requirements:
- Expert-level Java programming
- Spring Boot and microservices
- Database design and optimization
- System design for scale

What We Offer:
- Top-tier compensation
- Stock options
- Best-in-class benefits
- Learning and development opportunities
- Hybrid work model',
false, '2025-10-15 14:30:00');

-- Note: Users and Notifiers would be created through the API
-- The above jobs will be processed by the scheduler when it runs

-- Example query to check jobs
-- SELECT id, timestamp, JSON_EXTRACT(job, '$.company') as company, processed FROM jobs;

-- Example query to check scheduler state
-- SELECT * FROM scheduler_state;

-- Example query to reset scheduler (if needed for testing)
-- UPDATE scheduler_state SET current_run = 0, last_run_timestamp = NOW() WHERE scheduler_name = 'job-processor';

-- Example to manually mark jobs as unprocessed for re-testing
-- UPDATE jobs SET processed = false WHERE id IN (1,2,3,4,5);

