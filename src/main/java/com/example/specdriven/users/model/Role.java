package com.example.specdriven.users.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents a role with its associated permissions.
 * This is a domain model class, not a persistent entity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    private RoleName roleName;
    private List<Permission> permissions;
}
