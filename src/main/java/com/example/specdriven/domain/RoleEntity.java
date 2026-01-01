package com.example.specdriven.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Domain entity representing a role in the system.
 * Maps to the 'roles' table in the database.
 * Roles are predefined (ADMIN, USER, GUEST).
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleEntity {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "role_name")
    private String roleName;

    @Column(name = "description")
    private String description;
}
