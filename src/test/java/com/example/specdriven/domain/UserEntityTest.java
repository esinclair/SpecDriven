package com.example.specdriven.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for UserEntity.
 * Verifies domain entity structure and getter/setter behavior.
 */
class UserEntityTest {

    @Test
    void constructor_NoArgs_CreatesInstance() {
        UserEntity entity = new UserEntity();
        
        assertNotNull(entity);
    }

    @Test
    void constructor_AllArgs_SetsAllFields() {
        UUID id = UUID.randomUUID();
        String username = "testuser";
        String name = "Test User";
        String email = "test@example.com";
        String passwordHash = "hashedPassword123";
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();
        
        UserEntity entity = new UserEntity(id, username, name, email, passwordHash, createdAt, updatedAt);
        
        assertEquals(id, entity.getId());
        assertEquals(username, entity.getUsername());
        assertEquals(name, entity.getName());
        assertEquals(email, entity.getEmailAddress());
        assertEquals(passwordHash, entity.getPasswordHash());
        assertEquals(createdAt, entity.getCreatedAt());
        assertEquals(updatedAt, entity.getUpdatedAt());
    }

    @Test
    void settersAndGetters_WorkCorrectly() {
        UserEntity entity = new UserEntity();
        UUID id = UUID.randomUUID();
        String username = "testuser";
        String name = "Test User";
        String email = "test@example.com";
        String passwordHash = "hashedPassword123";
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();
        
        entity.setId(id);
        entity.setUsername(username);
        entity.setName(name);
        entity.setEmailAddress(email);
        entity.setPasswordHash(passwordHash);
        entity.setCreatedAt(createdAt);
        entity.setUpdatedAt(updatedAt);
        
        assertEquals(id, entity.getId());
        assertEquals(username, entity.getUsername());
        assertEquals(name, entity.getName());
        assertEquals(email, entity.getEmailAddress());
        assertEquals(passwordHash, entity.getPasswordHash());
        assertEquals(createdAt, entity.getCreatedAt());
        assertEquals(updatedAt, entity.getUpdatedAt());
    }

    @Test
    void emailAddress_CanBeSet() {
        UserEntity entity = new UserEntity();
        String email = "user@example.com";
        
        entity.setEmailAddress(email);
        
        assertEquals(email, entity.getEmailAddress());
    }

    @Test
    void passwordHash_IsStoredAsString() {
        UserEntity entity = new UserEntity();
        String hash = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
        
        entity.setPasswordHash(hash);
        
        assertEquals(hash, entity.getPasswordHash());
    }

    @Test
    void timestamps_CanBeSetAndRetrieved() {
        UserEntity entity = new UserEntity();
        LocalDateTime now = LocalDateTime.now();
        
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        
        assertEquals(now, entity.getCreatedAt());
        assertEquals(now, entity.getUpdatedAt());
    }
}
