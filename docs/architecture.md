# SmartRecruit — System Architecture

## 1. Architecture Overview

SmartRecruit is a distributed microservices platform built with **React/TypeScript** and **Spring Boot**.

## Project Structure

```
SmartRecruit/
├── docs
│   ├── 0- technical specifications.md
│   ├── 0- used system design.md
│   ├── actors.md
│   ├── architecture.md
│   ├── contribution-guide.md
│   ├── database.md
│   ├── endpoints.md
│   ├── implementation-phases.md
│   ├── production.md
│   ├── roadmap.md
│   └── SmartRecruit_Documentation_Updated.rar
├── kubernetes
│   ├── api-gateway
│   │   ├── api-gateway-deployment.yaml
│   │   └── api-gateway-service.yaml
│   ├── analytics-service.yaml
│   ├── candidate-ai-service.yaml
│   ├── identity-service.yaml
│   ├── interview-service.yaml
│   ├── notification-service.yaml
│   ├── recruitment-service.yaml
│   └── subscription-service.yaml
├── services
│   ├── analytics-service
│   │   ├── src
│   │   │   ├── main
│   │   │   │   ├── java
│   │   │   │   │   └── com
│   │   │   │   └── resources
│   │   │   │       └── application.properties
│   │   │   └── test
│   │   │       └── java
│   │   │           └── com
│   │   └── pom.xml
│   ├── api-gateway
│   │   ├── src
│   │   │   ├── main
│   │   │   │   ├── java
│   │   │   │   │   └── com
│   │   │   │   └── resources
│   │   │   │       └── application.yml
│   │   │   └── test
│   │   │       └── java
│   │   │           └── com
│   │   ├── Dockerfile
│   │   └── pom.xml
│   ├── candidate-ai-service
│   │   ├── src
│   │   │   ├── main
│   │   │   │   ├── java
│   │   │   │   │   └── com
│   │   │   │   └── resources
│   │   │   │       └── application.properties
│   │   │   └── test
│   │   │       └── java
│   │   │           └── com
│   │   └── pom.xml
│   ├── identity-service
│   │   ├── src
│   │   │   ├── main
│   │   │   │   ├── java
│   │   │   │   │   └── com
│   │   │   │   └── resources
│   │   │   │       ├── db
│   │   │   │       └── application.yml
│   │   │   └── test
│   │   │       └── java
│   │   │           └── com
│   │   └── pom.xml
│   ├── interview-service
│   │   ├── src
│   │   │   ├── main
│   │   │   │   ├── java
│   │   │   │   │   └── com
│   │   │   │   └── resources
│   │   │   │       └── application.properties
│   │   │   └── test
│   │   │       └── java
│   │   │           └── com
│   │   └── pom.xml
│   ├── notification-service
│   │   ├── src
│   │   │   ├── main
│   │   │   │   ├── java
│   │   │   │   │   └── com
│   │   │   │   └── resources
│   │   │   │       └── application.properties
│   │   │   └── test
│   │   │       └── java
│   │   │           └── com
│   │   └── pom.xml
│   ├── recruitment-service
│   │   ├── src
│   │   │   ├── main
│   │   │   │   ├── java
│   │   │   │   │   └── com
│   │   │   │   └── resources
│   │   │   │       └── application.properties
│   │   │   └── test
│   │   │       └── java
│   │   │           └── com
│   │   └── pom.xml
│   └── subscription-service
│       ├── src
│       │   ├── main
│       │   │   ├── java
│       │   │   │   └── com
│       │   │   └── resources
│       │   │       └── application.properties
│       │   └── test
│       │       └── java
│       │           └── com
│       └── pom.xml
├── src
│   ├── main
│   │   └── java
│   │       └── com
│   │           └── smartrecruit
│   └── test
│       └── java
│           └── com
│               └── smartrecruit
│                   ├── analytics
│                   ├── candidateai
│                   ├── interview
│                   ├── notification
│                   ├── recruitment
│                   └── subscription
├── .env.example
├── all_docs.txt
├── all_services_codes.txt
├── docker-compose.yml
├── Dockerfile
├── HELP.md
├── mvnw
├── mvnw.cmd
├── pom.xml
└── readme.md
```

## The architecture uses:

| Component                           | Description                                                                                            |
| ----------------------------------- | ------------------------------------------------------------------------------------------------------ |
| 7 Business Services                 | Core domain services organized around business capabilities.                                           |
| Spring Cloud Gateway                | Central API entry point for path-based routing and JWT validation.                                     |
| Kubernetes-Native Service Discovery | Kubernetes Services provide stable DNS names and load balancing between gateway and backend workloads. |
| PostgreSQL (Database-per-Service)   | Each microservice owns its dedicated PostgreSQL database.                                              |
| Apache Kafka                        | Asynchronous domain event streaming and inter-service communication.                                   |
| OpenFeign / REST                    | Synchronous service-to-service communication.                                                          |
| Redis                               | Selective caching layer for read-heavy and frequently accessed data.                                   |
| MinIO                               | Object storage for CVs, employment-proof documents, and other files.                                   |
| External Integrations               | AI providers, payment gateways, email services, webhooks.                                              |

### Core principles

1. Each service owns its data.
2. PostgreSQL is the source of truth for transactional data.
3. Redis is a performance layer, never the primary database.
4. Services never access another service's database directly.
5. OpenFeign/REST is used when an immediate response is required.
6. Kafka is used for asynchronous domain events.
7. Authorization is enforced by the backend.
8. Private files are stored in MinIO and accessed through authorized backend operations.
9. CQRS is used primarily where read requirements differ significantly from transactional workloads.

---

## 2. Business Services

| #   | Service              | Responsibilities                                         | Storage            |
| --- | -------------------- | -------------------------------------------------------- | ------------------ |
| 1   | Identity Service     | Authentication, users, profiles, recruiter approval, JWT | PostgreSQL         |
| 2   | Recruitment Service  | Businesses, jobs, applications                           | PostgreSQL + Redis |
| 3   | Candidate/AI Service | CVs, CV versions, AI analysis and recommendations        | PostgreSQL + MinIO |
| 4   | Subscription Service | Plans, subscriptions, trials, billing history            | PostgreSQL + Redis |
| 5   | Interview Service    | Scheduling, rescheduling, cancellation, delay requests   | PostgreSQL         |
| 6   | Notification Service | Email, in-app, webhook notifications                     | PostgreSQL         |
| 7   | Analytics Service    | Analytics read models and PDF reports                    | PostgreSQL + Redis |

### Service boundaries

- **Identity** owns accounts, authentication, roles, profiles, and recruiter approval.
- **Recruitment** owns businesses, jobs, and applications.
- **Candidate/AI** owns CV metadata, CV versions, document references, and AI processing.
- **Subscription** owns plans, subscriptions, trials, entitlements, and billing state.
- **Interview** owns interview scheduling and meeting-link metadata.
- **Notification** consumes events and manages notification delivery.
- **Analytics** owns reporting-oriented read models.

---

## 3. High-Level Architecture

```text
                         ┌──────────────────┐
                         │  React Frontend  │
                         └────────┬─────────┘
                                  │
                                  ▼
                         ┌──────────────────┐
                         │   API Gateway    │
                         │ Spring Cloud GW  │
                         └────────┬─────────┘
                                  │
              ┌───────────────────┼───────────────────┐
              │                   │                   │
              ▼                   ▼                   ▼
       ┌────────────┐      ┌────────────┐      ┌────────────┐
       │  Identity  │      │ Recruitment│      │ Candidate/ │
       │  Service   │      │  Service   │      │ AI Service │
       └─────┬──────┘      └─────┬──────┘      └─────┬──────┘
             │                   │                   │
             ▼                   ▼                   ▼
        PostgreSQL          PostgreSQL          PostgreSQL
                                 │                   │
                                 ▼                   ▼
                                Redis               MinIO

       ┌────────────┐      ┌────────────┐      ┌────────────┐
       │Subscription│      │  Interview │      │Notification│
       │  Service   │      │  Service   │      │  Service   │
       └─────┬──────┘      └─────┬──────┘      └─────┬──────┘
             │                   │                   │
             ▼                   ▼                   ▼
        PostgreSQL          PostgreSQL          PostgreSQL

                         ┌──────────────────┐
                         │   Apache Kafka   │
                         │   Event Backbone │
                         └────────┬─────────┘
                                  │
                                  ▼
                         ┌──────────────────┐
                         │ Analytics Service│
                         └────────┬─────────┘
                                  │
                                  ▼
                         PostgreSQL Read Model
                                  │
                                  ▼
                                Redis
```

Each service owns its PostgreSQL database. Redis is shared infrastructure used only as a cache. Candidate/AI additionally uses MinIO for file storage.

---

## 4. Frontend and API Documentation

### Frontend stack

- React
- TypeScript
- Vite
- React Router
- TanStack Query
- React Hook Form
- Zod
- Recharts
- Calendar component

### Swagger/OpenAPI

Every backend service exposes its own OpenAPI specification and Swagger UI.

Swagger is used for:

- Endpoint documentation
- Request/response inspection
- API testing
- Contract visibility during development

Kafka events are tested separately with Kafka tooling and integration tests.

---

## 5. Service Communication

SmartRecruit uses two communication styles.

### 5.1 Synchronous communication — REST + OpenFeign

Use OpenFeign when a service needs an immediate response.

```text
Recruitment Service
       │
       │ OpenFeign / REST
       ▼
Identity Service
```

A Feign client must call another service's API. It must never access that service's database.

Feign requests that act on behalf of a user propagate the bearer token; the receiving service validates it rather than trusting the caller or gateway alone.

Synchronous calls should use appropriate resilience controls:

- Connection timeout
- Read timeout
- Bounded retry
- Circuit breaker
- Bulkhead where required

Resilience4j supplies these fault-tolerance controls for synchronous service calls. Retries should be limited to safe transient failures.

### 5.2 Asynchronous communication — Kafka

Use Kafka when downstream processing can happen independently.

```text
Recruitment Service
       │
       │ JobCreated
       │ ApplicationSubmitted
       ▼
   Apache Kafka
       │
       ├──────────────► Notification Service
       │
       └──────────────► Analytics Service
```

Kafka is the internal event backbone. Consumers process events through normal Kafka consumer mechanisms; Kafka is not treated as a database.

---

## 6. Notification Architecture

Notification delivery is event-driven.

```text
Recruitment Service
       │
       │ Domain Event
       ▼
   Apache Kafka
       │
       ▼
Notification Service
       │
       ├── Email
       ├── In-app notification
       └── Webhook
```

Internal service communication uses Kafka. Email providers and external webhook targets are outbound integrations handled by Notification Service.

Webhooks do not replace Kafka for internal communication.

---

## 7. Public Job Timeline

The public job timeline can be accessed without authentication.

```text
Anonymous User
      │
      ▼
GET public job timeline
      │
      ├── Search / filter
      └── View offer
             │
             ▼
           Apply
             │
             ▼
    Authentication required
             │
             ▼
       Signup / Login
```

Backend authorization remains authoritative.

Because public job data is expected to be read frequently, the public timeline and related search results are primary candidates for Redis caching.

---

## 8. Identity and User Model

The domain uses a base `User` model with specialized roles.

```text
                         User
                           │
             ┌─────────────┼─────────────┐
             ▼             ▼             ▼
         Candidate      Recruiter    PlatformAdmin
```

### Common User data

- UUID/GUID
- Name
- Email
- Password hash
- Common contact information
- Account timestamps
- Account state

Role-specific data remains in the corresponding specialized model.

- **Candidate:** profile data and CV references
- **Recruiter:** employment-proof and approval information
- **PlatformAdmin:** administration-specific properties

---

## 9. Authentication and Authorization

Candidate registration activates the account.

Recruiter registration creates a pending account until administrator approval.

Spring Security provides stateless authentication with JWT bearer tokens. The Identity Service issues tokens carrying stable identity and role information; protected services validate the token and do not rely on an HTTP session.

Passwords are stored as BCrypt hashes, never as plaintext.

Authorization follows a layered model:

```text
JWT Authentication
        │
        ▼
Role Check
        │
        ▼
Resource / Company Ownership
        │
        ▼
Subscription Entitlement
        │
        ▼
Operation
```

Authentication and authorization state remain authoritative in Identity Service and PostgreSQL. Redis is not used as the authoritative security store.

### API security controls

- Spring Cloud Gateway is the public API entry point. It validates JWTs on protected routes and forwards the bearer token to backend services for their own validation and authorization.
- Public API traffic uses HTTPS/TLS at the deployment edge; internal service traffic must use the environment's approved transport protections.
- Every service validates request data server-side. Frontend validation improves usability but is not a security boundary.
- Spring Security protects authenticated endpoints. RBAC restricts operations by role, with resource and company ownership checks for object-level access.
- Registration and login are public; other routes are protected unless explicitly documented as public.

| Control          | Responsibility                                                                                                                 |
| ---------------- | ------------------------------------------------------------------------------------------------------------------------------ |
| Authentication   | Spring Security and stateless JWT bearer-token validation                                                                      |
| Authorization    | RBAC, resource/company ownership checks, and subscription entitlements                                                         |
| Password storage | BCrypt password hashing                                                                                                        |
| API perimeter    | API Gateway, HTTPS/TLS, server-side input validation, and endpoint protection                                                  |
| Service calls    | OpenFeign with bearer-token propagation where acting for a user, downstream token validation, and Resilience4j fault tolerance |

---

## 10. Candidate and AI Architecture

```text
Candidate
    │
    ▼
Candidate/AI Service
    │
    ├──► PostgreSQL
    │      Metadata
    │
    ├──► MinIO
    │      CV / document files
    │
    ├──► Parser
    │
    └──► External AI Provider
```

### Storage responsibilities

PostgreSQL stores:

- File metadata
- Object keys
- Versions
- Processing status
- Analysis references

MinIO stores the actual binary files.

Files include:

- Candidate CVs
- CV versions
- Recruiter employment-proof documents

Private files must be accessed through authorized backend operations. MinIO should not be exposed directly to anonymous users.

AI recommendation results may later be cached in Redis when repeated calculations justify caching. Persisted candidate data and AI analysis remain authoritative in PostgreSQL.

---

## 11. Subscription Architecture

### Plans

Candidate plans:

- Basic
- Professional
- Career Pro

Recruiter/company plans:

- Starter
- Growth
- Enterprise

New candidates receive the Basic plan automatically. Approved companies receive the configured Starter trial.

### Subscription ownership

Subscription Service owns:

- Plan state
- Subscription state
- Trial state
- Entitlements
- Renewal information
- Cancellation state
- Billing history

### Redis usage

Subscription plans are suitable for caching because they are small, frequently read, and changed less frequently than transactional subscription data.

```text
GET /subscription/plans
        │
        ▼
      Redis
      /   \
   HIT    MISS
    │       │
    │       ▼
    │   PostgreSQL
    │       │
    │       ▼
    │     Redis
    │       │
    └───┬───┘
        ▼
     Response
```

Example cache keys:

```text
subscription:plans
subscription:plan:{planId}
```

Redis must not be the source of truth for:

- Active subscription status
- Renewal dates
- Cancellation state
- Billing history
- Invoice history
- Payment records

---

## 12. Interview Architecture

Interview Service stores interview metadata and an external meeting URL. It does not implement video conferencing.

```text
Recruiter
    │
    │ Schedule interview + meeting URL
    ▼
Interview Service
    │
    ▼
PostgreSQL
    │
    └──► Notification Service
              │
              ▼
        Candidate receives link
```

Supported meeting providers can include:

- Zoom
- Google Meet
- Other approved providers

The service validates the URL format before storing it.

A candidate delay request does not modify the original interview until the recruiter approves it.

Redis is not initially required because interview scheduling, cancellation, and delay requests require authoritative transactional state.

---

## 13. Analytics and CQRS

Analytics is the primary CQRS implementation.

Analytics Service does not query other services' transactional databases. It consumes Kafka events and maintains its own read model.

```text
Business Services
       │
       │ Domain Events
       ▼
   Apache Kafka
       │
       ▼
Analytics Service
       │
       ▼
Analytics Read Model
       │
       ▼
   PostgreSQL
       │
       ▼
      Redis
       │
       ▼
Analytics Dashboard
```

The read model is optimized for:

- Recruiter dashboards
- Application statistics
- Job statistics
- Recruitment metrics
- Historical analytics
- PDF reports

Transactional services optimize for correct business writes, while Analytics optimizes for complex and frequent reads.

### CQRS scope

CQRS is not applied to every service.

| Service      | CQRS      |
| ------------ | --------- |
| Identity     | No        |
| Recruitment  | Optional  |
| Candidate/AI | Selective |
| Subscription | No        |
| Interview    | No        |
| Notification | No        |
| Analytics    | Yes       |

Recruitment may introduce a dedicated read model later if public search becomes sufficiently complex or high-volume.

---

## 14. Redis Caching Strategy

Redis is a cache, not a second database.

```text
PostgreSQL = Source of Truth
Redis      = Cache
```

### Recommended usage

| Data                         | Cache?       | Reason                           |
| ---------------------------- | ------------ | -------------------------------- |
| Public job timeline          | Yes          | High read volume                 |
| Job search results           | Yes          | Repeated / expensive queries     |
| Job filters / reference data | Yes          | Frequently reused                |
| Subscription plans           | Yes          | Frequently read                  |
| Analytics dashboard          | Yes          | Fast dashboard reads             |
| AI recommendations           | Optional     | Expensive repeated calculations  |
| User credentials             | No           | Authoritative security data      |
| User roles                   | No initially | Authorization data               |
| Application status           | No           | Transactional state              |
| Interview state              | No           | Strong consistency               |
| Billing history              | No           | Transactional data               |
| Notification state           | Optional     | PostgreSQL remains authoritative |

---

## 15. Recruitment Service and Cache-Aside

Recruitment Service is the primary Redis consumer.

```text
GET /jobs
    │
    ▼
Recruitment Service
    │
    ▼
  Redis
  /   \
HIT   MISS
 │      │
 │      ▼
 │  PostgreSQL
 │      │
 │      ▼
 │    Redis
 │      │
 └──┬───┘
    ▼
 Response
```

Possible cache keys:

```text
job:{jobId}
jobs:published:{page}
jobs:search:{queryHash}
jobs:filter:{filterHash}
reference:locations
reference:contract-types
```

Initial TTLs should be tuned through performance testing.

| Data                | Initial TTL  |
| ------------------- | ------------ |
| Individual job      | 5–15 minutes |
| Published job pages | 1–5 minutes  |
| Search results      | 1–5 minutes  |
| Filter results      | 1–5 minutes  |
| Reference data      | Longer TTL   |

---

## 16. Cache Invalidation

Authoritative updates are written to PostgreSQL first.

```text
Client
  │
  ▼
Service
  │
  ▼
PostgreSQL
  │
  ├──► Invalidate related Redis entries
  │
  └──► Publish domain event
             │
             ▼
           Kafka
```

Cache invalidation is performed after a successful database update.

Operations that require invalidation include:

- Job creation
- Job update
- Job publication
- Job closure
- Job deletion

The database remains authoritative if cache invalidation fails. A later cache miss can retrieve current data from PostgreSQL.

---

## 17. Redis vs Kafka

Redis and Kafka solve different problems.

| Technology | Responsibility                               |
| ---------- | -------------------------------------------- |
| PostgreSQL | Authoritative business data                  |
| Redis      | Low-latency cache and read acceleration      |
| Kafka      | Asynchronous communication and domain events |

Kafka handles:

- Event propagation
- Service decoupling
- Notification processing
- Analytics processing

Redis handles:

- Repeated read acceleration
- Database load reduction
- Temporary cached results
- Read-model acceleration

They are not interchangeable.

---

## 18. Data Consistency and Cache-Aside

### Write path

```text
Client
  │
  ▼
Service
  │
  ▼
PostgreSQL
  │
  ▼
Cache Invalidation
```

### Read path

```text
Client
  │
  ▼
Service
  │
  ▼
Redis
 ┌┴─────────────┐
HIT            MISS
 │               │
 ▼               ▼
Response      PostgreSQL
                  │
                  ▼
                Redis
                  │
                  ▼
               Response
```

All cache entries require TTLs to prevent indefinite staleness. Event-driven invalidation can additionally be used for high-value cached data.

---

## 19. Resilience

Resilience is required for synchronous service calls and external integrations where failures are possible.

| Pattern              | Purpose                                         |
| -------------------- | ----------------------------------------------- |
| Timeout              | Prevent indefinite synchronous calls            |
| Bounded retry        | Handle transient failures                       |
| Circuit breaker      | Stop repeated calls to an unhealthy dependency  |
| Bulkhead             | Isolate expensive or external processing        |
| Kafka retry / DLQ    | Handle failed event processing                  |
| Idempotent consumers | Tolerate duplicate event delivery               |
| Transactional outbox | Coordinate database changes and outgoing events |

Retries must be limited to transient failures and safe/idempotent operations. Resource-creating `POST` operations must not be blindly retried unless idempotency is guaranteed.

Example:

```text
Service A
    │
    ▼
  Feign
    │
    ▼
Circuit Breaker
    │
    ▼
Timeout
    │
    ▼
Bounded Retry + Backoff
    │
    ▼
Service B
```

---

## 20. Transactional Outbox

Services that update their database and publish Kafka events should use the transactional outbox pattern when reliable event publication is required.

```text
                 Service
                    │
          ┌─────────┴─────────┐
          ▼                   ▼
   Business Data          Outbox Record
     PostgreSQL             PostgreSQL
          │                   │
          └─────────┬─────────┘
                    │
                    ▼
             Outbox Publisher
                    │
                    ▼
                 Kafka
```

The pattern prevents a successful database transaction from being committed while its corresponding Kafka event is lost.

---

## 21. Data Ownership

Every service owns its own PostgreSQL database.

| Service      | Database                    |
| ------------ | --------------------------- |
| Identity     | `smartrecruit_identity`     |
| Recruitment  | `smartrecruit_recruitment`  |
| Candidate/AI | `smartrecruit_candidate`    |
| Subscription | `smartrecruit_subscription` |
| Interview    | `smartrecruit_interview`    |
| Notification | `smartrecruit_notification` |
| Analytics    | `smartrecruit_analytics`    |

### Ownership rules

- Services never access another service's database directly.
- Cross-service references are logical IDs, not foreign keys across databases.
- All primary keys use UUID/GUID identifiers.
- Cross-service communication uses APIs, Kafka events, or the API Gateway where appropriate.

---

## 22. UUID Identifier Strategy

All primary keys use UUID/GUID values.

```sql
id UUID PRIMARY KEY
```

The project does not use:

- `SERIAL`
- `BIGSERIAL`
- `AUTO_INCREMENT`
- `IDENTITY`

UUIDs provide globally unique identifiers suitable for distributed services.

---

## 23. File Storage

MinIO provides S3-compatible object storage for private documents.

```text
Candidate/AI Service
       │
       ├──────────► PostgreSQL
       │             File metadata
       │
       └──────────► MinIO
                     File binary
```

MinIO stores:

- Candidate CVs
- CV versions
- Recruiter employment-proof documents

PostgreSQL stores the corresponding metadata and object references.

Private files must be authorized by the backend and MinIO should not be exposed directly to anonymous users.

---

## 24. Deployment Architecture

SmartRecruit supports Docker Compose for development and Kubernetes for production.

### Development

Docker Compose runs shared development infrastructure locally. Application services can be run separately:

```text
Docker Compose infrastructure
├── API Gateway
├── Identity Service
├── Recruitment Service
├── Candidate/AI Service
├── Subscription Service
├── Interview Service
├── Notification Service
├── Analytics Service
├── PostgreSQL databases
├── Redis
├── Apache Kafka
└── MinIO
```

### API gateway

The `api-gateway` Spring Cloud Gateway application is the public API entry point. It preserves request paths and routes by `/api/v1` path families to the corresponding backend. Identity registration and login paths are public; other paths require a valid JWT signed with the same `JWT_SECRET` and issuer as Identity Service. The bearer token is forwarded to the backend, which remains responsible for its own authorization.

| Path family                                                           | Kubernetes Service     |
| --------------------------------------------------------------------- | ---------------------- |
| `/api/v1/auth/**`, `/api/v1/users/**`                                 | `identity-service`     |
| `/api/v1/jobs/**`, `/api/v1/applications/**`, `/api/v1/businesses/**` | `recruitment-service`  |
| `/api/v1/candidates/**`, `/api/v1/cvs/**`, `/api/v1/ai/**`            | `candidate-ai-service` |
| `/api/v1/subscriptions/**`, `/api/v1/plans/**`                        | `subscription-service` |
| `/api/v1/interviews/**`                                               | `interview-service`    |
| `/api/v1/notifications/**`                                            | `notification-service` |
| `/api/v1/analytics/**`                                                | `analytics-service`    |

### Production

Apply `kubernetes/api-gateway.yaml` for the gateway Deployment/LoadBalancer Service and stable ClusterIP Services for backend workloads. Build and publish the gateway image from the repository root:

```text
mvn -pl services/api-gateway -am package
docker build -t smartrecruit/api-gateway:latest -f services/api-gateway/Dockerfile .
```

Push the image to a registry accessible by the cluster, or update the manifest to the published image.

Backend Deployments are managed separately and must expose the matching `app` label and container port declared by their Kubernetes Service. The identity service uses port `8082`; the other services use `8080`. Create the `smartrecruit-secrets` Kubernetes Secret with key `identity-jwt-secret` before starting the gateway.

Gateway routes use Kubernetes Service DNS names directly (for example, `identity-service:8082`). Kubernetes DNS and ClusterIP Services provide discovery and in-cluster load balancing; no Eureka server, Kubernetes API credentials, or Spring Cloud Kubernetes discovery client is required.

Kubernetes provides the deployment platform:

```text
Kubernetes Cluster
├── API Gateway
├── Microservices
├── PostgreSQL
├── Kafka
├── Redis
└── MinIO
```

Kubernetes provides:

- Service discovery
- Load balancing
- Horizontal scaling
- Self-healing
- Rolling deployments
- Configuration management
- Secret management

No Eureka server is required because Kubernetes provides service discovery. The gateway manifest exposes the API with a `LoadBalancer` Service; use the cluster's ingress or load-balancer integration as appropriate for the target environment.

Redis should be deployed and configured for the required persistence and availability level of the target environment.

---

## 25. Overall Communication Model

```text
                         ┌──────────────────┐
                         │  React Frontend  │
                         └────────┬─────────┘
                                  │
                                  ▼
                         ┌──────────────────┐
                         │   API Gateway    │
                         └────────┬─────────┘
                                  │
                                  ▼
                         ┌──────────────────┐
                         │   Microservices  │
                         └───────┬───┬──────┘
                                 │   │
                   ┌─────────────┘   └─────────────┐
                   ▼                               ▼
            OpenFeign / REST                   Apache Kafka
            Synchronous                        Asynchronous
                   │                               │
                   ▼                               ▼
          Immediate response                 Domain events
                                                   │
                                      ┌────────────┴────────────┐
                                      ▼                         ▼
                                Notification                Analytics

                         Read-heavy operations
                                  │
                                  ▼
                                Redis
                                  │
                            Cache-aside
                                  │
                                  ▼
                              PostgreSQL
```

The architecture combines:

- Microservices
- Spring Boot
- React/TypeScript
- Spring Cloud Gateway
- Kubernetes-native service discovery
- OpenFeign
- Apache Kafka
- PostgreSQL
- Redis
- MinIO
- JWT
- Resilience4j
- Docker
- Kubernetes
- Swagger/OpenAPI
- CQRS for Analytics
- Transactional Outbox
- Cache-aside caching
- Event-driven communication

---

## 26. Architecture Constraints

The following rules are architectural constraints:

- No direct cross-service database access.
- No frontend-only authorization.
- No public access to private CV files.
- No direct React-to-AI-provider integration.
- No secret credentials committed to Git.
- No auto-increment integer primary keys.
- No internal video-conferencing implementation; interviews store external meeting links.
- OpenFeign/REST is used for required synchronous service communication.
- Kafka is used for asynchronous domain events and event-driven notification/analytics processing.
- PostgreSQL remains the source of truth for transactional business data.
- Redis is only a cache/read-performance layer.
- Redis must not replace PostgreSQL as the authoritative data store.
- Redis should be introduced selectively rather than automatically added to every service.
- Cached data must use TTLs and/or explicit invalidation.
- Application status, interview state, and billing history remain authoritative in PostgreSQL.
- Analytics may use a dedicated CQRS read model because its read requirements differ from transactional workloads.
- Cache failures must not make business data unavailable; services must be able to fall back to PostgreSQL.
