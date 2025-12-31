package com.example.specdriven.mapper;

import com.example.specdriven.api.model.Permission;
import com.example.specdriven.api.model.Role;
import com.example.specdriven.api.model.RoleName;
import com.example.specdriven.domain.PermissionEntity;
import com.example.specdriven.domain.RoleEntity;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper for converting between Role API models (DTOs) and domain entities.
 */
@Component
public class RoleMapper {

    /**
     * Convert RoleEntity to Role DTO with permissions.
     *
     * @param roleEntity the role entity
     * @param permissionEntities the permissions for this role
     * @return Role DTO
     */
    public Role toDto(RoleEntity roleEntity, List<PermissionEntity> permissionEntities) {
        Role role = new Role();
        role.setRoleName(RoleName.fromValue(roleEntity.getRoleName()));
        
        Set<Permission> permissions = permissionEntities.stream()
                .map(this::toPermissionDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        
        role.setPermissions(permissions);
        return role;
    }

    /**
     * Convert RoleEntity to Role DTO without permissions.
     *
     * @param roleEntity the role entity
     * @return Role DTO with empty permissions
     */
    public Role toDto(RoleEntity roleEntity) {
        return toDto(roleEntity, List.of());
    }

    /**
     * Convert PermissionEntity to Permission DTO.
     *
     * @param permissionEntity the permission entity
     * @return Permission DTO
     */
    public Permission toPermissionDto(PermissionEntity permissionEntity) {
        return Permission.fromValue(permissionEntity.getPermission());
    }
}
