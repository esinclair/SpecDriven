package com.example.specdriven.domain;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain entity representing user-role mappings.
 * Maps to the 'user_roles' join table in the database.
 * Uses composite primary key (user_id, role_id).
 */
@Entity
@Table(name = "user_roles")
@IdClass(UserRoleEntity.UserRoleId.class)
public class UserRoleEntity {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Id
    @Column(name = "role_id")
    private UUID roleId;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    // Constructors
    public UserRoleEntity() {
    }

    public UserRoleEntity(UUID userId, UUID roleId, LocalDateTime assignedAt) {
        this.userId = userId;
        this.roleId = roleId;
        this.assignedAt = assignedAt;
    }

    // Getters and Setters
    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getRoleId() {
        return roleId;
    }

    public void setRoleId(UUID roleId) {
        this.roleId = roleId;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    /**
     * Composite primary key class for UserRoleEntity.
     */
    public static class UserRoleId implements Serializable {
        private UUID userId;
        private UUID roleId;

        public UserRoleId() {
        }

        public UserRoleId(UUID userId, UUID roleId) {
            this.userId = userId;
            this.roleId = roleId;
        }

        public UUID getUserId() {
            return userId;
        }

        public void setUserId(UUID userId) {
            this.userId = userId;
        }

        public UUID getRoleId() {
            return roleId;
        }

        public void setRoleId(UUID roleId) {
            this.roleId = roleId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UserRoleId that = (UserRoleId) o;
            return Objects.equals(userId, that.userId) && Objects.equals(roleId, that.roleId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(userId, roleId);
        }
    }
}
