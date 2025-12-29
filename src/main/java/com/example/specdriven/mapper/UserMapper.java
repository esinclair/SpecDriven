package com.example.specdriven.mapper;

import com.example.specdriven.api.model.Role;
import com.example.specdriven.api.model.User;
import com.example.specdriven.domain.UserEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mapper for converting between User DTOs and UserEntity domain objects.
 */
@Component
public class UserMapper {
    
    private final PasswordEncoder passwordEncoder;
    private final RoleMapper roleMapper;
    
    public UserMapper(PasswordEncoder passwordEncoder, RoleMapper roleMapper) {
        this.passwordEncoder = passwordEncoder;
        this.roleMapper = roleMapper;
    }
    
    public User toDto(UserEntity entity) {
        User user = new User()
                .id(entity.getId())
                .username(entity.getUsername())
                .name(entity.getName())
                .emailAddress(entity.getEmailAddress());
        
        // Map roles if present
        if (entity.getRoles() != null) {
            List<Role> roles = entity.getRoles().stream()
                    .map(roleMapper::toDto)
                    .collect(Collectors.toList());
            user.setRoles(roles);
        }
        
        // Never include password in DTO
        return user;
    }
    
    public UserEntity toEntity(User dto, String plainPassword) {
        UserEntity entity = new UserEntity();
        entity.setId(dto.getId() != null ? dto.getId() : UUID.randomUUID());
        entity.setUsername(dto.getUsername());
        entity.setName(dto.getName());
        entity.setEmailAddress(dto.getEmailAddress());
        
        // Hash password if provided
        if (plainPassword != null) {
            entity.setPasswordHash(passwordEncoder.encode(plainPassword));
        }
        
        Instant now = Instant.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        
        return entity;
    }
    
    public void updateEntity(UserEntity entity, User dto, String plainPassword) {
        if (dto.getUsername() != null) {
            entity.setUsername(dto.getUsername());
        }
        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }
        if (dto.getEmailAddress() != null) {
            entity.setEmailAddress(dto.getEmailAddress());
        }
        if (plainPassword != null) {
            entity.setPasswordHash(passwordEncoder.encode(plainPassword));
        }
        entity.setUpdatedAt(Instant.now());
    }
}
