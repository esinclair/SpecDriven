package com.example.specdriven.domain;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain entity representing role-permission mappings.
 * Maps to the 'role_permissions' join table in the database.
 * Uses composite primary key (role_id, permission_id).
 */
@Entity
@Table(name = "role_permissions")
@IdClass(RolePermissionEntity.RolePermissionId.class)
public class RolePermissionEntity {

    @Id
    @Column(name = "role_id")
    private UUID roleId;

    @Id
    @Column(name = "permission_id")
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

    /**
     * Composite primary key class for RolePermissionEntity.
     */
    public static class RolePermissionId implements Serializable {
        private UUID roleId;
        private UUID permissionId;

        public RolePermissionId() {
        }

        public RolePermissionId(UUID roleId, UUID permissionId) {
            this.roleId = roleId;
            this.permissionId = permissionId;
        }

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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RolePermissionId that = (RolePermissionId) o;
            return Objects.equals(roleId, that.roleId) && Objects.equals(permissionId, that.permissionId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(roleId, permissionId);
        }
    }
}
