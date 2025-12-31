package com.example.specdriven.mapper;

import com.example.specdriven.api.model.*;
import com.example.specdriven.domain.RoleEntity;
import com.example.specdriven.domain.UserEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Mapper for converting between User API models (DTOs) and domain entities.
 * Handles password hashing during entity creation - passwords are NEVER returned in DTOs.
 */
@Component
public class UserMapper {

    private final PasswordEncoder passwordEncoder;

    public UserMapper(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Convert CreateUserRequest to UserEntity.
     * Hashes the password using BCrypt.
     *
     * @param request the create user request
     * @return new UserEntity with hashed password
     */
    public UserEntity toEntity(CreateUserRequest request) {
        UserEntity entity = new UserEntity();
        entity.setId(UUID.randomUUID());
        entity.setUsername(request.getUsername());
        entity.setName(request.getName());
        entity.setEmailAddress(request.getEmailAddress());
        entity.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        
        LocalDateTime now = LocalDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        
        return entity;
    }

    /**
     * Convert UserEntity to User DTO.
     * Password is NEVER included in the response.
     *
     * @param entity the user entity
     * @param roles the user's assigned roles (with permissions)
     * @return User DTO
     */
    public User toDto(UserEntity entity, List<Role> roles) {
        User user = new User();
        user.setId(entity.getId());
        user.setUsername(entity.getUsername());
        user.setName(entity.getName());
        user.setEmailAddress(entity.getEmailAddress());
        user.setRoles(roles != null ? roles : Collections.emptyList());
        // Note: Password is NEVER included in the response
        return user;
    }

    /**
     * Apply partial updates from UpdateUserRequest to existing UserEntity.
     * Only non-null fields are updated. Password is hashed if provided.
     *
     * @param request the update request
     * @param entity the existing entity to update
     * @return updated UserEntity
     */
    public UserEntity updateEntity(UpdateUserRequest request, UserEntity entity) {
        if (request.getUsername() != null) {
            entity.setUsername(request.getUsername());
        }
        if (request.getName() != null) {
            entity.setName(request.getName());
        }
        if (request.getEmailAddress() != null) {
            entity.setEmailAddress(request.getEmailAddress());
        }
        if (request.getPassword() != null) {
            entity.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        
        // Always update the timestamp on any update
        entity.setUpdatedAt(LocalDateTime.now());
        
        return entity;
    }

    /**
     * Convert RoleEntity to Role DTO.
     *
     * @param roleEntity the role entity
     * @param permissions the permissions for this role
     * @return Role DTO
     */
    public Role toRoleDto(RoleEntity roleEntity, Set<Permission> permissions) {
        Role role = new Role();
        role.setRoleName(RoleName.fromValue(roleEntity.getRoleName()));
        role.setPermissions(permissions != null ? permissions : new LinkedHashSet<>());
        return role;
    }

    /**
     * Convert RoleEntity to Role DTO with empty permissions.
     * Used when permissions are not loaded.
     *
     * @param roleEntity the role entity
     * @return Role DTO with empty permissions
     */
    public Role toRoleDto(RoleEntity roleEntity) {
        return toRoleDto(roleEntity, new LinkedHashSet<>());
    }
}
