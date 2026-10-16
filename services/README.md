# SmartRecruit backend

## Architecture

This repository contains the Spring Boot backend, not a web frontend. It uses Java 17, Spring Boot 3, PostgreSQL with one database per service, JWT resource-server security, MinIO for private files, Kafka for domain events, and Flyway migrations. The relevant APIs are Identity (8082), Recruitment (8090), Candidate/AI (8091), and Notification (8094). Swagger UI is available at `/swagger-ui/index.html` on each running service.

Identity stores shared user identity fields and the Candidate/Recruiter role. A recruiter-owned Business is stored in Recruitment; this keeps service database ownership separate. Candidate CVs are stored as private MinIO objects with metadata in the Candidate database. Recruitment writes notification events to a transactional outbox; Notification consumes Kafka events and stores one notification per recipient and event.

## Implemented flows

- Candidate registration, recruiter registration with employment-proof upload, BCrypt password hashing, JWT login, role authorization, profile read/update, administrator approval, and stateless logout.
- Recruiter company/business and offer management, public search/filter/detail, publishing, close/reopen, automatic expiration, and ownership checks.
- Candidate CV upload, signature/type/size validation, private MinIO storage, short-lived candidate download links, application submission with duplicate protection, application history, and recruiter status updates.
- Kafka notifications for new applications and status changes, outbox retry, idempotent persistence, read/unread, and per-user delete/list access.

## Requirements

- Java 17+
- Docker Compose
- Maven Wrapper (`mvnw.cmd` on Windows)

## Start local infrastructure

From the repository root, set the required secrets for the root `docker-compose.yml` and start its infrastructure:

```powershell
$env:POSTGRES_PASSWORD = 'replace-with-a-local-password'
$env:MINIO_ROOT_USER = 'minioadmin'
$env:MINIO_ROOT_PASSWORD = 'replace-with-a-long-local-password'
$env:REDIS_PASSWORD = 'replace-with-a-local-password'
docker compose up -d identity-db recruitment-db candidate-db notification-db kafka minio
```

The mapped local ports are PostgreSQL 5433/5434/5435/5438, Kafka 9093, and MinIO API 9002 (console 9003). Flyway creates/updates the service schemas when each service starts.

## Run services locally

Open a separate PowerShell terminal for each service. Set shared connection variables in each terminal (the URLs below correspond to the root Compose file):

```powershell
$env:POSTGRES_USER = 'smartrecruit'
$env:POSTGRES_PASSWORD = 'replace-with-a-local-password'
$env:IDENTITY_JWT_SECRET = 'replace-with-at-least-32-random-bytes'
$env:KAFKA_BOOTSTRAP_SERVERS = 'localhost:9093'
$env:MINIO_URL = 'http://localhost:9002'
$env:MINIO_ACCESS_KEY = 'minioadmin'
$env:MINIO_SECRET_KEY = 'replace-with-a-long-local-password'
```

For Identity, Recruitment, Candidate/AI, and Notification respectively, set the matching database URL and run the module:

```powershell
$env:IDENTITY_SERVICE_DB_URL = 'jdbc:postgresql://localhost:5433/smartrecruit_identity'
./mvnw.cmd -pl services/identity-service spring-boot:run
```

```powershell
$env:RECRUITMENT_SERVICE_DB_URL = 'jdbc:postgresql://localhost:5434/smartrecruit_recruitment'
./mvnw.cmd -pl services/recruitment-service spring-boot:run
```

```powershell
$env:CANDIDATE_AI_SERVICE_DB_URL = 'jdbc:postgresql://localhost:5435/smartrecruit_candidate'
./mvnw.cmd -pl services/candidate-ai-service spring-boot:run
```

```powershell
$env:NOTIFICATION_SERVICE_DB_URL = 'jdbc:postgresql://localhost:5438/smartrecruit_notification'
./mvnw.cmd -pl services/notification-service spring-boot:run
```

Use the same `IDENTITY_JWT_SECRET` and issuer for all services. For the employment-proof bucket, Identity accepts `MINIO_BUCKET` (default `smartrecruit-documents`); Candidate/AI accepts `MINIO_CV_BUCKET` (default `smartrecruit-cvs`). Both services create their bucket on first upload.

## API examples

- Candidate registration: `POST http://localhost:8082/api/v1/auth/register/candidate` with JSON fields `email`, `password` (12–72 characters), `fullName`, and optional profile fields.
- Recruiter registration: `POST http://localhost:8082/api/v1/auth/register/recruiter` as `multipart/form-data` with the same fields and an `employmentProof` file.
- Login: `POST http://localhost:8082/api/v1/auth/login`; send the returned token as `Authorization: Bearer <accessToken>`.
- Logout: `POST http://localhost:8082/api/v1/auth/logout` with the bearer token. JWT logout is stateless; the client must discard the token.
- Candidate CV upload: `POST http://localhost:8091/api/v1/candidate/cvs` as multipart with field `file`; supported types PDF, DOC and DOCX, maximum 10 MB.
- Recruitment endpoints use `http://localhost:8090/api/v1`; notifications use `http://localhost:8094/api/v1/notifications`.

After recruiter approval, create a Business, create an offer in `DRAFT`, then call its `/publish` endpoint. A candidate applies with JSON fields `jobId`, `candidateCvId`, and optional `message`.

## Tests and frontend

No test source files or frontend application are present in the repository. When Maven dependencies are available, run `./mvnw.cmd verify` from the repository root. The current backend implementation can be exercised through Swagger; a frontend must be added as a separate project if browser screens are required.
