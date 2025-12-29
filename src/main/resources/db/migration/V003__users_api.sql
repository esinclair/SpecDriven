-- V003: Users API Schema
-- Description: Create user_roles mapping table for user-role assignments

CREATE TABLE user_roles (
    user_id     UUID NOT NULL,
    role_name   VARCHAR(50) NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_name),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_name) REFERENCES roles(role_name) ON DELETE CASCADE
);

-- Index on user_id for fast lookup of user's roles
CREATE INDEX idx_user_roles_user ON user_roles(user_id);

-- Index on role_name for fast lookup of users with a specific role
CREATE INDEX idx_user_roles_role ON user_roles(role_name);
