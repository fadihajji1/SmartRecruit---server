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

Copy `.env.example` to `.env` in the project root. The example includes the default credentials and all configurable port settings.

```powershell
Copy-Item .env.example .env
```

When a port is already in use, change its corresponding setting in `.env`. For the gateway and identity service, `API_GATEWAY_PORT` and `IDENTITY_SERVICE_PORT` set both the application port and its host mapping. Other application `*_SERVICE_PORT` settings configure service destinations inside the Docker network.

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

2. Copy the generated secret to `JWT_SECRET` in `.env`.

Important: Use a randomly generated JWT secret. Never commit `.env` or share production credentials.

---

## 5. Build and Start the Project

Docker Compose starts : Configured databases, Redis, Kafka, Kafka UI, MinIO, pgAdmin, Identity Service, and API Gateway.

Open PowerShell in the project root.

```
# step 0  check the docker compose file is valid
docker compose config --quiet
# Step 1 — Build the application images from dockerfile
docker compose build
# Step 2 — Start the services
docker compose up -d
# Step 2 — Start the services without build
docker compose up -d --no-build
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

These are the default addresses. To resolve a port conflict, change the corresponding port value in `.env` and restart the stack with `docker compose up -d`. Keep application service ports aligned with each service's actual listening port.

### pgAdmin Server Configuration

When logging into pgAdmin at [http://localhost:5050](http://localhost:5050) (Email: `admin@admin.com`, Password: `admin` or value from `.env`):

> **Note:** Because pgAdmin runs inside the Docker network, use the internal service hostname (e.g. `identity-db`) and port `5432`, **not** `localhost` or mapped host ports.

Register each service database under **Servers > Register > Server...**:

| Service | General: **Name** | Connection: **Host** | Connection: **Port** | Connection: **Maintenance DB** | Connection: **Username** | Connection: **Password** |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Identity** | `Identity DB` | `identity-db` | `5432` | `identity_db` | `smartrecruit` | `postgres` |
| **Recruitment** | `Recruitment DB` | `recruitment-db` | `5432` | `recruitment_db` | `smartrecruit` | `postgres` |
| **Candidate / AI** | `Candidate DB` | `candidate-db` | `5432` | `candidate_db` | `smartrecruit` | `postgres` |
| **Subscription** | `Subscription DB` | `subscription-db` | `5432` | `subscription_db` | `smartrecruit` | `postgres` |
| **Interview** | `Interview DB` | `interview-db` | `5432` | `interview_db` | `smartrecruit` | `postgres` |
| **Notification** | `Notification DB` | `notification-db` | `5432` | `notification_db` | `smartrecruit` | `postgres` |
| **Analytics** | `Analytics DB` | `analytics-db` | `5432` | `analytics_db` | `smartrecruit` | `postgres` |

*(Use the `POSTGRES_PASSWORD` from your `.env` file if customized).*

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

### Default Platform Administrator Account

The Identity Service automatically provisions a default platform administrator on startup:

- **Email:** `admin@admin.com` (or the value of `ADMIN_EMAIL` in `.env`)
- **Password:** `admin` (or the value of `ADMIN_PASSWORD` in `.env`)
- **Role:** `PLATFORM_ADMINISTRATOR`
- **Status:** `ACTIVE`

You can use these credentials to log in via `POST /api/v1/auth/login` to obtain an administrator JWT.

---

## 9. Troubleshooting

### Port already in use

Check the affected port:

```powershell
netstat -ano | findstr :8080
```

If a port is occupied, stop the conflicting application or change its corresponding port value in `.env`, then restart the stack with `docker compose up -d`.

---

## 10. Stop the Project

Stop the containers without deleting local data:

```powershell
docker compose down
```

Start them again later with:

```powershell
docker compose up -d
```

## 11. Reset the Development Environment

**Warning: This permanently deletes the Docker volumes managed by this Compose project**, including local database records, Redis data, Kafka data, MinIO files, and pgAdmin state.

Use this only when a clean development environment is required:

```powershell
docker compose down --volumes --remove-orphans
docker compose down -v
docker compose up --build -d
```

## 12. Build and Cache Maintenance

### Clear Maven build output

From your project root, in PowerShell:

```powershell
.\mvnw.cmd clean
```

If you don't have the Maven Wrapper, use:

```powershell
mvn clean
```

This removes generated build output such as `target/`; it does not delete your source code or PostgreSQL data.

or

skip tests

```
mvn clean package -DskipTests
```

### Clear Docker build cache

To remove unused Docker build cache:

```powershell
docker builder prune
```

To remove all unused build cache:

```powershell
docker builder prune --all
```

This does not delete your database volumes.

### Rebuild SmartRecruit application images

After changing Java code or the Dockerfile:

```powershell
docker compose build --no-cache identity-service api-gateway
docker compose up -d --force-recreate identity-service api-gateway
```

This rebuilds those two services without using cached build layers.

### Clear Redis cache

To clear all cached Redis data:

```powershell
docker exec -it smartrecruit-redis redis-cli --no-auth-warning -a "YOUR_REDIS_PASSWORD" FLUSHDB
```

Replace `YOUR_REDIS_PASSWORD` with the value from your `.env` file. This deletes all keys in the selected Redis database, so use it only if that database contains disposable cache data.
