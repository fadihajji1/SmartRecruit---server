# SmartRecruit Database Architecture

## 1. Database strategy

SmartRecruit uses **PostgreSQL with database-per-service ownership**. Every service owns its data and exposes data only through its API/events. No service directly queries another service's database.

### Mandatory identifier rule

**Every database primary key is a GUID/UUID.** Auto-incremented `INT`, `BIGINT`, `SERIAL` and `BIGSERIAL` primary keys are not permitted.

Recommended PostgreSQL type:

```sql
id UUID PRIMARY KEY
```

UUIDs should be generated application-side or with PostgreSQL UUID support, consistently across all services.

## 2. Logical databases

| Service | PostgreSQL database | Main data |
|---|---|---|
| Identity | `smartrecruit_identity` | Users, candidate/recruiter/admin profiles, recruiter approval |
| Recruitment | `smartrecruit_recruitment` | Businesses, memberships, job offers, applications |
| Candidate/AI | `smartrecruit_candidate` | CV metadata, analysis, recommendations |
| Subscription | `smartrecruit_subscription` | Plans, subscriptions, history, invoices |
| Interview | `smartrecruit_interview` | Interviews and delay requests |
| Notification | `smartrecruit_notification` | Notifications and delivery state |
| Analytics | `smartrecruit_analytics` | Read models and reporting data |

## 3. Identity Service: inheritance model

The domain model uses a base `users` table plus role-specific tables. This implements the requirement that **Candidate, Recruiter and Platform Administrator inherit common User properties while adding their own properties**.

```text
users
  |
  +---- candidate_profiles
  |
  +---- recruiter_profiles
  |
  +---- admin_profiles
```

All tables use GUID/UUID primary keys. The child primary key is also a foreign key to `users.id`. This is a joined-inheritance style model.

### `users`

Suggested fields:

```text
id UUID PK
email VARCHAR UNIQUE NOT NULL
password_hash VARCHAR NOT NULL
first_name VARCHAR NOT NULL
last_name VARCHAR NOT NULL
phone VARCHAR
address VARCHAR
city VARCHAR
state VARCHAR
country VARCHAR
postal_code VARCHAR
role VARCHAR NOT NULL
status VARCHAR NOT NULL
created_at TIMESTAMP
updated_at TIMESTAMP
```

### `candidate_profiles`

```text
user_id UUID PK/FK -> users.id
profile-specific fields
```

Examples include candidate career/profile information and candidate-specific preferences. CV files are not stored in this database.

### `recruiter_profiles`

```text
user_id UUID PK/FK -> users.id
employment_proof_object_key VARCHAR
approval_status VARCHAR
approved_by UUID
approved_at TIMESTAMP
rejection_reason VARCHAR
```

The employment-proof binary is stored in MinIO; this table stores metadata/reference information.

### `admin_profiles`

```text
user_id UUID PK/FK -> users.id
admin-specific fields
```

Platform Administrator accounts are system/platform accounts but still inherit common User properties.

## 4. Recruitment Service

### `businesses`

```text
id UUID PK
name VARCHAR
description VARCHAR
address VARCHAR
status VARCHAR
created_at TIMESTAMP
updated_at TIMESTAMP
```

### `business_members`

```text
id UUID PK
business_id UUID
user_id UUID
role VARCHAR
created_at TIMESTAMP
```

`user_id` is a logical reference to Identity Service; it is not a cross-database foreign key.

### `job_offers`

```text
id UUID PK
business_id UUID
created_by_user_id UUID
title VARCHAR
description TEXT
location VARCHAR
contract_type VARCHAR
status VARCHAR
created_at TIMESTAMP
updated_at TIMESTAMP
```

### `applications`

```text
id UUID PK
job_id UUID
candidate_id UUID
candidate_cv_id UUID
status VARCHAR
created_at TIMESTAMP
updated_at TIMESTAMP
```

`job_id`, `candidate_id` and `candidate_cv_id` are logical GUID references owned by other domains where applicable. No cross-service DB foreign keys are created.

## 5. Candidate/AI Service

### `cvs`

```text
id UUID PK
candidate_id UUID
file_name VARCHAR
object_key VARCHAR UNIQUE
content_type VARCHAR
size_bytes BIGINT
version INTEGER
status VARCHAR
uploaded_at TIMESTAMP
deleted_at TIMESTAMP
```

The actual file is stored in **MinIO**. `object_key` identifies the object.

### `cv_analysis`

```text
id UUID PK
cv_id UUID
analysis_type VARCHAR
provider VARCHAR
model VARCHAR
status VARCHAR
score NUMERIC
result_reference TEXT
created_at TIMESTAMP
completed_at TIMESTAMP
```

### `recommendations`

```text
id UUID PK
candidate_id UUID
cv_id UUID
job_id UUID
score NUMERIC
reason TEXT
created_at TIMESTAMP
```

## 6. Subscription Service

All entities use UUID/GUID primary keys:

- `plans`
- `plan_features`
- `subscriptions`
- `subscription_history`
- `invoices`

Subscription history remains after cancellation.

## 7. Interview Service

### `interviews`

```text
id UUID PK
application_id UUID
candidate_id UUID
recruiter_id UUID
job_id UUID
scheduled_at TIMESTAMP
external_meeting_url TEXT NOT NULL
status VARCHAR
created_at TIMESTAMP
updated_at TIMESTAMP
```

The meeting URL is an external Zoom, Google Meet or similar URL supplied by the recruiter. SmartRecruit does not host the meeting.

### `delay_requests`

```text
id UUID PK
interview_id UUID
requested_by_user_id UUID
proposed_scheduled_at TIMESTAMP
status VARCHAR
created_at TIMESTAMP
resolved_at TIMESTAMP
```

The original interview remains unchanged until the recruiter approves the request.

## 8. Notification Service

### `notifications`

```text
id UUID PK
recipient_user_id UUID
type VARCHAR
title VARCHAR
content TEXT
source_event_id UUID NULL
read_at TIMESTAMP NULL
created_at TIMESTAMP
deleted_at TIMESTAMP NULL
```

The notification record is created from domain events. Delivery may then use in-app storage, email or webhook.

## 9. Analytics Service

Analytics tables/read models use UUID/GUID identifiers where an entity identifier is required. Analytics may denormalize data received through events rather than joining other service databases.

## 10. Cross-service relationships

Cross-service IDs are logical references:

```text
Recruitment.application.candidate_id -> Identity user GUID
Recruitment.application.job_id       -> Recruitment job GUID
Interview.application_id             -> Recruitment application GUID
CandidateCV.candidate_id             -> Identity user GUID
Notification.recipient_user_id       -> Identity user GUID
```

These are not foreign keys across databases.

## 11. File storage

```text
Candidate/AI Service
      |
      +--> PostgreSQL
      |      metadata/object key
      |
      +--> MinIO
             actual CV files
```

MinIO is the selected S3-compatible object-storage technology for this project.

## 12. Migrations

Use Flyway per service. Every migration must respect the UUID primary-key rule. Example:

```sql
CREATE TABLE example (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP NOT NULL
);
```

Migration ownership belongs to the service that owns the table.

## 13. Consistency and events

For reliable event publication, use a transactional outbox in services that both persist business state and publish Kafka events:

```text
Service DB transaction
       |
       +--> business data
       +--> outbox event
                |
                v
             Kafka
```

Consumers must be idempotent because events may be delivered more than once.

## 14. pgAdmin development tool connections

When using the local pgAdmin container (`http://localhost:5050`) to inspect databases, use Docker's internal container networking rather than host ports.

### Connection parameters

- **Default pgAdmin User:** `admin@admin.com`
- **Default pgAdmin Password:** `admin` (or `${PGADMIN_DEFAULT_PASSWORD}`)
- **PostgreSQL User:** `smartrecruit`
- **PostgreSQL Password:** `postgres` (or `${POSTGRES_PASSWORD}`)

### Server registration details

| Service | Registered Name | Host name/address | Port | Maintenance Database | Username |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Identity** | `Identity DB` | `identity-db` | `5432` | `identity_db` | `smartrecruit` |
| **Recruitment** | `Recruitment DB` | `recruitment-db` | `5432` | `recruitment_db` | `smartrecruit` |
| **Candidate / AI** | `Candidate DB` | `candidate-db` | `5432` | `candidate_db` | `smartrecruit` |
| **Subscription** | `Subscription DB` | `subscription-db` | `5432` | `subscription_db` | `smartrecruit` |
| **Interview** | `Interview DB` | `interview-db` | `5432` | `interview_db` | `smartrecruit` |
| **Notification** | `Notification DB` | `notification-db` | `5432` | `notification_db` | `smartrecruit` |
| **Analytics** | `Analytics DB` | `analytics-db` | `5432` | `analytics_db` | `smartrecruit` |
