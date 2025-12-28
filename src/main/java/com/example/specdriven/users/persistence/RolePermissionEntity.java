package com.example.specdriven.users.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("ROLE_PERMISSIONS")
public class RolePermissionEntity {
    @Id
    @Column("ID")
    private UUID id;

    @Column("ROLE_NAME")
    private String roleName;

    @Column("PERMISSION")
    private String permissionName;

    public RolePermissionEntity() {}

    public RolePermissionEntity(String roleName, String permissionName) {
        this.roleName = roleName;
        this.permissionName = permissionName;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getPermissionName() {
        return permissionName;
    }

    public void setPermissionName(String permissionName) {
        this.permissionName = permissionName;
    }
}

