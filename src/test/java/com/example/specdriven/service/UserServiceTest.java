package com.example.specdriven.service;

import com.example.specdriven.api.model.*;
import com.example.specdriven.domain.RoleEntity;
import com.example.specdriven.domain.UserEntity;
import com.example.specdriven.domain.UserRoleEntity;
import com.example.specdriven.exception.ConflictException;
import com.example.specdriven.exception.ResourceNotFoundException;
import com.example.specdriven.mapper.UserMapper;
import com.example.specdriven.repository.RoleRepository;
import com.example.specdriven.repository.UserRepository;
import com.example.specdriven.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService.
 * Tests business logic using mocked dependencies.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private UserEntity testUserEntity;
    private User testUserDto;
    private CreateUserRequest createRequest;
    private UpdateUserRequest updateRequest;

    @BeforeEach
    void setUp() {
        testUserEntity = new UserEntity();
        testUserEntity.setId(UUID.randomUUID());
        testUserEntity.setUsername("testuser");
        testUserEntity.setName("Test User");
        testUserEntity.setEmailAddress("test@example.com");
        testUserEntity.setPasswordHash("$2a$10$hashedpassword");
        testUserEntity.setCreatedAt(LocalDateTime.now());
        testUserEntity.setUpdatedAt(LocalDateTime.now());

        testUserDto = new User();
        testUserDto.setId(testUserEntity.getId());
        testUserDto.setUsername(testUserEntity.getUsername());
        testUserDto.setName(testUserEntity.getName());
        testUserDto.setEmailAddress(testUserEntity.getEmailAddress());
        testUserDto.setRoles(Collections.emptyList());

        createRequest = new CreateUserRequest(
                "testuser", "Test User", "Password123!", "test@example.com");

        updateRequest = new UpdateUserRequest();
        updateRequest.setName("Updated Name");
    }

    // =====================================
    // createUser tests
    // =====================================

    @Test
    void createUser_ValidRequest_ReturnsUser() {
        // Given
        when(userRepository.findByEmailAddress(createRequest.getEmailAddress()))
                .thenReturn(Optional.empty());
        when(userMapper.toEntity(createRequest)).thenReturn(testUserEntity);
        when(userRepository.save(testUserEntity)).thenReturn(testUserEntity);
        when(userMapper.toDto(testUserEntity, Collections.emptyList())).thenReturn(testUserDto);

        // When
        User result = userService.createUser(createRequest);

        // Then
        assertNotNull(result);
        assertEquals(testUserEntity.getId(), result.getId());
        verify(userRepository).save(testUserEntity);
    }

    @Test
    void createUser_DuplicateEmail_ThrowsConflictException() {
        // Given
        when(userRepository.findByEmailAddress(createRequest.getEmailAddress()))
                .thenReturn(Optional.of(testUserEntity));

        // When/Then
        ConflictException exception = assertThrows(ConflictException.class,
                () -> userService.createUser(createRequest));
        assertTrue(exception.getMessage().contains("Email address already exists"));
        verify(userRepository, never()).save(any());
    }

    // =====================================
    // getUserById tests
    // =====================================

    @Test
    void getUserById_ExistingUser_ReturnsUser() {
        // Given
        UUID userId = testUserEntity.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUserEntity));
        when(userRoleRepository.findByUserId(userId)).thenReturn(Collections.emptyList());
        when(userMapper.toDto(testUserEntity, Collections.emptyList())).thenReturn(testUserDto);

        // When
        User result = userService.getUserById(userId);

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getId());
    }

    @Test
    void getUserById_NonExistentUser_ThrowsResourceNotFoundException() {
        // Given
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When/Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> userService.getUserById(userId));
        assertTrue(exception.getMessage().contains("User not found"));
    }

    @Test
    void getUserById_UserWithRoles_ReturnsUserWithRoles() {
        // Given
        UUID userId = testUserEntity.getId();
        UUID roleId = UUID.randomUUID();

        UserRoleEntity userRole = new UserRoleEntity();
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);

        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setId(roleId);
        roleEntity.setRoleName("USER");

        Role roleDto = new Role();
        roleDto.setRoleName(RoleName.USER);
        roleDto.setPermissions(new LinkedHashSet<>());

        User userWithRoles = new User();
        userWithRoles.setId(userId);
        userWithRoles.setRoles(List.of(roleDto));

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUserEntity));
        when(userRoleRepository.findByUserId(userId)).thenReturn(List.of(userRole));
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(roleEntity));
        when(userMapper.toRoleDto(roleEntity)).thenReturn(roleDto);
        when(userMapper.toDto(testUserEntity, List.of(roleDto))).thenReturn(userWithRoles);

        // When
        User result = userService.getUserById(userId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getRoles().size());
        assertEquals(RoleName.USER, result.getRoles().get(0).getRoleName());
    }

    // =====================================
    // updateUser tests
    // =====================================

    @Test
    void updateUser_ValidRequest_ReturnsUpdatedUser() {
        // Given
        UUID userId = testUserEntity.getId();
        UserEntity updatedEntity = new UserEntity();
        updatedEntity.setId(userId);
        updatedEntity.setUsername("testuser");
        updatedEntity.setName("Updated Name");
        updatedEntity.setEmailAddress("test@example.com");

        User updatedDto = new User();
        updatedDto.setId(userId);
        updatedDto.setName("Updated Name");
        updatedDto.setRoles(Collections.emptyList());

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUserEntity));
        when(userMapper.updateEntity(updateRequest, testUserEntity)).thenReturn(updatedEntity);
        when(userRepository.save(updatedEntity)).thenReturn(updatedEntity);
        when(userRoleRepository.findByUserId(userId)).thenReturn(Collections.emptyList());
        when(userMapper.toDto(updatedEntity, Collections.emptyList())).thenReturn(updatedDto);

        // When
        User result = userService.updateUser(userId, updateRequest);

        // Then
        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        verify(userRepository).save(updatedEntity);
    }

    @Test
    void updateUser_NonExistentUser_ThrowsResourceNotFoundException() {
        // Given
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When/Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> userService.updateUser(userId, updateRequest));
        assertTrue(exception.getMessage().contains("User not found"));
    }

    @Test
    void updateUser_DuplicateEmail_ThrowsConflictException() {
        // Given
        UUID userId = testUserEntity.getId();
        UUID otherUserId = UUID.randomUUID();

        UserEntity otherUser = new UserEntity();
        otherUser.setId(otherUserId);
        otherUser.setEmailAddress("other@example.com");

        UpdateUserRequest emailUpdateRequest = new UpdateUserRequest();
        emailUpdateRequest.setEmailAddress("other@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUserEntity));
        when(userRepository.findByEmailAddress("other@example.com"))
                .thenReturn(Optional.of(otherUser));

        // When/Then
        ConflictException exception = assertThrows(ConflictException.class,
                () -> userService.updateUser(userId, emailUpdateRequest));
        assertTrue(exception.getMessage().contains("Email address already exists"));
    }

    @Test
    void updateUser_SameEmail_DoesNotThrowConflict() {
        // Given
        UUID userId = testUserEntity.getId();

        UpdateUserRequest sameEmailRequest = new UpdateUserRequest();
        sameEmailRequest.setEmailAddress("test@example.com"); // Same as existing

        UserEntity updatedEntity = new UserEntity();
        updatedEntity.setId(userId);
        updatedEntity.setEmailAddress("test@example.com");

        User updatedDto = new User();
        updatedDto.setId(userId);
        updatedDto.setRoles(Collections.emptyList());

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUserEntity));
        // Note: findByEmailAddress is not called when email isn't changing
        when(userMapper.updateEntity(sameEmailRequest, testUserEntity)).thenReturn(updatedEntity);
        when(userRepository.save(updatedEntity)).thenReturn(updatedEntity);
        when(userRoleRepository.findByUserId(userId)).thenReturn(Collections.emptyList());
        when(userMapper.toDto(updatedEntity, Collections.emptyList())).thenReturn(updatedDto);

        // When
        User result = userService.updateUser(userId, sameEmailRequest);

        // Then
        assertNotNull(result);
        // Verify that findByEmailAddress was NOT called (email not changing)
        verify(userRepository, never()).findByEmailAddress(anyString());
    }

    // =====================================
    // deleteUser tests
    // =====================================

    @Test
    void deleteUser_ExistingUser_DeletesUser() {
        // Given
        UUID userId = testUserEntity.getId();
        when(userRepository.existsById(userId)).thenReturn(true);

        // When
        userService.deleteUser(userId);

        // Then
        verify(userRepository).deleteById(userId);
    }

    @Test
    void deleteUser_NonExistentUser_ThrowsResourceNotFoundException() {
        // Given
        UUID userId = UUID.randomUUID();
        when(userRepository.existsById(userId)).thenReturn(false);

        // When/Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> userService.deleteUser(userId));
        assertTrue(exception.getMessage().contains("User not found"));
        verify(userRepository, never()).deleteById(any());
    }
}
