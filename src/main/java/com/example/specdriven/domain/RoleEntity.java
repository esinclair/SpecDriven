package com.example.specdriven.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.List;
import java.util.UUID;

/**
 * Domain entity representing a role in the system.
 * Mapped to the 'roles' table in the database.
 */
@Table("roles")
public class RoleEntity {
    
    @Id
    private UUID id;
    
    @Column("role_name")
    private String name;
    
    @Column("description")
    private String description;
    
    // Transient field: permissions loaded separately via join
    @Transient
    private List<PermissionEntity> permissions;
    
    // Constructors
    
    public RoleEntity() {
    }
    
    public RoleEntity(UUID id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }
    
    // Getters and Setters
    
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public List<PermissionEntity> getPermissions() {
        return permissions;
    }
    
    public void setPermissions(List<PermissionEntity> permissions) {
        this.permissions = permissions;
    }
}
