-- Users API Database Schema (V003)
-- Creates tables for users, roles, role_permissions, and user_roles

-- Users table (lowercase for H2 compatibility with Spring Data JDBC)
CREATE TABLE "users" (
    "id" UUID PRIMARY KEY,
    "username" VARCHAR(50) NOT NULL,
    "name" VARCHAR(200) NOT NULL,
    "email_address" VARCHAR(254) NOT NULL UNIQUE,
    "password_hash" VARCHAR(255) NOT NULL,
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX "idx_users_username" ON "users"("username");
CREATE INDEX "idx_users_email" ON "users"("email_address");

-- Roles table (catalog of available roles)
CREATE TABLE "roles" (
    "role_name" VARCHAR(50) PRIMARY KEY
);

-- Role permissions table (defines which permissions each role has)
CREATE TABLE "role_permissions" (
    "role_name" VARCHAR(50) NOT NULL,
    "permission" VARCHAR(50) NOT NULL,
    PRIMARY KEY ("role_name", "permission"),
    FOREIGN KEY ("role_name") REFERENCES "roles"("role_name") ON DELETE CASCADE
);

-- User roles table (many-to-many relationship between users and roles)
CREATE TABLE "user_roles" (
    "user_id" UUID NOT NULL,
    "role_name" VARCHAR(50) NOT NULL,
    PRIMARY KEY ("user_id", "role_name"),
    FOREIGN KEY ("user_id") REFERENCES "users"("id") ON DELETE CASCADE,
    FOREIGN KEY ("role_name") REFERENCES "roles"("role_name") ON DELETE CASCADE
);

-- Seed initial roles based on the API contract
INSERT INTO "roles" ("role_name") VALUES ('ADMIN');
INSERT INTO "roles" ("role_name") VALUES ('USER');
INSERT INTO "roles" ("role_name") VALUES ('VIEWER');

-- Seed role permissions based on the API contract
INSERT INTO "role_permissions" ("role_name", "permission") VALUES ('ADMIN', 'READ');
INSERT INTO "role_permissions" ("role_name", "permission") VALUES ('ADMIN', 'WRITE');
INSERT INTO "role_permissions" ("role_name", "permission") VALUES ('ADMIN', 'DELETE');
INSERT INTO "role_permissions" ("role_name", "permission") VALUES ('USER', 'READ');
INSERT INTO "role_permissions" ("role_name", "permission") VALUES ('USER', 'WRITE');
INSERT INTO "role_permissions" ("role_name", "permission") VALUES ('VIEWER', 'READ');
