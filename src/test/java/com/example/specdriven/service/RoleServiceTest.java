package com.example.specdriven.service;

import com.example.specdriven.api.model.RoleName;
import com.example.specdriven.domain.RoleEntity;
import com.example.specdriven.domain.UserRoleEntity;
import com.example.specdriven.exception.ResourceNotFoundException;
import com.example.specdriven.exception.ValidationException;
import com.example.specdriven.repository.RoleRepository;
import com.example.specdriven.repository.UserRepository;
import com.example.specdriven.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RoleService.
 * Tests role assignment and removal logic using mocks.
 */
@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    private RoleService roleService;

    @BeforeEach
    void setUp() {
        roleService = new RoleService(userRepository, roleRepository, userRoleRepository);
    }

    // Test: assignRole with valid user and role
    @Test
    void assignRole_ValidUserAndRole_CreatesAssignment() {
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        RoleEntity adminRole = new RoleEntity(roleId, "ADMIN", "Administrator role");

        when(userRepository.existsById(userId)).thenReturn(true);
        when(roleRepository.findByRoleName("ADMIN")).thenReturn(Optional.of(adminRole));
        when(userRoleRepository.findByUserIdAndRoleId(userId, roleId)).thenReturn(Collections.emptyList());
        when(userRoleRepository.save(any(UserRoleEntity.class))).thenAnswer(i -> i.getArgument(0));

        roleService.assignRole(userId, RoleName.ADMIN);

        ArgumentCaptor<UserRoleEntity> captor = ArgumentCaptor.forClass(UserRoleEntity.class);
        verify(userRoleRepository).save(captor.capture());
        
        UserRoleEntity saved = captor.getValue();
        assertEquals(userId, saved.getUserId());
        assertEquals(roleId, saved.getRoleId());
        assertNotNull(saved.getAssignedAt());
    }

    // Test: assignRole with non-existent user throws ResourceNotFoundException
    @Test
    void assignRole_UserNotFound_ThrowsResourceNotFoundException() {
        UUID userId = UUID.randomUUID();

        when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, 
                () -> roleService.assignRole(userId, RoleName.ADMIN));

        verify(userRoleRepository, never()).save(any());
    }

    // Test: assignRole when role is already assigned (idempotent)
    @Test
    void assignRole_AlreadyAssigned_DoesNotCreateDuplicate() {
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        RoleEntity adminRole = new RoleEntity(roleId, "ADMIN", "Administrator role");
        UserRoleEntity existingAssignment = new UserRoleEntity(userId, roleId, null);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(roleRepository.findByRoleName("ADMIN")).thenReturn(Optional.of(adminRole));
        when(userRoleRepository.findByUserIdAndRoleId(userId, roleId)).thenReturn(List.of(existingAssignment));

        roleService.assignRole(userId, RoleName.ADMIN);

        verify(userRoleRepository, never()).save(any());
    }

    // Test: assignRole with role not in database throws ValidationException
    @Test
    void assignRole_RoleNotInDatabase_ThrowsValidationException() {
        UUID userId = UUID.randomUUID();

        when(userRepository.existsById(userId)).thenReturn(true);
        when(roleRepository.findByRoleName("ADMIN")).thenReturn(Optional.empty());

        assertThrows(ValidationException.class, 
                () -> roleService.assignRole(userId, RoleName.ADMIN));

        verify(userRoleRepository, never()).save(any());
    }

    // Test: removeRole with valid user and assigned role
    @Test
    void removeRole_ValidUserAndRole_DeletesAssignment() {
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        RoleEntity userRole = new RoleEntity(roleId, "USER", "User role");

        when(userRepository.existsById(userId)).thenReturn(true);
        when(roleRepository.findByRoleName("USER")).thenReturn(Optional.of(userRole));

        roleService.removeRole(userId, RoleName.USER);

        verify(userRoleRepository).deleteByUserIdAndRoleId(userId, roleId);
    }

    // Test: removeRole with non-existent user throws ResourceNotFoundException
    @Test
    void removeRole_UserNotFound_ThrowsResourceNotFoundException() {
        UUID userId = UUID.randomUUID();

        when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, 
                () -> roleService.removeRole(userId, RoleName.ADMIN));

        verify(userRoleRepository, never()).deleteByUserIdAndRoleId(any(), any());
    }

    // Test: removeRole when role not assigned (idempotent)
    @Test
    void removeRole_NotAssigned_NoOp() {
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        RoleEntity adminRole = new RoleEntity(roleId, "ADMIN", "Administrator role");

        when(userRepository.existsById(userId)).thenReturn(true);
        when(roleRepository.findByRoleName("ADMIN")).thenReturn(Optional.of(adminRole));

        roleService.removeRole(userId, RoleName.ADMIN);

        // Should still call delete, but it's a no-op if not exists
        verify(userRoleRepository).deleteByUserIdAndRoleId(userId, roleId);
    }

    // Test: removeRole when role doesn't exist in database
    @Test
    void removeRole_RoleNotInDatabase_NoOp() {
        UUID userId = UUID.randomUUID();

        when(userRepository.existsById(userId)).thenReturn(true);
        when(roleRepository.findByRoleName("ADMIN")).thenReturn(Optional.empty());

        // Should not throw, just no-op
        roleService.removeRole(userId, RoleName.ADMIN);

        verify(userRoleRepository, never()).deleteByUserIdAndRoleId(any(), any());
    }
}
