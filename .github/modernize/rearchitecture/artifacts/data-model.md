# Existing Data Model

## Identity Service

`User` is an abstract JPA entity mapped to `users` with `JOINED` inheritance and discriminator `user_type`. Fields are UUID id, email, password hash, full name, phone, address, city, state, country, postal code, enum role, enum account status, and created/updated timestamps. Email is unique.

Concrete entities are `Candidate` -> `candidate_profiles`, `Recruiter` -> `recruiter_profiles`, and `PlatformAdministrator` -> `administrator_profiles`. Recruiter-specific persisted fields are employment-proof object key, approval status, rejection reason, and approved-at timestamp. Child primary keys are also foreign keys to `users.id` with cascade delete.

Identity transactions cover candidate registration, recruiter registration (including storage plus database cleanup on persistence failure), profile reads, and profile updates. Login issues a token and is not annotated transactional.

## Notification Service

`Notification` maps to `notifications` with UUID id, recipient email, optional recipient role, title, message, enum type, read flag, created-at, and nullable read-at. Indexes support recipient lookup and recipient/read filtering. Notification service methods use read-only transactions for reads and write transactions for create, read/unread transitions, and deletion.

## Planned/documented-only entities

The documentation describes Recruitment (businesses, memberships, jobs, applications), Candidate/AI (CVs, analyses, recommendations), Subscription (plans, features, subscriptions, history, invoices), Interview (interviews and delay requests), and Analytics read models. No corresponding source entities or migrations are present in this snapshot.
