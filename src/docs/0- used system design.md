## what system design applied in this SmartRecruit project?

Yes. For the **SmartRecruit system I documented**, the design is primarily a **microservices-based distributed system**, with several supporting architectural patterns.

### **Main system-design patterns applied**


| **System design**                       | **How it is applied in SmartRecruit**                                                                                                        |
| --------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------- |
| **Microservices Architecture**          | 9 independent business services: Auth/User, Business, Job, Application, CV/AI, Subscription, Interview, Notification, Analytics              |
| **Database-per-Service**                | Each service owns its PostgreSQL database/schema and data. Services do not directly access another service's DB.                             |
| **API Gateway**                         | Spring Cloud Gateway provides a single entry point for the React frontend and routes requests to backend services.                           |
| **Kubernetes-Native Service Discovery** | Kubernetes Services/DNS provide service discovery instead of Eureka.                                                                         |
| **Event-Driven Architecture**           | Apache Kafka is used for asynchronous communication and domain events between services.                                                      |
| **Synchronous REST Communication**      | REST APIs are used when an immediate response is required.                                                                                   |
| **Transactional Outbox Pattern**        | Recommended for reliably publishing database changes as Kafka events without losing events.                                                  |
| **Idempotent Consumers**                | Kafka consumers should safely process the same event more than once.                                                                         |
| **Saga / Process Manager**              | Used conceptually for multi-service business workflows such as subscriptions, applications, interviews, etc.                                 |
| **CQRS / Read Models**                  | Particularly useful for the Analytics service, where reporting data can be built from events rather than querying other services' databases. |
| **Circuit Breaker + Retry**             | Selective protection for critical synchronous service-to-service calls.                                                                      |
| **Resource/Tenant Authorization**       | Services verify that a recruiter actually owns or is authorized to access a business, job, application, etc.                                 |
| **Object Storage**                      | CVs and employment-proof documents are stored as files in S3-compatible object storage; PostgreSQL stores metadata.                          |
| **Caching**                             | use Redis as performance layer for selective caching layer for read-heavy and frequently accessed data.                                      |
| **External AI Adapter**                 | The CV/AI service isolates the external AI provider from the rest of the system.                                                             |
| **Mock Payment Adapter**                | Payment integration is abstracted so the project can start with a mock provider and later replace it.                                        |
| **Containerization**                    | Each service is packaged as a Docker image.                                                                                                  |
| **Orchestration**                       | Kubernetes is the production deployment target.                                                                                              |
| **Observability**                       | Spring Boot Actuator, Micrometer, metrics, structured logs and distributed tracing foundations are included.                                 |


### **Overall architecture**

The important distinction is that **Microservices Architecture is the main system design**, while the others support it:

```
                         React + TypeScript
                                │
                                ▼
                       ┌─────────────────┐
                       │   API Gateway   │
                       └────────┬────────┘
                                │
       ┌────────────────────────┼────────────────────────┐
       │            │            │            │            │
       ▼            ▼            ▼            ▼            ▼
    Auth/User    Business       Job      Application    CV/AI
       │            │            │            │            │
       └────────────┴────────────┴────────────┴────────────┘
                                │
                         Apache Kafka
                                │
       ┌────────────────────────┼───────────────────────┐
       ▼                        ▼                       ▼
 Subscription              Interview             Notification
       │                        │                       │
       └────────────────────────┼───────────────────────┘
                                ▼
                           Analytics
```

And underneath:

```
Each service
     │
     ├── REST API
     ├── Business logic
     ├── Own PostgreSQL DB
     └── Kafka producer/consumer
```

For files:

```
CV/AI Service
     │
     ├──────────────► PostgreSQL
     │                metadata
     │
     └──────────────► S3-compatible Object Storage
                      actual CV / documents
```

### **The most important design principle**

The architecture follows:

> **Each microservice owns its business capability and its data.**

For example, the Application Service **does not directly query the Job Service's PostgreSQL database**.

Instead:

```
Application Service
       │
       │ REST / Kafka
       ▼
   Job Service
       │
       ▼
 Job Service DB
```

 