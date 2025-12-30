-- V002__roles_permissions.sql
-- Roles and Permissions with predefined data

-- Roles table
CREATE TABLE roles (
    id          UUID PRIMARY KEY,
    role_name   VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
);

-- Permissions table
CREATE TABLE permissions (
    id          UUID PRIMARY KEY,
    permission  VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255)
);

-- Role-Permission mapping (many-to-many)
CREATE TABLE role_permissions (
    role_id       UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

CREATE INDEX idx_role_permissions_role ON role_permissions(role_id);
CREATE INDEX idx_role_permissions_permission ON role_permissions(permission_id);

-- Insert predefined roles
INSERT INTO roles (id, role_name, description) VALUES
    ('00000000-0000-0000-0000-000000000001', 'ADMIN', 'Full system access with all permissions'),
    ('00000000-0000-0000-0000-000000000002', 'USER', 'Standard user access with read and write permissions'),
    ('00000000-0000-0000-0000-000000000003', 'GUEST', 'Limited read-only access');

-- Insert predefined permissions
INSERT INTO permissions (id, permission, description) VALUES
    ('00000000-0000-0000-0000-000000000011', 'users:read', 'Read user data'),
    ('00000000-0000-0000-0000-000000000012', 'users:write', 'Create and update users'),
    ('00000000-0000-0000-0000-000000000013', 'users:delete', 'Delete users'),
    ('00000000-0000-0000-0000-000000000014', 'roles:assign', 'Assign roles to users');

-- Assign permissions to roles
-- ADMIN has all permissions
INSERT INTO role_permissions (role_id, permission_id) VALUES
    ('00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000011'), -- ADMIN -> users:read
    ('00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000012'), -- ADMIN -> users:write
    ('00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000013'), -- ADMIN -> users:delete
    ('00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000014'); -- ADMIN -> roles:assign

-- USER has read and write
INSERT INTO role_permissions (role_id, permission_id) VALUES
    ('00000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000011'), -- USER -> users:read
    ('00000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000012'); -- USER -> users:write

-- GUEST has read only
INSERT INTO role_permissions (role_id, permission_id) VALUES
    ('00000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000011'); -- GUEST -> users:read
