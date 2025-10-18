# API Examples and Testing Guide

## Complete API Flow Example

### 1. User Registration

```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "password123",
    "fullName": "John Doe"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxIiwiaWF0IjoxNjk3MzY...",
  "type": "Bearer",
  "userId": 1,
  "email": "john.doe@example.com",
  "fullName": "John Doe"
}
```

### 2. User Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "password123"
  }'
```

### 3. Create a Notifier

```bash
export TOKEN="your-jwt-token-here"

curl -X POST http://localhost:8080/api/notifiers \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "Senior Java Developer",
    "city": "San Francisco, New York",
    "salaryExpectation": "$120k-$180k",
    "companiesPreference": "Google, Amazon, Netflix, Meta",
    "experience": "5-7 years",
    "noticePeriod": "2 weeks",
    "college": "Stanford University",
    "resumeLatex": "\\documentclass{article}\\begin{document}My Resume\\end{document}",
    "additionalPreferences": "Remote work, flexible hours, good work-life balance"
  }'
```

**Response:**
```json
{
  "id": 1,
  "name": "Senior Java Developer",
  "city": "San Francisco, New York",
  "salaryExpectation": "$120k-$180k",
  "companiesPreference": "Google, Amazon, Netflix, Meta",
  "experience": "5-7 years",
  "noticePeriod": "2 weeks",
  "college": "Stanford University",
  "resumeLatex": "\\documentclass{article}...",
  "additionalPreferences": "Remote work, flexible hours, good work-life balance",
  "createdAt": "2025-10-15T10:30:00",
  "updatedAt": "2025-10-15T10:30:00",
  "unreadNotificationsCount": 0
}
```

### 4. Get All Notifiers

```bash
curl -X GET http://localhost:8080/api/notifiers \
  -H "Authorization: Bearer $TOKEN"
```

**Response:**
```json
[
  {
    "id": 1,
    "name": "Senior Java Developer",
    "unreadNotificationsCount": 3,
    ...
  },
  {
    "id": 2,
    "name": "Frontend Developer",
    "unreadNotificationsCount": 0,
    ...
  }
]
```

### 5. Get Single Notifier

```bash
curl -X GET http://localhost:8080/api/notifiers/1 \
  -H "Authorization: Bearer $TOKEN"
```

### 6. Update Notifier

```bash
curl -X PUT http://localhost:8080/api/notifiers/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "Senior Java Developer (Updated)",
    "city": "San Francisco, Seattle",
    "salaryExpectation": "$130k-$200k",
    "companiesPreference": "Google, Amazon, Netflix, Meta, Apple",
    "experience": "5-8 years",
    "noticePeriod": "4 weeks",
    "college": "Stanford University",
    "resumeLatex": "\\documentclass{article}...",
    "additionalPreferences": "Remote work mandatory"
  }'
```

### 7. Get Notifier Limit Information

Check how many notifiers the user has created and how many more they can create.

```bash
curl -X GET http://localhost:8080/api/notifiers/limit-info \
  -H "Authorization: Bearer $TOKEN"
```

**Response:**
```json
{
  "current": 2,
  "max": 5,
  "remaining": 3,
  "canCreateMore": true
}
```

**Response Fields:**
- `current`: Number of notifiers currently created by the user
- `max`: Maximum number of notifiers allowed per user (configurable via `notifier.max-per-user`)
- `remaining`: Number of notifiers the user can still create
- `canCreateMore`: Boolean indicating if the user can create more notifiers

**Note:** If a user tries to create more notifiers than the limit allows, the API will return an error:

```json
{
  "timestamp": "2025-10-18T03:30:00.000+00:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Maximum notifier limit reached. You can only create up to 5 notifiers.",
  "path": "/api/notifiers"
}
```

### 8. Get Notifications for Notifier

```bash
curl -X GET http://localhost:8080/api/notifications/notifier/1 \
  -H "Authorization: Bearer $TOKEN"
```

**Response:**
```json
[
  {
    "id": 1,
    "notifierId": 1,
    "timestamp": "2025-10-15T09:00:00",
    "schedulerRun": 1,
    "resumeLink": "https://res.cloudinary.com/your-cloud/resume_1_5_abc123.pdf",
    "companyName": "Google",
    "experience": "5-8 years",
    "location": "Mountain View, CA",
    "salary": "$150k-$200k",
    "jobDescription": "We are looking for a Senior Java Developer...",
    "relevanceScore": 0.92,
    "relevanceReason": "Excellent match. Company preference matched (Google), salary range aligns ($150k-$200k vs $130k-$200k), experience requirement matches (5-8 years), location in preferred city.",
    "originalJobPosting": "{\"company\":\"Google\",\"experience\":\"5-8 years\"...}",
    "createdAt": "2025-10-15T09:15:00",
    "viewed": false
  },
  {
    "id": 2,
    "notifierId": 1,
    "timestamp": "2025-10-15T09:30:00",
    "schedulerRun": 1,
    "resumeLink": "https://res.cloudinary.com/your-cloud/resume_1_8_def456.pdf",
    "companyName": "Amazon",
    "experience": "6+ years",
    "location": "Seattle, WA",
    "salary": "$140k-$180k",
    "jobDescription": "Amazon Web Services is hiring...",
    "relevanceScore": 0.85,
    "relevanceReason": "Strong match. Company preference matched (Amazon), experience aligns, location matches preference.",
    "originalJobPosting": "{\"company\":\"Amazon\"...}",
    "createdAt": "2025-10-15T09:45:00",
    "viewed": false
  }
]
```

### 9. Mark Notification as Viewed

```bash
curl -X PUT http://localhost:8080/api/notifications/1/viewed \
  -H "Authorization: Bearer $TOKEN"
```

### 10. Delete Notifier

```bash
curl -X DELETE http://localhost:8080/api/notifiers/1 \
  -H "Authorization: Bearer $TOKEN"
```

**Response:**
```json
{
  "success": true,
  "message": "Notifier deleted successfully"
}
```

## Testing the External Module Integration

### Insert Test Jobs

```sql
-- Insert sample jobs for testing
INSERT INTO jobs (timestamp, job, processed, created_at) VALUES
('2025-10-15 09:00:00', '{
  "company": "Google",
  "experience": "5-8 years",
  "location": "Mountain View, CA",
  "salary": "$150k-$200k",
  "description": "We are looking for a Senior Java Developer to join our Cloud Platform team. You will work on building scalable microservices using Spring Boot and Kubernetes. Strong background in distributed systems required. Excellent benefits and work-life balance."
}', false, NOW()),

('2025-10-15 09:30:00', '{
  "company": "Amazon",
  "experience": "6+ years",
  "location": "Seattle, WA",
  "salary": "$140k-$180k",
  "description": "Amazon Web Services is hiring Senior Backend Engineers. Work on high-performance systems serving millions of customers. Expertise in Java, Spring Boot, and AWS services required. Remote options available."
}', false, NOW()),

('2025-10-15 10:00:00', '{
  "company": "Netflix",
  "experience": "7+ years",
  "location": "Los Gatos, CA",
  "salary": "$170k-$230k",
  "description": "Netflix is seeking experienced Java developers to build the next generation of streaming technology. Must have strong experience with microservices, reactive programming, and distributed systems."
}', false, NOW()),

('2025-10-15 10:30:00', '{
  "company": "Meta",
  "experience": "4-6 years",
  "location": "Menlo Park, CA",
  "salary": "$130k-$190k",
  "description": "Meta is hiring Backend Engineers for our Infrastructure team. Work on systems that support billions of users. Java, Spring, and large-scale systems experience required."
}', false, NOW()),

('2025-10-15 11:00:00', '{
  "company": "Startup XYZ",
  "experience": "2-3 years",
  "location": "Austin, TX",
  "salary": "$80k-$100k",
  "description": "Early-stage startup looking for junior backend developers. Learn and grow with us!"
}', false, NOW());
```

### Plain Text Format Example

```sql
INSERT INTO jobs (timestamp, job, processed, created_at) VALUES
('2025-10-15 12:00:00', 
'Company: Apple
Experience: 5-10 years
Location: Cupertino, CA
Salary: $160k-$220k

Apple Inc. is looking for a Senior Software Engineer to join our Services team.

Responsibilities:
- Design and implement scalable backend services
- Work with cross-functional teams
- Mentor junior engineers

Requirements:
- 5+ years experience with Java/Spring Boot
- Strong understanding of distributed systems
- Experience with cloud platforms (AWS/GCP)

Benefits:
- Competitive salary and equity
- Comprehensive health coverage
- Flexible work arrangements', 
false, NOW());
```

## Frontend Integration Examples

### React Example

```javascript
// services/api.js
const API_BASE_URL = 'http://localhost:8080/api';

export const authService = {
  signup: async (email, password, fullName) => {
    const response = await fetch(`${API_BASE_URL}/auth/signup`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password, fullName })
    });
    if (!response.ok) throw new Error('Signup failed');
    return response.json();
  },

  login: async (email, password) => {
    const response = await fetch(`${API_BASE_URL}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    });
    if (!response.ok) throw new Error('Login failed');
    return response.json();
  }
};

export const notifierService = {
  getAll: async (token) => {
    const response = await fetch(`${API_BASE_URL}/notifiers`, {
      headers: { 'Authorization': `Bearer ${token}` }
    });
    if (!response.ok) throw new Error('Failed to fetch notifiers');
    return response.json();
  },

  create: async (token, notifierData) => {
    const response = await fetch(`${API_BASE_URL}/notifiers`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(notifierData)
    });
    if (!response.ok) throw new Error('Failed to create notifier');
    return response.json();
  },

  update: async (token, id, notifierData) => {
    const response = await fetch(`${API_BASE_URL}/notifiers/${id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(notifierData)
    });
    if (!response.ok) throw new Error('Failed to update notifier');
    return response.json();
  },

  delete: async (token, id) => {
    const response = await fetch(`${API_BASE_URL}/notifiers/${id}`, {
      method: 'DELETE',
      headers: { 'Authorization': `Bearer ${token}` }
    });
    if (!response.ok) throw new Error('Failed to delete notifier');
    return response.json();
  }
};

export const notificationService = {
  getForNotifier: async (token, notifierId) => {
    const response = await fetch(
      `${API_BASE_URL}/notifications/notifier/${notifierId}`,
      { headers: { 'Authorization': `Bearer ${token}` } }
    );
    if (!response.ok) throw new Error('Failed to fetch notifications');
    return response.json();
  },

  markAsViewed: async (token, notificationId) => {
    const response = await fetch(
      `${API_BASE_URL}/notifications/${notificationId}/viewed`,
      {
        method: 'PUT',
        headers: { 'Authorization': `Bearer ${token}` }
      }
    );
    if (!response.ok) throw new Error('Failed to mark as viewed');
    return response.json();
  }
};
```

### React Component Example

```javascript
// components/NotifierList.jsx
import { useState, useEffect } from 'react';
import { notifierService } from '../services/api';

export default function NotifierList() {
  const [notifiers, setNotifiers] = useState([]);
  const [loading, setLoading] = useState(true);
  const token = localStorage.getItem('token');

  useEffect(() => {
    loadNotifiers();
  }, []);

  const loadNotifiers = async () => {
    try {
      const data = await notifierService.getAll(token);
      setNotifiers(data);
    } catch (error) {
      console.error('Error loading notifiers:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div>Loading...</div>;

  return (
    <div className="notifier-list">
      <h2>My Notifiers</h2>
      {notifiers.map(notifier => (
        <div key={notifier.id} className="notifier-card">
          <h3>{notifier.name}</h3>
          <p>City: {notifier.city}</p>
          <p>Experience: {notifier.experience}</p>
          <p>Salary: {notifier.salaryExpectation}</p>
          {notifier.unreadNotificationsCount > 0 && (
            <span className="badge">
              {notifier.unreadNotificationsCount} new
            </span>
          )}
          <button onClick={() => viewNotifier(notifier.id)}>
            View Notifications
          </button>
        </div>
      ))}
    </div>
  );
}
```

## Testing Checklist

- [ ] Sign up a new user
- [ ] Login with the user
- [ ] Create a notifier with preferences
- [ ] Insert test jobs into database
- [ ] Wait for scheduler to run (or trigger manually)
- [ ] Check notifications for the notifier
- [ ] Verify resume PDFs are generated and uploaded
- [ ] Test relevance scoring with different job/preference combinations
- [ ] Mark notifications as viewed
- [ ] Update notifier preferences
- [ ] Delete notifier
- [ ] Test authentication errors (wrong password, etc.)
- [ ] Test validation errors (missing fields, etc.)

## Common Issues and Solutions

### 1. Scheduler Not Running

**Problem:** Jobs are not being processed

**Solutions:**
- Check `scheduler.enabled=true` in application.properties
- Verify `@EnableScheduling` is present in main application class
- Check scheduler state in database: `SELECT * FROM scheduler_state`
- Check logs for scheduler execution

### 2. Resume Generation Failing

**Problem:** Resume links are null

**Solutions:**
- Verify LaTeX syntax is correct
- Check LaTeX compiler service is accessible
- Verify Cloudinary credentials are correct
- Check logs for PDF generation errors

### 3. Low Relevance Scores

**Problem:** No jobs matching even with good criteria

**Solutions:**
- Lower `ai.relevance.threshold` (e.g., from 0.7 to 0.5)
- Improve AI prompt template
- Check OpenAI API is working
- Review job posting format

### 4. Authentication Errors

**Problem:** JWT token not working

**Solutions:**
- Check JWT secret is configured
- Verify token is being sent in Authorization header
- Check token hasn't expired
- Ensure Bearer prefix is included

## Performance Tips

1. **Database Indexing:**
```sql
CREATE INDEX idx_jobs_processed_timestamp ON jobs(processed, timestamp);
CREATE INDEX idx_notifications_notifier_viewed ON notifications(notifier_id, viewed);
CREATE INDEX idx_notifiers_user ON notifiers(user_id);
```

2. **Batch Processing:**
   - Process jobs in batches to reduce memory usage
   - Limit concurrent AI API calls

3. **Caching:**
   - Cache notifier preferences during scheduler run
   - Cache AI responses for identical jobs

4. **Monitoring:**
   - Track scheduler execution time
   - Monitor AI API latency
   - Alert on PDF generation failures

