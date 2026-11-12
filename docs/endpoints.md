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

Each service exposes its documentation independently at:

```text
http://localhost:<service-port>/swagger-ui/index.html
http://localhost:<service-port>/v3/api-docs
```

Swagger UI and the OpenAPI JSON are public so the UI can load its specification. Use **Authorize** in Swagger UI to provide a JWT for protected operations. All services validate Identity Service tokens using the same `JWT_SECRET` and `identity.jwt.issuer` configuration. Set a strong secret of at least 32 UTF-8 bytes in each service's environment; never commit it.

## 3. Identity Service

### Public
```text
POST /api/v1/auth/register/candidate
POST /api/v1/auth/register/recruiter
POST /api/v1/auth/login
```

Candidate registration accepts JSON. Recruiter registration uses `multipart/form-data` with a JSON `registration` part and an `employmentProof` PDF, JPEG, or PNG file (maximum 10 MB). The Identity Service stores the private file in MinIO and retains only its object key. Recruiter accounts remain pending until administrator approval.

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
```

## 4. Recruitment Service

### Public
```text
GET /api/v1/jobs
GET /api/v1/jobs/{jobId}
```

### Recruiter
```text
GET    /api/v1/recruiter/businesses
GET    /api/v1/recruiter/businesses/{businessId}
PATCH  /api/v1/recruiter/businesses/{businessId}
GET    /api/v1/recruiter/jobs
POST   /api/v1/recruiter/jobs
GET    /api/v1/recruiter/jobs/{jobId}
PATCH  /api/v1/recruiter/jobs/{jobId}
POST   /api/v1/recruiter/jobs/{jobId}/close
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

CV upload is `multipart/form-data` with a `file` part. The service accepts valid PDF, DOC and DOCX files up to 10 MB, stores their private binaries in MinIO, and returns metadata only. Download is streamed through the authenticated service; a CV is never exposed as a public object URL. ATS scan accepts an optional JSON body such as `{"jobDescription":"Senior Java developer..."}`. The local provider currently performs deterministic skills and document-structure analysis; it does not call an external model.

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
GET    /api/v1/candidate/subscription/invoices
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
GET    /api/v1/recruiter/subscription/invoices
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

The current implementation provides the authenticated in-app inbox, read/unread and delete operations. Email and webhook delivery are integration points, not configured delivery channels.

## 9. Analytics Service

```text
GET  /api/v1/recruiter/analytics/overview
GET  /api/v1/recruiter/analytics/offers
GET  /api/v1/recruiter/analytics/applications
GET  /api/v1/recruiter/analytics/export/pdf
```

Analytics aggregates domain events and scopes rows to the authenticated recruiter's `user_id`. Metrics remain empty until Recruitment and Interview publishers emit events with the recruiter and aggregate IDs described below.

## 11. Domain event envelope

Candidate/AI, Notification and Analytics consume JSON from the shared `smartrecruit.domain-events` Kafka topic. Publish one JSON object per event using this envelope:

```json
{
  "eventId": "UUID",
  "eventType": "JOB_PUBLISHED",
  "occurredAt": "2026-11-12T12:00:00Z",
  "aggregateId": "UUID",
  "payload": {}
}
```

The `eventId` must be a stable UUID across redelivery. Job events should include `jobId`, `businessId`, `recruiterId`, `title`, `description`, `location` and `status`. Application events should include `applicationId`, `jobId`, `candidateId`, `recruiterId`, `status` and `jobTitle`. Interview events should include `interviewId`, `applicationId`, `candidateId`, `recruiterId`, `jobId` and `status`. Use event names such as `JOB_PUBLISHED`, `JOB_UPDATED`, `JOB_CLOSED`, `JOB_DELETED`, `APPLICATION_SUBMITTED`, `APPLICATION_STATUS_CHANGED`, `INTERVIEW_SCHEDULED`, `INTERVIEW_RESCHEDULED` and `INTERVIEW_CANCELLED`.

For Notification events, include `recipientUserId` or `recipientUserIds` in `payload`. The service derives application, interview and subscription recipients from the payload when those fields are absent. Consumers are idempotent and retry failures before sending exhausted records to `<topic>.DLT`.

## 12. Operational endpoints

Each service should expose protected/appropriate actuator endpoints such as:

```text
GET /actuator/health
GET /actuator/info
GET /actuator/metrics
```

## 13. Error contract

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
