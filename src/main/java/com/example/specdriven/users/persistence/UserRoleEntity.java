package com.example.specdriven.users.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("user_roles")
public class UserRoleEntity {
    @Id
    private Long id;
    private UUID userId;
    private String roleName;

    public UserRoleEntity() {}

    public UserRoleEntity(UUID userId, String roleName) {
        this.userId = userId;
        this.roleName = roleName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

