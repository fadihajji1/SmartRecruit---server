# SmartRecruit — Local Setup and Testing Guide

## 1. Project Overview

SmartRecruit is a recruitment platform built with Spring Boot microservices.

## 2. Prerequisites

Install the following software before running the project.


| Software       | Requirement           | Download                                                                                               |
| -------------- | --------------------- | ------------------------------------------------------------------------------------------------------ |
| Git            | Latest stable version | [https://git-scm.com/downloads](https://git-scm.com/downloads)                                         |
| Java JDK       | 21                    | [https://adoptium.net/temurin/releases/?version=21](https://adoptium.net/temurin/releases/?version=21) |
| Docker Desktop | Latest stable version | [https://www.docker.com/products/docker-desktop/](https://www.docker.com/products/docker-desktop/)     |
| IDE            | Optional              | [https://code.visualstudio.com/](https://code.visualstudio.com/)                                       |


Important: Docker Desktop must be running before starting the project.

## 3. Obtain the Project

#### Clone the Git repository

```powershell
git clone <REPOSITORY_URL>
cd <REPOSITORY_FOLDER>
```

## 4. Configure Environment Variables

- Create `.env` file in the project root
- copy `.env.example` content into`.env`

```dotenv
# .env.exemple content
# PostgreSQL
POSTGRES_PASSWORD=postgres

# Redis
REDIS_PASSWORD=redis

# JWT: shared by Service and API Gateway
JWT_SECRET=replace_with_a_generated_random_secret
JWT_ISSUER=smartrecruit

# Local platform administrator bootstrap
ADMIN_PASSWORD=admin

# MinIO
MINIO_ROOT_USER=smartrecruitMinio
MINIO_ROOT_PASSWORD=SmartRecruitLocalMinio_2026
MINIO_BUCKET=smartrecruit-private

# pgAdmin: local development only
PGADMIN_DEFAULT_EMAIL=admin@admin.com
PGADMIN_DEFAULT_PASSWORD=admin
```

**or**

```powershell
# in powershell
Copy-Item .env.example .env
```

### generate jwt secret

1. in powershell:

```
$bytes = New-Object byte[] 32
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
$secret = [Convert]::ToBase64String($bytes)
$rng.Dispose()
$secret
```

2. Copy generated secret to **jwt_secret** in `.env`

Important: Use a randomly generated JWT secret. Never commit `.env` or share production credentials.

---

## 5. Build and Start the Project

Docker Compose starts : Configured databases, Redis, Kafka, Kafka UI, MinIO, pgAdmin, Identity Service, and API Gateway.

Open PowerShell in the project root.

```
# Step 1 — Build the application images
docker compose build
# Step 2 — Start the services
docker compose up -d
# Step 3 — Check container status
docker compose ps
```

## 6. Access the Local Services

When the containers are running, use these addresses.


| Component                   | Address                                                                        |
| --------------------------- | ------------------------------------------------------------------------------ |
| API Gateway                 | [http://localhost:8080](http://localhost:8080)                                 |
| Identity Service            | [http://localhost:8082](http://localhost:8082)                                 |
| Identity Service Swagger UI | [http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html) |
| Kafka UI                    | [http://localhost:8081](http://localhost:8081)                                 |
| MinIO Console               | [http://localhost:9003](http://localhost:9003)                                 |
| pgAdmin                     | [http://localhost:5050](http://localhost:5050)                                 |
| Identity PostgreSQL         | `localhost:5433`                                                               |
| Recruitment PostgreSQL      | `localhost:5434`                                                               |
| Candidate/AI PostgreSQL     | `localhost:5435`                                                               |
| Subscription PostgreSQL     | `localhost:5436`                                                               |
| Interview PostgreSQL        | `localhost:5437`                                                               |
| Notification PostgreSQL     | `localhost:5438`                                                               |
| Analytics PostgreSQL        | `localhost:5439`                                                               |
| Redis                       | `localhost:6380`                                                               |
| Kafka                       | `localhost:9093`                                                               |
| MinIO S3 API                | [http://localhost:9002](http://localhost:9002)                                 |
| Swagger-ui                  | [http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html) |


The application endpoints may differ from these infrastructure addresses if the configuration has been changed.

## 7. Initialize MinIO Storage

Open the MinIO Console:

[http://localhost:9003](http://localhost:9003)

Sign in using the values configured in `.env`.

Create the private bucket configured by `MINIO_BUCKET`, normally:

```text
smartrecruit-private
```

If the application requires additional buckets, create them according to the current Identity Service storage configuration.

---

## 8. Test the Identity Service

### Open Swagger UI

[http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html)

Review the endpoints exposed by the Identity Service.

---

## 10. Troubleshooting

### Port already in use

Check the affected port:

```powershell
netstat -ano | findstr :8080
```

If a port is occupied, stop the conflicting application or update the corresponding port mapping in `docker-compose.yml`.

---

## 11. Stop the Project

Stop the containers without deleting local data:

```powershell
docker compose down
```

Start them again later with:

```powershell
docker compose up -d
```

## 12. Reset the Development Environment

**Warning: This permanently deletes the Docker volumes managed by this Compose project**, including local database records, Redis data, Kafka data, MinIO files, and pgAdmin state.

Use this only when a clean development environment is required:

```powershell
docker compose down --volumes --remove-orphans
docker compose down -v
docker compose up --build -d
```

 