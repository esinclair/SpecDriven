-- Create user_roles join table
CREATE TABLE user_roles (
    user_id UUID NOT NULL,
    role_name VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role_name),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CHECK (role_name IN ('ADMIN', 'USER', 'AUDITOR'))
);

-- Create indexes for efficient queries
CREATE INDEX idx_user_roles_user ON user_roles(user_id);
CREATE INDEX idx_user_roles_role ON user_roles(role_name);
