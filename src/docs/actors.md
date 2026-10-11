# SmartRecruit Actors and Access Model

## 1. Actors

SmartRecruit exposes exactly **four application actors** in the use-case model:

| Actor | Description | Main responsibilities |
|---|---|---|
| **Anonymous User** | Unauthenticated visitor | Browse the public job timeline, search/filter offers, view public offer details, start the application flow |
| **Candidate** | Authenticated job seeker | Manage profile/CVs, apply to jobs, receive AI recommendations/ATS analysis, manage subscriptions, interviews and notifications |
| **Recruiter** | Authenticated company-side user approved by the platform | Manage authorized company information, job offers, applications, candidate hiring status, interviews, analytics and recruiter subscription |
| **Platform Administrator** | Platform management user | Manage users, recruiter approval, businesses, subscription plans/company subscriptions and platform administration |

External AI, MinIO, Kafka, email providers, webhook destinations and payment implementations are **system integrations/components, not actors** in the business use-case diagram.

## 2. Anonymous User

- View the public LinkedIn-style job timeline.
- Search and filter job offers.
- View public job-offer details.
- Start an application.
- When applying, authentication is required and the user is redirected to sign up/sign in.

## 3. Candidate

### Account and profile
- Register and sign in.
- Candidate accounts are automatically activated after registration.
- View and update permitted profile information.

### CV and AI
- Upload CVs.
- Delete CVs.
- Manage CV versions according to subscription limits.
- Request ATS analysis.
- Receive AI-suggested jobs and CV-related recommendations.

CV files are stored in MinIO/S3-compatible object storage; PostgreSQL stores metadata and analysis references.

### Jobs and applications
- Browse/search offers without authentication.
- Apply after authentication.
- View personal applications and statuses.

### Subscription
- View plans.
- Subscribe, upgrade, downgrade, renew and cancel according to plan rules.
- View billing/subscription history.

### Interviews
- View personal interviews and calendar.
- Open the external meeting link supplied by the recruiter.
- Cancel an interview where permitted.
- Request a delay with a proposed date/time.

### Notifications
- View notifications.
- Mark notifications read/unread.
- Delete own notifications.

## 4. Recruiter

### Registration and approval
- Register with required recruiter information.
- Upload employment proof during registration.
- Remain pending until Platform Administrator approval.
- Sign in only after approval.

### Business and recruitment
- Manage only authorized company information.
- Create, update, close, reopen and delete job offers according to authorization/subscription rules.
- View and manage applications belonging to authorized company offers.
- Update candidate application status.

### Interviews
- Schedule, reschedule and cancel interviews.
- Enter an external meeting URL, such as a Zoom or Google Meet link, for the candidate.
- Mark interviews as done.
- Accept/reject candidate delay requests.

The Interview Service does **not** host the video session. It stores and exposes the external meeting link.

### Dashboard and subscription
- View recruitment analytics and export PDF reports.
- Manage the company's subscription within the permitted lifecycle.

## 5. Platform Administrator

- View/manage users.
- Approve or reject recruiter registrations after reviewing employment proof.
- Manage businesses and business status.
- Manage subscription plans and company subscriptions.
- View subscription history/status.
- Perform platform-level administration.

## 6. Authorization principles

Authorization is enforced by backend services, not only by the frontend:

```text
Authentication
      |
      v
Role authorization
      |
      v
Resource/company ownership
      |
      v
Subscription entitlement
      |
      v
Operation
```

Examples:
- A recruiter cannot access another company's applications.
- A candidate cannot access another candidate's CV or interviews.
- A pending/rejected recruiter cannot sign in.
- Anonymous users cannot submit an authenticated application.
- Users can modify only their own notifications.
