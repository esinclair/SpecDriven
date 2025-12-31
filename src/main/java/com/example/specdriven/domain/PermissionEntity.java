package com.example.specdriven.domain;

import jakarta.persistence.*;

import java.util.UUID;

/**
 * Domain entity representing a permission in the system.
 * Maps to the 'permissions' table in the database.
 * Permissions are predefined (users:read, users:write, users:delete, roles:assign).
 */
@Entity
@Table(name = "permissions")
public class PermissionEntity {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "permission")
    private String permission;

    @Column(name = "description")
    private String description;

    // Constructors
    public PermissionEntity() {
    }

    public PermissionEntity(UUID id, String permission, String description) {
        this.id = id;
        this.permission = permission;
        this.description = description;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
