package com.example.specdriven.users;

import com.example.specdriven.api.model.CreateUserRequest;
import com.example.specdriven.api.model.Role;
import com.example.specdriven.api.model.RoleName;
import com.example.specdriven.api.model.UpdateUserRequest;
import com.example.specdriven.api.model.User;
import com.example.specdriven.users.persistence.UserEntity;
import com.example.specdriven.users.persistence.UserRoleEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Maps between OpenAPI DTOs and persistence entities.
 * T025: Domain model and mappers.
 */
@Component
@ConditionalOnProperty(name = "feature-flag.users-api", havingValue = "true")
public class UsersMapper {
    
    /**
     * Convert a UserEntity to an OpenAPI User model.
     * Password is never included in the output.
     * @param entity the user entity
     * @param userRoles the user's roles
     * @return the User model
     */
    public User toModel(UserEntity entity, Set<UserRoleEntity> userRoles) {
        User user = new User();
        user.setId(entity.getId());
        user.setUsername(entity.getUsername());
        user.setName(entity.getName());
        user.setEmailAddress(entity.getEmailAddress());
        user.setCreatedAt(entity.getCreatedAt().atOffset(ZoneOffset.UTC));
        user.setUpdatedAt(entity.getUpdatedAt().atOffset(ZoneOffset.UTC));
        
        // Map roles
        List<Role> roles = new ArrayList<>();
        for (UserRoleEntity userRole : userRoles) {
            Role role = new Role();
            role.setRoleName(RoleName.fromValue(userRole.getRoleName()));
            // Set permissions based on role (business logic)
            role.setPermissions(getPermissionsForRole(userRole.getRoleName()));
            roles.add(role);
        }
        user.setRoles(roles);
        
        return user;
    }
    
    /**
     * Convert a CreateUserRequest to a UserEntity.
     * Password must be hashed before storing.
     * @param request the create user request
     * @param hashedPassword the hashed password
     * @return the user entity
     */
    public UserEntity toEntity(CreateUserRequest request, String hashedPassword) {
        UserEntity entity = new UserEntity();
        entity.setId(UUID.randomUUID());
        entity.setUsername(request.getUsername());
        entity.setName(request.getName());
        entity.setEmailAddress(request.getEmailAddress());
        entity.setPasswordHash(hashedPassword);
        return entity;
    }
    
    /**
     * Apply updates from an UpdateUserRequest to a UserEntity.
     * @param entity the entity to update
     * @param request the update request
     * @param hashedPassword the hashed password (if password is being updated)
     */
    public void applyUpdate(UserEntity entity, UpdateUserRequest request, String hashedPassword) {
        if (request.getName() != null) {
            entity.setName(request.getName());
        }
        if (request.getEmailAddress() != null) {
            entity.setEmailAddress(request.getEmailAddress());
        }
        if (hashedPassword != null) {
            entity.setPasswordHash(hashedPassword);
        }
    }
    
    /**
     * Get the permissions for a given role.
     * This implements the role-permission mapping from the spec.
     * @param roleName the role name
     * @return the set of permissions for that role
     */
    private Set<com.example.specdriven.api.model.Permission> getPermissionsForRole(String roleName) {
        // Based on the OpenAPI spec and typical RBAC patterns:
        // ADMIN: all permissions
        // USER: read-only
        // AUDITOR: read-only
        Set<com.example.specdriven.api.model.Permission> permissions = new HashSet<>();
        
        switch (roleName) {
            case "ADMIN":
                permissions.add(com.example.specdriven.api.model.Permission.USER_READ);
                permissions.add(com.example.specdriven.api.model.Permission.USER_WRITE);
                permissions.add(com.example.specdriven.api.model.Permission.ROLE_ASSIGN);
                break;
            case "USER":
            case "AUDITOR":
                permissions.add(com.example.specdriven.api.model.Permission.USER_READ);
                break;
            default:
                // Unknown role, no permissions
                break;
        }
        
        return permissions;
    }
}
