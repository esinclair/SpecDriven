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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    private static final int MAX_PAGE_SIZE = 100;

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

    /**
     * List users with pagination and optional filters.
     *
     * @param page 1-based page number
     * @param pageSize number of items per page
     * @param username optional exact username filter
     * @param emailAddress optional exact email filter
     * @param name optional case-insensitive partial name filter
     * @param roleName optional role filter
     * @return UserPage with matching users and pagination metadata
     * @throws ValidationException if pagination parameters are invalid
     */
    @Transactional(readOnly = true)
    public UserPage listUsers(Integer page, Integer pageSize, String username, 
                              String emailAddress, String name, RoleName roleName) {
        // Validate pagination parameters
        validatePaginationParams(page, pageSize);

        // Create pageable (Spring Data uses 0-based page index)
        Pageable pageable = PageRequest.of(page - 1, pageSize);

        Page<UserEntity> userPage;

        // Handle role filtering separately (requires joining user_roles table)
        if (roleName != null) {
            userPage = listUsersByRole(roleName.getValue(), username, emailAddress, name, pageable);
        } else {
            userPage = listUsersByFilters(username, emailAddress, name, pageable);
        }

        // Map to UserPage DTO
        return toUserPage(userPage, page, pageSize);
    }

    /**
     * Validate pagination parameters.
     */
    private void validatePaginationParams(Integer page, Integer pageSize) {
        if (page == null || page < 1) {
            throw new ValidationException("Page must be >= 1");
        }
        if (pageSize == null || pageSize < 1) {
            throw new ValidationException("Page size must be >= 1");
        }
        if (pageSize > MAX_PAGE_SIZE) {
            throw new ValidationException("Page size must be <= " + MAX_PAGE_SIZE);
        }
    }

    /**
     * List users by filters (without role filter).
     */
    private Page<UserEntity> listUsersByFilters(String username, String emailAddress, 
                                                 String name, Pageable pageable) {
        // Determine which query to use based on filters provided
        boolean hasUsername = username != null && !username.isEmpty();
        boolean hasEmail = emailAddress != null && !emailAddress.isEmpty();
        boolean hasName = name != null && !name.isEmpty();

        if (hasUsername && hasEmail && hasName) {
            return userRepository.findByUsernameAndEmailAddressAndNameContainingIgnoreCase(
                    username, emailAddress, name, pageable);
        } else if (hasUsername && hasEmail) {
            return userRepository.findByUsernameAndEmailAddress(username, emailAddress, pageable);
        } else if (hasUsername && hasName) {
            return userRepository.findByUsernameAndNameContainingIgnoreCase(username, name, pageable);
        } else if (hasEmail && hasName) {
            return userRepository.findByEmailAddressAndNameContainingIgnoreCase(emailAddress, name, pageable);
        } else if (hasUsername) {
            return userRepository.findByUsername(username, pageable);
        } else if (hasEmail) {
            return userRepository.findByEmailAddress(emailAddress, pageable);
        } else if (hasName) {
            return userRepository.findByNameContainingIgnoreCase(name, pageable);
        } else {
            return userRepository.findAll(pageable);
        }
    }

    /**
     * List users by role (with optional additional filters).
     */
    private Page<UserEntity> listUsersByRole(String roleName, String username,
                                              String emailAddress, String name, Pageable pageable) {
        // First, find the role
        RoleEntity role = roleRepository.findByRoleName(roleName)
                .orElse(null);
        
        if (role == null) {
            // Role doesn't exist, return empty page
            return Page.empty(pageable);
        }

        // Get user IDs with this role
        List<UserRoleEntity> userRoles = userRoleRepository.findByRoleId(role.getId());
        List<UUID> userIds = userRoles.stream()
                .map(UserRoleEntity::getUserId)
                .collect(Collectors.toList());

        if (userIds.isEmpty()) {
            return Page.empty(pageable);
        }

        // Get users by IDs with pagination
        Page<UserEntity> usersWithRole = userRepository.findByIdIn(userIds, pageable);

        // Apply additional filters in memory if necessary
        if (username != null || emailAddress != null || name != null) {
            List<UserEntity> filtered = usersWithRole.getContent().stream()
                    .filter(user -> matchesFilters(user, username, emailAddress, name))
                    .collect(Collectors.toList());
            
            // Calculate proper pagination offset (safely convert long to int)
            int start = Math.toIntExact(pageable.getOffset());
            int end = Math.min(filtered.size(), start + pageable.getPageSize());
            
            // Handle case where start exceeds filtered size
            List<UserEntity> pageContent;
            if (start >= filtered.size()) {
                pageContent = java.util.Collections.emptyList();
            } else {
                pageContent = filtered.subList(start, end);
            }
            
            return new org.springframework.data.domain.PageImpl<>(
                    pageContent, pageable, filtered.size());
        }

        return usersWithRole;
    }

    /**
     * Check if a user matches the given filters.
     */
    private boolean matchesFilters(UserEntity user, String username, 
                                   String emailAddress, String name) {
        if (username != null && !username.equals(user.getUsername())) {
            return false;
        }
        if (emailAddress != null && !emailAddress.equals(user.getEmailAddress())) {
            return false;
        }
        if (name != null && (user.getName() == null || 
                !user.getName().toLowerCase().contains(name.toLowerCase()))) {
            return false;
        }
        return true;
    }

    /**
     * Convert Page<UserEntity> to UserPage DTO.
     */
    private UserPage toUserPage(Page<UserEntity> page, Integer pageNum, Integer pageSize) {
        List<User> users = page.getContent().stream()
                .map(entity -> {
                    List<Role> roles = loadUserRoles(entity.getId());
                    return userMapper.toDto(entity, roles);
                })
                .collect(Collectors.toList());

        UserPage userPage = new UserPage();
        userPage.setItems(users);
        userPage.setPage(pageNum);
        userPage.setPageSize(pageSize);
        userPage.setTotalItems((int) page.getTotalElements());
        userPage.setTotalPages(page.getTotalPages());

        return userPage;
    }
}
