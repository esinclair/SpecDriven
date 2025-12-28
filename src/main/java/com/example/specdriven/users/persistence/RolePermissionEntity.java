package com.example.specdriven.users.persistence;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Role-Permission mapping entity.
 * Uses Lombok annotations for boilerplate reduction.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("role_permissions")
public class RolePermissionEntity {
    @Column("role_name")
    private String roleName;
    
    @Column("permission_name")
    private String permissionName;
}
