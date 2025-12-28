package com.example.specdriven.users.persistence;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

/**
 * User-Role join entity mapped to user_roles table.
 * Uses Lombok annotations for boilerplate reduction.
 * Note: This entity has a composite key (user_id, role_name) in the database,
 * but Spring Data JDBC doesn't support composite keys directly in annotations.
 * Queries should use custom SQL or use both fields as the unique identifier.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("user_roles")
public class UserRoleEntity {
    @Column("user_id")
    private UUID userId;
    
    @Column("role_name")
    private String roleName;
}
