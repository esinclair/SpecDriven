package com.example.specdriven.mapper;

import com.example.specdriven.api.model.Role;
import com.example.specdriven.api.model.RoleName;
import com.example.specdriven.domain.RoleEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Role DTOs and RoleEntity domain objects.
 */
@Component
public class RoleMapper {
    
    public Role toDto(RoleEntity entity) {
        return new Role()
                .roleName(RoleName.fromValue(entity.getName()));
    }
    
    public RoleEntity toEntity(Role dto) {
        RoleEntity entity = new RoleEntity();
        entity.setName(dto.getRoleName().getValue());
        return entity;
    }
}
