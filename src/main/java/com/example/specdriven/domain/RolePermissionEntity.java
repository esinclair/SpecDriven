package com.example.specdriven.domain;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

/**
 * Domain entity representing role-permission mappings.
 * Maps to the 'role_permissions' join table in the database.
 * Uses composite primary key (role_id, permission_id).
 */
@Table("role_permissions")
public class RolePermissionEntity {

    @Column("role_id")
    private UUID roleId;

    @Column("permission_id")
    private UUID permissionId;

    // Constructors
    public RolePermissionEntity() {
    }

    public RolePermissionEntity(UUID roleId, UUID permissionId) {
        this.roleId = roleId;
        this.permissionId = permissionId;
    }

    // Getters and Setters
    public UUID getRoleId() {
        return roleId;
    }

    public void setRoleId(UUID roleId) {
        this.roleId = roleId;
    }

    public UUID getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(UUID permissionId) {
        this.permissionId = permissionId;
    }
}
