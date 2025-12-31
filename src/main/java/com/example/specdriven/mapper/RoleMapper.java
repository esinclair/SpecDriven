package com.example.specdriven.mapper;

import com.example.specdriven.api.model.Permission;
import com.example.specdriven.api.model.Role;
import com.example.specdriven.api.model.RoleName;
import com.example.specdriven.domain.PermissionEntity;
import com.example.specdriven.domain.RoleEntity;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper for converting between Role API models and RoleEntity domain models.
 */
@Component
public class RoleMapper {
    
    /**
     * Convert RoleEntity to Role DTO with permissions.
     * For now, returns empty permissions set until permissions loading is implemented.
     * 
     * @param entity the role entity
     * @return role DTO for API response
     */
    public Role toDto(RoleEntity entity) {
        Role dto = new Role();
        
        // Map role name to enum
        dto.setRoleName(RoleName.fromValue(entity.getRoleName()));
        
        // TODO: Load permissions for role (Phase 9 - Role Management)
        // For now, return empty set
        dto.setPermissions(Collections.emptySet());
        
        return dto;
    }
    
    /**
     * Map permission entity to permission enum.
     * 
     * @param entity the permission entity
     * @return permission enum value
     */
    private Permission mapPermission(PermissionEntity entity) {
        return Permission.fromValue(entity.getPermission());
    }
}
