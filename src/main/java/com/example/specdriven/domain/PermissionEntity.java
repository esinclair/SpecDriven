package com.example.specdriven.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

/**
 * Domain entity representing a permission in the system.
 * Mapped to the 'permissions' table in the database.
 */
@Table("permissions")
public class PermissionEntity {
    
    @Id
    private UUID id;
    
    @Column("permission")
    private String permission;
    
    @Column("description")
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
