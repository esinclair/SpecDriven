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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for user management operations (CRUD).
 * Contains business logic for creating, retrieving, updating, and deleting users.
 */
@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository,
                      RoleRepository roleRepository,
                      UserRoleRepository userRoleRepository,
                      UserMapper userMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.userMapper = userMapper;
    }

    /**
     * Create a new user.
     * Validates email uniqueness before creation.
     *
     * @param request the create user request
     * @return the created User DTO
     * @throws ConflictException if email address already exists
     */
    @Transactional
    public User createUser(CreateUserRequest request) {
        // Validate email uniqueness
        validateEmailUniqueness(request.getEmailAddress(), null);

        // Create entity and save
        UserEntity entity = userMapper.toEntity(request);
        UserEntity savedEntity = userRepository.save(entity);

        logger.info("Created user with ID: {}", savedEntity.getId());

        // Return DTO with empty roles (new user has no roles)
        return userMapper.toDto(savedEntity, Collections.emptyList());
    }

    /**
     * Get a user by ID.
     * Loads the user's roles as well.
     *
     * @param userId the user ID
     * @return the User DTO with roles
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public User getUserById(UUID userId) {
        UserEntity entity = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Load user's roles
        List<Role> roles = loadUserRoles(userId);

        return userMapper.toDto(entity, roles);
    }

    /**
     * Update an existing user.
     * Validates email uniqueness if email is being changed.
     *
     * @param userId the user ID
     * @param request the update request
     * @return the updated User DTO
     * @throws ResourceNotFoundException if user not found
     * @throws ConflictException if new email already exists
     */
    @Transactional
    public User updateUser(UUID userId, UpdateUserRequest request) {
        UserEntity entity = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Validate email uniqueness if email is being changed
        if (request.getEmailAddress() != null && 
            !request.getEmailAddress().equals(entity.getEmailAddress())) {
            validateEmailUniqueness(request.getEmailAddress(), userId);
        }

        // Apply updates
        UserEntity updatedEntity = userMapper.updateEntity(request, entity);
        UserEntity savedEntity = userRepository.save(updatedEntity);

        logger.info("Updated user with ID: {}", userId);

        // Load and return with roles
        List<Role> roles = loadUserRoles(userId);
        return userMapper.toDto(savedEntity, roles);
    }

    /**
     * Delete a user by ID.
     * Role assignments are deleted automatically via database FK cascade.
     *
     * @param userId the user ID
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional
    public void deleteUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }

        userRepository.deleteById(userId);
        logger.info("Deleted user with ID: {}", userId);
    }

    /**
     * Validate that an email address is unique.
     *
     * @param emailAddress the email to check
     * @param excludeUserId optional user ID to exclude (for updates)
     * @throws ConflictException if email already exists
     */
    private void validateEmailUniqueness(String emailAddress, UUID excludeUserId) {
        Optional<UserEntity> existingUser = userRepository.findByEmailAddress(emailAddress);
        
        if (existingUser.isPresent()) {
            // If updating, allow the same user to keep their email
            if (excludeUserId != null && existingUser.get().getId().equals(excludeUserId)) {
                return;
            }
            throw new ConflictException("Email address already exists: " + emailAddress);
        }
    }

    /**
     * Load roles for a user.
     *
     * @param userId the user ID
     * @return list of Role DTOs
     */
    private List<Role> loadUserRoles(UUID userId) {
        List<UserRoleEntity> userRoles = userRoleRepository.findByUserId(userId);
        
        return userRoles.stream()
                .map(userRole -> {
                    Optional<RoleEntity> roleEntity = roleRepository.findById(userRole.getRoleId());
                    return roleEntity.map(userMapper::toRoleDto).orElse(null);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
