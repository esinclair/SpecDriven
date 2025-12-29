package com.example.specdriven.domain;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain entity representing the user-role relationship.
 * Mapped to the 'user_roles' table in the database.
 * 
 * This is a join table entity with a composite primary key.
 */
@Table("user_roles")
public class UserRoleEntity {
    
    @Column("user_id")
    private UUID userId;
    
    @Column("role_name")
    private String roleName;
    
    @Column("assigned_at")
    private Instant assignedAt;
    
    // Constructors
    
    public UserRoleEntity() {
    }
    
    public UserRoleEntity(UUID userId, String roleName, Instant assignedAt) {
        this.userId = userId;
        this.roleName = roleName;
        this.assignedAt = assignedAt;
    }
    
    // Getters and Setters
    
    public UUID getUserId() {
        return userId;
    }
    
    public void setUserId(UUID userId) {
        this.userId = userId;
    }
    
    public String getRoleName() {
        return roleName;
    }
    
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
    
    public Instant getAssignedAt() {
        return assignedAt;
    }
    
    public void setAssignedAt(Instant assignedAt) {
        this.assignedAt = assignedAt;
    }
}
