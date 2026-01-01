package com.example.specdriven.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

/**
 * Domain entity representing role-permission mappings.
 * Maps to the 'role_permissions' join table in the database.
 * Uses composite primary key (role_id, permission_id).
 */
@Entity
@Table(name = "role_permissions")
@IdClass(RolePermissionEntity.RolePermissionId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RolePermissionEntity {

    @Id
    @Column(name = "role_id")
    private UUID roleId;

    @Id
    @Column(name = "permission_id")
    private UUID permissionId;

    /**
     * Composite primary key class for RolePermissionEntity.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class RolePermissionId implements Serializable {
        private UUID roleId;
        private UUID permissionId;
    }
}
