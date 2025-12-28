package com.example.specdriven.users.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("USER_ROLES")
public class UserRoleEntity {
    @Id
    @Column("ID")
    private UUID id;

    @Column("USER_ID")
    private UUID userId;

    @Column("ROLE_NAME")
    private String roleName;

    public UserRoleEntity() {}

    public UserRoleEntity(UUID userId, String roleName) {
        this.userId = userId;
        this.roleName = roleName;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}

