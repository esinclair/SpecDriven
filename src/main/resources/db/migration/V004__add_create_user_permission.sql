-- V004__add_create_user_permission.sql
-- Add CREATE_USER permission and assign it to ADMIN role

INSERT INTO permissions (id, permission, description) VALUES
    ('00000000-0000-0000-0000-000000000015', 'CREATE_USER', 'Ability to create new users');

-- Assign CREATE_USER to ADMIN role
INSERT INTO role_permissions (role_id, permission_id) VALUES
    ('00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000015');
