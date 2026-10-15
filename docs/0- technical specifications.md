# SmartRecruit Platform Requirements

## F01. User Authentication

### Sign Up

User Story: As a user, I want to sign up so that I can create an account on SmartRecruit.

Acceptance Criteria:

- Required registration information is provided.
- Email address must be unique.
- Account is created successfully.
- Validation errors are displayed for invalid data.
- Recruiters must upload proof of employment during registration.
- Employment proof must be verified later by a Platform Administrator.

### Sign In

User Story: As a user, I want to sign in so that I can securely access my account.

Acceptance Criteria:

- Valid credentials are accepted.
- Invalid credentials are rejected.
- User is redirected to the appropriate dashboard.
- A secure authentication session/token is generated.
- Candidate accounts are automatically activated upon registration.
- Recruiter accounts require Administrator approval before being allowed to sign in.

---

## F02. User Profile Management

### Manage Profile

User Story: As a user, I want to modify my personal information.

Acceptance Criteria:

- Users can view editable profile information.
- Users can update permitted profile fields.
- Changes are validated before saving.
- Updated information is displayed immediately after saving.

---

## F03. Offer Search

### Search Job Offers

User Story: As a user, I want to search for job offers.

Acceptance Criteria:

- Authentication is not required to search job offers.
- Authentication is required before applying to a job offer.

### Candidate CV Management

#### Upload CV

User Story: As a candidate, I want to upload my CV.

#### Delete CV

User Story: As a candidate, I want to delete my CV.

#### ATS Scan

User Story: As a candidate, I want to scan my CV using AI-powered ATS analysis.

---

## F04. Business Management

### Platform Administrator

#### View Businesses List

User Story: As a platform administrator, I want to view the businesses list.

Acceptance Criteria:

- View all registered businesses.
- Create businesses.
- Update businesses.
- Deactivate businesses.
- Information is validated before persistence.

#### View Business Details

User Story: As a platform administrator, I want to view business details.

#### Add Business

User Story: As a platform administrator, I want to add a business.

#### Update Business

User Story: As a platform administrator, I want to update a business.

#### Activate/Deactivate Business

User Story: As a platform administrator, I want to activate or deactivate a business.

#### Delete Business

User Story: As a platform administrator, I want to delete a business.

### Recruiter

#### View Businesses List

User Story: As a recruiter, I want to view businesses list.

Acceptance Criteria:

- Authorized company information can be viewed.
- Authorized company information can be updated.
- Required fields are validated.
- Changes are persisted.
- Unauthorized businesses cannot be modified.

#### View Business Details

User Story: As a recruiter, I want to view business details.

#### Add Business

User Story: As a recruiter, I want to add a business.

#### Update Business

User Story: As a recruiter, I want to update a business.

#### Delete Business

User Story: As a recruiter, I want to delete a business.

#### Activate/Deactivate Business

User Story: As a recruiter, I want to activate or deactivate a business.

---

## F05. User Management

### Platform Administrator

#### View Users List

User Story: As a platform administrator, I want to view users list.

Acceptance Criteria:

- Only Platform Administrators have access to platform management.

#### View User Details

User Story: As a platform administrator, I want to view user details.

#### Add User

User Story: As a platform administrator, I want to add a user and assign a role.

#### Update User

User Story: As a platform administrator, I want to update a user.

#### Activate/Deactivate User

User Story: As a platform administrator, I want to activate or deactivate a user.

#### Delete User

User Story: As a platform administrator, I want to delete a user.

### Candidate Management for Recruiters

#### View Candidates by Application

User Story: As a recruiter, I want to view candidates who applied for offers grouped by application.

Acceptance Criteria:

- Recruiters can access only candidates linked to authorized applications.
- Recruiters can open or download candidate CVs.
- Candidate list displays: Full Name, Email, Applied Position, Company Name, Status (Pending, Hired, Rejected).
- Clicking a candidate opens a details modal/page with candidate information and uploaded CV.

#### View Candidate Details

User Story: As a recruiter, I want to view candidate details.

#### Update Candidate Application Status

User Story: As a recruiter, I want to set candidate status as Pending, Hired or Rejected.

#### View Candidate CV

User Story: As a recruiter, I want to view candidate CVs.

---

## F06. Dashboard Management

### Recruiter Dashboard

#### View Activity & Analytics

User Story: As a recruiter, I want to monitor overall platform and recruitment activity.

Acceptance Criteria:

- Access relevant business activity information.
- Monitor offers and applications.
- Dashboard data is restricted to authorized access.

#### Export Dashboard

User Story: As a recruiter, I want to export analytics and activity reports as PDF.

---

## F06. Subscription Management

### Platform Administrator

#### Manage Subscription Plans

User Story: As a platform administrator, I want to manage subscription plans and company subscriptions.

Acceptance Criteria:

- Create and update plans.
- Plans contain: Name, Description, Price, Billing Cycle (Monthly/Yearly), Included Features and Limits.
- List all plans with Name, Price, Billing Cycle and Status.
- View company subscriptions with Company, Plan, Status, Start Date and End Date.
- Plan names must be unique.
- Only administrators can create or manage subscription plans.
- Existing active subscriptions are not automatically modified when plans change.
- Assign plans to companies.
- Validate required assignment fields.
- Subscription history is preserved after cancellation.

#### View Subscription Details

User Story: As a platform administrator, I want to view subscription details.

#### Assign Plan to Company

User Story: As a platform administrator, I want to assign a subscription plan to a company so that the company can access the corresponding features.

#### Renew Subscription

User Story: As a platform administrator, I want to update or renew a company's subscription so that access remains continuous.

#### Activate/Deactivate Subscription

User Story: As a platform administrator, I want to activate or deactivate a subscription.

#### Suspend or Cancel Subscription

User Story: As a platform administrator, I want to suspend or cancel a subscription.

#### View Subscription Status and History

User Story: As a platform administrator, I want to view subscription status and history so that I can monitor active, expired and pending subscriptions.

### Recruiter Subscription Features

#### View Available Plans

User Story: As a recruiter, I want to view available subscription plans so that I can choose the one that fits my company's needs.

#### Subscribe to Plan

User Story: As a recruiter, I want to subscribe to a plan so that my company gets access to the corresponding features.

#### View Current Subscription

User Story: As a recruiter, I want to view my current subscription so that I know my plan, status and renewal date.

#### Upgrade or Downgrade Subscription

User Story: As a recruiter, I want to upgrade or downgrade my plan so that I can adapt to my company's changing needs.

#### Renew Subscription

User Story: As a recruiter, I want to renew my subscription so that my company's access is not interrupted.

#### Cancel Subscription

User Story: As a recruiter, I want to cancel my subscription so that I can stop being billed.

#### View Billing History

User Story: As a recruiter, I want to view my billing history so that I can track payments and invoices.  

Acceptance Criteria:

- View active plans.
- View plan pricing and included features.
- Subscribe to plans.
- View remaining days until renewal.
- Receive message if no subscription exists.
- Upgrade or downgrade plans.
- Upgrades apply immediately.
- Downgrades apply at next renewal.
- Cancellation preserves billing history.
- View invoices and billing history.

### Candidate Subscription Features

#### View Plans

User Story: As a candidate, I want to view available subscription plans so that I can choose the one that fits my job search.

Acceptance Criteria:

- View active candidate plans.
- View premium features.
- Current plan is clearly identified.
- Users without subscription are automatically assigned the Basic plan.
- Upgrade and downgrade behavior is clearly explained.
- Cancellation preserves billing history.

#### Subscribe to Plan

User Story: As a candidate, I want to subscribe to a plan so that I can unlock premium features.

#### View Current Subscription

User Story: As a candidate, I want to view my current subscription so that I know my plan, status and renewal date.

#### Upgrade or Downgrade Subscription

User Story: As a candidate, I want to upgrade or downgrade my plan so that I can adapt to my needs.

#### Cancel Subscription

User Story: As a candidate, I want to cancel my subscription so that I can stop being billed.

#### View Billing History

User Story: As a candidate, I want to view my billing history so that I can track payments and invoices.

---

## F08. Applications Management

### Recruiter Features

#### View Applications

User Story: As a recruiter, I want to view all applications submitted to offers belonging to my company.

Acceptance Criteria:

- View applications submitted to company offers.
- View application details.
- Search and filter applications by Offer, Status, Date and Candidate.
- Access restricted to company applications.
- Closed offers stop new applications.
- Existing applications remain accessible.
- Deleted offers set application status to "Unavailable".

#### Update Application

User Story: As a recruiter, I want to update application in dashboard.

#### Close Offer

User Story: As a recruiter, I want to set an offer status as closed in dashboard.

#### Reopen Offer

User Story: As a recruiter, I want to reopen an offer in dashboard.

#### Delete Application

User Story: As a recruiter, I want to delete application in dashboard.

---

## F09. Job Offer Management

### Recruiter Features

#### View Offers Timeline

User Story: As a recruiter, I want to view recent offers in a LinkedIn-style timeline.

Acceptance Criteria:

- Offers are displayed in a timeline feed.
- Offer updates preserve existing applications.
- Recruiters cannot manage applications from other companies.
- Unauthorized data is inaccessible.

#### View Offer Details

User Story: As a recruiter, I want to view an offer details on timeline.

#### Create Offer

User Story: As a recruiter, I want to post an application offer on timeline.

#### Update Offer

User Story: As a recruiter, I want to update an offer post on timeline.

#### Delete Offer

User Story: As a recruiter, I want to delete an offer post on timeline.

### Candidate Features

#### View Job Offers Timeline

User Story: As a candidate, I want to view job applications posted on timeline.

Acceptance Criteria:

- Filter by Location, Contract Type, Position Title and Application Status.
- Filters update results dynamically.
- Filters can be cleared.

#### View Applications Dashboard

User Story: As a candidate, I want to view job applications on dashboard.

#### Apply for Job

User Story: As a candidate, I want to apply for a job by entering my information and uploading a CV.

#### Upload CV for AI Suggestions

User Story: As a candidate, I want to upload my CV so that I receive AI-suggested job offers on timeline.

Acceptance Criteria:

- CV is uploaded and parsed.
- AI-generated job recommendations are returned.
- Recommendations are based on profile and skills.
- Upload and parsing errors are handled gracefully.

---

## F11. Interview Management

### Recruiter Features

#### View Schedule List

User Story: As a recruiter, I want to view schedules list.

Acceptance Criteria:

- View interviews linked to company applications.
- Schedule interviews.
- Select candidate, date and time.
- Reschedule interviews.
- Cancel interviews.
- Invalid or conflicting schedules are rejected.

#### View Calendar

User Story: As a recruiter, I want to view schedules in calendar.

#### Schedule Interview

User Story: As a recruiter, I want to schedule an interview by providing external link of zoom/google meet to candidate.

#### Mark Interview as Done

User Story: As a recruiter, I want to mark schedule as Done.

#### Cancel Interview

User Story: As a recruiter, I want to cancel schedules list.

#### Accept/Reject Delay Request

User Story: As a recruiter, I want to accept/reject a schedule delay.

### Candidate Features

#### View Interview Schedule List

User Story: As a candidate, I want to view my interview schedule list so that I know my upcoming interviews.

Acceptance Criteria:

- View personal interviews only.
- Entries display Offer, Recruiter, Date and Time.

#### View Interviews Calendar

User Story: As a candidate, I want to view my interviews in a calendar so that I can plan my time.

Acceptance Criteria:

- Interviews appear in calendar format.
- Dates and times are accurate.
- Navigation between periods is supported.

#### View Interview Details

User Story: As a candidate, I want to view schedules details.

Acceptance Criteria:

- Display Offer, Recruiter, Date, Time, Location/Meeting Link and Status.

#### Cancel Interview

User Story: As a candidate, I want to cancel schedules list.

Acceptance Criteria:

- Cancellation confirmation is displayed.
- Recruiter receives notification.
- Invalid cancellations are rejected.

#### Request Interview Delay

User Story: As a candidate, I want to request a delay of an interview so that I can propose a new time.

Acceptance Criteria:

- Candidate proposes a new date/time.
- Recruiter is notified.
- Original interview remains unchanged until approved.

---

## F15. Notification Management

### Recruiter Features

#### View Notifications

User Story: As a recruiter, I want to view notification list.

Acceptance Criteria:

- Recruiters receive recruitment-related notifications.
- Interview scheduling generates notifications.
- Candidate status changes generate notifications.

#### Mark Notification as Read

User Story: As a recruiter, I want to mark notification as read.

#### Mark Notification as Unread

User Story: As a recruiter, I want to mark notification as unread.

#### Delete Notification

User Story: As a recruiter, I want to delete notification.

### Candidate Features

#### View Notifications

User Story: As a candidate, I want to view my notification list so that I can see all my alerts.

Acceptance Criteria:

- Notifications are listed chronologically.
- Unread notifications are visually distinct.
- Notifications show content and date.

#### Mark Notification as Read

User Story: As a candidate, I want to mark a notification as read so that I can track what I have seen.

Acceptance Criteria:

- Unread counter updates.
- Read state is persisted.

#### Mark Notification as Unread

User Story: As a candidate, I want to mark a notification as unread so that I can revisit it later.

Acceptance Criteria:

- Notification state is reverted to unread.

#### Delete Notification

User Story: As a candidate, I want to delete a notification so that I can clean up my list.

Acceptance Criteria:

- Deletion is confirmed.
- Users can delete only their own notifications.

---

## Rules:

- a recruiter must upload an employment document on sign up to be review by admin then the latter approve/reject account registration
- a user can search an offer without authentication in timeline (timeline is publicly open to everyone), once a user applies for a job offer , the system will redirect him to signup page

### subscription plan:

#### Candidate Subscription Plans


| Basic(Free)                                                                                                                                                                                                                                                                                      | Professional                                                                                                                                                                                                                                                                                                                              | Career Pro                                                                                                                                                                                                                                                                                                                                                                                                                 |
| ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Target: New job seekers-Features:- Complete profile creation- CV upload (1 CV)- Apply to jobs- View application status- Interview management- Notifications- Basic job search filtersLimited :- AI recommendations (1 day trial) Limits- 1 CV- 10 applications/month- Basic support | Target: Active job seekersSuggested Price:- $4.99/month $49/year- Features Everything in Basic, plus:- Unlimited job applications ATS CV score analysis- Advanced AI job matching- Save unlimited jobs - Premium filters - Email alerts - Application insights- Priority supportLimits:- 5 CV versions - 100 ATS scans/month | Target: Senior professionals & executivesSuggested Price:- $9.99/month $99/year Features Everything in Professional, plus:- Unlimited ATS scans AI CV optimization suggestions- AI cover letter generation - AI interview preparation - Career analytics dashboard - Priority application badge - Early access to premium jobs - Dedicated supportplus:- Unlimited CV versions - Unlimited AI recommendations |


#### Recruiter / Company Subscription Plans


| Starter                                                                                                                                                                                                                                                                                                                                                                              | Growth                                                                                                                                                                                                                                                                                                                                                                                    | Enterprise                                                                                                                                                                                                                                                                                                                                    |
| ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Target:** Small companies, startups**Suggested Price:**- $29/month- $290/year**Features**- Company profile management- Candidate management Job posting- Candidate applications- CV viewing & download- Interview scheduling- Basic notifications- Basic dashboard analytics**Limits:**- 1 recruiter account- 5 active job offers- 200 candidate profile views/month | **Target:** Growing companies**Suggested Price**- $79/month- $790/year**Features** Everything in Starter, plus:- 10 recruiters- 25 active job offers- Advanced candidate search- Candidate status management- Business dashboard analytics- PDF exports- AI candidate ranking**Limits**:- 20 active offers/month- 3 recruiter accounts- 5,000 candidate profile views/month | **Target:** Large organizations**Suggested Price**- $199/month- $1,990/year**Features** Everything in Growth, plus:- Unlimited recruiters- Unlimited job offers- Advanced analytics- AI candidate matching- API access- Audit logs- Priority support**plus:** - Unlimited offers- Unlimited recruiters- Unlimited active offers |


Default Assignment Rules  
Candidates

- Every new candidate automatically receives **Basic (Free)**.
- Upgrade to Professional or Career Pro through subscription.

Recruiter

- Every newly approved company automatically receives a **14-day Starter trial**.
- After trial expiration:
  - Read-only access to historical data.
  - Job posting disabled until subscription is activated.

 