package com.example.specdriven.mapper;

import com.example.specdriven.api.model.*;
import com.example.specdriven.domain.RoleEntity;
import com.example.specdriven.domain.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserMapper.
 * Tests DTO/Entity conversions and password hashing.
 */
@ExtendWith(MockitoExtension.class)
class UserMapperTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper(passwordEncoder);
    }

    // =====================================
    // toEntity (CreateUserRequest) tests
    // =====================================

    @Test
    void toEntity_ValidCreateRequest_ReturnsEntity() {
        // Given
        CreateUserRequest request = new CreateUserRequest(
                "testuser", "Test User", "Password123!", "test@example.com");
        when(passwordEncoder.encode("Password123!")).thenReturn("$2a$10$hashedpassword");

        // When
        UserEntity result = userMapper.toEntity(request);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals("Test User", result.getName());
        assertEquals("test@example.com", result.getEmailAddress());
        assertEquals("$2a$10$hashedpassword", result.getPasswordHash());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
    }

    @Test
    void toEntity_CallsPasswordEncoder() {
        // Given
        CreateUserRequest request = new CreateUserRequest(
                "user", "User", "MyPassword!", "user@example.com");
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");

        // When
        userMapper.toEntity(request);

        // Then
        verify(passwordEncoder).encode("MyPassword!");
    }

    @Test
    void toEntity_GeneratesUniqueIds() {
        // Given
        CreateUserRequest request = new CreateUserRequest(
                "user", "User", "Password!", "user@example.com");
        when(passwordEncoder.encode(anyString())).thenReturn("hash");

        // When
        UserEntity entity1 = userMapper.toEntity(request);
        UserEntity entity2 = userMapper.toEntity(request);

        // Then
        assertNotEquals(entity1.getId(), entity2.getId());
    }

    // =====================================
    // toDto tests
    // =====================================

    @Test
    void toDto_ValidEntity_ReturnsDto() {
        // Given
        UserEntity entity = new UserEntity();
        entity.setId(UUID.randomUUID());
        entity.setUsername("testuser");
        entity.setName("Test User");
        entity.setEmailAddress("test@example.com");
        entity.setPasswordHash("$2a$10$secrethash");
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        // When
        User result = userMapper.toDto(entity, Collections.emptyList());

        // Then
        assertNotNull(result);
        assertEquals(entity.getId(), result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals("Test User", result.getName());
        assertEquals("test@example.com", result.getEmailAddress());
        assertNotNull(result.getRoles());
        assertTrue(result.getRoles().isEmpty());
    }

    @Test
    void toDto_NeverIncludesPassword() {
        // Given
        UserEntity entity = new UserEntity();
        entity.setId(UUID.randomUUID());
        entity.setUsername("user");
        entity.setName("User");
        entity.setEmailAddress("user@example.com");
        entity.setPasswordHash("$2a$10$verysecretpasswordhash");

        // When
        User result = userMapper.toDto(entity, Collections.emptyList());

        // Then - User DTO should not have password field
        // The User class doesn't have a password field, so this test verifies
        // the mapping doesn't expose the password hash through any means
        String resultString = result.toString();
        assertFalse(resultString.contains("verysecretpasswordhash"));
        assertFalse(resultString.contains("passwordHash"));
    }

    @Test
    void toDto_WithRoles_IncludesRoles() {
        // Given
        UserEntity entity = new UserEntity();
        entity.setId(UUID.randomUUID());
        entity.setUsername("user");
        entity.setName("User");
        entity.setEmailAddress("user@example.com");

        Role adminRole = new Role();
        adminRole.setRoleName(RoleName.ADMIN);
        adminRole.setPermissions(new LinkedHashSet<>());

        Role userRole = new Role();
        userRole.setRoleName(RoleName.USER);
        userRole.setPermissions(new LinkedHashSet<>());

        List<Role> roles = Arrays.asList(adminRole, userRole);

        // When
        User result = userMapper.toDto(entity, roles);

        // Then
        assertEquals(2, result.getRoles().size());
    }

    @Test
    void toDto_NullRoles_ReturnsEmptyList() {
        // Given
        UserEntity entity = new UserEntity();
        entity.setId(UUID.randomUUID());
        entity.setUsername("user");
        entity.setName("User");
        entity.setEmailAddress("user@example.com");

        // When
        User result = userMapper.toDto(entity, null);

        // Then
        assertNotNull(result.getRoles());
        assertTrue(result.getRoles().isEmpty());
    }

    // =====================================
    // updateEntity tests
    // =====================================

    @Test
    void updateEntity_AllFields_UpdatesAll() {
        // Given
        UserEntity entity = new UserEntity();
        entity.setId(UUID.randomUUID());
        entity.setUsername("olduser");
        entity.setName("Old Name");
        entity.setEmailAddress("old@example.com");
        entity.setPasswordHash("oldhash");
        entity.setUpdatedAt(LocalDateTime.now().minusDays(1));

        UpdateUserRequest request = new UpdateUserRequest();
        request.setUsername("newuser");
        request.setName("New Name");
        request.setEmailAddress("new@example.com");
        request.setPassword("NewPassword!");

        when(passwordEncoder.encode("NewPassword!")).thenReturn("newhash");

        // When
        UserEntity result = userMapper.updateEntity(request, entity);

        // Then
        assertEquals("newuser", result.getUsername());
        assertEquals("New Name", result.getName());
        assertEquals("new@example.com", result.getEmailAddress());
        assertEquals("newhash", result.getPasswordHash());
        assertNotNull(result.getUpdatedAt());
    }

    @Test
    void updateEntity_PartialUpdate_OnlyUpdatesProvidedFields() {
        // Given
        UserEntity entity = new UserEntity();
        entity.setId(UUID.randomUUID());
        entity.setUsername("originaluser");
        entity.setName("Original Name");
        entity.setEmailAddress("original@example.com");
        entity.setPasswordHash("originalhash");

        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("Only Name Changed");
        // username, email, password are null

        // When
        UserEntity result = userMapper.updateEntity(request, entity);

        // Then
        assertEquals("originaluser", result.getUsername()); // Unchanged
        assertEquals("Only Name Changed", result.getName()); // Changed
        assertEquals("original@example.com", result.getEmailAddress()); // Unchanged
        assertEquals("originalhash", result.getPasswordHash()); // Unchanged
    }

    @Test
    void updateEntity_PasswordOnly_HashesNewPassword() {
        // Given
        UserEntity entity = new UserEntity();
        entity.setId(UUID.randomUUID());
        entity.setUsername("user");
        entity.setName("User");
        entity.setEmailAddress("user@example.com");
        entity.setPasswordHash("oldhash");

        UpdateUserRequest request = new UpdateUserRequest();
        request.setPassword("BrandNewPassword!");

        when(passwordEncoder.encode("BrandNewPassword!")).thenReturn("brandnewhash");

        // When
        UserEntity result = userMapper.updateEntity(request, entity);

        // Then
        assertEquals("brandnewhash", result.getPasswordHash());
        verify(passwordEncoder).encode("BrandNewPassword!");
    }

    @Test
    void updateEntity_NoPassword_DoesNotCallEncoder() {
        // Given
        UserEntity entity = new UserEntity();
        entity.setId(UUID.randomUUID());
        entity.setUsername("user");
        entity.setName("User");
        entity.setEmailAddress("user@example.com");
        entity.setPasswordHash("existinghash");

        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("New Name");

        // When
        UserEntity result = userMapper.updateEntity(request, entity);

        // Then
        assertEquals("existinghash", result.getPasswordHash());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void updateEntity_AlwaysUpdatesTimestamp() {
        // Given
        LocalDateTime originalTime = LocalDateTime.now().minusDays(1);
        UserEntity entity = new UserEntity();
        entity.setId(UUID.randomUUID());
        entity.setUsername("user");
        entity.setName("User");
        entity.setEmailAddress("user@example.com");
        entity.setUpdatedAt(originalTime);

        UpdateUserRequest request = new UpdateUserRequest();
        // Empty update

        // When
        UserEntity result = userMapper.updateEntity(request, entity);

        // Then
        assertNotEquals(originalTime, result.getUpdatedAt());
        assertTrue(result.getUpdatedAt().isAfter(originalTime));
    }

    // =====================================
    // toRoleDto tests
    // =====================================

    @Test
    void toRoleDto_ValidEntity_ReturnsDto() {
        // Given
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setId(UUID.randomUUID());
        roleEntity.setRoleName("ADMIN");
        roleEntity.setDescription("Administrator role");

        Set<Permission> permissions = new LinkedHashSet<>();
        permissions.add(Permission.USER_READ);
        permissions.add(Permission.USER_WRITE);

        // When
        Role result = userMapper.toRoleDto(roleEntity, permissions);

        // Then
        assertNotNull(result);
        assertEquals(RoleName.ADMIN, result.getRoleName());
        assertEquals(2, result.getPermissions().size());
        assertTrue(result.getPermissions().contains(Permission.USER_READ));
        assertTrue(result.getPermissions().contains(Permission.USER_WRITE));
    }

    @Test
    void toRoleDto_NullPermissions_ReturnsEmptySet() {
        // Given
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setId(UUID.randomUUID());
        roleEntity.setRoleName("USER");

        // When
        Role result = userMapper.toRoleDto(roleEntity, null);

        // Then
        assertNotNull(result.getPermissions());
        assertTrue(result.getPermissions().isEmpty());
    }

    @Test
    void toRoleDto_NoPermissions_ReturnsEmptySet() {
        // Given
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setId(UUID.randomUUID());
        roleEntity.setRoleName("USER");

        // When
        Role result = userMapper.toRoleDto(roleEntity);

        // Then
        assertNotNull(result.getPermissions());
        assertTrue(result.getPermissions().isEmpty());
    }
}
