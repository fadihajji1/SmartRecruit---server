# Existing Tech Stack

- Language/runtime: Java 21, Maven multi-module build.
- Framework: Spring Boot parent `4.1.1` in the root POM; Spring Cloud BOM `2025.1.3`; Spring Cloud Gateway in the gateway module.
- Security: Spring Security resource-server JWT validation with HMAC-SHA256; Identity issues JWTs.
- Persistence: Spring Data JPA/Hibernate and PostgreSQL. Flyway is configured in Identity and Notification.
- API documentation: springdoc OpenAPI starter, with Swagger/OpenAPI test assertions in each module.
- Object storage: MinIO Java SDK in Identity for recruiter employment-proof files.
- Infrastructure images: PostgreSQL 17 Alpine, Redis 7 Alpine, Apache Kafka 4.2.2, Kafka UI, MinIO, Zipkin, pgAdmin.
- Serialization/validation: Jakarta validation and Spring MVC/WebFlux JSON handling.
- Tests: JUnit/Spring Boot context tests and MockMvc-style OpenAPI checks; no domain integration suite is visible for the planned services.

## Observed constraints and inconsistencies

- `docs/implementation-phases.md` says Spring Boot 3.x, while the root build declares Spring Boot 4.1.1.
- The docs describe OpenFeign, Kafka, Redis caching, Resilience4j, and Actuator as architecture elements, but the source dependency/configuration inventory does not show these implemented in the business-service modules.
- The gateway routes all seven business domains, while Compose only builds/runs Identity, Notification, and Gateway.
- Notification declares a default application port of 8084, but Compose overrides it to 8080 and maps host port 8088.
