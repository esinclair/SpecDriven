package com.example.specdriven.mapper;

import com.example.specdriven.api.model.Permission;
import com.example.specdriven.api.model.Role;
import com.example.specdriven.api.model.RoleName;
import com.example.specdriven.domain.PermissionEntity;
import com.example.specdriven.domain.RoleEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RoleMapper.
 * Tests DTO/Entity conversions for roles and permissions.
 */
class RoleMapperTest {

    private RoleMapper roleMapper;

    @BeforeEach
    void setUp() {
        roleMapper = new RoleMapper();
    }

    // =====================================
    // toDto (with permissions) tests
    // =====================================

    @Test
    void toDto_WithPermissions_ReturnsCompleteRole() {
        // Given
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setId(UUID.randomUUID());
        roleEntity.setRoleName("ADMIN");
        roleEntity.setDescription("Administrator role");

        PermissionEntity permission1 = new PermissionEntity();
        permission1.setId(UUID.randomUUID());
        permission1.setPermission("USER_READ");
        permission1.setDescription("Read users");

        PermissionEntity permission2 = new PermissionEntity();
        permission2.setId(UUID.randomUUID());
        permission2.setPermission("USER_WRITE");
        permission2.setDescription("Write users");

        List<PermissionEntity> permissions = List.of(permission1, permission2);

        // When
        Role result = roleMapper.toDto(roleEntity, permissions);

        // Then
        assertNotNull(result);
        assertEquals(RoleName.ADMIN, result.getRoleName());
        assertNotNull(result.getPermissions());
        assertEquals(2, result.getPermissions().size());

        List<Permission> permissionList = new ArrayList<>(result.getPermissions());
        assertTrue(permissionList.contains(Permission.USER_READ));
        assertTrue(permissionList.contains(Permission.USER_WRITE));
    }

    @Test
    void toDto_WithEmptyPermissions_ReturnsRoleWithEmptyPermissionSet() {
        // Given
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setId(UUID.randomUUID());
        roleEntity.setRoleName("USER");
        roleEntity.setDescription("Regular user role");

        // When
        Role result = roleMapper.toDto(roleEntity, List.of());

        // Then
        assertNotNull(result);
        assertEquals(RoleName.USER, result.getRoleName());
        assertNotNull(result.getPermissions());
        assertTrue(result.getPermissions().isEmpty());
    }

    @Test
    void toDto_WithAllPermissions_ReturnsRoleWithAllPermissions() {
        // Given
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setId(UUID.randomUUID());
        roleEntity.setRoleName("ADMIN");

        PermissionEntity permission1 = new PermissionEntity();
        permission1.setPermission("USER_READ");

        PermissionEntity permission2 = new PermissionEntity();
        permission2.setPermission("USER_WRITE");

        PermissionEntity permission3 = new PermissionEntity();
        permission3.setPermission("ROLE_ASSIGN");

        List<PermissionEntity> permissions = List.of(permission1, permission2, permission3);

        // When
        Role result = roleMapper.toDto(roleEntity, permissions);

        // Then
        assertNotNull(result);
        assertEquals(3, result.getPermissions().size());
        assertTrue(result.getPermissions().contains(Permission.USER_READ));
        assertTrue(result.getPermissions().contains(Permission.USER_WRITE));
        assertTrue(result.getPermissions().contains(Permission.ROLE_ASSIGN));
    }

    @Test
    void toDto_PreservesPermissionOrder_UsingLinkedHashSet() {
        // Given
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setId(UUID.randomUUID());
        roleEntity.setRoleName("ADMIN");

        PermissionEntity permission1 = new PermissionEntity();
        permission1.setPermission("ROLE_ASSIGN");

        PermissionEntity permission2 = new PermissionEntity();
        permission2.setPermission("USER_READ");

        PermissionEntity permission3 = new PermissionEntity();
        permission3.setPermission("USER_WRITE");

        // Order matters - should be preserved in LinkedHashSet
        List<PermissionEntity> permissions = List.of(permission1, permission2, permission3);

        // When
        Role result = roleMapper.toDto(roleEntity, permissions);

        // Then
        assertNotNull(result);
        assertEquals(3, result.getPermissions().size());

        // Verify order is preserved
        List<Permission> permissionList = new ArrayList<>(result.getPermissions());
        assertEquals(Permission.ROLE_ASSIGN, permissionList.get(0));
        assertEquals(Permission.USER_READ, permissionList.get(1));
        assertEquals(Permission.USER_WRITE, permissionList.get(2));
    }

    @Test
    void toDto_WithDifferentRoleNames_MapsCorrectly() {
        // Test ADMIN
        RoleEntity adminRole = new RoleEntity();
        adminRole.setRoleName("ADMIN");
        Role adminDto = roleMapper.toDto(adminRole, List.of());
        assertEquals(RoleName.ADMIN, adminDto.getRoleName());

        // Test USER
        RoleEntity userRole = new RoleEntity();
        userRole.setRoleName("USER");
        Role userDto = roleMapper.toDto(userRole, List.of());
        assertEquals(RoleName.USER, userDto.getRoleName());

        // Test AUDITOR
        RoleEntity auditorRole = new RoleEntity();
        auditorRole.setRoleName("AUDITOR");
        Role auditorDto = roleMapper.toDto(auditorRole, List.of());
        assertEquals(RoleName.AUDITOR, auditorDto.getRoleName());
    }

    // =====================================
    // toDto (without permissions) tests
    // =====================================

    @Test
    void toDto_WithoutPermissions_ReturnsRoleWithEmptyPermissions() {
        // Given
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setId(UUID.randomUUID());
        roleEntity.setRoleName("USER");
        roleEntity.setDescription("User role");

        // When
        Role result = roleMapper.toDto(roleEntity);

        // Then
        assertNotNull(result);
        assertEquals(RoleName.USER, result.getRoleName());
        assertNotNull(result.getPermissions());
        assertTrue(result.getPermissions().isEmpty());
    }

    @Test
    void toDto_WithoutPermissions_DelegatesToMainMethod() {
        // Given
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setId(UUID.randomUUID());
        roleEntity.setRoleName("ADMIN");

        // When
        Role result = roleMapper.toDto(roleEntity);

        // Then
        assertNotNull(result);
        assertEquals(RoleName.ADMIN, result.getRoleName());
        assertEquals(0, result.getPermissions().size());
    }

    // =====================================
    // toPermissionDto tests
    // =====================================

    @Test
    void toPermissionDto_UserRead_ReturnsCorrectPermission() {
        // Given
        PermissionEntity permissionEntity = new PermissionEntity();
        permissionEntity.setId(UUID.randomUUID());
        permissionEntity.setPermission("USER_READ");
        permissionEntity.setDescription("Read users");

        // When
        Permission result = roleMapper.toPermissionDto(permissionEntity);

        // Then
        assertNotNull(result);
        assertEquals(Permission.USER_READ, result);
    }

    @Test
    void toPermissionDto_UserWrite_ReturnsCorrectPermission() {
        // Given
        PermissionEntity permissionEntity = new PermissionEntity();
        permissionEntity.setId(UUID.randomUUID());
        permissionEntity.setPermission("USER_WRITE");

        // When
        Permission result = roleMapper.toPermissionDto(permissionEntity);

        // Then
        assertEquals(Permission.USER_WRITE, result);
    }

    @Test
    void toPermissionDto_RoleAssign_ReturnsCorrectPermission() {
        // Given
        PermissionEntity permissionEntity = new PermissionEntity();
        permissionEntity.setId(UUID.randomUUID());
        permissionEntity.setPermission("ROLE_ASSIGN");

        // When
        Permission result = roleMapper.toPermissionDto(permissionEntity);

        // Then
        assertEquals(Permission.ROLE_ASSIGN, result);
    }

    @Test
    void toPermissionDto_MultipleConversions_ReturnsCorrectPermissions() {
        // Given
        PermissionEntity permission1 = new PermissionEntity();
        permission1.setPermission("USER_READ");

        PermissionEntity permission2 = new PermissionEntity();
        permission2.setPermission("USER_WRITE");

        PermissionEntity permission3 = new PermissionEntity();
        permission3.setPermission("ROLE_ASSIGN");

        // When & Then
        assertEquals(Permission.USER_READ, roleMapper.toPermissionDto(permission1));
        assertEquals(Permission.USER_WRITE, roleMapper.toPermissionDto(permission2));
        assertEquals(Permission.ROLE_ASSIGN, roleMapper.toPermissionDto(permission3));
    }

    // =====================================
    // Edge case tests
    // =====================================

    @Test
    void toDto_WithSinglePermission_ReturnsRoleWithOnePermission() {
        // Given
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setRoleName("AUDITOR");

        PermissionEntity permission = new PermissionEntity();
        permission.setPermission("USER_READ");

        // When
        Role result = roleMapper.toDto(roleEntity, List.of(permission));

        // Then
        assertEquals(1, result.getPermissions().size());
        assertTrue(result.getPermissions().contains(Permission.USER_READ));
    }

    @Test
    void toDto_EmptyPermissionsListExplicit_ReturnsRoleWithEmptyPermissions() {
        // Given
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setRoleName("USER");

        // When
        Role result = roleMapper.toDto(roleEntity, Collections.emptyList());

        // Then - should handle empty list
        assertNotNull(result);
        assertEquals(RoleName.USER, result.getRoleName());
        assertTrue(result.getPermissions().isEmpty());
    }

    @Test
    void toDto_DuplicatePermissions_ResultsInUniqueSet() {
        // Given
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setRoleName("ADMIN");

        PermissionEntity permission1 = new PermissionEntity();
        permission1.setId(UUID.randomUUID());
        permission1.setPermission("USER_READ");

        PermissionEntity permission2 = new PermissionEntity();
        permission2.setId(UUID.randomUUID());
        permission2.setPermission("USER_READ");  // Duplicate value

        List<PermissionEntity> permissions = List.of(permission1, permission2);

        // When
        Role result = roleMapper.toDto(roleEntity, permissions);

        // Then - Set should deduplicate based on enum equality
        assertNotNull(result);
        // Both entities map to same enum, so set will have only 1 entry
        assertEquals(1, result.getPermissions().size());
        assertTrue(result.getPermissions().contains(Permission.USER_READ));
    }

    @Test
    void toDto_MultipleCallsSameEntity_ReturnsConsistentResults() {
        // Given
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setId(UUID.randomUUID());
        roleEntity.setRoleName("ADMIN");

        PermissionEntity permission = new PermissionEntity();
        permission.setPermission("USER_READ");

        List<PermissionEntity> permissions = List.of(permission);

        // When
        Role result1 = roleMapper.toDto(roleEntity, permissions);
        Role result2 = roleMapper.toDto(roleEntity, permissions);

        // Then
        assertEquals(result1.getRoleName(), result2.getRoleName());
        assertEquals(result1.getPermissions().size(), result2.getPermissions().size());
    }

    @Test
    void toDto_RoleWithNoPermissions_ThenWithPermissions_IndependentResults() {
        // Given
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setRoleName("USER");

        PermissionEntity permission = new PermissionEntity();
        permission.setPermission("USER_READ");

        // When
        Role resultEmpty = roleMapper.toDto(roleEntity);
        Role resultWithPermission = roleMapper.toDto(roleEntity, List.of(permission));

        // Then
        assertTrue(resultEmpty.getPermissions().isEmpty());
        assertEquals(1, resultWithPermission.getPermissions().size());
    }
}

