package com.example.specdriven.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

/**
 * Domain entity representing a role in the system.
 * Maps to the 'roles' table in the database.
 * Roles are predefined (ADMIN, USER, GUEST).
 */
@Table("roles")
public class RoleEntity {

    @Id
    @Column("id")
    private UUID id;

    @Column("role_name")
    private String roleName;

    @Column("description")
    private String description;

    // Constructors
    public RoleEntity() {
    }

    public RoleEntity(UUID id, String roleName, String description) {
        this.id = id;
        this.roleName = roleName;
        this.description = description;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
