# SmartRecruit — What was wrong and what was fixed

## TL;DR
The errors you were seeing in the browser console were **not code bugs** — the code
and the admin logic are in good shape. Every `/admin/*` endpoint the Angular frontend
calls is already implemented in the matching Spring Boot service. The errors came from
**two environmental problems**:

1. **Stale processes** were holding ports 8080, 8091, 8092, 8094 when `start-backend.bat`
   was run again, so the new `api-gateway`, `candidate-ai-service`, `subscription-service`
   and `notification-service` JVMs crashed with "Port … was already in use".
2. **Postgres rejected `identity-service`** with
   `FATAL: authentification par mot de passe échouée pour l'utilisateur « smartrecruit »`.
   The `identity-db` Docker volume was initialized at some point with a different
   `POSTGRES_PASSWORD` than the one now in `.env`. Postgres only applies
   `POSTGRES_PASSWORD` on **first boot** — after that the password is fixed in the data
   directory and keeps mismatching `.env`.

With `api-gateway` dead, every `/admin/*` call fell through to the stale instance (so
the admin pages returned `404`), and with `identity-service` + `notification-service` down
the frontend saw `500`s on `/notifications/unread-count` and `/admin/users`.

## What was done

### 1. `fix-and-restart.bat` (new, in this folder)
One-shot repair script. Run it instead of `start-backend.bat`:

```
fix-and-restart.bat
```

It performs, in order:

1. **Kills stale Java processes** on 8080, 8082, 8090, 8091, 8092, 8093, 8094, 8095.
2. Brings up the Docker **infrastructure** (`docker compose up -d`).
3. For each Postgres container (`smartrecruit-identity-postgres`, etc.) runs
   `ALTER USER smartrecruit WITH PASSWORD '${POSTGRES_PASSWORD}'`
   so the role password is **aligned with the current `.env`** without losing data.
4. Starts all 8 services, logging to `logs\<service>.log`.
5. Health-checks each service and the gateway, up to 90 seconds.

### 2. `RecruitmentController.java` (rewritten, readable)
`services/recruitment-service/src/main/java/com/smartrecruit/recruitment/api/RecruitmentController.java`
was previously written as one line per endpoint — hard to read, no section breaks.
It has been reformatted into clearly separated groups:
- Public job catalogue (`/jobs`)
- Recruiter — businesses
- Recruiter — jobs
- Applications
- Platform administrator (businesses, jobs, stats)

**Behavior is unchanged** — same routes, same security, same return types — only
formatting and Swagger annotations were added.

## Admin flows coverage (verified)

| Flow | Backend endpoint | Frontend page |
| --- | --- | --- |
| List all users | `GET /api/v1/admin/users` | `AdminComponent` → Users tab |
| Change user status (ban / activate) | `PATCH /api/v1/admin/users/{id}` | Users tab status dropdown |
| Pending recruiter queue | derived from users list | Recruiters tab |
| Approve recruiter hiring request | `POST /api/v1/admin/recruiters/{id}/approve` | Approve button |
| Reject recruiter hiring request | `POST /api/v1/admin/recruiters/{id}/reject` | Reject button |
| View employment proof | `GET /api/v1/admin/recruiters/{id}/employment-proof` | View proof button |
| List businesses | `GET /api/v1/admin/businesses` | Businesses tab |
| Create business (assign to a recruiter) | `POST /api/v1/admin/businesses` | Create business form |
| Update business | `PATCH /api/v1/admin/businesses/{id}` | Reactivate action |
| Deactivate business | `DELETE /api/v1/admin/businesses/{id}` | Deactivate action |
| List all jobs | `GET /api/v1/admin/jobs` | Jobs tab |
| Close a published job | `POST /api/v1/admin/jobs/{id}/close` | Close offer button |
| Platform stats | `GET /api/v1/admin/recruitment/stats` | Overview KPIs |
| Subscription plans CRUD | `GET/POST/PATCH /api/v1/admin/subscription/plans*` | Subscriptions page |
| Company subscriptions oversight | `GET/PATCH /api/v1/admin/subscription/company-subscriptions*` | Subscriptions page |

## What to run

```
cd "<...>\smart recruter\SmartRecruit---server-master"
fix-and-restart.bat

# in another terminal:
cd "<...>\smart recruter\SmartRecruit-Stitch-Angular"
npm start
```

Then open http://localhost:4200.
Log in with the admin from `.env`: `admin@gmail.com` / `admin123`.

## If something still fails

Open the matching log file under `logs\` and look for the first `Caused by:` line.
Common causes and fixes:

| Symptom in log | Fix |
| --- | --- |
| `Port … was already in use` | Rerun `fix-and-restart.bat` — it kills stale PIDs. |
| `password authentication failed for user "smartrecruit"` | Rerun `fix-and-restart.bat` — step 3 re-aligns the password. |
| `Connection refused` to `localhost:9093` (Kafka) | Wait longer, or `docker compose restart kafka`. |
| `Connection refused` to `localhost:9002` (MinIO) | `docker compose restart minio`. |
