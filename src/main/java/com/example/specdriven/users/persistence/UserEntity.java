package com.example.specdriven.users.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.util.UUID;

@Table("USERS")
public class UserEntity {
    @Id
    @Column("ID")
    private UUID id;
    
    @Column("USERNAME")
    private String username;
    
    @Column("NAME")
    private String name;
    
    @Column("EMAIL_ADDRESS")
    private String emailAddress;
    
    @Column("PASSWORD_HASH")
    private String passwordHash;
    
    @Column("CREATED_AT")
    private java.time.LocalDateTime createdAt;
    
    @Column("UPDATED_AT")
    private java.time.LocalDateTime updatedAt;

    public UserEntity() {
        this.id = UUID.randomUUID();
    }

    @PersistenceCreator
    public UserEntity(UUID id, String username, String name, String emailAddress, String passwordHash,
                     java.time.LocalDateTime createdAt, java.time.LocalDateTime updatedAt) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.emailAddress = emailAddress;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmailAddress() { return emailAddress; }
    public void setEmailAddress(String emailAddress) { this.emailAddress = emailAddress; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }
    public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(java.time.LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
