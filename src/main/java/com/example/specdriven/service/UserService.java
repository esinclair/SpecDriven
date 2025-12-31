package com.example.specdriven.service;

import com.example.specdriven.api.model.CreateUserRequest;
import com.example.specdriven.api.model.UpdateUserRequest;
import com.example.specdriven.api.model.User;
import com.example.specdriven.domain.RoleEntity;
import com.example.specdriven.domain.UserEntity;
import com.example.specdriven.domain.UserRoleEntity;
import com.example.specdriven.exception.ConflictException;
import com.example.specdriven.exception.ResourceNotFoundException;
import com.example.specdriven.mapper.UserMapper;
import com.example.specdriven.repository.RoleRepository;
import com.example.specdriven.repository.UserRepository;
import com.example.specdriven.repository.UserRoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Service for managing user operations (CRUD).
 * Handles business logic, validation, and bootstrap mode.
 */
@Service
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    
    public UserService(UserRepository userRepository,
                      UserRoleRepository userRoleRepository,
                      RoleRepository roleRepository,
                      UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
        this.userMapper = userMapper;
    }
    
    /**
     * Create a new user.
     * Validates email uniqueness before creation.
     * 
     * @param request the create user request
     * @return the created user DTO
     * @throws ConflictException if email address already exists
     */
    public User createUser(CreateUserRequest request) {
        // Validate email uniqueness
        validateEmailUniqueness(request.getEmailAddress(), null);
        
        // Create and save user entity
        UserEntity entity = userMapper.toEntity(request);
        UserEntity saved = userRepository.save(entity);
        
        // Load roles (none for new user) and return DTO
        List<RoleEntity> roles = loadUserRoles(saved.getId());
        return userMapper.toDto(saved, roles);
    }
    
    /**
     * Get a user by ID.
     * 
     * @param userId the user ID
     * @return the user DTO with roles
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public User getUserById(UUID userId) {
        UserEntity entity = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        // Load user's roles
        List<RoleEntity> roles = loadUserRoles(userId);
        
        return userMapper.toDto(entity, roles);
    }
    
    /**
     * Update an existing user.
     * Only updates fields that are present in the request.
     * Validates email uniqueness if email is being changed.
     * 
     * @param userId the user ID
     * @param request the update request
     * @return the updated user DTO
     * @throws ResourceNotFoundException if user not found
     * @throws ConflictException if email address conflicts with another user
     */
    public User updateUser(UUID userId, UpdateUserRequest request) {
        // Find existing user
        UserEntity entity = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        // Validate email uniqueness if email is being changed
        if (request.getEmailAddress() != null &&
                !request.getEmailAddress().equals(entity.getEmailAddress())) {
            validateEmailUniqueness(request.getEmailAddress(), userId);
        }
        
        // Apply updates
        userMapper.updateEntity(request, entity);
        
        // Save updated entity
        UserEntity updated = userRepository.save(entity);
        
        // Load roles and return DTO
        List<RoleEntity> roles = loadUserRoles(userId);
        return userMapper.toDto(updated, roles);
    }
    
    /**
     * Delete a user by ID.
     * Role assignments are cascade deleted by database foreign key constraint.
     * 
     * @param userId the user ID
     * @throws ResourceNotFoundException if user not found
     */
    public void deleteUser(UUID userId) {
        // Verify user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        
        // Delete user (cascade deletes role assignments)
        userRepository.deleteById(userId);
    }
    
    /**
     * Check if system is in bootstrap mode (no users exist).
     * Bootstrap mode allows creating the first user without authentication.
     * 
     * @return true if no users exist
     */
    @Transactional(readOnly = true)
    public boolean isBootstrapMode() {
        return userRepository.count() == 0;
    }
    
    /**
     * Validate that an email address is not already taken by another user.
     * 
     * @param emailAddress the email to check
     * @param excludeUserId user ID to exclude from check (for updates), or null
     * @throws ConflictException if email is already taken
     */
    private void validateEmailUniqueness(String emailAddress, UUID excludeUserId) {
        Optional<UserEntity> existing = userRepository.findByEmailAddress(emailAddress);
        
        if (existing.isPresent()) {
            // If excluding a user ID (for updates), check it's not the same user
            if (excludeUserId == null || !existing.get().getId().equals(excludeUserId)) {
                throw new ConflictException("Email address already in use: " + emailAddress);
            }
        }
    }
    
    /**
     * Load all roles assigned to a user.
     * 
     * @param userId the user ID
     * @return list of role entities (may be empty)
     */
    private List<RoleEntity> loadUserRoles(UUID userId) {
        // Get all role IDs for this user
        Iterable<UserRoleEntity> userRoles = userRoleRepository.findByUserId(userId);
        List<UUID> roleIds = StreamSupport.stream(userRoles.spliterator(), false)
                .map(UserRoleEntity::getRoleId)
                .collect(Collectors.toList());
        
        // Load role entities
        if (roleIds.isEmpty()) {
            return List.of();
        }
        
        Iterable<RoleEntity> roles = roleRepository.findAllById(roleIds);
        return StreamSupport.stream(roles.spliterator(), false)
                .collect(Collectors.toList());
    }
}
