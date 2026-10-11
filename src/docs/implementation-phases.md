# SmartRecruit Implementation Phases

## 1. Implementation strategy

The implementation follows the seven-service architecture and prioritizes working business flows before production hardening.

## Phase 0 — Project foundations

### Backend
- Create seven Spring Boot services.
- Configure Java 21 and Spring Boot 3.x.
- Configure Spring Security/JWT.
- Configure Spring Cloud OpenFeign.
- Configure Resilience4j for Feign dependencies.
- Configure Kafka.
- Configure OpenAPI/Swagger for every service.
- Establish UUID/GUID ID conventions.

### Infrastructure
- Docker Compose.
- PostgreSQL databases.
- Kafka + Kafka UI.
- MinIO.

## Phase 1 — Identity Service

Implement:
1. Base `User` model.
2. Candidate, Recruiter and Platform Administrator specialized profiles inheriting User properties.
3. Candidate registration/activation.
4. Recruiter registration with employment-proof upload to MinIO.
5. Recruiter pending/approved/rejected lifecycle.
6. Login/JWT.
7. Profile management.
8. Administrator recruiter approval.
9. Swagger/OpenAPI.

### Tests
- Registration validation.
- Unique email.
- Recruiter cannot sign in while pending/rejected.
- Candidate automatically active.
- Role authorization.
- MinIO upload metadata.

## Phase 2 — Recruitment Service

Implement the grouped recruitment domain:
1. Business CRUD and status.
2. Recruiter/company authorization.
3. Public job timeline/search/filter.
4. Job creation/update/close/reopen/delete.
5. Application creation/list/details/status.
6. Closed-offer and deleted-offer rules.
7. Swagger/OpenAPI.
8. Feign calls where Identity/Subscription validation is required.

## Phase 3 — Candidate/AI Service

Implement:
1. CV upload to MinIO.
2. CV metadata/version management.
3. CV deletion.
4. Parsing pipeline.
5. ATS analysis.
6. AI recommendations.
7. External AI provider adapter.
8. Swagger/OpenAPI.

Use timeouts, retry and circuit breaker around external AI calls.

## Phase 4 — Subscription Service

Implement:
- Candidate plans.
- Recruiter/company plans.
- Basic default for new candidates.
- Starter trial for approved companies.
- Upgrade/downgrade/renew/cancel.
- Subscription history.
- Invoices/billing records.
- Mock payment adapter.
- Swagger/OpenAPI.

## Phase 5 — Interview Service

Implement:
1. Interview scheduling.
2. Candidate/recruiter access control.
3. Calendar/list/details.
4. External meeting URL field.
5. Zoom/Google Meet URL validation.
6. Reschedule/cancel/done.
7. Delay request workflow.
8. Swagger/OpenAPI.

The platform does not create or host the video meeting. The recruiter supplies the external meeting URL.

## Phase 6 — Kafka and Notification Service

Implement domain events such as:

```text
JobCreated
ApplicationSubmitted
ApplicationStatusChanged
InterviewScheduled
InterviewCancelled
InterviewDelayRequested
SubscriptionActivated
```

Notification flow:

```text
Job Service / Recruitment Service
     |
     | JobCreated
     v
   Kafka
     |
     | consumer
     v
Notification Service
     |
     +── Email
     +── In-app notification
     +── Webhook
```

Implement consumer idempotency, retry and dead-letter topics.

## Phase 7 — Analytics Service

Implement:
- Kafka-driven read models.
- Recruiter dashboard metrics.
- Offer/application metrics.
- PDF export.
- Swagger/OpenAPI.

## Phase 8 — Resilience and integration hardening

For synchronous Feign communication:
- Configure connect/read timeouts.
- Add bounded retry with backoff.
- Add circuit breakers.
- Add fallbacks/controlled errors where meaningful.
- Test dependency timeout/failure scenarios.

For Kafka:
- Retry topics/consumer retry policy.
- Dead-letter topics.
- Idempotent consumers.
- Transactional outbox for reliable event publication.

## Phase 9 — Productionization

### Docker
- Build reproducible images.
- Publish versioned images to a shared container registry.

### Kubernetes
- Deployment per service.
- Kubernetes Service per service.
- Ingress/API Gateway.
- ConfigMaps and Secrets.
- PostgreSQL, Kafka and MinIO infrastructure.

### Observability
- Actuator.
- Micrometer metrics.
- Structured logs.
- Tracing foundations.

## Phase 10 — End-to-end validation

### Candidate flow
```text
Register -> Login -> Upload CV -> Search jobs -> Apply
-> Receive notifications -> Manage interview -> Open external meeting link
```

### Recruiter flow
```text
Register + employment proof -> Admin approval -> Login
-> Manage company/jobs -> Review applications
-> Schedule interview + external meeting URL -> Analytics
```

### Administrator flow
```text
Login -> Review recruiters -> Manage businesses
-> Manage subscription plans -> Platform administration
```
