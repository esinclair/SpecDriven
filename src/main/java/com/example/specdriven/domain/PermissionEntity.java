package com.example.specdriven.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Domain entity representing a permission in the system.
 * Maps to the 'permissions' table in the database.
 * Permissions are predefined (users:read, users:write, users:delete, roles:assign).
 */
@Entity
@Table(name = "permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PermissionEntity {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "permission")
    private String permission;

    @Column(name = "description")
    private String description;
}
