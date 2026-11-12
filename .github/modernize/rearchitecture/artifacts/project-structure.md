# Project Structure

## Project type

Backend monorepo containing eight Maven modules: one Spring Cloud Gateway and seven Spring Boot business-service modules. No frontend source exists in this repository; the React/TypeScript frontend described by the documentation is external/planned.

## Modules and layers

- `services/api-gateway`: reactive Spring Cloud Gateway entry point; route configuration in `src/main/resources/application.yml` and JWT filtering in `GatewaySecurityConfiguration`.
- `services/identity-service`: the most complete domain. Layers are controller, DTO, service, JPA model, repository, storage adapter, exception handling, security/configuration, and Flyway migrations.
- `services/notification-service`: implemented domain. Layers are controller, DTO, service, JPA model, repository, exception handling, security/configuration, and Flyway migration.
- `services/recruitment-service`, `candidate-ai-service`, `subscription-service`, `interview-service`, `analytics-service`: application bootstrap, JWT security/OpenAPI configuration, properties, and documentation smoke tests; no domain controllers, entities, repositories, migrations, Kafka consumers/producers, or Feign clients are present in source.

## Entry points

- Maven reactor: root `pom.xml`.
- Spring Boot main classes: one per service module.
- Gateway route table: `services/api-gateway/src/main/resources/application.yml`.
- Identity REST controller: `/api/v1/auth/**` and `/api/v1/users/me`.
- Notification REST controller: `/api/v1/notifications/**`.
- Database migrations: Identity V1/V2 and Notification V1.

## Functional areas

Implemented: authentication/profile management, recruiter employment-proof storage, JWT issuance/validation, and notification CRUD/read-state management.

Documented but not implemented in this source snapshot: recruitment/business/jobs/applications, CV/AI, subscriptions/billing, interviews, Kafka-driven notification delivery, and analytics/read models.

## Configuration and operational files

- `.env.example`: local secret/config variable contract.
- `docker-compose.yml`: local PostgreSQL instances, Redis, Kafka, Kafka UI, MinIO, pgAdmin, Zipkin, Identity, Notification, and Gateway containers.
- `Dockerfile`: parameterized Maven build/runtime image for a selected module.
- `kubernetes/`: manifests for the intended production services, but not a complete deployment set for all infrastructure dependencies.
- `docs/`: architectural and product documentation; `src/docs/` duplicates most documents and is not consistently identical.
