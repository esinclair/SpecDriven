package com.example.specdriven.service;

import com.example.specdriven.api.model.*;
import com.example.specdriven.domain.RoleEntity;
import com.example.specdriven.domain.UserEntity;
import com.example.specdriven.domain.UserRoleEntity;
import com.example.specdriven.exception.ConflictException;
import com.example.specdriven.exception.ResourceNotFoundException;
import com.example.specdriven.exception.ValidationException;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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

    // =====================================
    // listUsers tests
    // =====================================

    @Test
    void listUsers_NullPage_ThrowsValidationException() {
        ValidationException exception = assertThrows(ValidationException.class,
                () -> userService.listUsers(null, 10, null, null, null, null));
        assertTrue(exception.getMessage().contains("Page must be >= 1"));
    }

    @Test
    void listUsers_ZeroPage_ThrowsValidationException() {
        ValidationException exception = assertThrows(ValidationException.class,
                () -> userService.listUsers(0, 10, null, null, null, null));
        assertTrue(exception.getMessage().contains("Page must be >= 1"));
    }

    @Test
    void listUsers_NegativePage_ThrowsValidationException() {
        ValidationException exception = assertThrows(ValidationException.class,
                () -> userService.listUsers(-1, 10, null, null, null, null));
        assertTrue(exception.getMessage().contains("Page must be >= 1"));
    }

    @Test
    void listUsers_NullPageSize_ThrowsValidationException() {
        ValidationException exception = assertThrows(ValidationException.class,
                () -> userService.listUsers(1, null, null, null, null, null));
        assertTrue(exception.getMessage().contains("Page size must be >= 1"));
    }

    @Test
    void listUsers_ZeroPageSize_ThrowsValidationException() {
        ValidationException exception = assertThrows(ValidationException.class,
                () -> userService.listUsers(1, 0, null, null, null, null));
        assertTrue(exception.getMessage().contains("Page size must be >= 1"));
    }

    @Test
    void listUsers_NegativePageSize_ThrowsValidationException() {
        ValidationException exception = assertThrows(ValidationException.class,
                () -> userService.listUsers(1, -1, null, null, null, null));
        assertTrue(exception.getMessage().contains("Page size must be >= 1"));
    }

    @Test
    void listUsers_ExceedsMaxPageSize_ThrowsValidationException() {
        ValidationException exception = assertThrows(ValidationException.class,
                () -> userService.listUsers(1, 101, null, null, null, null));
        assertTrue(exception.getMessage().contains("Page size must be <= 100"));
    }

    @Test
    void listUsers_ValidParams_ReturnsUserPage() {
        // Given
        Page<UserEntity> entityPage = new org.springframework.data.domain.PageImpl<>(
                List.of(testUserEntity),
                PageRequest.of(0, 10),
                1
        );
        when(userRepository.findAll(any(Pageable.class))).thenReturn(entityPage);
        when(userRoleRepository.findByUserId(any())).thenReturn(Collections.emptyList());
        when(userMapper.toDto(any(UserEntity.class), anyList())).thenReturn(testUserDto);

        // When
        UserPage result = userService.listUsers(1, 10, null, null, null, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getPage());
        assertEquals(10, result.getPageSize());
    }

    @Test
    void listUsers_WithRoleFilter_ReturnsFilteredResults() {
        // Given
        UUID roleId = UUID.randomUUID();
        RoleEntity role = new RoleEntity(roleId, "USER", "User role");
        UserRoleEntity userRole = new UserRoleEntity(testUserEntity.getId(), roleId, LocalDateTime.now());
        
        Page<UserEntity> entityPage = new org.springframework.data.domain.PageImpl<>(
                List.of(testUserEntity),
                PageRequest.of(0, 10),
                1
        );
        
        when(roleRepository.findByRoleName("USER")).thenReturn(Optional.of(role));
        when(userRoleRepository.findByRoleId(roleId)).thenReturn(List.of(userRole));
        when(userRepository.findByIdIn(anyList(), any(Pageable.class))).thenReturn(entityPage);
        when(userRoleRepository.findByUserId(any())).thenReturn(Collections.emptyList());
        when(userMapper.toDto(any(UserEntity.class), anyList())).thenReturn(testUserDto);

        // When
        UserPage result = userService.listUsers(1, 10, null, null, null, RoleName.USER);

        // Then
        assertNotNull(result);
    }

    @Test
    void listUsers_WithNonExistentRole_ReturnsEmptyPage() {
        // Given
        when(roleRepository.findByRoleName("ADMIN")).thenReturn(Optional.empty());

        // When
        UserPage result = userService.listUsers(1, 10, null, null, null, RoleName.ADMIN);

        // Then
        assertNotNull(result);
        assertTrue(result.getItems().isEmpty());
    }

    @Test
    void listUsers_WithRoleAndAdditionalFilters_AppliesFilters() {
        // Given
        UUID roleId = UUID.randomUUID();
        RoleEntity role = new RoleEntity(roleId, "USER", "User role");
        UserRoleEntity userRole = new UserRoleEntity(testUserEntity.getId(), roleId, LocalDateTime.now());
        
        Page<UserEntity> entityPage = new org.springframework.data.domain.PageImpl<>(
                List.of(testUserEntity),
                PageRequest.of(0, 10),
                1
        );
        
        when(roleRepository.findByRoleName("USER")).thenReturn(Optional.of(role));
        when(userRoleRepository.findByRoleId(roleId)).thenReturn(List.of(userRole));
        when(userRepository.findByIdIn(anyList(), any(Pageable.class))).thenReturn(entityPage);
        when(userRoleRepository.findByUserId(any())).thenReturn(Collections.emptyList());
        when(userMapper.toDto(any(UserEntity.class), anyList())).thenReturn(testUserDto);

        // When - with username filter that matches
        UserPage result = userService.listUsers(1, 10, "testuser", null, null, RoleName.USER);

        // Then
        assertNotNull(result);
    }

    @Test
    void listUsers_WithRoleAndNonMatchingFilters_ReturnsEmptyResults() {
        // Given
        UUID roleId = UUID.randomUUID();
        RoleEntity role = new RoleEntity(roleId, "USER", "User role");
        UserRoleEntity userRole = new UserRoleEntity(testUserEntity.getId(), roleId, LocalDateTime.now());
        
        Page<UserEntity> entityPage = new org.springframework.data.domain.PageImpl<>(
                List.of(testUserEntity),
                PageRequest.of(0, 10),
                1
        );
        
        when(roleRepository.findByRoleName("USER")).thenReturn(Optional.of(role));
        when(userRoleRepository.findByRoleId(roleId)).thenReturn(List.of(userRole));
        when(userRepository.findByIdIn(anyList(), any(Pageable.class))).thenReturn(entityPage);

        // When - with username filter that doesn't match
        UserPage result = userService.listUsers(1, 10, "nonexistent", null, null, RoleName.USER);

        // Then
        assertNotNull(result);
        assertTrue(result.getItems().isEmpty());
    }

    @Test
    void listUsers_WithUsernameFilter_CallsCorrectRepository() {
        // Given
        Page<UserEntity> entityPage = new org.springframework.data.domain.PageImpl<>(
                Collections.emptyList(), PageRequest.of(0, 10), 0);
        when(userRepository.findByUsername(anyString(), any(Pageable.class))).thenReturn(entityPage);

        // When
        userService.listUsers(1, 10, "testuser", null, null, null);

        // Then
        verify(userRepository).findByUsername(eq("testuser"), any(Pageable.class));
    }

    @Test
    void listUsers_WithEmailFilter_CallsCorrectRepository() {
        // Given
        Page<UserEntity> entityPage = new org.springframework.data.domain.PageImpl<>(
                Collections.emptyList(), PageRequest.of(0, 10), 0);
        when(userRepository.findByEmailAddress(anyString(), any(Pageable.class))).thenReturn(entityPage);

        // When
        userService.listUsers(1, 10, null, "test@example.com", null, null);

        // Then
        verify(userRepository).findByEmailAddress(eq("test@example.com"), any(Pageable.class));
    }

    @Test
    void listUsers_WithNameFilter_CallsCorrectRepository() {
        // Given
        Page<UserEntity> entityPage = new org.springframework.data.domain.PageImpl<>(
                Collections.emptyList(), PageRequest.of(0, 10), 0);
        when(userRepository.findByNameContainingIgnoreCase(anyString(), any(Pageable.class))).thenReturn(entityPage);

        // When
        userService.listUsers(1, 10, null, null, "Test", null);

        // Then
        verify(userRepository).findByNameContainingIgnoreCase(eq("Test"), any(Pageable.class));
    }

    @Test
    void listUsers_WithUsernameAndEmailFilters_CallsCorrectRepository() {
        // Given
        Page<UserEntity> entityPage = new org.springframework.data.domain.PageImpl<>(
                Collections.emptyList(), PageRequest.of(0, 10), 0);
        when(userRepository.findByUsernameAndEmailAddress(anyString(), anyString(), any(Pageable.class)))
                .thenReturn(entityPage);

        // When
        userService.listUsers(1, 10, "testuser", "test@example.com", null, null);

        // Then
        verify(userRepository).findByUsernameAndEmailAddress(eq("testuser"), eq("test@example.com"), any(Pageable.class));
    }

    @Test
    void listUsers_WithUsernameAndNameFilters_CallsCorrectRepository() {
        // Given
        Page<UserEntity> entityPage = new org.springframework.data.domain.PageImpl<>(
                Collections.emptyList(), PageRequest.of(0, 10), 0);
        when(userRepository.findByUsernameAndNameContainingIgnoreCase(anyString(), anyString(), any(Pageable.class)))
                .thenReturn(entityPage);

        // When
        userService.listUsers(1, 10, "testuser", null, "Test", null);

        // Then
        verify(userRepository).findByUsernameAndNameContainingIgnoreCase(eq("testuser"), eq("Test"), any(Pageable.class));
    }

    @Test
    void listUsers_WithEmailAndNameFilters_CallsCorrectRepository() {
        // Given
        Page<UserEntity> entityPage = new org.springframework.data.domain.PageImpl<>(
                Collections.emptyList(), PageRequest.of(0, 10), 0);
        when(userRepository.findByEmailAddressAndNameContainingIgnoreCase(anyString(), anyString(), any(Pageable.class)))
                .thenReturn(entityPage);

        // When
        userService.listUsers(1, 10, null, "test@example.com", "Test", null);

        // Then
        verify(userRepository).findByEmailAddressAndNameContainingIgnoreCase(eq("test@example.com"), eq("Test"), any(Pageable.class));
    }

    @Test
    void listUsers_WithAllFilters_CallsCorrectRepository() {
        // Given
        Page<UserEntity> entityPage = new org.springframework.data.domain.PageImpl<>(
                Collections.emptyList(), PageRequest.of(0, 10), 0);
        when(userRepository.findByUsernameAndEmailAddressAndNameContainingIgnoreCase(
                anyString(), anyString(), anyString(), any(Pageable.class)))
                .thenReturn(entityPage);

        // When
        userService.listUsers(1, 10, "testuser", "test@example.com", "Test", null);

        // Then
        verify(userRepository).findByUsernameAndEmailAddressAndNameContainingIgnoreCase(
                eq("testuser"), eq("test@example.com"), eq("Test"), any(Pageable.class));
    }

    @Test
    void listUsers_WithRoleButNoUsersWithRole_ReturnsEmptyPage() {
        // Given
        UUID roleId = UUID.randomUUID();
        RoleEntity role = new RoleEntity(roleId, "ADMIN", "Admin role");
        
        when(roleRepository.findByRoleName("ADMIN")).thenReturn(Optional.of(role));
        when(userRoleRepository.findByRoleId(roleId)).thenReturn(Collections.emptyList());

        // When
        UserPage result = userService.listUsers(1, 10, null, null, null, RoleName.ADMIN);

        // Then
        assertNotNull(result);
        assertTrue(result.getItems().isEmpty());
    }

    @Test
    void updateUser_ChangingEmailToOwnedEmail_DoesNotThrowConflict() {
        // Given - test the validateEmailUniqueness excludeUserId branch
        UUID userId = testUserEntity.getId();

        UpdateUserRequest updateEmailRequest = new UpdateUserRequest();
        updateEmailRequest.setEmailAddress("new@example.com");

        UserEntity updatedEntity = new UserEntity();
        updatedEntity.setId(userId);
        updatedEntity.setEmailAddress("new@example.com");

        User updatedDto = new User();
        updatedDto.setId(userId);
        updatedDto.setRoles(Collections.emptyList());

        // The email "new@example.com" exists but belongs to THIS user (same ID)
        UserEntity sameUser = new UserEntity();
        sameUser.setId(userId); // Same ID as the user being updated
        sameUser.setEmailAddress("new@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUserEntity));
        when(userRepository.findByEmailAddress("new@example.com")).thenReturn(Optional.of(sameUser));
        when(userMapper.updateEntity(updateEmailRequest, testUserEntity)).thenReturn(updatedEntity);
        when(userRepository.save(updatedEntity)).thenReturn(updatedEntity);
        when(userRoleRepository.findByUserId(userId)).thenReturn(Collections.emptyList());
        when(userMapper.toDto(updatedEntity, Collections.emptyList())).thenReturn(updatedDto);

        // When
        User result = userService.updateUser(userId, updateEmailRequest);

        // Then - should NOT throw conflict, because the email belongs to the same user
        assertNotNull(result);
    }

    @Test
    void listUsers_WithEmptyStrings_UsesCorrectFilters() {
        // Given - empty strings should NOT count as filters
        Page<UserEntity> entityPage = new org.springframework.data.domain.PageImpl<>(
                Collections.emptyList(), PageRequest.of(0, 10), 0);
        when(userRepository.findAll(any(Pageable.class))).thenReturn(entityPage);

        // When - empty strings should be treated as null (no filter)
        userService.listUsers(1, 10, "", "", "", null);

        // Then
        verify(userRepository).findAll(any(Pageable.class));
        verify(userRepository, never()).findByUsername(anyString(), any(Pageable.class));
    }

    @Test
    void listUsers_WithRoleAndEmailFilter_FiltersCorrectly() {
        // Given - tests email filter branch in matchesFilters
        UUID roleId = UUID.randomUUID();
        RoleEntity role = new RoleEntity(roleId, "USER", "User role");
        UserRoleEntity userRole = new UserRoleEntity(testUserEntity.getId(), roleId, LocalDateTime.now());
        
        Page<UserEntity> entityPage = new org.springframework.data.domain.PageImpl<>(
                List.of(testUserEntity),
                PageRequest.of(0, 10),
                1
        );
        
        when(roleRepository.findByRoleName("USER")).thenReturn(Optional.of(role));
        when(userRoleRepository.findByRoleId(roleId)).thenReturn(List.of(userRole));
        when(userRepository.findByIdIn(anyList(), any(Pageable.class))).thenReturn(entityPage);

        // When - with email filter that doesn't match
        UserPage result = userService.listUsers(1, 10, null, "other@example.com", null, RoleName.USER);

        // Then
        assertNotNull(result);
        assertTrue(result.getItems().isEmpty());
    }

    @Test
    void listUsers_WithRoleAndNameFilter_FiltersCorrectly() {
        // Given - tests name filter branch in matchesFilters
        UUID roleId = UUID.randomUUID();
        RoleEntity role = new RoleEntity(roleId, "USER", "User role");
        UserRoleEntity userRole = new UserRoleEntity(testUserEntity.getId(), roleId, LocalDateTime.now());
        
        Page<UserEntity> entityPage = new org.springframework.data.domain.PageImpl<>(
                List.of(testUserEntity),
                PageRequest.of(0, 10),
                1
        );
        
        when(roleRepository.findByRoleName("USER")).thenReturn(Optional.of(role));
        when(userRoleRepository.findByRoleId(roleId)).thenReturn(List.of(userRole));
        when(userRepository.findByIdIn(anyList(), any(Pageable.class))).thenReturn(entityPage);

        // When - with name filter that doesn't match
        UserPage result = userService.listUsers(1, 10, null, null, "NonExistent", RoleName.USER);

        // Then
        assertNotNull(result);
        assertTrue(result.getItems().isEmpty());
    }

    @Test
    void listUsers_WithRoleAndMatchingName_ReturnsResults() {
        // Given - tests name filter branch in matchesFilters when it matches
        UUID roleId = UUID.randomUUID();
        RoleEntity role = new RoleEntity(roleId, "USER", "User role");
        UserRoleEntity userRole = new UserRoleEntity(testUserEntity.getId(), roleId, LocalDateTime.now());
        
        Page<UserEntity> entityPage = new org.springframework.data.domain.PageImpl<>(
                List.of(testUserEntity),
                PageRequest.of(0, 10),
                1
        );
        
        when(roleRepository.findByRoleName("USER")).thenReturn(Optional.of(role));
        when(userRoleRepository.findByRoleId(roleId)).thenReturn(List.of(userRole));
        when(userRepository.findByIdIn(anyList(), any(Pageable.class))).thenReturn(entityPage);
        when(userRoleRepository.findByUserId(any())).thenReturn(Collections.emptyList());
        when(userMapper.toDto(any(UserEntity.class), anyList())).thenReturn(testUserDto);

        // When - with name filter that matches (case-insensitive)
        UserPage result = userService.listUsers(1, 10, null, null, "test", RoleName.USER);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getItems().size());
    }

    @Test
    void listUsers_WithRoleAndNullUserName_FiltersCorrectly() {
        // Given - tests the null name branch in matchesFilters
        UUID roleId = UUID.randomUUID();
        RoleEntity role = new RoleEntity(roleId, "USER", "User role");
        
        UserEntity userWithNullName = new UserEntity();
        userWithNullName.setId(UUID.randomUUID());
        userWithNullName.setUsername("noname");
        userWithNullName.setEmailAddress("noname@example.com");
        userWithNullName.setName(null); // null name
        
        UserRoleEntity userRole = new UserRoleEntity(userWithNullName.getId(), roleId, LocalDateTime.now());
        
        Page<UserEntity> entityPage = new org.springframework.data.domain.PageImpl<>(
                List.of(userWithNullName),
                PageRequest.of(0, 10),
                1
        );
        
        when(roleRepository.findByRoleName("USER")).thenReturn(Optional.of(role));
        when(userRoleRepository.findByRoleId(roleId)).thenReturn(List.of(userRole));
        when(userRepository.findByIdIn(anyList(), any(Pageable.class))).thenReturn(entityPage);

        // When - with name filter, user has null name
        UserPage result = userService.listUsers(1, 10, null, null, "test", RoleName.USER);

        // Then - should be filtered out
        assertNotNull(result);
        assertTrue(result.getItems().isEmpty());
    }
}
