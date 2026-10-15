# SmartRecruit Roadmap

## 1. Roadmap objective

Deliver SmartRecruit as a seven-service microservices platform with documented REST APIs, Swagger-based development testing, Feign-based synchronous communication, Kafka-driven asynchronous processing, MinIO file storage, resilient integrations and Kubernetes-ready deployment.

## 2. Roadmap

| Phase | Goal | Main deliverables |
|---|---|---|
| 1 | Foundations | Repositories, Spring Boot services, PostgreSQL, UUID convention, Docker Compose, Kafka, MinIO, Swagger |
| 2 | Identity | User inheritance model, candidate/recruiter/admin, JWT, recruiter approval, employment proof |
| 3 | Recruitment | Businesses, jobs, public timeline, applications and lifecycle rules |
| 4 | Candidate/AI | CV upload, MinIO storage, parsing, ATS and AI recommendations |
| 5 | Subscriptions | Plans, trials, upgrades/downgrades, history and mock payment |
| 6 | Interviews | Scheduling, external meeting links, calendar and delay workflow |
| 7 | Notifications + Events | Kafka events, notification consumer, in-app/email/webhook delivery |
| 8 | Analytics | Event-driven read models and PDF reporting |
| 9 | Hardening | Feign resilience, retries, timeouts, circuit breakers, DLQ, observability and security |
| 10 | Deployment | Docker registry, Kubernetes manifests and end-to-end validation |

## 3. Cross-phase quality gates

Every phase must preserve:
- UUID/GUID primary keys.
- Database-per-service ownership.
- No cross-service database access.
- Swagger/OpenAPI documentation.
- Server-side authorization.
- Feign for synchronous service communication.
- Kafka for asynchronous events.
- Resilience for important synchronous dependencies.
- MinIO for CV/document object storage.

## 4. Important business gates

### Identity
- Candidate automatically active.
- Recruiter requires employment proof and administrator approval.
- Candidate, Recruiter and Platform Administrator inherit common User properties.

### Recruitment
- Job timeline is public.
- Authentication is required before application.
- Closed jobs reject new applications.
- Deleted jobs preserve application history with `Unavailable` semantics.

### CV/AI
- CV binary stored in MinIO.
- PostgreSQL stores metadata/object key.
- External AI is accessed only through Candidate/AI Service.

### Interviews
- Recruiter supplies external Zoom/Google Meet or similar meeting URL.
- SmartRecruit does not host the video session.
- Candidate delay requests do not change the original interview until approved.

### Notifications
- Domain event -> Kafka -> Notification consumer.
- Notification Service creates in-app records and can trigger email/webhook delivery.

## 5. Final target architecture

```text
React
  |
API Gateway
  |
  +--> Identity
  +--> Recruitment
  +--> Candidate/AI
  +--> Subscription
  +--> Interview
  +--> Notification
  +--> Analytics

Synchronous: REST + OpenFeign + resilience
Asynchronous: Kafka + idempotent consumers + DLQ
Storage: PostgreSQL per service + MinIO for files
Testing: Swagger/OpenAPI
Deployment: Docker Compose + Kubernetes
```
