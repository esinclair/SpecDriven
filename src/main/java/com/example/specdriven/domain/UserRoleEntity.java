package com.example.specdriven.domain;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain entity representing user-role mappings.
 * Maps to the 'user_roles' join table in the database.
 * Uses composite primary key (user_id, role_id).
 */
@Table("user_roles")
public class UserRoleEntity {

    @Column("user_id")
    private UUID userId;

    @Column("role_id")
    private UUID roleId;

    @Column("assigned_at")
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
}
