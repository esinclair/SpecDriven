package com.example.specdriven.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Domain entity representing a user in the system.
 * Mapped to the 'users' table in the database.
 */
@Table("users")
public class UserEntity implements Persistable<UUID> {
    
    @Id
    private UUID id;
    
    @Column("username")
    private String username;
    
    @Column("name")
    private String name;
    
    @Column("email_address")
    private String emailAddress;
    
    @Column("password_hash")
    private String passwordHash;
    
    @Column("created_at")
    private Instant createdAt;
    
    @Column("updated_at")
    private Instant updatedAt;
    
    // Transient field: roles loaded separately via join
    @Transient
    private List<RoleEntity> roles;
    
    // Transient field to track if entity is new
    @Transient
    private boolean isNew = true;
    
    // Constructors
    
    public UserEntity() {
    }
    
    public UserEntity(UUID id, String username, String name, String emailAddress, 
                     String passwordHash, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.emailAddress = emailAddress;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Getters and Setters
    
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmailAddress() {
        return emailAddress;
    }
    
    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }
    
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
    
    public Instant getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public List<RoleEntity> getRoles() {
        return roles;
    }
    
    public void setRoles(List<RoleEntity> roles) {
        this.roles = roles;
    }
    
    @Override
    public boolean isNew() {
        return isNew || id == null;
    }
    
    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }
}
