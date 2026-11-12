CREATE TABLE plans (
    id UUID PRIMARY KEY,
    plan_code VARCHAR(40) NOT NULL UNIQUE,
    display_name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    audience VARCHAR(24) NOT NULL,
    price NUMERIC(10,2) NOT NULL CHECK (price >= 0),
    currency VARCHAR(3) NOT NULL,
    billing_period_days INTEGER NOT NULL CHECK (billing_period_days BETWEEN 1 AND 366),
    trial_days INTEGER NOT NULL DEFAULT 0 CHECK (trial_days BETWEEN 0 AND 90),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE plan_features (
    plan_id UUID NOT NULL REFERENCES plans(id) ON DELETE CASCADE,
    feature_order INTEGER NOT NULL,
    feature VARCHAR(160) NOT NULL,
    PRIMARY KEY (plan_id, feature_order)
);

CREATE TABLE subscriptions (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    audience VARCHAR(24) NOT NULL,
    plan_id UUID NOT NULL REFERENCES plans(id),
    pending_plan_id UUID REFERENCES plans(id),
    status VARCHAR(24) NOT NULL,
    starts_at TIMESTAMPTZ NOT NULL,
    ends_at TIMESTAMPTZ NOT NULL,
    cancelled_at TIMESTAMPTZ,
    change_effective_at TIMESTAMPTZ,
    auto_renew BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX ix_subscriptions_owner ON subscriptions(owner_id, audience, created_at DESC);
CREATE UNIQUE INDEX uk_subscriptions_one_current ON subscriptions(owner_id, audience)
    WHERE status IN ('TRIALING', 'ACTIVE', 'PAST_DUE', 'SUSPENDED');

CREATE TABLE subscription_history (
    id UUID PRIMARY KEY,
    subscription_id UUID NOT NULL REFERENCES subscriptions(id),
    actor_user_id UUID NOT NULL,
    action VARCHAR(32) NOT NULL,
    from_status VARCHAR(24),
    to_status VARCHAR(24) NOT NULL,
    from_plan_code VARCHAR(40),
    to_plan_code VARCHAR(40),
    details VARCHAR(500),
    occurred_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX ix_subscription_history_order ON subscription_history(subscription_id, occurred_at DESC);

CREATE TABLE invoices (
    id UUID PRIMARY KEY,
    subscription_id UUID NOT NULL REFERENCES subscriptions(id),
    owner_id UUID NOT NULL,
    plan_code VARCHAR(40) NOT NULL,
    amount NUMERIC(10,2) NOT NULL CHECK (amount >= 0),
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(16) NOT NULL,
    payment_reference VARCHAR(100) NOT NULL,
    issued_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX ix_invoices_owner_order ON invoices(owner_id, issued_at DESC);

INSERT INTO plans (id, plan_code, display_name, description, audience, price, currency, billing_period_days, trial_days, active, created_at, updated_at) VALUES
('3d705708-3f77-4ac4-a267-fcbeee4688a1', 'CANDIDATE_FREE', 'Candidate Free', 'Core candidate profile and application tools.', 'CANDIDATE', 0, 'USD', 30, 0, TRUE, NOW(), NOW()),
('c2047073-a49c-49df-a6ae-a1b7d76d0f4f', 'CANDIDATE_PLUS', 'Candidate Plus', 'More CV versions and enhanced ATS analysis.', 'CANDIDATE', 9.99, 'USD', 30, 0, TRUE, NOW(), NOW()),
('57364b3e-c57f-4fc3-b492-6abefbb75b33', 'RECRUITER_STARTER', 'Recruiter Starter', 'Starter tools for approved recruiting teams.', 'RECRUITER', 29.99, 'USD', 30, 14, TRUE, NOW(), NOW()),
('c3bf0ed8-84e8-4870-b785-a80d9a06a12e', 'RECRUITER_PRO', 'Recruiter Pro', 'Advanced hiring workflow and analytics.', 'RECRUITER', 79.99, 'USD', 30, 0, TRUE, NOW(), NOW());

INSERT INTO plan_features (plan_id, feature_order, feature) VALUES
('3d705708-3f77-4ac4-a267-fcbeee4688a1', 0, '1 CV version'),
('3d705708-3f77-4ac4-a267-fcbeee4688a1', 1, 'Apply to published jobs'),
('c2047073-a49c-49df-a6ae-a1b7d76d0f4f', 0, '5 CV versions'),
('c2047073-a49c-49df-a6ae-a1b7d76d0f4f', 1, 'Enhanced ATS analysis'),
('c2047073-a49c-49df-a6ae-a1b7d76d0f4f', 2, 'Job recommendations'),
('57364b3e-c57f-4fc3-b492-6abefbb75b33', 0, 'Up to 5 active job offers'),
('57364b3e-c57f-4fc3-b492-6abefbb75b33', 1, 'Application pipeline'),
('c3bf0ed8-84e8-4870-b785-a80d9a06a12e', 0, 'Unlimited active job offers'),
('c3bf0ed8-84e8-4870-b785-a80d9a06a12e', 1, 'Recruitment analytics'),
('c3bf0ed8-84e8-4870-b785-a80d9a06a12e', 2, 'Priority workflow');
