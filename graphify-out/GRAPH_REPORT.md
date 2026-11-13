# Graph Report - SmartRecruit---server-master  (2026-11-13)

## Corpus Check
- 207 files · ~90,690 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 22 file(s) not represented in the graph (top: .properties 8, (none) 5, .bat 5)

## Summary
- 2338 nodes · 5284 edges · 141 communities (106 shown, 35 thin omitted)
- Extraction: 94% EXTRACTED · 6% INFERRED · 0% AMBIGUOUS · INFERRED: 331 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `3e3a632f`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- org.springframework.security.oauth2.jwt.Jwt
- org.springframework.http.ResponseEntity
- Notification
- SubscriptionPlan.java
- CandidateAiService
- recruitment/config/ApiSecurityConfiguration.java
- SmartRecruit — System Architecture
- AnalyticsEvent
- uuid
- spring
- Interview
- analytics/config/KafkaConfiguration.java
- Subscription
- org.junit.jupiter.api.Test
- CvAnalysis
- SmartRecruit Database Architecture
- SubscriptionPlan
- SecurityConfiguration.java
- SmartRecruit Database Architecture
- User
- containers
- environment
- SmartRecruit API Endpoints
- org.springframework.boot.autoconfigure.SpringBootApplication
- SmartRecruit API Endpoints
- services
- SmartRecruit — Local Setup and Testing Guide
- SmartRecruit Implementation Phases
- JobProjection
- org.springframework.context.annotation.Bean
- environment
- SmartRecruit Implementation Phases
- org.springframework.transaction.annotation.Transactional
- seed-demo.mjs
- SmartRecruit Contribution Guide
- org.springframework.data.domain.Page
- api-gateway/src/main/resources/application.yml
- SmartRecruit Contribution Guide
- IdentityService.java
- analytics-service
- candidate-ai-service
- SmartRecruit Platform Requirements
- 3. Candidate
- org.springframework.context.annotation.Configuration
- SubscriptionAudience
- SmartRecruit Platform Requirements
- 3. Candidate
- args
- environment
- SmartRecruit — System Architecture
- UserResponse
- Invoice
- interval
- Platform Administrator
- AdminUserService
- Platform Administrator
- POSTGRES_PASSWORD
- Recruiter Features
- AccountStatus
- Job
- OutboxMessage
- Recruiter Features
- Platform Administrator
- SmartRecruit Production and Development Architecture
- analytics-service.yaml
- api-gateway-service.yaml
- candidate-ai-service.yaml
- identity-service.yaml
- interview-service.yaml
- notification-service.yaml
- recruitment-service.yaml
- subscription-service.yaml
- SubscriptionHistory
- Platform Administrator
- SmartRecruit Production and Development Architecture
- volumes
- Recruiter Features
- 4. Important business gates
- Recruiter Features
- 4. Important business gates
- Candidate Features
- RecruitmentService.java
- Candidate Features
- GatewaySecurityConfiguration.java
- JobRepository
- IdentityService
- kafka-ui
- pgadmin
- redis
- SmartRecruit — What was wrong and what was fixed
- AdminUserService.java
- subscription-service
- Platform Administrator
- Recruiter Subscription Features
- Candidate Subscription Features
- PaymentGateway
- .publicJobs
- Platform Administrator
- Recruiter Subscription Features
- Candidate Subscription Features
- HeuristicResumeAnalysisProvider
- Status
- analytics-db
- candidate-db
- identity-db
- notification-db
- Candidate CV Management
- Status
- Candidate CV Management
- what system design applied in this SmartRecruit project?
- what system design applied in this SmartRecruit project?
- subscription plan:
- 11. Subscription Architecture
- 24. Deployment Architecture
- Getting Started
- subscription plan:
- 18. Data Consistency and Cache-Aside
- 4. Frontend and API Documentation
- 5. Service Communication
- Status
- 10. Candidate and AI Architecture
- 13. Analytics and CQRS
- 14. Redis Caching Strategy
- 21. Data Ownership
- 2. Business Services
- 8. Identity and User Model
- 9. Authentication and Authorization
- The architecture uses:
- com.smartrecruit:analytics-service
- com.smartrecruit:api-gateway
- com.smartrecruit:candidate-ai-service
- com.smartrecruit:identity-service
- com.smartrecruit:interview-service
- com.smartrecruit:notification-service
- com.smartrecruit:recruitment-service
- com.smartrecruit:smartrecruit-parent
- com.smartrecruit:subscription-service

## God Nodes (most connected - your core abstractions)
1. `User` - 48 edges
2. `RecruitmentService` - 44 edges
3. `Subscription` - 43 edges
4. `SubscriptionPlan` - 43 edges
5. `Job` - 42 edges
6. `RecruitmentController` - 40 edges
7. `CandidateAiService` - 36 edges
8. `SubscriptionAudience` - 36 edges
9. `SubscriptionService` - 33 edges
10. `SubscriptionController` - 30 edges

## Surprising Connections (you probably didn't know these)
- `AnalyticsController` --references--> `AnalyticsService`  [EXTRACTED]
  services/analytics-service/src/main/java/com/smartrecruit/analytics/api/AnalyticsController.java → services/analytics-service/src/main/java/com/smartrecruit/analytics/service/AnalyticsService.java
- `CvController` --references--> `CandidateAiService`  [EXTRACTED]
  services/candidate-ai-service/src/main/java/com/smartrecruit/candidateai/api/CvController.java → services/candidate-ai-service/src/main/java/com/smartrecruit/candidateai/service/CandidateAiService.java
- `CvAnalysisRepository` --references--> `CvAnalysis`  [EXTRACTED]
  services/candidate-ai-service/src/main/java/com/smartrecruit/candidateai/repository/CvAnalysisRepository.java → services/candidate-ai-service/src/main/java/com/smartrecruit/candidateai/domain/CvAnalysis.java
- `JobProjectionRepository` --references--> `JobProjection`  [EXTRACTED]
  services/candidate-ai-service/src/main/java/com/smartrecruit/candidateai/repository/JobProjectionRepository.java → services/candidate-ai-service/src/main/java/com/smartrecruit/candidateai/domain/JobProjection.java
- `JobEventConsumer` --references--> `CandidateAiService`  [EXTRACTED]
  services/candidate-ai-service/src/main/java/com/smartrecruit/candidateai/events/JobEventConsumer.java → services/candidate-ai-service/src/main/java/com/smartrecruit/candidateai/service/CandidateAiService.java

## Import Cycles
- None detected.

## Communities (141 total, 35 thin omitted)

### Community 0 - "org.springframework.security.oauth2.jwt.Jwt"
Cohesion: 0.06
Nodes (21): AnalyticsController, AnalyticsOverviewResponse, CvController, CvAnalysisRequest, RecommendationResponse, AuthController, CandidateRegistrationRequest, LoginRequest (+13 more)

### Community 1 - "org.springframework.http.ResponseEntity"
Cohesion: 0.11
Nodes (16): ApiExceptionHandler, ErrorBody, ApiExceptionHandler, ErrorBody, ApiError, ApiExceptionHandler, EmailAlreadyRegisteredException, UserNotFoundException (+8 more)

### Community 2 - "Notification"
Cohesion: 0.05
Nodes (8): StorageException, MinioEmploymentProofStorage, NotificationController, Notification, NotificationResponse, DomainEventConsumer, NotificationRepository, NotificationService

### Community 3 - "SubscriptionPlan.java"
Cohesion: 0.10
Nodes (3): Candidate, PlatformAdministrator, Recruiter

### Community 4 - "CandidateAiService"
Cohesion: 0.08
Nodes (9): CandidateCv, CvStatus, ACTIVE, DELETED, CandidateCvResponse, CandidateCvRepository, CandidateAiService, DownloadedCv (+1 more)

### Community 5 - "recruitment/config/ApiSecurityConfiguration.java"
Cohesion: 0.15
Nodes (7): ApiSecurityConfiguration, ApiSecurityConfiguration, OpenApiConfiguration, ApiSecurityConfiguration, ApiSecurityConfiguration, ApiSecurityConfiguration, ApiSecurityConfiguration

### Community 6 - "SmartRecruit — System Architecture"
Cohesion: 0.04
Nodes (45): 10. Candidate and AI Architecture, 11. Subscription Architecture, 12. Interview Architecture, 13. Analytics and CQRS, 14. Redis Caching Strategy, 15. Recruitment Service and Cache-Aside, 16. Cache Invalidation, 17. Redis vs Kafka (+37 more)

### Community 7 - "AnalyticsEvent"
Cohesion: 0.09
Nodes (5): AnalyticsEvent, AnalyticsEventConsumer, AnalyticsEventRepository, AnalyticsService, PdfReportWriter

### Community 8 - "uuid"
Cohesion: 0.11
Nodes (4): ApplicationMetricResponse, OfferMetricResponse, CvAnalysisRepository, JobProjectionRepository

### Community 9 - "spring"
Cohesion: 0.05
Nodes (37): email, password, reset-password, name, password, url, username, enabled (+29 more)

### Community 10 - "Interview"
Cohesion: 0.10
Nodes (8): InterviewController, InterviewRequest, Interview, Status, CANCELLED, COMPLETED, SCHEDULED, InterviewRepository

### Community 11 - "analytics/config/KafkaConfiguration.java"
Cohesion: 0.14
Nodes (3): InterviewEvents, OutboxPublisher, OutboxRepository

### Community 13 - "org.junit.jupiter.api.Test"
Cohesion: 0.18
Nodes (8): ApiDocumentationTests, ApiGatewayApplicationTests, ApiDocumentationTests, IdentityServiceApplicationTests, ApiDocumentationTests, ApiDocumentationTests, ApiDocumentationTests, ApiDocumentationTests

### Community 14 - "CvAnalysis"
Cohesion: 0.09
Nodes (8): AnalysisStatus, COMPLETED, FAILED, CvAnalysis, CvAnalysisResponse, TypeReference<>, ResumeAnalysis, ResumeAnalysisProvider

### Community 15 - "SmartRecruit Database Architecture"
Cohesion: 0.06
Nodes (32): 10. Cross-service relationships, 11. File storage, 12. Migrations, 13. Consistency and events, 14. pgAdmin development tool connections, 1. Database strategy, 2. Logical databases, 3. Identity Service: inheritance model (+24 more)

### Community 16 - "SubscriptionPlan"
Cohesion: 0.13
Nodes (4): SubscriptionPlan, PlanRequest, PlanResponse, SubscriptionPlanRepository

### Community 17 - "SecurityConfiguration.java"
Cohesion: 0.10
Nodes (3): IdentitySecurityProperties, SecurityConfiguration, TokenService

### Community 18 - "SmartRecruit Database Architecture"
Cohesion: 0.07
Nodes (29): 10. Cross-service relationships, 11. File storage, 12. Migrations, 13. Consistency and events, 1. Database strategy, 2. Logical databases, 3. Identity Service: inheritance model, 4. Recruitment Service (+21 more)

### Community 20 - "containers"
Cohesion: 0.07
Nodes (28): apiVersion, env, image, imagePullPolicy, livenessProbe, name, ports, readinessProbe (+20 more)

### Community 21 - "environment"
Cohesion: 0.10
Nodes (28): environment, environment, environment, ANALYTICS_DB_URL, ANALYTICS_DB_USERNAME, ANALYTICS_SERVICE_URL, CANDIDATE_AI_DB_URL, CANDIDATE_AI_DB_USERNAME (+20 more)

### Community 22 - "SmartRecruit API Endpoints"
Cohesion: 0.07
Nodes (27): 11. Domain event envelope, 12. Feign and resilience, 12. Operational endpoints, 13. Error contract, 1. Conventions, 2. Swagger documentation, 3. Identity Service, 4. Recruitment Service (+19 more)

### Community 23 - "org.springframework.boot.autoconfigure.SpringBootApplication"
Cohesion: 0.12
Nodes (8): AnalyticsServiceApplication, ApiGatewayApplication, CandidateAiServiceApplication, IdentityServiceApplication, InterviewServiceApplication, NotificationServiceApplication, RecruitmentServiceApplication, SubscriptionServiceApplication

### Community 24 - "SmartRecruit API Endpoints"
Cohesion: 0.07
Nodes (26): 10. Operational endpoints, 11. Error contract, 12. Feign and resilience, 1. Conventions, 2. Swagger documentation, 3. Identity Service, 4. Recruitment Service, 5. Candidate/AI Service (+18 more)

### Community 26 - "services"
Cohesion: 0.08
Nodes (24): container_name, image, ports, restart, volumes, name, container_name, image (+16 more)

### Community 27 - "SmartRecruit — Local Setup and Testing Guide"
Cohesion: 0.08
Nodes (24): 10. Stop the Project, 11. Reset the Development Environment, 12. Build and Cache Maintenance, 1. Project Overview, 2. Prerequisites, 3. Obtain the Project, 4. Configure Environment Variables, 5. Build and Start the Project (+16 more)

### Community 28 - "SmartRecruit Implementation Phases"
Cohesion: 0.08
Nodes (23): 1. Implementation strategy, Administrator flow, Backend, Candidate flow, Current implementation status, Docker, Infrastructure, Kubernetes (+15 more)

### Community 30 - "org.springframework.context.annotation.Bean"
Cohesion: 0.18
Nodes (3): KafkaConfiguration, KafkaConfiguration, KafkaConfiguration

### Community 31 - "environment"
Cohesion: 0.09
Nodes (23): CLUSTER_ID, KAFKA_ADVERTISED_LISTENERS, KAFKA_AUTO_CREATE_TOPICS_ENABLE, KAFKA_CONTROLLER_LISTENER_NAMES, KAFKA_CONTROLLER_QUORUM_VOTERS, KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS, KAFKA_INTER_BROKER_LISTENER_NAME, KAFKA_LISTENER_SECURITY_PROTOCOL_MAP (+15 more)

### Community 32 - "SmartRecruit Implementation Phases"
Cohesion: 0.09
Nodes (22): 1. Implementation strategy, Administrator flow, Backend, Candidate flow, Docker, Infrastructure, Kubernetes, Observability (+14 more)

### Community 33 - "org.springframework.transaction.annotation.Transactional"
Cohesion: 0.20
Nodes (3): Business, BusinessRepository, RecruitmentService

### Community 34 - "seed-demo.mjs"
Cohesion: 0.20
Nodes (19): api(), ApiError, APPLICATIONS, args, attempt(), CANDIDATES, email(), ensureCandidate() (+11 more)

### Community 35 - "SmartRecruit Contribution Guide"
Cohesion: 0.10
Nodes (19): 10. Security rules, 11. Testing, 12. Pull requests, 13. Definition of Done, 1. Repository organization, 2. Ownership principle, 3. API and Swagger rules, 4. Service communication (+11 more)

### Community 36 - "org.springframework.data.domain.Page"
Cohesion: 0.24
Nodes (3): InvoiceRepository, SubscriptionHistoryRepository, SubscriptionRepository

### Community 37 - "api-gateway/src/main/resources/application.yml"
Cohesion: 0.10
Nodes (19): name, gateway, server, cors-configurations, connect-timeout, response-timeout, identity, jwt (+11 more)

### Community 38 - "SmartRecruit Contribution Guide"
Cohesion: 0.10
Nodes (19): 10. Security rules, 11. Testing, 12. Pull requests, 13. Definition of Done, 1. Repository organization, 2. Ownership principle, 3. API and Swagger rules, 4. Service communication (+11 more)

### Community 40 - "analytics-service"
Cohesion: 0.11
Nodes (19): condition, container_name, depends_on, ports, profiles, restart, analytics-db, kafka (+11 more)

### Community 41 - "candidate-ai-service"
Cohesion: 0.11
Nodes (18): container_name, depends_on, ports, profiles, restart, condition, candidate-db, identity-db (+10 more)

### Community 42 - "SmartRecruit Platform Requirements"
Cohesion: 0.11
Nodes (17): Close Offer, Delete Application, Export Dashboard, F01. User Authentication, F02. User Profile Management, F06. Dashboard Management, F08. Applications Management, Manage Profile (+9 more)

### Community 43 - "3. Candidate"
Cohesion: 0.11
Nodes (17): 1. Actors, 2. Anonymous User, 3. Candidate, 4. Recruiter, 5. Platform Administrator, 6. Authorization principles, Account and profile, Business and recruitment (+9 more)

### Community 44 - "org.springframework.context.annotation.Configuration"
Cohesion: 0.16
Nodes (4): MinioConfiguration, MinioConfiguration, MinioProperties, KafkaProducerConfiguration

### Community 45 - "SubscriptionAudience"
Cohesion: 0.13
Nodes (11): SubscriptionAudience, CANDIDATE, RECRUITER, SubscriptionStatus, ACTIVE, CANCELLED, EXPIRED, PAST_DUE (+3 more)

### Community 46 - "SmartRecruit Platform Requirements"
Cohesion: 0.11
Nodes (17): Close Offer, Delete Application, Export Dashboard, F01. User Authentication, F02. User Profile Management, F06. Dashboard Management, F08. Applications Management, Manage Profile (+9 more)

### Community 47 - "3. Candidate"
Cohesion: 0.11
Nodes (17): 1. Actors, 2. Anonymous User, 3. Candidate, 4. Recruiter, 5. Platform Administrator, 6. Authorization principles, Account and profile, Business and recruitment (+9 more)

### Community 48 - "args"
Cohesion: 0.19
Nodes (17): build, build, container_name, depends_on, ports, profiles, restart, APP_JAR (+9 more)

### Community 49 - "environment"
Cohesion: 0.12
Nodes (17): ADMIN_EMAIL, ADMIN_PASSWORD, IDENTITY_DB_URL, IDENTITY_DB_USERNAME, IDENTITY_SERVICE_PORT, MINIO_BUCKET, MINIO_ROOT_PASSWORD, MINIO_ROOT_USER (+9 more)

### Community 50 - "SmartRecruit — System Architecture"
Cohesion: 0.12
Nodes (16): 12. Interview Architecture, 15. Recruitment Service and Cache-Aside, 16. Cache Invalidation, 17. Redis vs Kafka, 19. Resilience, 1. Architecture Overview, 20. Transactional Outbox, 22. UUID Identifier Strategy (+8 more)

### Community 51 - "UserResponse"
Cohesion: 0.23
Nodes (3): AdminUserController, AdminUpdateUserRequest, UserResponse

### Community 52 - "Invoice"
Cohesion: 0.18
Nodes (5): Invoice, InvoiceStatus, FAILED, PAID, VOID

### Community 53 - "interval"
Cohesion: 0.47
Nodes (15): healthcheck, healthcheck, interval, retries, start_period, test, timeout, healthcheck (+7 more)

### Community 54 - "Platform Administrator"
Cohesion: 0.13
Nodes (15): Activate/Deactivate Business, Activate/Deactivate Business, Add Business, Add Business, Delete Business, Delete Business, F04. Business Management, Platform Administrator (+7 more)

### Community 56 - "Platform Administrator"
Cohesion: 0.13
Nodes (15): Activate/Deactivate Business, Activate/Deactivate Business, Add Business, Add Business, Delete Business, Delete Business, F04. Business Management, Platform Administrator (+7 more)

### Community 57 - "POSTGRES_PASSWORD"
Cohesion: 0.29
Nodes (14): environment, environment, POSTGRES_DB, POSTGRES_PASSWORD, POSTGRES_USER, environment, environment, environment (+6 more)

### Community 58 - "Recruiter Features"
Cohesion: 0.14
Nodes (14): Accept/Reject Delay Request, Cancel Interview, Cancel Interview, Candidate Features, F11. Interview Management, Mark Interview as Done, Recruiter Features, Request Interview Delay (+6 more)

### Community 59 - "AccountStatus"
Cohesion: 0.19
Nodes (6): AccountStatus, ACTIVE, INACTIVE, PENDING, REJECTED, AccountNotActiveException

### Community 62 - "Recruiter Features"
Cohesion: 0.14
Nodes (14): Accept/Reject Delay Request, Cancel Interview, Cancel Interview, Candidate Features, F11. Interview Management, Mark Interview as Done, Recruiter Features, Request Interview Delay (+6 more)

### Community 63 - "Platform Administrator"
Cohesion: 0.15
Nodes (13): Activate/Deactivate User, Add User, Candidate Management for Recruiters, Delete User, F05. User Management, Platform Administrator, Update Candidate Application Status, Update User (+5 more)

### Community 64 - "SmartRecruit Production and Development Architecture"
Cohesion: 0.15
Nodes (12): 10. Scaling, 11. Database identifiers, 1. Purpose, 2. Development model, 3. Production topology, 4. Kubernetes, 5. File storage, 6. Kafka (+4 more)

### Community 65 - "analytics-service.yaml"
Cohesion: 0.15
Nodes (12): apiVersion, kind, metadata, name, name, port, targetPort, app (+4 more)

### Community 66 - "api-gateway-service.yaml"
Cohesion: 0.15
Nodes (12): apiVersion, kind, metadata, name, name, port, targetPort, app (+4 more)

### Community 67 - "candidate-ai-service.yaml"
Cohesion: 0.15
Nodes (12): apiVersion, kind, metadata, name, name, port, targetPort, app (+4 more)

### Community 68 - "identity-service.yaml"
Cohesion: 0.15
Nodes (12): apiVersion, kind, metadata, name, name, port, targetPort, app (+4 more)

### Community 69 - "interview-service.yaml"
Cohesion: 0.15
Nodes (12): apiVersion, kind, metadata, name, name, port, targetPort, app (+4 more)

### Community 70 - "notification-service.yaml"
Cohesion: 0.15
Nodes (12): apiVersion, kind, metadata, name, name, port, targetPort, app (+4 more)

### Community 71 - "recruitment-service.yaml"
Cohesion: 0.15
Nodes (12): apiVersion, kind, metadata, name, name, port, targetPort, app (+4 more)

### Community 72 - "subscription-service.yaml"
Cohesion: 0.15
Nodes (12): apiVersion, kind, metadata, name, name, port, targetPort, app (+4 more)

### Community 74 - "Platform Administrator"
Cohesion: 0.15
Nodes (13): Activate/Deactivate User, Add User, Candidate Management for Recruiters, Delete User, F05. User Management, Platform Administrator, Update Candidate Application Status, Update User (+5 more)

### Community 75 - "SmartRecruit Production and Development Architecture"
Cohesion: 0.15
Nodes (12): 10. Scaling, 11. Database identifiers, 1. Purpose, 2. Development model, 3. Production topology, 4. Kubernetes, 5. File storage, 6. Kafka (+4 more)

### Community 76 - "volumes"
Cohesion: 0.17
Nodes (12): volumes, analytics_postgres_data, candidate_postgres_data, identity_postgres_data, interview_postgres_data, kafka_data, minio_data, notification_postgres_data (+4 more)

### Community 77 - "Recruiter Features"
Cohesion: 0.17
Nodes (12): Apply for Job, Candidate Features, Create Offer, Delete Offer, F09. Job Offer Management, Recruiter Features, Update Offer, Upload CV for AI Suggestions (+4 more)

### Community 78 - "4. Important business gates"
Cohesion: 0.17
Nodes (11): 1. Roadmap objective, 2. Roadmap, 3. Cross-phase quality gates, 4. Important business gates, 5. Final target architecture, CV/AI, Identity, Interviews (+3 more)

### Community 79 - "Recruiter Features"
Cohesion: 0.17
Nodes (12): Apply for Job, Candidate Features, Create Offer, Delete Offer, F09. Job Offer Management, Recruiter Features, Update Offer, Upload CV for AI Suggestions (+4 more)

### Community 80 - "4. Important business gates"
Cohesion: 0.17
Nodes (11): 1. Roadmap objective, 2. Roadmap, 3. Cross-phase quality gates, 4. Important business gates, 5. Final target architecture, CV/AI, Identity, Interviews (+3 more)

### Community 81 - "Candidate Features"
Cohesion: 0.18
Nodes (11): Candidate Features, Delete Notification, Delete Notification, F15. Notification Management, Mark Notification as Read, Mark Notification as Read, Mark Notification as Unread, Mark Notification as Unread (+3 more)

### Community 83 - "Candidate Features"
Cohesion: 0.18
Nodes (11): Candidate Features, Delete Notification, Delete Notification, F15. Notification Management, Mark Notification as Read, Mark Notification as Read, Mark Notification as Unread, Mark Notification as Unread (+3 more)

### Community 88 - "kafka-ui"
Cohesion: 0.22
Nodes (9): DYNAMIC_CONFIG_ENABLED, KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS, KAFKA_CLUSTERS_0_NAME, container_name, environment, image, ports, restart (+1 more)

### Community 89 - "pgadmin"
Cohesion: 0.22
Nodes (9): PGADMIN_DEFAULT_EMAIL, PGADMIN_DEFAULT_PASSWORD, container_name, environment, image, ports, restart, volumes (+1 more)

### Community 90 - "redis"
Cohesion: 0.22
Nodes (9): REDIS_PASSWORD, command, container_name, environment, image, ports, restart, volumes (+1 more)

### Community 91 - "SmartRecruit — What was wrong and what was fixed"
Cohesion: 0.22
Nodes (8): 1. `fix-and-restart.bat` (new, in this folder), 2. `RecruitmentController.java` (rewritten, readable), Admin flows coverage (verified), If something still fails, SmartRecruit — What was wrong and what was fixed, TL;DR, What to run, What was done

### Community 92 - "AdminUserService.java"
Cohesion: 0.29
Nodes (4): UserRole, CANDIDATE, PLATFORM_ADMINISTRATOR, RECRUITER

### Community 93 - "subscription-service"
Cohesion: 0.25
Nodes (8): subscription-db, subscription-service, condition, container_name, depends_on, ports, profiles, restart

### Community 94 - "Platform Administrator"
Cohesion: 0.25
Nodes (8): Activate/Deactivate Subscription, Assign Plan to Company, Manage Subscription Plans, Platform Administrator, Renew Subscription, Suspend or Cancel Subscription, View Subscription Details, View Subscription Status and History

### Community 95 - "Recruiter Subscription Features"
Cohesion: 0.25
Nodes (8): Cancel Subscription, Recruiter Subscription Features, Renew Subscription, Subscribe to Plan, Upgrade or Downgrade Subscription, View Available Plans, View Billing History, View Current Subscription

### Community 96 - "Candidate Subscription Features"
Cohesion: 0.25
Nodes (8): Cancel Subscription, Candidate Subscription Features, F06. Subscription Management, Subscribe to Plan, Upgrade or Downgrade Subscription, View Billing History, View Current Subscription, View Plans

### Community 97 - "PaymentGateway"
Cohesion: 0.36
Nodes (3): MockPaymentGateway, PaymentGateway, PaymentResult

### Community 99 - "Platform Administrator"
Cohesion: 0.25
Nodes (8): Activate/Deactivate Subscription, Assign Plan to Company, Manage Subscription Plans, Platform Administrator, Renew Subscription, Suspend or Cancel Subscription, View Subscription Details, View Subscription Status and History

### Community 100 - "Recruiter Subscription Features"
Cohesion: 0.25
Nodes (8): Cancel Subscription, Recruiter Subscription Features, Renew Subscription, Subscribe to Plan, Upgrade or Downgrade Subscription, View Available Plans, View Billing History, View Current Subscription

### Community 101 - "Candidate Subscription Features"
Cohesion: 0.25
Nodes (8): Cancel Subscription, Candidate Subscription Features, F06. Subscription Management, Subscribe to Plan, Upgrade or Downgrade Subscription, View Billing History, View Current Subscription, View Plans

### Community 103 - "Status"
Cohesion: 0.29
Nodes (7): Status, HIRED, INTERVIEW, PENDING, REJECTED, REVIEWING, UNAVAILABLE

### Community 104 - "analytics-db"
Cohesion: 0.33
Nodes (6): container_name, image, ports, restart, volumes, analytics-db

### Community 105 - "candidate-db"
Cohesion: 0.33
Nodes (6): container_name, image, ports, restart, volumes, candidate-db

### Community 106 - "identity-db"
Cohesion: 0.33
Nodes (6): container_name, image, ports, restart, volumes, identity-db

### Community 107 - "notification-db"
Cohesion: 0.33
Nodes (6): container_name, image, ports, restart, volumes, notification-db

### Community 108 - "Candidate CV Management"
Cohesion: 0.33
Nodes (6): ATS Scan, Candidate CV Management, Delete CV, F03. Offer Search, Search Job Offers, Upload CV

### Community 109 - "Status"
Cohesion: 0.33
Nodes (6): Status, CLOSED, DELETED, DRAFT, EXPIRED, PUBLISHED

### Community 110 - "Candidate CV Management"
Cohesion: 0.33
Nodes (6): ATS Scan, Candidate CV Management, Delete CV, F03. Offer Search, Search Job Offers, Upload CV

### Community 111 - "what system design applied in this SmartRecruit project?"
Cohesion: 0.40
Nodes (4): **Main system-design patterns applied**, **Overall architecture**, **The most important design principle**, what system design applied in this SmartRecruit project?

### Community 112 - "what system design applied in this SmartRecruit project?"
Cohesion: 0.40
Nodes (4): **Main system-design patterns applied**, **Overall architecture**, **The most important design principle**, what system design applied in this SmartRecruit project?

### Community 113 - "subscription plan:"
Cohesion: 0.50
Nodes (4): Candidate Subscription Plans, Recruiter / Company Subscription Plans, Rules:, subscription plan:

### Community 114 - "11. Subscription Architecture"
Cohesion: 0.50
Nodes (4): 11. Subscription Architecture, Plans, Redis usage, Subscription ownership

### Community 115 - "24. Deployment Architecture"
Cohesion: 0.50
Nodes (4): 24. Deployment Architecture, API gateway, Development, Production

### Community 116 - "Getting Started"
Cohesion: 0.50
Nodes (3): Getting Started, Maven Parent overrides, Reference Documentation

### Community 117 - "subscription plan:"
Cohesion: 0.50
Nodes (4): Candidate Subscription Plans, Recruiter / Company Subscription Plans, Rules:, subscription plan:

### Community 118 - "18. Data Consistency and Cache-Aside"
Cohesion: 0.67
Nodes (3): 18. Data Consistency and Cache-Aside, Read path, Write path

### Community 119 - "4. Frontend and API Documentation"
Cohesion: 0.67
Nodes (3): 4. Frontend and API Documentation, Frontend stack, Swagger/OpenAPI

### Community 120 - "5. Service Communication"
Cohesion: 0.67
Nodes (3): 5.1 Synchronous communication — REST + OpenFeign, 5.2 Asynchronous communication — Kafka, 5. Service Communication

### Community 121 - "Status"
Cohesion: 0.67
Nodes (3): Status, ACTIVE, INACTIVE

## Knowledge Gaps
- **47 isolated node(s):** `com.smartrecruit:smartrecruit-parent`, `com.smartrecruit:analytics-service`, `com.smartrecruit:api-gateway`, `com.smartrecruit:candidate-ai-service`, `COMPLETED` (+42 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 975 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **35 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `CandidateAiService` connect `CandidateAiService` to `org.springframework.security.oauth2.jwt.Jwt`, `Notification`, `uuid`, `CvAnalysis`, `JobProjection`, `org.springframework.context.annotation.Bean`?**
  _High betweenness centrality (0.013) - this node is a cross-community bridge._
- **What connects `com.smartrecruit:smartrecruit-parent`, `com.smartrecruit:analytics-service`, `com.smartrecruit:api-gateway` to the rest of the system?**
  _47 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `org.springframework.security.oauth2.jwt.Jwt` be split into smaller, more focused modules?**
  _Cohesion score 0.0570876999448428 - nodes in this community are weakly interconnected._
- **Why does `Job` connect `Job` to `org.springframework.security.oauth2.jwt.Jwt`, `org.springframework.transaction.annotation.Transactional`, `.publicJobs`, `Status`, `uuid`, `Status`, `RecruitmentService.java`, `JobRepository`, `bigdecimal`, `OutboxMessage`?**
  _High betweenness centrality (0.010) - this node is a cross-community bridge._
- **Should `org.springframework.http.ResponseEntity` be split into smaller, more focused modules?**
  _Cohesion score 0.10556110556110557 - nodes in this community are weakly interconnected._
- **Why does `CvAnalysis` connect `CvAnalysis` to `uuid`, `SubscriptionPlan.java`, `CandidateAiService`?**
  _High betweenness centrality (0.009) - this node is a cross-community bridge._
- **Should `Notification` be split into smaller, more focused modules?**
  _Cohesion score 0.05213089802130898 - nodes in this community are weakly interconnected._