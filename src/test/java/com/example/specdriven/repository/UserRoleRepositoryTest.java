package com.example.specdriven.repository;

import com.example.specdriven.domain.RoleEntity;
import com.example.specdriven.domain.UserEntity;
import com.example.specdriven.domain.UserRoleEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for UserRoleRepository.
 * Tests role assignment queries with an actual in-memory H2 database.
 *
 * Each test is transactional and rolls back after completion for isolation.
 */
@SpringBootTest
@Transactional
class UserRoleRepositoryTest {

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private UserEntity testUser;
    private RoleEntity adminRole;
    private RoleEntity userRole;

    @BeforeEach
    void setUp() {
        // Create a test user
        testUser = new UserEntity();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("roletest");
        testUser.setName("Role Test User");
        testUser.setEmailAddress("roletest@example.com");
        testUser.setPasswordHash("$2a$10$hashedpassword");
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
        userRepository.save(testUser);
        userRepository.flush();

        // Get predefined roles from database (created by Flyway migrations)
        adminRole = roleRepository.findByRoleName("ADMIN")
                .orElseThrow(() -> new RuntimeException("ADMIN role not found"));
        userRole = roleRepository.findByRoleName("USER")
                .orElseThrow(() -> new RuntimeException("USER role not found"));
    }

    @Test
    void save_AssignsRoleToUser() {
        // Given
        UserRoleEntity userRoleEntity = new UserRoleEntity(
                testUser.getId(), adminRole.getId(), LocalDateTime.now());

        // When
        UserRoleEntity saved = userRoleRepository.save(userRoleEntity);
        userRoleRepository.flush();

        // Then
        assertNotNull(saved);
        assertEquals(testUser.getId(), saved.getUserId());
        assertEquals(adminRole.getId(), saved.getRoleId());
    }

    @Test
    void findByUserId_ReturnsUserRoles() {
        // Given
        UserRoleEntity userRoleEntity = new UserRoleEntity(
                testUser.getId(), adminRole.getId(), LocalDateTime.now());
        userRoleRepository.save(userRoleEntity);
        userRoleRepository.flush();

        // When
        List<UserRoleEntity> roles = userRoleRepository.findByUserId(testUser.getId());

        // Then
        assertEquals(1, roles.size());
        assertEquals(adminRole.getId(), roles.get(0).getRoleId());
    }

    @Test
    void findByUserId_ReturnsEmptyList_WhenNoRoles() {
        // When
        List<UserRoleEntity> roles = userRoleRepository.findByUserId(testUser.getId());

        // Then
        assertTrue(roles.isEmpty());
    }

    @Test
    void findByUserId_ReturnsMultipleRoles() {
        // Given - assign multiple roles
        userRoleRepository.save(new UserRoleEntity(testUser.getId(), adminRole.getId(), LocalDateTime.now()));
        userRoleRepository.save(new UserRoleEntity(testUser.getId(), userRole.getId(), LocalDateTime.now()));
        userRoleRepository.flush();

        // When
        List<UserRoleEntity> roles = userRoleRepository.findByUserId(testUser.getId());

        // Then
        assertEquals(2, roles.size());
    }

    @Test
    void findByUserIdAndRoleId_ReturnsMapping_WhenExists() {
        // Given
        userRoleRepository.save(new UserRoleEntity(testUser.getId(), adminRole.getId(), LocalDateTime.now()));
        userRoleRepository.flush();

        // When
        List<UserRoleEntity> result = userRoleRepository.findByUserIdAndRoleId(testUser.getId(), adminRole.getId());

        // Then
        assertEquals(1, result.size());
        assertEquals(testUser.getId(), result.get(0).getUserId());
        assertEquals(adminRole.getId(), result.get(0).getRoleId());
    }

    @Test
    void findByUserIdAndRoleId_ReturnsEmptyList_WhenNotExists() {
        // When
        List<UserRoleEntity> result = userRoleRepository.findByUserIdAndRoleId(testUser.getId(), adminRole.getId());

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void deleteByUserIdAndRoleId_RemovesMapping() {
        // Given
        userRoleRepository.save(new UserRoleEntity(testUser.getId(), adminRole.getId(), LocalDateTime.now()));
        userRoleRepository.flush();

        // Verify it exists
        List<UserRoleEntity> beforeDelete = userRoleRepository.findByUserIdAndRoleId(testUser.getId(), adminRole.getId());
        assertEquals(1, beforeDelete.size());

        // When
        userRoleRepository.deleteByUserIdAndRoleId(testUser.getId(), adminRole.getId());
        userRoleRepository.flush();

        // Then
        List<UserRoleEntity> afterDelete = userRoleRepository.findByUserIdAndRoleId(testUser.getId(), adminRole.getId());
        assertTrue(afterDelete.isEmpty());
    }

    @Test
    void deleteByUserIdAndRoleId_DoesNotAffectOtherRoles() {
        // Given - assign multiple roles
        userRoleRepository.save(new UserRoleEntity(testUser.getId(), adminRole.getId(), LocalDateTime.now()));
        userRoleRepository.save(new UserRoleEntity(testUser.getId(), userRole.getId(), LocalDateTime.now()));
        userRoleRepository.flush();

        // When - delete only admin role
        userRoleRepository.deleteByUserIdAndRoleId(testUser.getId(), adminRole.getId());
        userRoleRepository.flush();

        // Then - user role should still exist
        List<UserRoleEntity> remaining = userRoleRepository.findByUserId(testUser.getId());
        assertEquals(1, remaining.size());
        assertEquals(userRole.getId(), remaining.get(0).getRoleId());
    }

    @Test
    void findByRoleId_ReturnsUsersWithRole() {
        // Given - create another user and assign the same role
        UserEntity anotherUser = new UserEntity();
        anotherUser.setId(UUID.randomUUID());
        anotherUser.setUsername("anotheruser");
        anotherUser.setName("Another User");
        anotherUser.setEmailAddress("another@example.com");
        anotherUser.setPasswordHash("$2a$10$hashedpassword");
        anotherUser.setCreatedAt(LocalDateTime.now());
        anotherUser.setUpdatedAt(LocalDateTime.now());
        userRepository.save(anotherUser);
        userRepository.flush();

        userRoleRepository.save(new UserRoleEntity(testUser.getId(), adminRole.getId(), LocalDateTime.now()));
        userRoleRepository.save(new UserRoleEntity(anotherUser.getId(), adminRole.getId(), LocalDateTime.now()));
        userRoleRepository.flush();

        // When
        List<UserRoleEntity> adminUsers = userRoleRepository.findByRoleId(adminRole.getId());

        // Then
        assertEquals(2, adminUsers.size());
    }

    @Test
    void findByRoleId_ReturnsEmptyList_WhenNoUsersHaveRole() {
        // When
        List<UserRoleEntity> result = userRoleRepository.findByRoleId(adminRole.getId());

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void duplicateRoleAssignment_PreventedByCompositeKey() {
        // Given
        userRoleRepository.save(new UserRoleEntity(testUser.getId(), adminRole.getId(), LocalDateTime.now()));
        userRoleRepository.flush();

        // When - try to assign the same role again (should update, not create duplicate)
        userRoleRepository.save(new UserRoleEntity(testUser.getId(), adminRole.getId(), LocalDateTime.now()));
        userRoleRepository.flush();

        // Then - should still only have one entry
        List<UserRoleEntity> roles = userRoleRepository.findByUserId(testUser.getId());
        assertEquals(1, roles.size());
    }
}
