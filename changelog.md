# Changelog

## [Unreleased]

### Identity Service
- Separated current-user profile management from the authentication controller.
- Added a dedicated profile controller for authenticated user profile reads and updates.
- Added administrator user management endpoints for listing, fetching, creating, updating, and deleting users.
- Restricted admin user operations to platform administrators via method-level security.
- Added recruiter review endpoints for pending-account approval and rejection.
- Added employment-proof retrieval for recruiter verification.
- Kept recruiter approval status synchronized with the account status lifecycle.

### Local Development Configuration
- Made Docker Compose service ports configurable through concise `*_PORT` variables in `.env`.
- Updated `.env.example` and setup documentation with the port settings and conflict-resolution guidance.

### Notes
- Candidate and recruiter registration remain public.
- Recruiter accounts require employment proof during creation.
- Approval decisions are only allowed for pending recruiters.
- Role changes are intentionally not exposed through the CRUD API to preserve subtype integrity.
