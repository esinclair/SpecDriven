-- Create users table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    username VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email_address VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create unique indexes for username and email_address (case-insensitive)
CREATE UNIQUE INDEX idx_users_username ON users(LOWER(username));
CREATE UNIQUE INDEX idx_users_email ON users(LOWER(email_address));
