-- =================================================================
-- Job Notifier - Complete Database Setup Script
-- =================================================================
-- This script creates the database, tables, and sample data
-- Run this script to set up the complete database from scratch

-- =================================================================
-- 1. DATABASE CREATION
-- =================================================================

-- Create database if it doesn't exist
CREATE DATABASE IF NOT EXISTS job_notifier_db;

-- Use the database
USE job_notifier_db;

-- =================================================================
-- 2. TABLE CREATION
-- =================================================================

-- Drop tables if they exist (for clean setup)
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS notifiers;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS jobs;
DROP TABLE IF EXISTS scheduler_state;

-- Users Table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Notifiers Table
CREATE TABLE notifiers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    city VARCHAR(255),
    salary_expectation VARCHAR(255),
    companies_preference VARCHAR(1000),
    experience VARCHAR(255),
    notice_period VARCHAR(255),
    college VARCHAR(255),
    resume_latex TEXT,
    additional_preferences VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Jobs Table (populated by external Telegram module)
CREATE TABLE jobs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    timestamp TIMESTAMP NOT NULL,
    job VARCHAR(5000) NOT NULL,
    processed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_processed_timestamp (processed, timestamp),
    INDEX idx_timestamp (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Notifications Table
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    notifier_id BIGINT NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    scheduler_run INT NOT NULL,
    resume_link VARCHAR(1000),
    company_name VARCHAR(255) NOT NULL,
    experience VARCHAR(255),
    location VARCHAR(255),
    salary VARCHAR(255),
    job_description TEXT,
    relevance_score DOUBLE NOT NULL,
    relevance_reason VARCHAR(1000),
    original_job_posting TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    viewed BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (notifier_id) REFERENCES notifiers(id) ON DELETE CASCADE,
    INDEX idx_notifier_id (notifier_id),
    INDEX idx_notifier_viewed (notifier_id, viewed),
    INDEX idx_timestamp (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Scheduler State Table
CREATE TABLE scheduler_state (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    scheduler_name VARCHAR(255) NOT NULL UNIQUE,
    current_run INT NOT NULL DEFAULT 0,
    max_runs INT NOT NULL,
    last_run_timestamp TIMESTAMP NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    INDEX idx_scheduler_name (scheduler_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =================================================================
-- 3. SAMPLE DATA
-- =================================================================

-- Sample User
INSERT INTO users (email, password, full_name, created_at) VALUES
('john.doe@example.com', '$2a$10$YourHashedPasswordHere123456789012345678901234567890123', 'John Doe', '2025-10-14 08:00:00');

-- Sample Notifiers
INSERT INTO notifiers (user_id, name, city, salary_expectation, companies_preference, experience, notice_period, college, resume_latex, additional_preferences, created_at) VALUES
(1, 'Senior Backend Developer Profile', 'San Francisco, CA', '150k-200k USD', 'Google, Amazon, Netflix, Microsoft, Meta, Apple', '6 years', '2 months', 'Stanford University', 
'\\documentclass[11pt,a4paper,sans]{moderncv}
\\moderncvstyle{banking}
\\moderncvcolor{blue}
\\usepackage[scale=0.85]{geometry}

\\name{John}{Doe}
\\title{Senior Backend Engineer}
\\email{john.doe@example.com}
\\phone[mobile]{+1~(555)~123~4567}
\\social[linkedin]{johndoe}
\\social[github]{johndoe}

\\begin{document}
\\makecvtitle

\\section{Summary}
Senior Backend Engineer with 6+ years of experience in building scalable distributed systems. Expert in Java, Spring Boot, microservices architecture, and cloud platforms. Proven track record of designing and implementing high-performance systems serving millions of users.

\\section{Experience}
\\cventry{2021--Present}{Senior Software Engineer}{Tech Corp}{San Francisco, CA}{}{
\\begin{itemize}
\\item Led development of microservices architecture serving 10M+ daily users
\\item Improved system performance by 40\\% through optimization and caching strategies
\\item Mentored team of 5 junior engineers and conducted code reviews
\\item Technologies: Java, Spring Boot, Kubernetes, AWS, PostgreSQL, Redis
\\end{itemize}}

\\cventry{2019--2021}{Backend Engineer}{StartupXYZ}{San Francisco, CA}{}{
\\begin{itemize}
\\item Built RESTful APIs and microservices from ground up
\\item Implemented CI/CD pipelines reducing deployment time by 60\\%
\\item Designed database schemas and optimized queries for performance
\\item Technologies: Java, Spring Framework, Docker, MongoDB, RabbitMQ
\\end{itemize}}

\\section{Education}
\\cventry{2015--2019}{Bachelor of Science in Computer Science}{Stanford University}{California}{}{}

\\section{Technical Skills}
\\cvitem{Languages}{Java, Python, SQL, JavaScript}
\\cvitem{Frameworks}{Spring Boot, Spring Cloud, Hibernate, JPA}
\\cvitem{Databases}{PostgreSQL, MySQL, MongoDB, Redis}
\\cvitem{Cloud}{AWS (EC2, S3, Lambda, RDS), Docker, Kubernetes}
\\cvitem{Tools}{Git, Jenkins, Maven, Gradle, IntelliJ IDEA}

\\section{Certifications}
\\cvitem{}{AWS Certified Solutions Architect - Professional}
\\cvitem{}{Oracle Certified Professional Java SE 11 Developer}

\\end{document}', 
'Prefer remote or hybrid work options. Interested in fintech and cloud infrastructure roles. Open to relocation for the right opportunity. Passionate about system design and scalability challenges.', 
'2025-10-14 08:30:00'),

(1, 'Full Stack Engineer Profile', 'Seattle, WA', '130k-170k USD', 'Amazon, Microsoft, Salesforce', '4 years', '1 month', 'MIT', 
'\\documentclass[11pt,a4paper,sans]{moderncv}
\\moderncvstyle{classic}
\\moderncvcolor{green}
\\usepackage[scale=0.85]{geometry}

\\name{John}{Doe}
\\title{Full Stack Engineer}
\\email{john.doe@example.com}
\\phone[mobile]{+1~(555)~123~4567}
\\social[linkedin]{johndoe}
\\social[github]{johndoe}

\\begin{document}
\\makecvtitle

\\section{Professional Summary}
Versatile Full Stack Engineer with 4 years of experience building end-to-end web applications. Strong expertise in both frontend and backend technologies, with a focus on creating seamless user experiences and robust backend systems.

\\section{Work Experience}
\\cventry{2022--Present}{Full Stack Developer}{WebTech Solutions}{Seattle, WA}{}{
\\begin{itemize}
\\item Developed responsive web applications using React and Spring Boot
\\item Integrated third-party APIs and payment gateways
\\item Implemented authentication and authorization systems
\\item Technologies: React, TypeScript, Java, Spring Boot, PostgreSQL
\\end{itemize}}

\\cventry{2020--2022}{Software Developer}{Digital Agency}{Boston, MA}{}{
\\begin{itemize}
\\item Built e-commerce platforms and content management systems
\\item Collaborated with designers to implement pixel-perfect UIs
\\item Optimized application performance and user experience
\\item Technologies: Vue.js, Node.js, Express, MySQL
\\end{itemize}}

\\section{Education}
\\cventry{2016--2020}{Bachelor of Science in Computer Science}{MIT}{Massachusetts}{GPA: 3.8/4.0}{}

\\section{Technical Expertise}
\\cvitem{Frontend}{React, Vue.js, TypeScript, HTML5, CSS3, Redux}
\\cvitem{Backend}{Java, Spring Boot, Node.js, RESTful APIs}
\\cvitem{Databases}{PostgreSQL, MySQL, MongoDB}
\\cvitem{DevOps}{Docker, AWS, CI/CD, Git}

\\section{Projects}
\\cvitem{E-Commerce Platform}{Built scalable platform with 50k+ monthly users, implemented real-time inventory management}
\\cvitem{Social Media Dashboard}{Created analytics dashboard processing 1M+ daily events with real-time updates}

\\end{document}', 
'Looking for companies with strong engineering culture and opportunities for growth. Interested in working on products with direct user impact. Open to learning new technologies and frameworks.', 
'2025-10-14 09:00:00');

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
}', false, '2025-10-15 11:00:00');

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


INSERT INTO jobs (timestamp, job, processed, created_at) VALUES
('2025-10-15 15:00:00', '{
  "company": "Apple",
  "experience": "5-7 years",
  "location": "Cupertino, CA",
  "salary": "$155k-$210k",
  "description": "Apple is seeking talented Backend Engineers to join our Services team. Work on services that power Apple Music, iCloud, and App Store serving hundreds of millions of users globally. We are looking for engineers with strong Java and distributed systems experience. Key technologies: Java, Spring Boot, Kafka, Cassandra, Redis. You will design and build highly scalable backend services, optimize performance at massive scale, and collaborate with teams across Apple. We offer exceptional compensation, comprehensive benefits including healthcare and 401k matching, stock grants, and the opportunity to work on products used by millions. Hybrid work schedule with flexible hours."
}', false, '2025-10-15 15:00:00');

-- =================================================================
-- 4. VERIFICATION QUERIES
-- =================================================================

-- Verify database and tables were created
SHOW TABLES;

-- Check sample jobs
SELECT COUNT(*) as total_jobs FROM jobs;

-- View jobs with company names
SELECT 
    id, 
    timestamp, 
    JSON_EXTRACT(job, '$.company') as company, 
    processed 
FROM jobs 
LIMIT 5;

-- =================================================================
-- 5. USEFUL QUERIES FOR TESTING
-- =================================================================

-- Check all jobs
-- SELECT id, timestamp, JSON_EXTRACT(job, '$.company') as company, processed FROM jobs;

-- Check scheduler state
-- SELECT * FROM scheduler_state;

-- Reset scheduler (if needed for testing)
-- UPDATE scheduler_state SET current_run = 0, last_run_timestamp = NOW() WHERE scheduler_name = 'job-processor';

-- Mark jobs as unprocessed for re-testing
-- UPDATE jobs SET processed = false;

-- Check notifications
-- SELECT n.id, n.company_name, n.relevance_score, n.created_at, no.name as notifier_name
-- FROM notifications n
-- JOIN notifiers no ON n.notifier_id = no.id
-- ORDER BY n.created_at DESC;

-- Count notifications per notifier
-- SELECT notifier_id, COUNT(*) as notification_count 
-- FROM notifications 
-- GROUP BY notifier_id;

-- Check unread notifications
-- SELECT n.company_name, n.relevance_score, no.name as notifier_name
-- FROM notifications n
-- JOIN notifiers no ON n.notifier_id = no.id
-- WHERE n.viewed = FALSE
-- ORDER BY n.relevance_score DESC;

-- =================================================================
-- 6. HOW TO USE THIS SCRIPT
-- =================================================================

-- Method 1: Command Line
-- mysql -u root -p < sample_data.sql

-- Method 2: MySQL Client
-- mysql -u root -p
-- source /path/to/sample_data.sql

-- Method 3: MySQL Workbench
-- File → Run SQL Script → Select this file

-- =================================================================
-- NOTES:
-- - Users and Notifiers should be created through the API (signup/login)
-- - The jobs inserted here will be processed by the scheduler when it runs
-- - Make sure your application.properties has the correct database credentials
-- - The Spring Boot app will NOT auto-create tables if you run this script first
--   (set spring.jpa.hibernate.ddl-auto=none in that case)
-- =================================================================

