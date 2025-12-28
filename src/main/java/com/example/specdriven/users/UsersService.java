package com.example.specdriven.users;

import com.example.specdriven.api.model.CreateUserRequest;
import com.example.specdriven.api.model.RoleName;
import com.example.specdriven.api.model.UpdateUserRequest;
import com.example.specdriven.api.model.User;
import com.example.specdriven.api.model.UserPage;
import com.example.specdriven.error.ApiErrorCode;
import com.example.specdriven.error.ErrorResponseFactory;
import com.example.specdriven.users.persistence.UserEntity;
import com.example.specdriven.users.persistence.UserRepository;
import com.example.specdriven.users.persistence.UserRoleEntity;
import com.example.specdriven.users.persistence.UserRoleRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Business logic for user management operations.
 * T027-T028, T042-T043, T047-T050, T057, T081-T083: Service layer implementation.
 */
@Service
public class UsersService {
    
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final UsersMapper usersMapper;
    private final PasswordHasher passwordHasher;
    
    public UsersService(UserRepository userRepository,
                       UserRoleRepository userRoleRepository,
                       UsersMapper usersMapper,
                       PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.usersMapper = usersMapper;
        this.passwordHasher = passwordHasher;
    }
    
    /**
     * Create a new user.
     * T027: createUser with uniqueness handling.
     * @param request the create user request
     * @return the created user
     * @throws ResponseStatusException 409 if username or email already exists
     */
    @Transactional
    public User createUser(CreateUserRequest request) {
        // Check for duplicate username
        if (userRepository.existsByUsername(request.getUsername())) {
            throw ErrorResponseFactory.conflict(ApiErrorCode.RESOURCE_CONFLICT,
                "Username already exists: " + request.getUsername());
        }
        
        // Check for duplicate email
        if (userRepository.existsByEmailAddress(request.getEmailAddress())) {
            throw ErrorResponseFactory.conflict(ApiErrorCode.RESOURCE_CONFLICT,
                "Email address already exists: " + request.getEmailAddress());
        }
        
        // Hash password
        String hashedPassword = passwordHasher.hash(request.getPassword());
        
        // Create entity
        UserEntity entity = usersMapper.toEntity(request, hashedPassword);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        
        // Save user
        userRepository.save(entity);
        
        // Get roles (initially empty, unless default roles are specified)
        Set<UserRoleEntity> roles = userRoleRepository.findByUserId(entity.getId());
        
        // Return user model
        return usersMapper.toModel(entity, roles);
    }
    
    /**
     * Get a user by ID.
     * T028: getUserById with 404 handling.
     * @param userId the user ID
     * @return the user
     * @throws ResponseStatusException 404 if user not found
     */
    public User getUserById(UUID userId) {
        UserEntity entity = userRepository.findById(userId)
            .orElseThrow(() -> ErrorResponseFactory.notFound(ApiErrorCode.RESOURCE_NOT_FOUND,
                "User not found: " + userId));
        
        Set<UserRoleEntity> roles = userRoleRepository.findByUserId(userId);
        
        return usersMapper.toModel(entity, roles);
    }
    
    /**
     * Get a user by username (for authentication).
     * T049: getUserByUsername for authentication.
     * @param username the username
     * @return the user entity, or empty if not found
     */
    public Optional<UserEntity> getUserEntityByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    /**
     * Update a user.
     * T042: updateUser with validation and conflict handling.
     * @param userId the user ID
     * @param request the update request
     * @return the updated user
     * @throws ResponseStatusException 404 if user not found, 400 if no fields provided, 409 if conflict
     */
    @Transactional
    public User updateUser(UUID userId, UpdateUserRequest request) {
        // Validate at least one field is provided
        if (request.getName() == null && 
            request.getEmailAddress() == null && 
            request.getPassword() == null) {
            throw ErrorResponseFactory.badRequest(ApiErrorCode.VALIDATION_FAILED,
                "At least one field must be provided for update");
        }
        
        // Find existing user
        UserEntity entity = userRepository.findById(userId)
            .orElseThrow(() -> ErrorResponseFactory.notFound(ApiErrorCode.RESOURCE_NOT_FOUND,
                "User not found: " + userId));
        
        // Check for email conflict if email is being updated
        if (request.getEmailAddress() != null && 
            !request.getEmailAddress().equals(entity.getEmailAddress())) {
            if (userRepository.existsByEmailAddress(request.getEmailAddress())) {
                throw ErrorResponseFactory.conflict(ApiErrorCode.RESOURCE_CONFLICT,
                    "Email address already exists: " + request.getEmailAddress());
            }
        }
        
        // Hash password if being updated
        String hashedPassword = null;
        if (request.getPassword() != null) {
            hashedPassword = passwordHasher.hash(request.getPassword());
        }
        
        // Apply updates
        usersMapper.applyUpdate(entity, request, hashedPassword);
        entity.setUpdatedAt(LocalDateTime.now());
        
        // Save
        userRepository.save(entity);
        
        // Return updated user
        Set<UserRoleEntity> roles = userRoleRepository.findByUserId(userId);
        return usersMapper.toModel(entity, roles);
    }
    
    /**
     * Delete a user.
     * T043: deleteUser with 404 handling.
     * @param userId the user ID
     * @throws ResponseStatusException 404 if user not found
     */
    @Transactional
    public void deleteUser(UUID userId) {
        // Check if user exists
        if (!userRepository.existsById(userId)) {
            throw ErrorResponseFactory.notFound(ApiErrorCode.RESOURCE_NOT_FOUND,
                "User not found: " + userId);
        }
        
        // Delete roles first (foreign key constraint)
        userRoleRepository.deleteByUserId(userId);
        
        // Delete user
        userRepository.deleteById(userId);
    }
    
    /**
     * List users with pagination and filtering.
     * T081-T083: listUsers with pagination and filters.
     * @param page the page number (1-based)
     * @param pageSize the page size (1-100)
     * @param username optional exact username filter
     * @param emailAddress optional exact email filter
     * @param name optional partial name filter
     * @param roleName optional role filter
     * @return the user page
     * @throws ResponseStatusException 400 if pagination parameters invalid
     */
    public UserPage listUsers(Integer page, Integer pageSize, 
                             String username, String emailAddress, 
                             String name, RoleName roleName) {
        // Validate pagination parameters
        if (page == null || pageSize == null) {
            throw ErrorResponseFactory.badRequest(ApiErrorCode.VALIDATION_FAILED,
                "Page and pageSize parameters are required");
        }
        if (page < 1) {
            throw ErrorResponseFactory.badRequest(ApiErrorCode.VALIDATION_FAILED,
                "Page must be >= 1");
        }
        if (pageSize < 1 || pageSize > 100) {
            throw ErrorResponseFactory.badRequest(ApiErrorCode.VALIDATION_FAILED,
                "PageSize must be between 1 and 100");
        }
        
        // Build query based on filters
        List<UserEntity> allEntities;
        
        // Apply filters
        if (roleName != null) {
            // Filter by role - need to join with user_roles
            Set<UUID> userIds = userRoleRepository.findUserIdsByRoleName(roleName.getValue());
            allEntities = userRepository.findByIdIn(userIds);
        } else if (username != null) {
            // Exact username match
            allEntities = userRepository.findByUsernameContaining(username);
        } else if (emailAddress != null) {
            // Exact email match
            allEntities = userRepository.findByEmailAddressContaining(emailAddress);
        } else if (name != null) {
            // Partial name match (case-insensitive)
            allEntities = userRepository.findByNameContainingIgnoreCase(name);
        } else {
            // No filters
            allEntities = userRepository.findAllUsers();
        }
        
        // Sort by createdAt descending
        allEntities.sort((a, b) -> {
            if (a.getCreatedAt() == null || b.getCreatedAt() == null) return 0;
            return b.getCreatedAt().compareTo(a.getCreatedAt());
        });
        
        // Calculate pagination
        int totalElements = allEntities.size();
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalElements);
        
        // Get the page of entities
        List<UserEntity> pageEntities;
        if (startIndex >= totalElements) {
            pageEntities = new ArrayList<>();
        } else {
            pageEntities = allEntities.subList(startIndex, endIndex);
        }
        
        // Map to DTOs
        List<User> users = pageEntities.stream()
            .map(entity -> {
                Set<UserRoleEntity> roles = userRoleRepository.findByUserId(entity.getId());
                return usersMapper.toModel(entity, roles);
            })
            .collect(Collectors.toList());
        
        // Build page response
        UserPage userPage = new UserPage();
        userPage.setItems(users);
        userPage.setPage(page);
        userPage.setPageSize(pageSize);
        userPage.setTotalItems(totalElements);
        userPage.setTotalPages(totalPages);
        
        return userPage;
    }
    
    /**
     * Assign a role to a user (idempotent).
     * T057: assignRoleToUser with validation.
     * @param userId the user ID
     * @param roleName the role name
     * @throws ResponseStatusException 404 if user not found, 400 if invalid role
     */
    @Transactional
    public void assignRoleToUser(UUID userId, RoleName roleName) {
        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw ErrorResponseFactory.notFound(ApiErrorCode.RESOURCE_NOT_FOUND,
                "User not found: " + userId);
        }
        
        // Validate role name
        if (roleName == null) {
            throw ErrorResponseFactory.badRequest(ApiErrorCode.VALIDATION_FAILED,
                "Role name is required");
        }
        
        // Check if role already assigned (idempotent)
        if (userRoleRepository.existsByUserIdAndRoleName(userId, roleName.getValue())) {
            return; // Already assigned, nothing to do
        }
        
        // Assign role
        UserRoleEntity userRole = new UserRoleEntity();
        userRole.setUserId(userId);
        userRole.setRoleName(roleName.getValue());
        userRoleRepository.save(userRole);
    }
    
    /**
     * Remove a role from a user (idempotent).
     * T057: removeRoleFromUser with validation.
     * @param userId the user ID
     * @param roleName the role name
     * @throws ResponseStatusException 404 if user not found
     */
    @Transactional
    public void removeRoleFromUser(UUID userId, RoleName roleName) {
        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw ErrorResponseFactory.notFound(ApiErrorCode.RESOURCE_NOT_FOUND,
                "User not found: " + userId);
        }
        
        // Remove role (idempotent - no error if not assigned)
        userRoleRepository.deleteByUserIdAndRoleName(userId, roleName.getValue());
    }
    
    /**
     * Count total users.
     * T050: countUsers for bootstrap detection.
     * @return the number of users
     */
    public long countUsers() {
        return userRepository.count();
    }
}
