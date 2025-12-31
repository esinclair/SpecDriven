package com.example.specdriven.repository;

import com.example.specdriven.domain.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for UserRepository.
 * Tests repository methods with an actual in-memory H2 database.
 * 
 * Note: Uses JpaRepository with flush() to ensure data visibility in tests.
 * Each test is transactional and rolls back after completion for isolation.
 */
@SpringBootTest
@Transactional
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void save_CreatesNewUser() {
        UserEntity user = createTestUser("testuser", "test@example.com");
        
        UserEntity saved = userRepository.save(user);
        userRepository.flush();
        
        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals("testuser", saved.getUsername());
        assertEquals("test@example.com", saved.getEmailAddress());
    }

    @Test
    void findById_ReturnsUser_WhenExists() {
        UserEntity user = createTestUser("testuser", "test@example.com");
        UserEntity saved = userRepository.save(user);
        userRepository.flush();
        
        Optional<UserEntity> found = userRepository.findById(saved.getId());
        
        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
        assertEquals("testuser", found.get().getUsername());
    }

    @Test
    void findById_ReturnsEmpty_WhenNotExists() {
        UUID nonExistentId = UUID.randomUUID();
        
        Optional<UserEntity> found = userRepository.findById(nonExistentId);
        
        assertFalse(found.isPresent());
    }

    @Test
    void findByEmailAddress_ReturnsUser_WhenExists() {
        UserEntity user = createTestUser("testuser", "test@example.com");
        userRepository.save(user);
        userRepository.flush();
        
        Optional<UserEntity> found = userRepository.findByEmailAddress("test@example.com");
        
        assertTrue(found.isPresent());
        assertEquals("test@example.com", found.get().getEmailAddress());
        assertEquals("testuser", found.get().getUsername());
    }

    @Test
    void findByEmailAddress_ReturnsEmpty_WhenNotExists() {
        Optional<UserEntity> found = userRepository.findByEmailAddress("nonexistent@example.com");
        
        assertFalse(found.isPresent());
    }

    @Test
    void findByUsername_ReturnsUser_WhenExists() {
        UserEntity user = createTestUser("testuser", "test@example.com");
        userRepository.save(user);
        userRepository.flush();
        
        Optional<UserEntity> found = userRepository.findByUsername("testuser");
        
        assertTrue(found.isPresent());
        assertEquals("testuser", found.get().getUsername());
        assertEquals("test@example.com", found.get().getEmailAddress());
    }

    @Test
    void findByUsername_ReturnsEmpty_WhenNotExists() {
        Optional<UserEntity> found = userRepository.findByUsername("nonexistent");
        
        assertFalse(found.isPresent());
    }

    @Test
    void count_ReturnsZero_WhenNoUsers() {
        long count = userRepository.count();
        
        assertEquals(0, count);
    }

    @Test
    void count_ReturnsCorrectCount_WhenUsersExist() {
        userRepository.save(createTestUser("user1", "user1@example.com"));
        userRepository.save(createTestUser("user2", "user2@example.com"));
        userRepository.save(createTestUser("user3", "user3@example.com"));
        userRepository.flush();
        
        long count = userRepository.count();
        
        assertEquals(3, count);
    }

    @Test
    void findAll_WithPageable_ReturnsPagedResults() {
        // Create multiple users
        userRepository.save(createTestUser("user1", "user1@example.com"));
        userRepository.save(createTestUser("user2", "user2@example.com"));
        userRepository.save(createTestUser("user3", "user3@example.com"));
        userRepository.save(createTestUser("user4", "user4@example.com"));
        userRepository.flush();
        
        PageRequest pageRequest = PageRequest.of(0, 2); // First page, 2 items per page
        Page<UserEntity> page = userRepository.findAll(pageRequest);
        
        assertNotNull(page);
        assertEquals(2, page.getContent().size());
        assertEquals(4, page.getTotalElements());
        assertEquals(2, page.getTotalPages());
    }

    @Test
    void findAll_WithPageable_ReturnsSecondPage() {
        // Create multiple users
        userRepository.save(createTestUser("user1", "user1@example.com"));
        userRepository.save(createTestUser("user2", "user2@example.com"));
        userRepository.save(createTestUser("user3", "user3@example.com"));
        userRepository.flush();
        
        PageRequest pageRequest = PageRequest.of(1, 2); // Second page, 2 items per page
        Page<UserEntity> page = userRepository.findAll(pageRequest);
        
        assertNotNull(page);
        assertEquals(1, page.getContent().size()); // Only 1 item on second page
        assertEquals(3, page.getTotalElements());
    }

    @Test
    void deleteById_RemovesUser() {
        UserEntity user = createTestUser("testuser", "test@example.com");
        UserEntity saved = userRepository.save(user);
        userRepository.flush();
        
        userRepository.deleteById(saved.getId());
        userRepository.flush();
        
        Optional<UserEntity> found = userRepository.findById(saved.getId());
        assertFalse(found.isPresent());
    }

    @Test
    void update_ModifiesExistingUser() {
        UserEntity user = createTestUser("testuser", "test@example.com");
        UserEntity saved = userRepository.save(user);
        userRepository.flush();
        
        saved.setName("Updated Name");
        saved.setUpdatedAt(LocalDateTime.now());
        UserEntity updated = userRepository.save(saved);
        userRepository.flush();
        
        assertEquals("Updated Name", updated.getName());
        assertEquals(saved.getId(), updated.getId());
    }

    private UserEntity createTestUser(String username, String email) {
        UserEntity user = new UserEntity();
        // Manual ID assignment required because the migration doesn't have UUID auto-generation (DEFAULT random_uuid())
        // Spring Data JDBC requires the ID to be set before save for new entities without @GeneratedValue
        user.setId(UUID.randomUUID());
        user.setUsername(username);
        user.setName("Test User");
        user.setEmailAddress(email);
        user.setPasswordHash("$2a$10$hashedPassword");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }
}
