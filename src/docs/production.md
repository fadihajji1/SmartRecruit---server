# SmartRecruit Production and Development Architecture

## 1. Purpose

This document defines how the seven-service SmartRecruit architecture is developed, packaged and deployed.

## 2. Development model

Developers use Docker Compose for shared local infrastructure and can run individual Spring Boot services locally for fast development. Each service has Swagger UI for REST API testing.

Recommended local infrastructure:

- PostgreSQL databases/service instances.
- Kafka.
- Kafka UI or equivalent development tooling.
- MinIO for S3-compatible CV/document storage.

The Identity Service requires `POSTGRES_PASSWORD`, `IDENTITY_JWT_SECRET` (at least 32 characters), `MINIO_ROOT_USER`, and `MINIO_ROOT_PASSWORD` in its environment. Configure `IDENTITY_DB_URL` when the service cannot use the local Compose default.

All services that accept Identity Service JWTs must be configured with the same `IDENTITY_JWT_SECRET` and issuer (`smartrecruit-identity`). Swagger UI and `/v3/api-docs` are publicly accessible for development and documentation loading; protected API operations continue to require a valid JWT.

For team consistency, Dockerfiles and Compose configuration are version-controlled. Container images can be published to a shared registry for integration testing.

## 3. Production topology

```text
                         React
                           |
                           v
                    API Gateway / Ingress
                           |
      +--------------------+--------------------+
      |         |          |         |           |
      v         v          v         v           v
 Identity  Recruitment Candidate Subscription Interview
      |         |          |         |           |
      +---------+----------+---------+-----------+
                           |
                         Kafka
                           |
                    +------+-------+
                    |              |
                    v              v
              Notification     Analytics

Candidate/AI Service ----> MinIO
All services ------------> PostgreSQL (database-per-service)
```

## 4. Kubernetes

Production runs on Kubernetes. Kubernetes Services provide native service discovery. There is no requirement for Eureka.

Typical components:

- Ingress/API Gateway.
- Kubernetes Deployment per service.
- Kubernetes Service per service.
- ConfigMaps for non-secret configuration.
- Secrets for credentials/tokens.
- Persistent storage for PostgreSQL/MinIO where self-hosted.
- Kafka deployment or managed Kafka.

Internal service URLs follow Kubernetes DNS, for example:

```text
http://identity-service
http://recruitment-service
http://candidate-ai-service
http://subscription-service
http://interview-service
http://notification-service
http://analytics-service
```

Feign clients target these service names through configuration.

## 5. File storage

MinIO is the selected S3-compatible storage technology for CVs and recruiter employment-proof documents. PostgreSQL stores metadata and object keys.

```text
Candidate/Identity upload
          |
          v
       MinIO
          ^
          |
PostgreSQL stores metadata/object key
```

Private objects must not be anonymously readable. Access should be mediated by the owning service and, where appropriate, short-lived signed URLs.

## 6. Kafka

Kafka is the internal event backbone. Example events include:

```text
JobCreated
JobClosed
JobDeleted
ApplicationSubmitted
ApplicationStatusChanged
InterviewScheduled
InterviewCancelled
InterviewDelayRequested
SubscriptionActivated
CVUploaded
ATSAnalysisCompleted
```

The Notification Service uses consumer groups to process relevant events. It then creates in-app notifications and can trigger email or webhooks.

## 7. Resilience

All important synchronous Feign integrations must use bounded resilience configuration:

- Connect timeout.
- Read timeout.
- Bounded retry count.
- Exponential/backoff delay.
- Circuit breaker.
- Optional bulkhead for expensive AI/external work.

Example flow:

```text
Feign Client
    |
    v
Timeout
    |
Retry (bounded)
    |
Circuit Breaker
    |
    v
Downstream Service
```

Kafka consumers use retry and dead-letter topics. Consumers are idempotent.

## 8. Security

- HTTPS at the public edge.
- JWT authentication.
- Role and resource ownership authorization.
- Recruiter company isolation.
- Private CV/document access.
- Secrets stored outside Git.
- No direct browser access to internal service databases.

## 9. Observability

Use Spring Boot Actuator and Micrometer for:

- Health endpoints.
- Metrics.
- Request timing.
- Feign dependency timing/failures.
- Kafka consumer metrics.
- Structured logs.
- Distributed tracing foundations.

## 10. Scaling

Services scale independently. Public job browsing can cause higher load on Recruitment Service, while AI processing can require separate scaling for Candidate/AI workers. Notification and Analytics consumers can scale horizontally using Kafka consumer groups.

## 11. Database identifiers

Every PostgreSQL primary key is UUID/GUID. Auto-increment integer identifiers are prohibited. Cross-service IDs are logical references, not cross-database foreign keys.
