package com.example.specdriven.mapper;

import com.example.specdriven.api.model.CreateUserRequest;
import com.example.specdriven.api.model.Role;
import com.example.specdriven.api.model.UpdateUserRequest;
import com.example.specdriven.api.model.User;
import com.example.specdriven.domain.RoleEntity;
import com.example.specdriven.domain.UserEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mapper for converting between User API models (DTOs) and UserEntity domain models.
 * Handles password hashing and ensures passwords are never exposed in responses.
 */
@Component
public class UserMapper {
    
    private final PasswordEncoder passwordEncoder;
    private final RoleMapper roleMapper;
    
    public UserMapper(PasswordEncoder passwordEncoder, RoleMapper roleMapper) {
        this.passwordEncoder = passwordEncoder;
        this.roleMapper = roleMapper;
    }
    
    /**
     * Convert CreateUserRequest to UserEntity.
     * Hashes the password using BCrypt and generates a new UUID.
     * 
     * @param request the create user request
     * @return new user entity ready for persistence
     */
    public UserEntity toEntity(CreateUserRequest request) {
        UserEntity entity = new UserEntity();
        entity.setId(UUID.randomUUID());
        entity.setUsername(request.getUsername());
        entity.setName(request.getName());
        entity.setEmailAddress(request.getEmailAddress());
        
        // Hash password before storing
        entity.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        
        LocalDateTime now = LocalDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        
        return entity;
    }
    
    /**
     * Convert UserEntity to User DTO with roles.
     * Password is NEVER included in the response.
     * 
     * @param entity the user entity
     * @param roles the user's roles
     * @return user DTO for API response
     */
    public User toDto(UserEntity entity, List<RoleEntity> roles) {
        User dto = new User();
        dto.setId(entity.getId());
        dto.setUsername(entity.getUsername());
        dto.setName(entity.getName());
        dto.setEmailAddress(entity.getEmailAddress());
        
        // Map roles to DTOs
        List<Role> roleDtos = roles.stream()
                .map(roleMapper::toDto)
                .collect(Collectors.toList());
        dto.setRoles(roleDtos);
        
        // Password is NEVER mapped to DTO
        return dto;
    }
    
    /**
     * Apply updates from UpdateUserRequest to existing UserEntity.
     * Only updates fields that are present (not null) in the request.
     * Hashes password if provided.
     * 
     * @param request the update request
     * @param entity the existing entity to update
     */
    public void updateEntity(UpdateUserRequest request, UserEntity entity) {
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
            // Hash new password before storing
            entity.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        
        // Always update the updated_at timestamp
        entity.setUpdatedAt(LocalDateTime.now());
    }
}
