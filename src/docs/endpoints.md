# SmartRecruit API Endpoints

## 1. Conventions

- Base path: `/api/v1`.
- All resource identifiers are GUID/UUID values.
- JSON is the default representation.
- Authentication uses JWT bearer tokens for protected operations.
- Every service exposes OpenAPI/Swagger UI for development/testing.
- Internal synchronous calls use Spring Cloud OpenFeign over REST.
- Kafka events are not API endpoints and are tested through Kafka tooling.

## 2. Swagger documentation

```text
Identity
  └── Swagger

Recruitment
  └── Swagger

Candidate/AI
  └── Swagger

Subscription
  └── Swagger

Interview
  └── Swagger

Notification
  └── Swagger

Analytics
  └── Swagger
```

Swagger UI is the primary development tool for manually testing REST endpoints before frontend integration.

## 3. Identity Service

### Public
```text
POST /api/v1/auth/register/candidate
POST /api/v1/auth/register/recruiter
POST /api/v1/auth/login
POST /api/v1/auth/logout
```

Recruiter registration is `multipart/form-data` and requires an `employmentProof` file (maximum 10 MB). The service stores the file privately in MinIO; the user record retains its object key. Recruiter accounts remain pending until administrator approval.

### Candidate
```text
GET   /api/v1/users/me
PATCH /api/v1/users/me
```

### Recruiter
```text
GET   /api/v1/recruiters/me
PATCH /api/v1/recruiters/me
```

### Administrator
```text
GET   /api/v1/admin/users
GET   /api/v1/admin/users/{userId}
PATCH /api/v1/admin/users/{userId}
POST  /api/v1/admin/recruiters/{userId}/approve
POST  /api/v1/admin/recruiters/{userId}/reject
GET   /api/v1/admin/recruiters/{userId}/employment-proof
```

The employment-proof endpoint returns a short-lived (5 minute) presigned URL and is administrator-only. User responses omit password hashes.

## 4. Recruitment Service

### Public
```text
GET /api/v1/jobs?q=&location=&contractType=&page=&size=
GET /api/v1/jobs/{jobId}
```

### Recruiter
```text
GET    /api/v1/recruiter/businesses
POST   /api/v1/recruiter/businesses
GET    /api/v1/recruiter/businesses/{businessId}
PATCH  /api/v1/recruiter/businesses/{businessId}
DELETE /api/v1/recruiter/businesses/{businessId}
GET    /api/v1/admin/businesses
POST   /api/v1/admin/businesses
GET    /api/v1/admin/businesses/{businessId}
PATCH  /api/v1/admin/businesses/{businessId}
DELETE /api/v1/admin/businesses/{businessId}
GET    /api/v1/recruiter/jobs
POST   /api/v1/recruiter/jobs
GET    /api/v1/recruiter/jobs/{jobId}
PATCH  /api/v1/recruiter/jobs/{jobId}
POST   /api/v1/recruiter/jobs/{jobId}/close
POST   /api/v1/recruiter/jobs/{jobId}/publish
POST   /api/v1/recruiter/jobs/{jobId}/reopen
DELETE /api/v1/recruiter/jobs/{jobId}
GET    /api/v1/recruiter/applications
GET    /api/v1/recruiter/applications/{applicationId}
PATCH  /api/v1/recruiter/applications/{applicationId}/status
```

### Candidate
```text
POST /api/v1/applications
GET  /api/v1/candidate/applications
GET  /api/v1/candidate/applications/{applicationId}
```

## 5. Candidate/AI Service

```text
GET    /api/v1/candidate/cvs
POST   /api/v1/candidate/cvs
GET    /api/v1/candidate/cvs/{cvId}
GET    /api/v1/candidate/cvs/{cvId}/download
DELETE /api/v1/candidate/cvs/{cvId}
POST   /api/v1/candidate/cvs/{cvId}/ats-scan
GET    /api/v1/candidate/recommendations
```

CV upload accepts PDF, DOC or DOCX files up to 10 MB, validates the file signature, and stores the object in MinIO. Download returns a short-lived signed URL to the owning candidate. Private objects are never public.

## 6. Subscription Service

### Public
```text
GET /api/v1/subscriptions/plans
```

### Candidate
```text
GET    /api/v1/candidate/subscription
POST   /api/v1/candidate/subscription
POST   /api/v1/candidate/subscription/upgrade
POST   /api/v1/candidate/subscription/downgrade
POST   /api/v1/candidate/subscription/renew
POST   /api/v1/candidate/subscription/cancel
GET    /api/v1/candidate/subscription/history
```

### Recruiter
```text
GET    /api/v1/recruiter/subscription
POST   /api/v1/recruiter/subscription
POST   /api/v1/recruiter/subscription/upgrade
POST   /api/v1/recruiter/subscription/downgrade
POST   /api/v1/recruiter/subscription/renew
POST   /api/v1/recruiter/subscription/cancel
GET    /api/v1/recruiter/subscription/history
```

### Administrator
```text
GET   /api/v1/admin/subscription/plans
POST  /api/v1/admin/subscription/plans
PATCH /api/v1/admin/subscription/plans/{planId}
GET   /api/v1/admin/subscription/company-subscriptions
PATCH /api/v1/admin/subscription/company-subscriptions/{subscriptionId}
```

## 7. Interview Service

### Recruiter
```text
GET   /api/v1/recruiter/interviews
POST  /api/v1/recruiter/interviews
GET   /api/v1/recruiter/interviews/{interviewId}
PATCH /api/v1/recruiter/interviews/{interviewId}
POST  /api/v1/recruiter/interviews/{interviewId}/cancel
POST  /api/v1/recruiter/interviews/{interviewId}/done
POST  /api/v1/recruiter/interviews/{interviewId}/delay-request/approve
POST  /api/v1/recruiter/interviews/{interviewId}/delay-request/reject
```

When scheduling, the request includes an external meeting URL, for example a Zoom or Google Meet link.

### Candidate
```text
GET  /api/v1/candidate/interviews
GET  /api/v1/candidate/interviews/{interviewId}
POST /api/v1/candidate/interviews/{interviewId}/cancel
POST /api/v1/candidate/interviews/{interviewId}/delay-request
```

## 8. Notification Service

```text
GET    /api/v1/notifications
PATCH  /api/v1/notifications/{notificationId}/read
PATCH  /api/v1/notifications/{notificationId}/unread
DELETE /api/v1/notifications/{notificationId}
```

The Notification Service is event-driven internally. It consumes relevant Kafka events and creates notification records, then can deliver through in-app, email or outbound webhook mechanisms.

Recruitment events are written to a database outbox in the same transaction as an application/status change, published to Kafka, then persisted idempotently by the Notification Service. Each notification endpoint is scoped to the JWT subject user ID.

## 9. Analytics Service

```text
GET  /api/v1/recruiter/analytics/overview
GET  /api/v1/recruiter/analytics/offers
GET  /api/v1/recruiter/analytics/applications
GET  /api/v1/recruiter/analytics/export/pdf
```

## 10. Operational endpoints

Each service should expose protected/appropriate actuator endpoints such as:

```text
GET /actuator/health
GET /actuator/info
GET /actuator/metrics
```

## 11. Error contract

Use a consistent JSON error shape, for example:

```json
{
  "timestamp": "...",
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "path": "/api/v1/jobs"
}
```

## 12. Feign and resilience

Internal Feign clients must define explicit connect/read timeouts and resilience policies. Retry only safe transient failures; avoid blindly retrying non-idempotent commands. Circuit breakers should return controlled errors when a dependency is unavailable.
