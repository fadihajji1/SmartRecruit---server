CREATE TABLE users (
    id UUID PRIMARY KEY,
    user_type VARCHAR(32) NOT NULL,
    email VARCHAR(320) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(32),
    address VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100),
    postal_code VARCHAR(32),
    role VARCHAR(32) NOT NULL,
    status VARCHAR(24) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE candidate_profiles (
    user_id UUID PRIMARY KEY,

    CONSTRAINT fk_candidate_profiles_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE TABLE recruiter_profiles (
    user_id UUID PRIMARY KEY,
    employment_proof_object_key VARCHAR(512) NOT NULL,
    approval_status VARCHAR(24) NOT NULL DEFAULT 'PENDING',
    rejection_reason VARCHAR(1000),
    approved_at TIMESTAMP WITH TIME ZONE,

    CONSTRAINT fk_recruiter_profiles_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE TABLE administrator_profiles (
    user_id UUID PRIMARY KEY,
    CONSTRAINT fk_administrator_profiles_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);