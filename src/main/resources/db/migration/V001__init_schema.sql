-- V001: Initialize Users Schema
-- Description: Create users table with indexes for core user management

CREATE TABLE users (
    id           UUID PRIMARY KEY DEFAULT random_uuid(),
    username     VARCHAR(100) NOT NULL,
    name         VARCHAR(255) NOT NULL,
    email_address VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index on email for login lookups and uniqueness checks
CREATE INDEX idx_users_email ON users(email_address);

-- Index on username for filtering operations
CREATE INDEX idx_users_username ON users(username);
