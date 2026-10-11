# SmartRecruit Contribution Guide

## 1. Repository organization

The project uses multiple repositories, with one repository per service and separate repositories where appropriate for frontend/infrastructure. Current business-service repositories correspond to:

```text
smartrecruit-identity-service
smartrecruit-recruitment-service
smartrecruit-candidate-ai-service
smartrecruit-subscription-service
smartrecruit-interview-service
smartrecruit-notification-service
smartrecruit-analytics-service
smartrecruit-frontend
smartrecruit-infrastructure
```

## 2. Ownership principle

Each service owns its domain logic, database and migrations. No developer may bypass a service API to read/write another service's database.

## 3. API and Swagger rules

Every service must:
- Expose OpenAPI documentation.
- Keep Swagger examples accurate.
- Use `/api/v1`.
- Use UUID/GUID identifiers.
- Return consistent validation/error responses.
- Add integration/contract tests for important endpoints.

## 4. Service communication

Use **Spring Cloud OpenFeign** for synchronous service-to-service REST communication.

Use **Kafka** for asynchronous domain events.

Do not create direct database dependencies between services.

## 5. Resilience rules

Feign clients must define:
- Connect timeout.
- Read timeout.
- Bounded retries with backoff for transient/safe failures.
- Circuit breaker for important dependencies.

Do not retry non-idempotent commands blindly. Use idempotency keys or idempotent command handling when a command can be retried safely.

Kafka consumers must use bounded retries and dead-letter handling.

## 6. Kafka rules

Events should represent domain facts, for example:

```json
{
  "eventId": "uuid",
  "eventType": "ApplicationStatusChanged",
  "aggregateId": "uuid",
  "occurredAt": "...",
  "version": 1,
  "payload": {}
}
```

Do not put passwords, tokens, CV binary content or other sensitive secrets into Kafka events.

Notification Service consumes relevant events and transforms them into notification records and outbound delivery actions. It must not treat Kafka as a query database.

## 7. Database rules

- PostgreSQL per service.
- Every primary key is UUID/GUID.
- No `SERIAL`, `BIGSERIAL` or auto-increment integer PKs.
- Use Flyway migrations.
- No cross-service foreign keys.
- Store timestamps consistently.

## 8. File-storage rules

CVs and recruiter employment-proof files are stored in MinIO. PostgreSQL stores metadata/object keys. Do not commit uploaded files to Git.

## 9. Interview rules

The Interview Service stores an external meeting URL supplied by the recruiter. SmartRecruit does not implement video conferencing. URLs may point to Zoom, Google Meet or another approved provider.

## 10. Security rules

- Backend authorization is authoritative.
- Verify recruiter company/resource ownership.
- Candidates can access only their own private resources.
- Private CV objects require controlled access.
- Never commit secrets.

## 11. Testing

### Unit tests
Business logic, validation, authorization and resilience behavior.

### Integration tests
Spring context, PostgreSQL, MinIO and Kafka interactions where relevant.

### API/contract tests
Use OpenAPI/Swagger during development and automated API tests for stable contracts.

### Resilience tests
Simulate timeout, transient failure, circuit opening and recovery.

### Security tests
Verify role restrictions, ownership checks and recruiter approval rules.

## 12. Pull requests

A PR should describe:
- Business requirement/issue.
- Services affected.
- Database migrations.
- API changes.
- Kafka event changes.
- Resilience implications.
- Tests performed.

## 13. Definition of Done

A feature is complete when:
- API is documented in Swagger/OpenAPI.
- Authorization is enforced server-side.
- UUID/GUID identifiers are used.
- Database migration exists.
- Tests pass.
- Feign calls have timeout/resilience configuration where applicable.
- Kafka events are documented when applicable.
- No direct cross-service database access exists.
