-- V002: Roles and Permissions Schema
-- Description: Create roles, permissions, and role_permissions tables with seed data

-- Roles table
CREATE TABLE roles (
    id          UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    role_name   VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
);

-- Permissions table
CREATE TABLE permissions (
    id          UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    permission  VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255)
);

-- Role-Permission mapping (many-to-many)
CREATE TABLE role_permissions (
    role_id      UUID NOT NULL,
    permission_id UUID NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

-- Seed predefined roles
INSERT INTO roles (id, role_name, description) VALUES
    ('00000000-0000-0000-0000-000000000001', 'ADMIN', 'Full system access'),
    ('00000000-0000-0000-0000-000000000002', 'USER', 'Standard user access'),
    ('00000000-0000-0000-0000-000000000003', 'GUEST', 'Limited read-only access');

-- Seed predefined permissions
INSERT INTO permissions (id, permission, description) VALUES
    ('00000000-0000-0000-0000-000000000011', 'users:read', 'Read user data'),
    ('00000000-0000-0000-0000-000000000012', 'users:write', 'Create and update users'),
    ('00000000-0000-0000-0000-000000000013', 'users:delete', 'Delete users'),
    ('00000000-0000-0000-0000-000000000014', 'roles:assign', 'Assign roles to users');

-- Assign permissions to ADMIN role (all permissions)
INSERT INTO role_permissions (role_id, permission_id)
SELECT '00000000-0000-0000-0000-000000000001', id FROM permissions;

-- Assign permissions to USER role (read and write only)
INSERT INTO role_permissions (role_id, permission_id) VALUES
    ('00000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000011'),
    ('00000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000012');

-- Assign permissions to GUEST role (read only)
INSERT INTO role_permissions (role_id, permission_id) VALUES
    ('00000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000011');
